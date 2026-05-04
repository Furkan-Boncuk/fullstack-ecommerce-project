package com.furkan.ecommerce.order.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderPaymentView(
        Long orderId,
        Long userId,
        String status,
        BigDecimal totalAmount,
        Instant expiresAt,
        List<OrderPaymentLineView> items
) {
    public record OrderPaymentLineView(
            Long productId,
            String productName,
            String productImageUrl,
            BigDecimal unitPrice,
            Integer quantity
    ) {}
}
