package com.furkan.ecommerce.payment.api.dto;

import jakarta.validation.constraints.NotNull;

public record PaymentInitRequest(
        @NotNull Long orderId
) {}
