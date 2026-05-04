package com.furkan.ecommerce.order.dto;

public enum OrderPaymentState {
    PAYABLE,
    NOT_PAYABLE,
    NOT_FULFILLABLE_AFTER_CALLBACK;

    public boolean isPayable() {
        return this == PAYABLE;
    }

    public boolean isFulfillableAfterCallback() {
        return this != NOT_FULFILLABLE_AFTER_CALLBACK;
    }
}
