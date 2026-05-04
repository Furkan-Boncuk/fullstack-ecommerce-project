package com.furkan.ecommerce.auth.dto;

public record AuthResponse(String accessToken, String tokenType, long expiresInSeconds, AuthPrincipalView user) {}
