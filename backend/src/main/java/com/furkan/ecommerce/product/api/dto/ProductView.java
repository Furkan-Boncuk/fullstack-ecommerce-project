package com.furkan.ecommerce.product.api.dto;

import java.math.BigDecimal;

public record ProductView(
        Long id,
        String name,
        String description,
        ProductCategorySummary category,
        String imageUrl,
        BigDecimal price,
        Integer stock
) {}
