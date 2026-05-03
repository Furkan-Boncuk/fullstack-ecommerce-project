package com.furkan.ecommerce.cart.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartItemRequest(
        @NotNull(message = "{validation.required}") Long productId,
        @NotNull(message = "{validation.required}") @Min(value = 1, message = "{validation.quantity.min}") Integer quantity
) {}
