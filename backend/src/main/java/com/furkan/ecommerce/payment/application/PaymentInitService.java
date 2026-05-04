package com.furkan.ecommerce.payment.application;

import com.furkan.ecommerce.auth.AuthReadApi;
import com.furkan.ecommerce.auth.dto.AuthPaymentProfileView;
import com.furkan.ecommerce.common.exception.BusinessException;
import com.furkan.ecommerce.common.exception.ResourceNotFoundException;
import com.furkan.ecommerce.order.OrderReadApi;
import com.furkan.ecommerce.order.dto.OrderPaymentView;
import com.furkan.ecommerce.payment.config.PaymentCallbackProperties;
import com.furkan.ecommerce.payment.domain.Payment;
import com.furkan.ecommerce.payment.domain.PaymentAttempt;
import com.furkan.ecommerce.payment.domain.PaymentAttemptStatus;
import com.furkan.ecommerce.payment.domain.PaymentStatus;
import com.furkan.ecommerce.payment.persistence.PaymentAttemptRepository;
import com.furkan.ecommerce.payment.persistence.PaymentRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
class PaymentInitService {
    private static final List<PaymentAttemptStatus> ACTIVE_ATTEMPT_STATUSES = List.of(
            PaymentAttemptStatus.INIT_REQUESTED,
            PaymentAttemptStatus.ACTION_REQUIRED
    );

    private final PaymentGateway gateway;
    private final AuthReadApi authReadApi;
    private final OrderReadApi orderReadApi;
    private final PaymentRepository paymentRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final PaymentCallbackProperties callbackProperties;
    private final TransactionTemplate transactionTemplate;
    private final PaymentMapper paymentMapper;
    private final PaymentProfileValidator profileValidator;

    PaymentGateway.PaymentResult init(Long userId, Long orderId, String customerIp) {
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
        profileValidator.validate(userProfile);

        Long attemptId = preparation.attemptId();
        String attemptReference = preparation.attemptReference();
        Instant attemptExpiresAt = preparation.expiresAt();
        PaymentGateway.PaymentResult gatewayResult;
        try {
            gatewayResult = gateway.initCheckout(new PaymentGateway.CheckoutRequest(
                    order.orderId(),
                    attemptReference,
                    order.totalAmount(),
                    order.items().stream()
                            .map(paymentMapper::toCheckoutLine)
                            .toList(),
                    userProfile,
                    customerIp,
                    callbackProperties.callbackUrl(),
                    attemptExpiresAt
            ));
        } catch (BusinessException ex) {
            transactionTemplate.execute(status -> failInitAttempt(attemptId, ex.code()));
            throw ex;
        }

        return transactionTemplate.execute(status -> finalizeInit(attemptId, gatewayResult));
    }

    private OrderPaymentView getOwnedPayableOrder(Long userId, Long orderId) {
        OrderPaymentView order = orderReadApi.findPaymentViewById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("ORDER_NOT_FOUND", "Order not found"));
        if (!order.userId().equals(userId)) {
            throw new AccessDeniedException("Order does not belong to authenticated user");
        }
        if (!order.status().isPayable()) {
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

    private Void failInitAttempt(Long attemptId, String errorCode) {
        PaymentAttempt attempt = paymentAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("PAYMENT_ATTEMPT_NOT_FOUND", "Payment attempt not found"));
        attempt.markFailed(null, errorCode);
        attempt.getPayment().markFailed(null, errorCode);
        return null;
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
}
