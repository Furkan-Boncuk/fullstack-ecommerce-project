package com.furkan.ecommerce.order.dto;

import java.util.List;

public record OrderInventoryView(Long orderId, List<OrderInventoryLineView> lines) {
    public record OrderInventoryLineView(Long productId, Integer quantity) {}
}

