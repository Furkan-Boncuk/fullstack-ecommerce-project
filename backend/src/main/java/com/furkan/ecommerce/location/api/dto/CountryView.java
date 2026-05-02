package com.furkan.ecommerce.location.api.dto;

public record CountryView(
        Long id,
        String code,
        String name,
        String displayName
) {}
