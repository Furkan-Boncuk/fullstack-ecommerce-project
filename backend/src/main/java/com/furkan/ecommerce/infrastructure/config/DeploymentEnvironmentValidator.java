package com.furkan.ecommerce.infrastructure.config;

import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class DeploymentEnvironmentValidator implements ApplicationRunner {
    private static final List<String> REQUIRED_DEPLOYMENT_PROPERTIES = List.of(
            "spring.datasource.url",
            "spring.datasource.username",
            "spring.datasource.password",
            "spring.data.redis.host",
            "app.redis.key-prefix",
            "app.jwt.secret",
            "app.security.pii.encryption-key",
            "app.payment.callback-url",
            "app.payment.frontend-result-url",
            "app.payment.iyzico.api-key",
            "app.payment.iyzico.secret-key",
            "app.payment.iyzico.base-url",
            "app.cors.allowed-origin-patterns"
    );

    private static final List<String> INSECURE_DEPLOYMENT_VALUES = List.of(
            "please-change-this-secret-key-to-32-char-minimum",
            "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
            "https://example-ngrok-domain.ngrok-free.app/api/v1/payments/callback",
            "http://localhost:5173/payment/result",
            "http://localhost:5173"
    );

    private final Environment environment;

    public DeploymentEnvironmentValidator(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!acceptsProfile("staging") && !acceptsProfile("prod")) {
            return;
        }

        List<String> missing = REQUIRED_DEPLOYMENT_PROPERTIES.stream()
                .filter(property -> isBlank(environment.getProperty(property)))
                .toList();

        if (!missing.isEmpty()) {
            throw new IllegalStateException("Missing deployment configuration: " + String.join(", ", missing));
        }

        List<String> insecure = REQUIRED_DEPLOYMENT_PROPERTIES.stream()
                .filter(property -> hasInsecureValue(environment.getProperty(property)))
                .toList();

        if (!insecure.isEmpty()) {
            throw new IllegalStateException("Insecure deployment defaults are still configured: " + String.join(", ", insecure));
        }
    }

    private boolean acceptsProfile(String profile) {
        return environment.acceptsProfiles(org.springframework.core.env.Profiles.of(profile));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private boolean hasInsecureValue(String value) {
        if (value == null) {
            return false;
        }
        return INSECURE_DEPLOYMENT_VALUES.stream().anyMatch(value::contains);
    }
}
