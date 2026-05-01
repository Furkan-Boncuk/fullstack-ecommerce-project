package com.furkan.ecommerce.product.api.dto;

import java.math.BigDecimal;

public record ProductView(Long id, String name, BigDecimal price, Integer stock) {}
