package com.furkan.ecommerce.order.domain;

public enum OrderStatus {
    PENDING,
    PAYMENT_FAILED,
    PAID,
    EXPIRED,
    CANCELLED,
    REQUIRES_REVIEW
}
