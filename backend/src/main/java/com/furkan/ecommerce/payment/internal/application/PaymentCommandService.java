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
import com.furkan.ecommerce.payment.api.event.PaymentFailedEvent;
import com.furkan.ecommerce.payment.api.event.PaymentSucceededEvent;
import java.time.Instant;
import java.util.UUID;
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

    private final PaymentGateway gateway;
    private final AuthReadApi authReadApi;
    private final OrderReadApi orderReadApi;
    private final PaymentRepository paymentRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final OutboxRecorder outboxRecorder;
    private final PaymentCallbackProperties callbackProperties;
    private final TransactionTemplate transactionTemplate;

    public PaymentGateway.PaymentResult init(Long userId, Long orderId) {
        OrderPaymentView order = orderReadApi.findPaymentViewById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("ORDER_NOT_FOUND", "Order not found"));
        if (!order.userId().equals(userId)) {
            throw new AccessDeniedException("Order does not belong to authenticated user");
        }
        if (!ORDER_PENDING.equals(order.status())) {
            throw new BusinessException("ORDER_INVALID_STATE", "Order is not payable");
        }
        var existingPayment = paymentRepository.findByOrderId(orderId);
        if (existingPayment.isPresent()) {
            return toResult(existingPayment.get());
        }

        AuthPaymentProfileView userProfile = authReadApi.findPaymentProfileById(order.userId())
                .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "User not found"));
        validatePaymentProfile(userProfile);

        var result = gateway.initCheckout(orderId, order.totalAmount(), userProfile);
        try {
            return transactionTemplate.execute(status -> persistInitiatedPayment(order, result));
        } catch (DataIntegrityViolationException duplicate) {
            return paymentRepository.findByOrderId(order.orderId())
                    .map(this::toResult)
                    .orElseThrow(() -> duplicate);
        }
    }

    public void handleCallback(String token) {
        if (isBlank(token)) {
            throw new BusinessException("PAYMENT_CALLBACK_TOKEN_MISSING", "Payment callback token is missing");
        }
        PaymentGateway.VerifyResult verifyResult = gateway.verifyCheckout(token);
        Long orderId = parseOrderId(verifyResult.providerReference());
        OrderPaymentView order = orderReadApi.findPaymentViewById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("ORDER_NOT_FOUND", "Order not found"));

        transactionTemplate.executeWithoutResult(status -> handleVerifiedCallback(token, verifyResult, order));
    }

    private PaymentGateway.PaymentResult persistInitiatedPayment(OrderPaymentView order, PaymentGateway.PaymentResult result) {
        Payment payment = Payment.initiate(
                order.orderId(),
                order.totalAmount(),
                callbackProperties.provider(),
                result.transactionId(),
                result.checkoutUrl(),
                result.checkoutToken(),
                result.providerReference()
        );
        paymentRepository.saveAndFlush(payment);
        return result;
    }

    private void handleVerifiedCallback(String token, PaymentGateway.VerifyResult verifyResult, OrderPaymentView order) {
        UUID callbackId = callbackEventId(token);
        try {
            processedEventRepository.saveAndFlush(ProcessedEvent.of(CALLBACK_CONSUMER, callbackId));
        } catch (DataIntegrityViolationException duplicate) {
            return;
        }

        Payment payment = paymentRepository.findByOrderId(order.orderId())
                .orElseGet(() -> Payment.initiate(
                        order.orderId(),
                        order.totalAmount(),
                        callbackProperties.provider(),
                        null,
                        null,
                        token,
                        verifyResult.providerReference()
                ));

        if (verifyResult.success()) {
            Payment.TransitionResult transition = payment.markSucceeded(verifyResult.transactionId());
            if (transition == Payment.TransitionResult.CHANGED) {
                outboxRecorder.record(new PaymentSucceededEvent(UUID.randomUUID(), order.orderId(), verifyResult.transactionId(), Instant.now()));
            }
        } else {
            Payment.TransitionResult transition = payment.markFailed(verifyResult.transactionId());
            if (transition == Payment.TransitionResult.CHANGED) {
                outboxRecorder.record(new PaymentFailedEvent(UUID.randomUUID(), order.orderId(), verifyResult.transactionId(), Instant.now()));
            }
        }
        paymentRepository.save(payment);
    }

    private PaymentGateway.PaymentResult toResult(Payment payment) {
        return switch (payment.getStatus()) {
            case SUCCEEDED -> PaymentGateway.PaymentResult.succeeded(payment.getTransactionId(), payment.getProviderReference());
            case FAILED -> PaymentGateway.PaymentResult.failure("PAYMENT_PREVIOUSLY_FAILED");
            case INITIATED -> new PaymentGateway.PaymentResult(
                    false,
                    payment.getTransactionId(),
                    "PAYMENT_PENDING_ACTION_REQUIRED",
                    payment.getCheckoutUrl(),
                    payment.getCheckoutToken(),
                    payment.getProviderReference()
            );
        };
    }

    private UUID callbackEventId(String token) {
        return UUID.nameUUIDFromBytes((CALLBACK_CONSUMER + ":" + token).getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private Long parseOrderId(String providerReference) {
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
            throw new BusinessException(
                    "PAYMENT_PROFILE_INCOMPLETE",
                    "User profile is incomplete for payment. Required: firstName,lastName,phoneNumber,identityNumber,address,city,country,zipCode"
            );
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
