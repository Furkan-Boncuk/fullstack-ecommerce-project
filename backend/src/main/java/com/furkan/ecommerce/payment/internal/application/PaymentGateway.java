package com.furkan.ecommerce.payment.internal.application;

import java.math.BigDecimal;

public interface PaymentGateway {
    PaymentResult charge(Long orderId, BigDecimal amount);

    record PaymentResult(boolean success, String transactionId, String errorCode) {
        public static PaymentResult success(String tx) {
            return new PaymentResult(true, tx, null);
        }

        public static PaymentResult failure(String errorCode) {
            return new PaymentResult(false, null, errorCode);
        }
    }
}
