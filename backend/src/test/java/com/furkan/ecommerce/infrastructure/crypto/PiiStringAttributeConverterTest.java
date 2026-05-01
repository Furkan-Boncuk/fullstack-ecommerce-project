package com.furkan.ecommerce.infrastructure.crypto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PiiStringAttributeConverterTest {
    private PiiStringAttributeConverter converter;

    @BeforeEach
    void setUp() {
        new PiiCrypto(new PiiEncryptionProperties("MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=")).initialize();
        converter = new PiiStringAttributeConverter();
    }

    @Test
    void should_encrypt_database_value_and_decrypt_to_plaintext() {
        String encrypted = converter.convertToDatabaseColumn("12345678901");

        assertThat(encrypted).startsWith("enc:v1:");
        assertThat(encrypted).doesNotContain("12345678901");
        assertThat(converter.convertToEntityAttribute(encrypted)).isEqualTo("12345678901");
    }

    @Test
    void should_read_legacy_plaintext_value_without_failing() {
        assertThat(converter.convertToEntityAttribute("legacy-value")).isEqualTo("legacy-value");
    }
}

