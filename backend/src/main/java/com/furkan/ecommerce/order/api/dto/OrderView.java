package com.furkan.ecommerce.order.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderView(
        Long id,
        String status,
        BigDecimal totalAmount,
        Instant createdAt,
        Instant expiresAt,
        List<OrderItemView> items
) {
    public record OrderItemView(
            Long productId,
            String productName,
            String productImageUrl,
            BigDecimal unitPrice,
            Integer quantity,
            BigDecimal lineTotal
    ) {}
}
