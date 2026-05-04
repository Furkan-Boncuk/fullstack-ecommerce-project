package com.furkan.ecommerce.payment.internal.domain;

public enum PaymentStatus {
    INITIATED,
    ACTION_REQUIRED,
    SUCCEEDED,
    FAILED,
    REQUIRES_REVIEW
}
