package com.furkan.ecommerce.location.dto;

public record CityView(
        Long id,
        String name,
        String plateCode,
        String countryCode
) {}
