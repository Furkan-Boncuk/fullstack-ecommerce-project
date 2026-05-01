package com.furkan.ecommerce.order.api.dto;

import java.math.BigDecimal;

public record OrderView(Long id, String status, BigDecimal totalAmount) {}
