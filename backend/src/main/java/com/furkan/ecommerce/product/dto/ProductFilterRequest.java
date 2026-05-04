package com.furkan.ecommerce.product.dto;

import java.math.BigDecimal;

public record ProductFilterRequest(
        String search,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        String categorySlug,
        Boolean inStock
) {}
