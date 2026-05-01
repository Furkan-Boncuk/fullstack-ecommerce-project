package com.furkan.ecommerce.product.api.dto;

import java.math.BigDecimal;

public record ProductFilterRequest(
        String search,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        String categorySlug,
        Boolean inStock
) {}
