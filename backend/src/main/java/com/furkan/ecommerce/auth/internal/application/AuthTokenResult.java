package com.furkan.ecommerce.auth.internal.application;

import com.furkan.ecommerce.auth.api.dto.AuthPrincipalView;

public record AuthTokenResult(String accessToken, String tokenType, long expiresInSeconds, String refreshToken, AuthPrincipalView user) {
}
