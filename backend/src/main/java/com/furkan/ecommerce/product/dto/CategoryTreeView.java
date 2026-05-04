package com.furkan.ecommerce.product.dto;

import java.util.List;

public record CategoryTreeView(
        Long id,
        String name,
        String slug,
        List<CategoryTreeView> children
) {}
