package com.furkan.ecommerce.order.api.dto;

import java.math.BigDecimal;

public record OrderPaymentView(
        Long orderId,
        Long userId,
        String status,
        BigDecimal totalAmount
) {}

