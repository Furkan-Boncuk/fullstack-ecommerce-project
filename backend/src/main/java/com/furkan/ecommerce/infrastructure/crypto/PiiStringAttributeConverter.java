package com.furkan.ecommerce.infrastructure.crypto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class PiiStringAttributeConverter implements AttributeConverter<String, String> {
    @Override
    public String convertToDatabaseColumn(String attribute) {
        return PiiCrypto.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return PiiCrypto.decrypt(dbData);
    }
}

