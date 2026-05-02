package com.furkan.ecommerce.location.api.dto;

public record CityView(
        Long id,
        String name,
        String plateCode,
        String countryCode
) {}
