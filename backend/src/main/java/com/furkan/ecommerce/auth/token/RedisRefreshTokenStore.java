package com.furkan.ecommerce.auth.token;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisRefreshTokenStore implements RefreshTokenStore {
    private static final String PREFIX = "auth:refresh:user:";
    private static final String INDEX_PREFIX = "auth:refresh:index:";

    private final StringRedisTemplate redis;
    private final String keyPrefix;

    public RedisRefreshTokenStore(
            StringRedisTemplate redis,
            @Value("${app.redis.key-prefix:}") String keyPrefix
    ) {
        this.redis = redis;
        this.keyPrefix = keyPrefix == null ? "" : keyPrefix;
    }

    @Override
    public void save(Long userId, String tokenHash, String jti, Instant expiresAt) {
        deleteByUserId(userId);
        String value = String.join("|", tokenHash, jti, expiresAt.toString());
        Duration ttl = Duration.between(Instant.now(), expiresAt);
        redis.opsForValue().set(userKey(userId), value, ttl);
        redis.opsForValue().set(indexKey(tokenHash), userId.toString(), ttl);
    }

    @Override
    public Optional<RefreshTokenRecord> findByUserId(Long userId) {
        String value = redis.opsForValue().get(userKey(userId));
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        String[] parts = value.split("\\|");
        if (parts.length != 3) {
            return Optional.empty();
        }
        return Optional.of(new RefreshTokenRecord(userId, parts[0], parts[1], Instant.parse(parts[2])));
    }

    @Override
    public Optional<RefreshTokenRecord> findByTokenHash(String tokenHash) {
        String userIdRaw = redis.opsForValue().get(indexKey(tokenHash));
        if (userIdRaw == null || userIdRaw.isBlank()) {
            return Optional.empty();
        }
        Long userId = Long.parseLong(userIdRaw);
        return findByUserId(userId).filter(record -> record.tokenHash().equals(tokenHash));
    }

    @Override
    public void deleteByUserId(Long userId) {
        String value = redis.opsForValue().get(userKey(userId));
        if (value != null && !value.isBlank()) {
            String[] parts = value.split("\\|");
            if (parts.length == 3) {
                redis.delete(indexKey(parts[0]));
            }
        }
        redis.delete(userKey(userId));
    }

    String userKey(Long userId) {
        return keyPrefix + PREFIX + userId;
    }

    String indexKey(String hash) {
        return keyPrefix + INDEX_PREFIX + hash;
    }
}
