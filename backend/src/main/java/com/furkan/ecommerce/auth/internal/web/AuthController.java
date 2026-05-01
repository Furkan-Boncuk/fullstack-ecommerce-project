package com.furkan.ecommerce.auth.internal.web;

import com.furkan.ecommerce.auth.api.dto.AuthResponse;
import com.furkan.ecommerce.auth.api.dto.LoginRequest;
import com.furkan.ecommerce.auth.api.dto.RegisterRequest;
import com.furkan.ecommerce.auth.internal.application.AuthCommandService;
import com.furkan.ecommerce.auth.internal.application.AuthTokenResult;
import com.furkan.ecommerce.auth.internal.config.AuthCookieProperties;
import com.furkan.ecommerce.auth.internal.exception.AuthException;
import com.furkan.ecommerce.auth.internal.mapper.AuthMapper;
import com.furkan.ecommerce.infrastructure.jwt.JwtProperties;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private static final String REFRESH_COOKIE = "refresh_token";

    private final AuthCommandService service;
    private final AuthMapper mapper;
    private final JwtProperties jwtProperties;
    private final AuthCookieProperties cookieProperties;

    @PostMapping("/register")
    ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return withRefreshCookie(service.register(request));
    }

    @PostMapping("/login")
    ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return withRefreshCookie(service.login(request));
    }

    @PostMapping("/refresh")
    ResponseEntity<AuthResponse> refresh(@CookieValue(value = REFRESH_COOKIE, required = false) String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new AuthException("Missing refresh token");
        }
        return withRefreshCookie(service.refresh(refreshToken));
    }

    @PostMapping("/logout")
    ResponseEntity<Void> logout(@CookieValue(value = REFRESH_COOKIE, required = false) String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new AuthException("Missing refresh token");
        }
        service.logout(refreshToken);
        var cookie = ResponseCookie.from(cookieProperties.refreshCookieName(), "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path(cookieProperties.refreshCookiePath())
                .maxAge(0)
                .build();
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, cookie.toString()).build();
    }

    private ResponseEntity<AuthResponse> withRefreshCookie(AuthTokenResult result) {
        ResponseCookie cookie = ResponseCookie.from(cookieProperties.refreshCookieName(), result.refreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path(cookieProperties.refreshCookiePath())
                .maxAge(jwtProperties.refreshTtl())
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(mapper.toResponse(result));
    }
}
