package com.furkan.ecommerce.cart.api.dto;

import java.util.List;

public record CartView(Long userId, List<CartLineView> items) {
    public record CartLineView(Long productId, Integer quantity) {}
}
