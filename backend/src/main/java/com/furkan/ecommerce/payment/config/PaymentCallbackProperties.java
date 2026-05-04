package com.furkan.ecommerce.payment.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.payment")
public record PaymentCallbackProperties(
        String callbackUrl,
        String frontendResultUrl,
        String provider,
        Duration attemptTtl,
        IyzicoProperties iyzico
) {
    public Duration attemptTtl() {
        return attemptTtl == null ? Duration.ofMinutes(30) : attemptTtl;
    }

    public record IyzicoProperties(
            String apiKey,
            String secretKey,
            String baseUrl
    ) {}
}
