package com.furkan.ecommerce.payment.application;

import com.furkan.ecommerce.common.exception.BusinessException;
import com.furkan.ecommerce.common.exception.ResourceNotFoundException;
import com.furkan.ecommerce.common.outbox.OutboxRecorder;
import com.furkan.ecommerce.common.outbox.ProcessedEvent;
import com.furkan.ecommerce.common.outbox.ProcessedEventRepository;
import com.furkan.ecommerce.order.OrderReadApi;
import com.furkan.ecommerce.order.dto.OrderPaymentView;
import com.furkan.ecommerce.payment.config.PaymentCallbackProperties;
import com.furkan.ecommerce.payment.domain.Payment;
import com.furkan.ecommerce.payment.domain.PaymentAttempt;
import com.furkan.ecommerce.payment.event.PaymentFailedEvent;
import com.furkan.ecommerce.payment.event.PaymentRequiresReviewEvent;
import com.furkan.ecommerce.payment.event.PaymentSucceededEvent;
import com.furkan.ecommerce.payment.persistence.PaymentAttemptRepository;
import com.furkan.ecommerce.payment.persistence.PaymentRepository;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
class PaymentCallbackService {
    private static final String CALLBACK_CONSUMER = "payment-callback";

    private final PaymentGateway gateway;
    private final OrderReadApi orderReadApi;
    private final PaymentRepository paymentRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final OutboxRecorder outboxRecorder;
    private final PaymentCallbackProperties callbackProperties;
    private final TransactionTemplate transactionTemplate;
    private final PaymentReferenceParser referenceParser;

    PaymentCommandService.PaymentCallbackResult handleCallback(String token) {
        if (isBlank(token)) {
            throw new BusinessException("PAYMENT_CALLBACK_TOKEN_MISSING", "Payment callback token is missing");
        }
        PaymentGateway.VerifyResult verifyResult = gateway.verifyCheckout(token);
        Long orderId = resolveCallbackOrderId(token, verifyResult.providerReference());
        OrderPaymentView order = orderReadApi.findPaymentViewById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("ORDER_NOT_FOUND", "Order not found"));
        return transactionTemplate.execute(status -> handleVerifiedCallback(token, verifyResult, order));
    }

    private PaymentCommandService.PaymentCallbackResult handleVerifiedCallback(
            String token,
            PaymentGateway.VerifyResult verifyResult,
            OrderPaymentView order
    ) {
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
            return new PaymentCommandService.PaymentCallbackResult(order.orderId(), "review");
        }

        if (verifyResult.success()) {
            PaymentAttempt.TransitionResult attemptTransition = attempt.markSucceeded(verifyResult.transactionId());
            Payment.TransitionResult paymentTransition = payment.markSucceeded(verifyResult.transactionId());
            if (attemptTransition == PaymentAttempt.TransitionResult.CHANGED || paymentTransition == Payment.TransitionResult.CHANGED) {
                outboxRecorder.record(new PaymentSucceededEvent(UUID.randomUUID(), order.orderId(), verifyResult.transactionId(), Instant.now()));
            }
            return new PaymentCommandService.PaymentCallbackResult(order.orderId(), "success");
        }

        PaymentAttempt.TransitionResult attemptTransition = attempt.markFailed(verifyResult.transactionId(), verifyResult.errorCode());
        Payment.TransitionResult paymentTransition = payment.markFailed(verifyResult.transactionId(), verifyResult.errorCode());
        if (attemptTransition == PaymentAttempt.TransitionResult.CHANGED || paymentTransition == Payment.TransitionResult.CHANGED) {
            outboxRecorder.record(new PaymentFailedEvent(UUID.randomUUID(), order.orderId(), verifyResult.transactionId(), Instant.now()));
        }
        return new PaymentCommandService.PaymentCallbackResult(order.orderId(), "failed");
    }

    private String reviewReason(OrderPaymentView order, PaymentGateway.VerifyResult verifyResult) {
        if (verifyResult.success()) {
            if (!order.status().isFulfillableAfterCallback()) {
                return "ORDER_NOT_FULFILLABLE";
            }
            if (verifyResult.paidAmount() != null && verifyResult.paidAmount().compareTo(order.totalAmount()) != 0) {
                return "PAYMENT_AMOUNT_MISMATCH";
            }
        }
        return null;
    }

    private PaymentCommandService.PaymentCallbackResult callbackResultFromCurrentState(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .map(payment -> new PaymentCommandService.PaymentCallbackResult(orderId, switch (payment.getStatus()) {
                    case SUCCEEDED -> "success";
                    case REQUIRES_REVIEW -> "review";
                    case FAILED -> "failed";
                    default -> "pending";
                }))
                .orElse(new PaymentCommandService.PaymentCallbackResult(orderId, "pending"));
    }

    private UUID callbackEventId(String token) {
        return UUID.nameUUIDFromBytes((CALLBACK_CONSUMER + ":" + token).getBytes(StandardCharsets.UTF_8));
    }

    private Long resolveCallbackOrderId(String token, String providerReference) {
        if (!isBlank(providerReference)) {
            try {
                return referenceParser.parseOrderId(providerReference);
            } catch (BusinessException ignored) {
                // Some providers do not echo conversationId reliably on callback verification.
            }
        }

        return paymentAttemptRepository.findByCheckoutToken(token)
                .map(PaymentAttempt::getOrderId)
                .orElseThrow(() -> new BusinessException("PAYMENT_PROVIDER_REFERENCE_INVALID", "Invalid provider reference"));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
