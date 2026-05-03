package com.furkan.ecommerce.auth.api.dto;

public record AuthResponse(String accessToken, String tokenType, long expiresInSeconds, AuthPrincipalView user) {}
