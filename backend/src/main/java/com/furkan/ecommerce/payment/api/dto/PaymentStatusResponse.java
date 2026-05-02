package com.furkan.ecommerce.payment.api.dto;

import java.time.Instant;

public record PaymentStatusResponse(
        Long orderId,
        String paymentStatus,
        String latestAttemptStatus,
        String transactionId,
        String errorCode,
        String checkoutUrl,
        Instant expiresAt
) {}
