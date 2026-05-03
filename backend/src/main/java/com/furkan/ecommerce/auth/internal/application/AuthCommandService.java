package com.furkan.ecommerce.auth.internal.application;

import com.furkan.ecommerce.auth.api.dto.LoginRequest;
import com.furkan.ecommerce.auth.api.dto.RegisterRequest;
import com.furkan.ecommerce.auth.api.dto.UpdatePaymentProfileRequest;
import com.furkan.ecommerce.auth.api.dto.AuthPaymentProfileView;
import com.furkan.ecommerce.auth.internal.domain.User;
import com.furkan.ecommerce.auth.internal.exception.AuthException;
import com.furkan.ecommerce.auth.internal.exception.EmailAlreadyExistsException;
import com.furkan.ecommerce.auth.internal.mapper.AuthMapper;
import com.furkan.ecommerce.auth.internal.persistence.UserRepository;
import com.furkan.ecommerce.auth.internal.token.RefreshTokenStore;
import com.furkan.ecommerce.common.exception.BusinessException;
import com.furkan.ecommerce.common.exception.ResourceNotFoundException;
import com.furkan.ecommerce.infrastructure.jwt.JwtProperties;
import com.furkan.ecommerce.infrastructure.jwt.JwtTokenProvider;
import com.furkan.ecommerce.location.api.LocationReadApi;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthCommandService {
    private final UserRepository userRepository;
    private final RefreshTokenStore refreshTokenStore;
    private final JwtTokenProvider tokenProvider;
    private final JwtProperties jwtProperties;
    private final LocationReadApi locationReadApi;
    private final AuthMapper authMapper;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @Transactional
    public AuthTokenResult register(RegisterRequest request) {
        userRepository.findByEmail(request.email()).ifPresent(user -> {
            throw new EmailAlreadyExistsException(request.email());
        });

        String hash = encoder.encode(request.password());
        User user = userRepository.save(User.register(request.email(), hash));
        return issueTokens(user);
    }

    @Transactional
    public AuthTokenResult login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new AuthException("Invalid credentials"));
        if (!encoder.matches(request.password(), user.getPasswordHash())) {
            throw new AuthException("Invalid credentials");
        }
        return issueTokens(user);
    }

    @Transactional
    public AuthTokenResult refresh(String refreshTokenPlain) {
        String hash = hash(refreshTokenPlain);
        var token = refreshTokenStore.findByTokenHash(hash)
                .orElseThrow(() -> new AuthException("Invalid refresh token"));

        if (token.expiresAt().isBefore(Instant.now())) {
            refreshTokenStore.deleteByUserId(token.userId());
            throw new AuthException("Refresh token expired");
        }

        User user = userRepository.findById(token.userId())
                .orElseThrow(() -> new AuthException("User not found"));
        return issueTokens(user);
    }

    @Transactional
    public void logout(String refreshTokenPlain) {
        String hash = hash(refreshTokenPlain);
        refreshTokenStore.findByTokenHash(hash).ifPresent(token -> refreshTokenStore.deleteByUserId(token.userId()));
    }

    @Transactional
    public AuthPaymentProfileView updatePaymentProfile(Long userId, UpdatePaymentProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "User not found"));

        validateTurkeyLocation(request);

        user.updatePaymentProfile(
                request.firstName(),
                request.lastName(),
                request.phoneNumber(),
                request.identityNumber(),
                request.address(),
                request.city(),
                request.country(),
                request.zipCode()
        );

        if (!user.hasCompletePaymentProfile()) {
            throw new BusinessException(
                    "PAYMENT_PROFILE_INCOMPLETE",
                    "User profile is incomplete for payment. Required: firstName,lastName,phoneNumber,identityNumber,address,city,country,zipCode"
            );
        }

        return authMapper.toPaymentProfileView(user);
    }

    private void validateTurkeyLocation(UpdatePaymentProfileRequest request) {
        if (!locationReadApi.isSupportedTurkeyLocation(request.country(), request.city())) {
            throw new BusinessException(
                    "PAYMENT_PROFILE_INVALID_LOCATION",
                    "Country must be Turkey and city must be a supported Turkey city"
            );
        }
    }

    private AuthTokenResult issueTokens(User user) {
        String access = tokenProvider.generateAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRoles().stream().map(Enum::name).toList()
        );

        String rawRefresh = UUID.randomUUID().toString() + UUID.randomUUID();
        String jti = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plus(jwtProperties.refreshTtl());
        refreshTokenStore.save(user.getId(), hash(rawRefresh), jti, expiresAt);

        return new AuthTokenResult(access, "Bearer", jwtProperties.accessTtl().toSeconds(), rawRefresh, authMapper.toPrincipalView(user));
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder().encodeToString(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
