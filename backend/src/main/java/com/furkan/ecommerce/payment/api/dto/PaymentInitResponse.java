package com.furkan.ecommerce.payment.api.dto;

public record PaymentInitResponse(
        boolean success,
        String transactionId,
        String errorCode,
        String checkoutUrl,
        String checkoutToken,
        String providerReference
) {}

