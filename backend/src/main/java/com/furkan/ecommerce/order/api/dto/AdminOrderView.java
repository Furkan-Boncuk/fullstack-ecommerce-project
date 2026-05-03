package com.furkan.ecommerce.order.api.dto;

public record AdminOrderView(
        Long id,
        Long userId,
        String userEmail,
        String status,
        java.math.BigDecimal totalAmount,
        java.time.Instant createdAt,
        java.time.Instant expiresAt,
        OrderView.ShippingAddressView shippingAddress,
        java.util.List<OrderView.OrderItemView> items
) {
}
