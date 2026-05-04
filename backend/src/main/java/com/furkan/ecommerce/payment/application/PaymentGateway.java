package com.furkan.ecommerce.payment.application;

import com.furkan.ecommerce.auth.dto.AuthPaymentProfileView;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface PaymentGateway {
    PaymentResult initCheckout(CheckoutRequest request);

    VerifyResult verifyCheckout(String checkoutToken);

    record CheckoutRequest(
            Long orderId,
            String attemptReference,
            BigDecimal amount,
            List<CheckoutLine> lines,
            AuthPaymentProfileView userProfile,
            String customerIp,
            String callbackUrl,
            Instant expiresAt
    ) {}

    record CheckoutLine(
            Long productId,
            String productName,
            String productImageUrl,
            BigDecimal unitPrice,
            Integer quantity
    ) {
        public BigDecimal lineTotal() {
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    record PaymentResult(
            boolean success,
            String status,
            String transactionId,
            String errorCode,
            String checkoutUrl,
            String checkoutToken,
            String providerReference,
            Instant expiresAt
    ) {
        public static PaymentResult pendingAction(String code, String checkoutUrl, String checkoutToken, String providerReference, Instant expiresAt) {
            return new PaymentResult(false, "ACTION_REQUIRED", null, code, checkoutUrl, checkoutToken, providerReference, expiresAt);
        }

        public static PaymentResult inProgress(String providerReference, Instant expiresAt) {
            return new PaymentResult(false, "INIT_REQUESTED", null, "PAYMENT_INIT_IN_PROGRESS", null, null, providerReference, expiresAt);
        }

        public static PaymentResult succeeded(String transactionId, String providerReference) {
            return new PaymentResult(true, "SUCCEEDED", transactionId, null, null, null, providerReference, null);
        }

        public static PaymentResult failure(String errorCode) {
            return new PaymentResult(false, "FAILED", null, errorCode, null, null, null, null);
        }

        public static PaymentResult review(String errorCode) {
            return new PaymentResult(false, "REQUIRES_REVIEW", null, errorCode, null, null, null, null);
        }
    }

    record VerifyResult(
            boolean success,
            String transactionId,
            String errorCode,
            String providerReference,
            BigDecimal paidAmount
    ) {}
}
