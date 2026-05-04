package com.furkan.ecommerce.payment.dto;

import jakarta.validation.constraints.NotBlank;

public record PaymentCallbackRequest(
        @NotBlank(message = "{validation.required}") String token
) {
}
