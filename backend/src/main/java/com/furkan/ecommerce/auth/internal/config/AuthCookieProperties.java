package com.furkan.ecommerce.auth.internal.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record AuthCookieProperties(String refreshCookieName, String refreshCookiePath) {
}
