package com.furkan.ecommerce.order.internal.domain;

public enum OrderStatus {
    PENDING,
    PAYMENT_FAILED,
    PAID,
    EXPIRED,
    CANCELLED,
    REQUIRES_REVIEW
}
