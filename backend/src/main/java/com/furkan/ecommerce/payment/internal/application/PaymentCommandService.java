package com.furkan.ecommerce.payment.internal;

import com.furkan.ecommerce.auth.api.AuthReadApi;
import com.furkan.ecommerce.auth.api.dto.AuthPaymentProfileView;
import com.furkan.ecommerce.common.exception.BusinessException;
import com.furkan.ecommerce.common.exception.ResourceNotFoundException;
import com.furkan.ecommerce.common.outbox.OutboxRecorder;
import com.furkan.ecommerce.common.outbox.ProcessedEvent;
import com.furkan.ecommerce.common.outbox.ProcessedEventRepository;
import com.furkan.ecommerce.order.api.OrderReadApi;
import com.furkan.ecommerce.order.api.dto.OrderPaymentView;
import com.furkan.ecommerce.payment.api.dto.PaymentStatusResponse;
import com.furkan.ecommerce.payment.api.event.PaymentFailedEvent;
import com.furkan.ecommerce.payment.api.event.PaymentRequiresReviewEvent;
import com.furkan.ecommerce.payment.api.event.PaymentSucceededEvent;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class PaymentCommandService {
    private static final String CALLBACK_CONSUMER = "payment-callback";
    private static final String ORDER_PENDING = "PENDING";
    private static final String ORDER_PAYMENT_FAILED = "PAYMENT_FAILED";
    private static final String ORDER_EXPIRED = "EXPIRED";
    private static final String ORDER_CANCELLED = "CANCELLED";
    private static final String ORDER_REQUIRES_REVIEW = "REQUIRES_REVIEW";
    private static final Pattern ORDER_REFERENCE = Pattern.compile("^order-(\\d+)-.+$");
    private static final List<PaymentAttemptStatus> ACTIVE_ATTEMPT_STATUSES = List.of(
            PaymentAttemptStatus.INIT_REQUESTED,
            PaymentAttemptStatus.ACTION_REQUIRED
    );

    private final PaymentGateway gateway;
    private final AuthReadApi authReadApi;
    private final OrderReadApi orderReadApi;
    private final PaymentRepository paymentRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final OutboxRecorder outboxRecorder;
    private final PaymentCallbackProperties callbackProperties;
    private final TransactionTemplate transactionTemplate;

    public PaymentGateway.PaymentResult init(Long userId, Long orderId, String customerIp) {
        OrderPaymentView order = getOwnedPayableOrder(userId, orderId);
        InitPreparation preparation;
        try {
            preparation = transactionTemplate.execute(status -> prepareInit(order));
        } catch (DataIntegrityViolationException duplicate) {
            preparation = transactionTemplate.execute(status -> recoverConcurrentInit(order));
        }
        if (preparation.reusableResult() != null) {
            return preparation.reusableResult();
        }

        AuthPaymentProfileView userProfile = authReadApi.findPaymentProfileById(order.userId())
                .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "User not found"));
        validatePaymentProfile(userProfile);

        Long attemptId = preparation.attemptId();
        String attemptReference = preparation.attemptReference();
        Instant attemptExpiresAt = preparation.expiresAt();
        PaymentGateway.PaymentResult gatewayResult = gateway.initCheckout(new PaymentGateway.CheckoutRequest(
                order.orderId(),
                attemptReference,
                order.totalAmount(),
                order.items().stream()
                        .map(line -> new PaymentGateway.CheckoutLine(
                                line.productId(),
                                line.productName(),
                                line.productImageUrl(),
                                line.unitPrice(),
                                line.quantity()
                        ))
                        .toList(),
                userProfile,
                customerIp,
                callbackProperties.callbackUrl(),
                attemptExpiresAt
        ));

        return transactionTemplate.execute(status -> finalizeInit(attemptId, gatewayResult));
    }

    public PaymentCallbackResult handleCallback(String token) {
        if (isBlank(token)) {
            throw new BusinessException("PAYMENT_CALLBACK_TOKEN_MISSING", "Payment callback token is missing");
        }
        PaymentGateway.VerifyResult verifyResult = gateway.verifyCheckout(token);
        Long orderId = parseOrderId(verifyResult.providerReference());
        OrderPaymentView order = orderReadApi.findPaymentViewById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("ORDER_NOT_FOUND", "Order not found"));
        return transactionTemplate.execute(status -> handleVerifiedCallback(token, verifyResult, order));
    }

    public PaymentStatusResponse getStatus(Long userId, Long orderId) {
        OrderPaymentView order = orderReadApi.findPaymentViewById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("ORDER_NOT_FOUND", "Order not found"));
        if (!order.userId().equals(userId)) {
            throw new AccessDeniedException("Order does not belong to authenticated user");
        }
        return transactionTemplate.execute(status -> paymentRepository.findByOrderId(orderId)
                .map(payment -> toStatusResponse(orderId, payment))
                .orElse(new PaymentStatusResponse(orderId, "NOT_STARTED", null, null, null, null, order.expiresAt())));
    }

    private OrderPaymentView getOwnedPayableOrder(Long userId, Long orderId) {
        OrderPaymentView order = orderReadApi.findPaymentViewById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("ORDER_NOT_FOUND", "Order not found"));
        if (!order.userId().equals(userId)) {
            throw new AccessDeniedException("Order does not belong to authenticated user");
        }
        if (!ORDER_PENDING.equals(order.status()) && !ORDER_PAYMENT_FAILED.equals(order.status())) {
            throw new BusinessException("ORDER_INVALID_STATE", "Order is not payable");
        }
        if (!order.expiresAt().isAfter(Instant.now())) {
            throw new BusinessException("ORDER_EXPIRED", "Order payment window has expired");
        }
        return order;
    }

    private InitPreparation prepareInit(OrderPaymentView order) {
        Instant now = Instant.now();
        Payment payment = paymentRepository.findByOrderId(order.orderId()).orElse(null);
        if (payment != null) {
            if (payment.getStatus() == PaymentStatus.SUCCEEDED) {
                return InitPreparation.reuse(toResult(payment, latestAttempt(payment).orElse(null)));
            }
            if (payment.getStatus() == PaymentStatus.REQUIRES_REVIEW) {
                return InitPreparation.reuse(PaymentGateway.PaymentResult.review(payment.getErrorCode()));
            }
            Optional<PaymentAttempt> activeAttempt = paymentAttemptRepository
                    .findFirstByPaymentAndStatusInAndExpiresAtAfterOrderByIdDesc(payment, ACTIVE_ATTEMPT_STATUSES, now);
            if (activeAttempt.isPresent()) {
                return InitPreparation.reuse(toResult(payment, activeAttempt.get()));
            }
            expireLatestAttemptIfNeeded(payment, now);
        } else {
            payment = paymentRepository.saveAndFlush(Payment.initiate(order.orderId(), order.totalAmount(), callbackProperties.provider()));
        }

        String attemptReference = attemptReference(order.orderId());
        Instant expiresAt = order.expiresAt().isBefore(now.plus(callbackProperties.attemptTtl()))
                ? order.expiresAt()
                : now.plus(callbackProperties.attemptTtl());
        PaymentAttempt attempt = paymentAttemptRepository.saveAndFlush(
                PaymentAttempt.request(payment, order.orderId(), attemptReference, order.totalAmount(), expiresAt)
        );
        return InitPreparation.newAttempt(attempt.getId(), attemptReference, expiresAt);
    }

    private InitPreparation recoverConcurrentInit(OrderPaymentView order) {
        Payment payment = paymentRepository.findByOrderId(order.orderId())
                .orElseThrow(() -> new BusinessException("PAYMENT_INIT_CONFLICT", "Payment init is already in progress"));
        return paymentAttemptRepository
                .findFirstByPaymentAndStatusInAndExpiresAtAfterOrderByIdDesc(payment, ACTIVE_ATTEMPT_STATUSES, Instant.now())
                .map(attempt -> InitPreparation.reuse(toResult(payment, attempt)))
                .orElseGet(() -> InitPreparation.reuse(PaymentGateway.PaymentResult.failure("PAYMENT_INIT_CONFLICT")));
    }

    private PaymentGateway.PaymentResult finalizeInit(Long attemptId, PaymentGateway.PaymentResult gatewayResult) {
        PaymentAttempt attempt = paymentAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("PAYMENT_ATTEMPT_NOT_FOUND", "Payment attempt not found"));
        Payment payment = attempt.getPayment();
        if ("ACTION_REQUIRED".equals(gatewayResult.status())) {
            attempt.markActionRequired(gatewayResult.checkoutUrl(), gatewayResult.checkoutToken());
            payment.markActionRequired(gatewayResult.checkoutUrl(), gatewayResult.checkoutToken(), attempt.getAttemptReference());
            return PaymentGateway.PaymentResult.pendingAction(
                    gatewayResult.errorCode(),
                    gatewayResult.checkoutUrl(),
                    gatewayResult.checkoutToken(),
                    attempt.getAttemptReference(),
                    attempt.getExpiresAt()
            );
        }
        attempt.markFailed(gatewayResult.transactionId(), gatewayResult.errorCode());
        payment.markFailed(gatewayResult.transactionId(), gatewayResult.errorCode());
        return PaymentGateway.PaymentResult.failure(gatewayResult.errorCode());
    }

    private PaymentCallbackResult handleVerifiedCallback(String token, PaymentGateway.VerifyResult verifyResult, OrderPaymentView order) {
        UUID callbackId = callbackEventId(token);
        try {
            processedEventRepository.saveAndFlush(ProcessedEvent.of(CALLBACK_CONSUMER, callbackId));
        } catch (DataIntegrityViolationException duplicate) {
            return callbackResultFromCurrentState(order.orderId());
        }

        Payment payment = paymentRepository.findByOrderId(order.orderId())
                .orElseGet(() -> paymentRepository.saveAndFlush(Payment.initiate(order.orderId(), order.totalAmount(), callbackProperties.provider())));
        PaymentAttempt attempt = paymentAttemptRepository.findByAttemptReference(verifyResult.providerReference())
                .or(() -> paymentAttemptRepository.findByCheckoutToken(token))
                .orElseGet(() -> paymentAttemptRepository.saveAndFlush(PaymentAttempt.request(
                        payment,
                        order.orderId(),
                        verifyResult.providerReference(),
                        order.totalAmount(),
                        order.expiresAt()
                )));

        String reviewReason = reviewReason(order, verifyResult);
        if (reviewReason != null) {
            attempt.markFailed(verifyResult.transactionId(), reviewReason);
            if (payment.markRequiresReview(reviewReason) == Payment.TransitionResult.CHANGED) {
                outboxRecorder.record(new PaymentRequiresReviewEvent(UUID.randomUUID(), order.orderId(), reviewReason, Instant.now()));
            }
            return new PaymentCallbackResult(order.orderId(), "review");
        }

        if (verifyResult.success()) {
            PaymentAttempt.TransitionResult attemptTransition = attempt.markSucceeded(verifyResult.transactionId());
            Payment.TransitionResult paymentTransition = payment.markSucceeded(verifyResult.transactionId());
            if (attemptTransition == PaymentAttempt.TransitionResult.CHANGED || paymentTransition == Payment.TransitionResult.CHANGED) {
                outboxRecorder.record(new PaymentSucceededEvent(UUID.randomUUID(), order.orderId(), verifyResult.transactionId(), Instant.now()));
            }
            return new PaymentCallbackResult(order.orderId(), "success");
        }

        PaymentAttempt.TransitionResult attemptTransition = attempt.markFailed(verifyResult.transactionId(), verifyResult.errorCode());
        Payment.TransitionResult paymentTransition = payment.markFailed(verifyResult.transactionId(), verifyResult.errorCode());
        if (attemptTransition == PaymentAttempt.TransitionResult.CHANGED || paymentTransition == Payment.TransitionResult.CHANGED) {
            outboxRecorder.record(new PaymentFailedEvent(UUID.randomUUID(), order.orderId(), verifyResult.transactionId(), Instant.now()));
        }
        return new PaymentCallbackResult(order.orderId(), "failed");
    }

    private String reviewReason(OrderPaymentView order, PaymentGateway.VerifyResult verifyResult) {
        if (verifyResult.success()) {
            if (ORDER_EXPIRED.equals(order.status()) || ORDER_CANCELLED.equals(order.status()) || ORDER_REQUIRES_REVIEW.equals(order.status())) {
                return "ORDER_NOT_FULFILLABLE";
            }
            if (verifyResult.paidAmount() != null && verifyResult.paidAmount().compareTo(order.totalAmount()) != 0) {
                return "PAYMENT_AMOUNT_MISMATCH";
            }
        }
        return null;
    }

    private PaymentCallbackResult callbackResultFromCurrentState(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .map(payment -> new PaymentCallbackResult(orderId, switch (payment.getStatus()) {
                    case SUCCEEDED -> "success";
                    case REQUIRES_REVIEW -> "review";
                    case FAILED -> "failed";
                    default -> "pending";
                }))
                .orElse(new PaymentCallbackResult(orderId, "pending"));
    }

    private PaymentStatusResponse toStatusResponse(Long orderId, Payment payment) {
        PaymentAttempt attempt = latestAttempt(payment).orElse(null);
        return new PaymentStatusResponse(
                orderId,
                payment.getStatus().name(),
                attempt == null ? null : attempt.getStatus().name(),
                payment.getTransactionId(),
                payment.getErrorCode(),
                attempt == null ? payment.getCheckoutUrl() : attempt.getCheckoutUrl(),
                attempt == null ? null : attempt.getExpiresAt()
        );
    }

    private PaymentGateway.PaymentResult toResult(Payment payment, PaymentAttempt attempt) {
        return switch (payment.getStatus()) {
            case SUCCEEDED -> PaymentGateway.PaymentResult.succeeded(payment.getTransactionId(), payment.getProviderReference());
            case REQUIRES_REVIEW -> PaymentGateway.PaymentResult.review(payment.getErrorCode());
            case FAILED -> PaymentGateway.PaymentResult.failure(payment.getErrorCode() == null ? "PAYMENT_PREVIOUSLY_FAILED" : payment.getErrorCode());
            case INITIATED, ACTION_REQUIRED -> {
                if (attempt == null) {
                    yield PaymentGateway.PaymentResult.failure("PAYMENT_ATTEMPT_NOT_FOUND");
                }
                if (attempt.getStatus() == PaymentAttemptStatus.INIT_REQUESTED) {
                    yield PaymentGateway.PaymentResult.inProgress(attempt.getAttemptReference(), attempt.getExpiresAt());
                }
                yield PaymentGateway.PaymentResult.pendingAction(
                        "PAYMENT_PENDING_ACTION_REQUIRED",
                        attempt.getCheckoutUrl(),
                        attempt.getCheckoutToken(),
                        attempt.getAttemptReference(),
                        attempt.getExpiresAt()
                );
            }
        };
    }

    private Optional<PaymentAttempt> latestAttempt(Payment payment) {
        return paymentAttemptRepository.findFirstByPaymentOrderByIdDesc(payment);
    }

    private void expireLatestAttemptIfNeeded(Payment payment, Instant now) {
        latestAttempt(payment).ifPresent(attempt -> attempt.markExpired(now));
    }

    private String attemptReference(Long orderId) {
        return "order-" + orderId + "-" + UUID.randomUUID();
    }

    private UUID callbackEventId(String token) {
        return UUID.nameUUIDFromBytes((CALLBACK_CONSUMER + ":" + token).getBytes(StandardCharsets.UTF_8));
    }

    private Long parseOrderId(String providerReference) {
        if (isBlank(providerReference)) {
            throw new BusinessException("PAYMENT_PROVIDER_REFERENCE_INVALID", "Invalid provider reference");
        }
        var matcher = ORDER_REFERENCE.matcher(providerReference);
        if (matcher.matches()) {
            return Long.valueOf(matcher.group(1));
        }
        try {
            return Long.valueOf(providerReference);
        } catch (NumberFormatException ex) {
            throw new BusinessException("PAYMENT_PROVIDER_REFERENCE_INVALID", "Invalid provider reference");
        }
    }

    private void validatePaymentProfile(AuthPaymentProfileView profile) {
        if (isBlank(profile.firstName())
                || isBlank(profile.lastName())
                || isBlank(profile.phoneNumber())
                || isBlank(profile.identityNumber())
                || isBlank(profile.address())
                || isBlank(profile.city())
                || isBlank(profile.country())
                || isBlank(profile.zipCode())) {
            throw new BusinessException("PAYMENT_PROFILE_INCOMPLETE", "User profile is incomplete for payment");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record InitPreparation(
            Long attemptId,
            String attemptReference,
            Instant expiresAt,
            PaymentGateway.PaymentResult reusableResult
    ) {
        static InitPreparation newAttempt(Long attemptId, String attemptReference, Instant expiresAt) {
            return new InitPreparation(attemptId, attemptReference, expiresAt, null);
        }

        static InitPreparation reuse(PaymentGateway.PaymentResult result) {
            return new InitPreparation(null, null, null, result);
        }
    }

    public record PaymentCallbackResult(Long orderId, String status) {}
}
