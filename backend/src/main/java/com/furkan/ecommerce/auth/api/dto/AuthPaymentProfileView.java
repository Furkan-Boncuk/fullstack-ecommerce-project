package com.furkan.ecommerce.auth.api.dto;

public record AuthPaymentProfileView(
        Long userId,
        String email,
        String firstName,
        String lastName,
        String phoneNumber,
        String identityNumber,
        String address,
        String city,
        String country,
        String zipCode
) {}

