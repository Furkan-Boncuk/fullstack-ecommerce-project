package com.furkan.ecommerce.auth.internal.application;

public record AuthTokenResult(String accessToken, String tokenType, long expiresInSeconds, String refreshToken) {
}
