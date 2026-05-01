package com.furkan.ecommerce.product.api.dto;

import java.util.List;

public record CategoryTreeView(
        Long id,
        String name,
        String slug,
        List<CategoryTreeView> children
) {}
