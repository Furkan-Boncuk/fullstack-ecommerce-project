package com.furkan.ecommerce.payment.domain;

public enum PaymentAttemptStatus {
    INIT_REQUESTED,
    ACTION_REQUIRED,
    SUCCEEDED,
    FAILED,
    EXPIRED
}
