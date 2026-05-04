package com.furkan.ecommerce.auth.application;

import com.furkan.ecommerce.auth.dto.AuthPrincipalView;

public record AuthTokenResult(String accessToken, String tokenType, long expiresInSeconds, String refreshToken, AuthPrincipalView user) {
}
