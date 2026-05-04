package com.furkan.ecommerce.order.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderView(
        Long id,
        String status,
        BigDecimal totalAmount,
        Instant createdAt,
        Instant expiresAt,
        ShippingAddressView shippingAddress,
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

    public record ShippingAddressView(
            String firstName,
            String lastName,
            String phoneNumber,
            String address,
            String city,
            String country,
            String zipCode
    ) {}
}
