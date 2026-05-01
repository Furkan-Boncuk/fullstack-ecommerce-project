package com.furkan.ecommerce.infrastructure.crypto;

import jakarta.annotation.PostConstruct;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PiiCrypto {
    private static final String PREFIX = "enc:v1:";
    private static final String CIPHER = "AES/GCM/NoPadding";
    private static final int IV_BYTES = 12;
    private static final int TAG_BITS = 128;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static volatile SecretKey key;

    private final PiiEncryptionProperties properties;

    @PostConstruct
    void initialize() {
        String rawKey = properties.encryptionKey();
        if (rawKey == null || rawKey.isBlank()) {
            throw new IllegalStateException("PII encryption key is required");
        }
        byte[] decoded = Base64.getDecoder().decode(rawKey);
        if (decoded.length != 32) {
            throw new IllegalStateException("PII encryption key must be a Base64 encoded 32-byte key");
        }
        key = new SecretKeySpec(decoded, "AES");
    }

    static String encrypt(String value) {
        if (value == null || value.isBlank() || value.startsWith(PREFIX)) {
            return value;
        }
        try {
            byte[] iv = new byte[IV_BYTES];
            SECURE_RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.ENCRYPT_MODE, requireKey(), new GCMParameterSpec(TAG_BITS, iv));
            byte[] ciphertext = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            byte[] payload = ByteBuffer.allocate(iv.length + ciphertext.length)
                    .put(iv)
                    .put(ciphertext)
                    .array();
            return PREFIX + Base64.getEncoder().encodeToString(payload);
        } catch (Exception ex) {
            throw new IllegalStateException("Could not encrypt PII field", ex);
        }
    }

    static String decrypt(String value) {
        if (value == null || value.isBlank() || !value.startsWith(PREFIX)) {
            return value;
        }
        try {
            byte[] payload = Base64.getDecoder().decode(value.substring(PREFIX.length()));
            ByteBuffer buffer = ByteBuffer.wrap(payload);
            byte[] iv = new byte[IV_BYTES];
            buffer.get(iv);
            byte[] ciphertext = new byte[buffer.remaining()];
            buffer.get(ciphertext);
            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.DECRYPT_MODE, requireKey(), new GCMParameterSpec(TAG_BITS, iv));
            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new IllegalStateException("Could not decrypt PII field", ex);
        }
    }

    private static SecretKey requireKey() {
        SecretKey current = key;
        if (current == null) {
            throw new IllegalStateException("PII encryption key is not initialized");
        }
        return current;
    }
}

