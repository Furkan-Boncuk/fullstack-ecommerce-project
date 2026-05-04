package com.furkan.ecommerce.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record AuthCookieProperties(
        String refreshCookieName,
        String refreshCookiePath,
        boolean refreshCookieSecure
) {
}
