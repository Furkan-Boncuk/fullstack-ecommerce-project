package com.furkan.ecommerce.cart.dto;

import java.math.BigDecimal;
import java.util.List;

public record CartView(Long userId, List<CartLineView> items, CartSummaryView summary) {
    public record CartLineView(
            Long productId,
            String name,
            String imageUrl,
            BigDecimal unitPrice,
            Integer quantity,
            BigDecimal lineTotal,
            Integer availableStock
    ) {}

    public record CartSummaryView(Integer itemCount, BigDecimal subtotal) {}
}
