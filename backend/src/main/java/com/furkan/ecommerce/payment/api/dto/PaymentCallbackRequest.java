package com.furkan.ecommerce.payment.api.dto;

import jakarta.validation.constraints.NotBlank;

public record PaymentCallbackRequest(
        @NotBlank(message = "{validation.required}") String token
) {
}
