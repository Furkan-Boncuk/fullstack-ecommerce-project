package com.furkan.ecommerce.payment.api.dto;

import java.time.Instant;

public record PaymentInitResponse(
        boolean success,
        String status,
        String transactionId,
        String errorCode,
        String checkoutUrl,
        String checkoutToken,
        String providerReference,
        Instant expiresAt
) {}
