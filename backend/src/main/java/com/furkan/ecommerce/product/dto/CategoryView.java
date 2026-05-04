package com.furkan.ecommerce.product.dto;

public record CategoryView(
        Long id,
        String name,
        String slug,
        String description,
        String imageUrl,
        Long parentId,
        Integer sortOrder
) {}
