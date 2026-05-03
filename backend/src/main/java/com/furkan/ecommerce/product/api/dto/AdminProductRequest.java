package com.furkan.ecommerce.product.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record AdminProductRequest(
        @NotBlank(message = "{validation.required}")
        @Size(max = 255, message = "{validation.productNameSize}")
        String name,
        @Size(max = 1000, message = "{validation.productDescriptionSize}")
        String description,
        @NotNull(message = "{validation.required}")
        @DecimalMin(value = "0.01", message = "{validation.pricePositive}")
        BigDecimal price,
        @NotNull(message = "{validation.required}")
        @Min(value = 0, message = "{validation.stockPositiveOrZero}")
        Integer stock,
        @Size(max = 500, message = "{validation.imageUrlSize}")
        String imageUrl,
        @NotNull(message = "{validation.required}")
        Long categoryId
) {
}
