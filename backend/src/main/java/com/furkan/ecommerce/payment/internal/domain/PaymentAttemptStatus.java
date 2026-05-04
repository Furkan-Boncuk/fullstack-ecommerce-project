package com.furkan.ecommerce.payment.internal.domain;

public enum PaymentAttemptStatus {
    INIT_REQUESTED,
    ACTION_REQUIRED,
    SUCCEEDED,
    FAILED,
    EXPIRED
}
