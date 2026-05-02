package com.furkan.ecommerce.auth.internal.web;

import com.furkan.ecommerce.auth.api.AuthReadApi;
import com.furkan.ecommerce.auth.api.dto.AuthResponse;
import com.furkan.ecommerce.auth.api.dto.LoginRequest;
import com.furkan.ecommerce.auth.api.dto.RegisterRequest;
import com.furkan.ecommerce.auth.api.dto.UpdatePaymentProfileRequest;
import com.furkan.ecommerce.auth.api.dto.AuthPaymentProfileView;
import com.furkan.ecommerce.auth.internal.application.AuthCommandService;
import com.furkan.ecommerce.auth.internal.application.AuthTokenResult;
import com.furkan.ecommerce.auth.internal.config.AuthCookieProperties;
import com.furkan.ecommerce.auth.internal.exception.AuthException;
import com.furkan.ecommerce.auth.internal.mapper.AuthMapper;
import com.furkan.ecommerce.common.exception.ResourceNotFoundException;
import com.furkan.ecommerce.common.exception.UnauthorizedException;
import com.furkan.ecommerce.infrastructure.security.SecurityPrincipal;
import com.furkan.ecommerce.infrastructure.jwt.JwtProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthCommandService service;
    private final AuthReadApi readApi;
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
    ResponseEntity<AuthResponse> refresh(HttpServletRequest request) {
        String refreshToken = extractRefreshToken(request);
        return withRefreshCookie(service.refresh(refreshToken));
    }

    @PostMapping("/logout")
    ResponseEntity<Void> logout(HttpServletRequest request) {
        String refreshToken = extractRefreshToken(request);
        service.logout(refreshToken);
        var cookie = ResponseCookie.from(cookieProperties.refreshCookieName(), "")
                .httpOnly(true)
                .secure(cookieProperties.refreshCookieSecure())
                .sameSite("Lax")
                .path(cookieProperties.refreshCookiePath())
                .maxAge(0)
                .build();
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, cookie.toString()).build();
    }


    @GetMapping("/profile/payment")
    ResponseEntity<AuthPaymentProfileView> getPaymentProfile(@AuthenticationPrincipal SecurityPrincipal principal) {
        if (principal == null) {
            throw new UnauthorizedException("Unauthorized");
        }
        return ResponseEntity.ok(readApi.findPaymentProfileById(principal.userId())
                .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "User not found")));
    }

    @PatchMapping("/profile/payment")
    ResponseEntity<AuthPaymentProfileView> updatePaymentProfile(
            @AuthenticationPrincipal SecurityPrincipal principal,
            @Valid @RequestBody UpdatePaymentProfileRequest request
    ) {
        if (principal == null) {
            throw new UnauthorizedException("Unauthorized");
        }
        return ResponseEntity.ok(service.updatePaymentProfile(principal.userId(), request));
    }

    private ResponseEntity<AuthResponse> withRefreshCookie(AuthTokenResult result) {
        ResponseCookie cookie = ResponseCookie.from(cookieProperties.refreshCookieName(), result.refreshToken())
                .httpOnly(true)
                .secure(cookieProperties.refreshCookieSecure())
                .sameSite("Lax")
                .path(cookieProperties.refreshCookiePath())
                .maxAge(jwtProperties.refreshTtl())
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(mapper.toResponse(result));
    }

    private String extractRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            throw new AuthException("Missing refresh token");
        }

        for (Cookie cookie : cookies) {
            if (cookieProperties.refreshCookieName().equals(cookie.getName())
                    && cookie.getValue() != null
                    && !cookie.getValue().isBlank()) {
                return cookie.getValue();
            }
        }

        throw new AuthException("Missing refresh token");
    }
}
