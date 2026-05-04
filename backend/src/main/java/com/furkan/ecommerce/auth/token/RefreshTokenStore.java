package com.furkan.ecommerce.auth.token;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenStore {
    void save(Long userId, String tokenHash, String jti, Instant expiresAt);
    Optional<RefreshTokenRecord> findByUserId(Long userId);
    Optional<RefreshTokenRecord> findByTokenHash(String tokenHash);
    void deleteByUserId(Long userId);

    record RefreshTokenRecord(Long userId, String tokenHash, String jti, Instant expiresAt) {}
}
