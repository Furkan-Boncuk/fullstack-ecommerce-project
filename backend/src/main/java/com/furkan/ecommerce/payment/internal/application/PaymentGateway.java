package com.furkan.ecommerce.payment.internal;

import com.furkan.ecommerce.auth.api.dto.AuthPaymentProfileView;
import java.math.BigDecimal;

public interface PaymentGateway {
    PaymentResult initCheckout(Long orderId, BigDecimal amount, AuthPaymentProfileView userProfile);

    VerifyResult verifyCheckout(String checkoutToken);

    record PaymentResult(
            boolean success,
            String transactionId,
            String errorCode,
            String checkoutUrl,
            String checkoutToken,
            String providerReference
    ) {
        public static PaymentResult succeeded(String tx, String providerReference) {
            return new PaymentResult(true, tx, null, null, null, providerReference);
        }

        public static PaymentResult failure(String errorCode) {
            return new PaymentResult(false, null, errorCode, null, null, null);
        }

        public static PaymentResult pendingAction(
                String errorCode,
                String checkoutUrl,
                String checkoutToken,
                String providerReference
        ) {
            return new PaymentResult(false, null, errorCode, checkoutUrl, checkoutToken, providerReference);
        }
    }

    record VerifyResult(boolean success, String transactionId, String errorCode, String providerReference) {}
}
