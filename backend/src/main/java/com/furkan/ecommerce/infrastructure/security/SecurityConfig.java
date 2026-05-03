package com.furkan.ecommerce.infrastructure.security;

import com.furkan.ecommerce.auth.internal.config.AuthCookieProperties;
import com.furkan.ecommerce.infrastructure.crypto.PiiEncryptionProperties;
import com.furkan.ecommerce.infrastructure.jwt.JwtProperties;
import com.furkan.ecommerce.payment.internal.PaymentCallbackProperties;
import java.util.List;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties({JwtProperties.class, AuthCookieProperties.class, PaymentCallbackProperties.class, PiiEncryptionProperties.class, CorsProperties.class})
public class SecurityConfig {
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/actuator/health/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/products/**", "/api/v1/categories/**", "/api/v1/locations/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/payments/callback").permitAll()
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(CorsProperties properties) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(properties.allowedOriginPatterns());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Set-Cookie", "Authorization", "X-Correlation-Id"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration paymentCallbackConfiguration = new CorsConfiguration();
        paymentCallbackConfiguration.setAllowedOriginPatterns(List.of("https://*.iyzipay.com"));
        paymentCallbackConfiguration.setAllowedMethods(List.of("POST", "OPTIONS"));
        paymentCallbackConfiguration.setAllowedHeaders(List.of("*"));
        paymentCallbackConfiguration.setAllowCredentials(false);
        paymentCallbackConfiguration.setMaxAge(3600L);
        source.registerCorsConfiguration("/api/v1/payments/callback", paymentCallbackConfiguration);
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
