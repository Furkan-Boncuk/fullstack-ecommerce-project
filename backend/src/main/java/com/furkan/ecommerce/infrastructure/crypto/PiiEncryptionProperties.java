package com.furkan.ecommerce.infrastructure.crypto;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.pii")
public record PiiEncryptionProperties(String encryptionKey) {
}

