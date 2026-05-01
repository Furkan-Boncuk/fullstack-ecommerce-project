package com.furkan.ecommerce.payment.internal;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.payment")
public record PaymentCallbackProperties(
        String callbackUrl,
        String provider,
        IyzicoProperties iyzico
) {
    public record IyzicoProperties(
            String apiKey,
            String secretKey,
            String baseUrl
    ) {}
}
