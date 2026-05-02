package com.furkan.ecommerce.payment.internal;

public enum PaymentAttemptStatus {
    INIT_REQUESTED,
    ACTION_REQUIRED,
    SUCCEEDED,
    FAILED,
    EXPIRED
}
