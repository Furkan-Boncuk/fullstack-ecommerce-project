package com.furkan.ecommerce.payment.application;

import com.furkan.ecommerce.common.exception.BusinessException;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
class PaymentReferenceParser {
    private static final Pattern ORDER_REFERENCE = Pattern.compile("^order-(\\d+)-.+$");

    Long parseOrderId(String providerReference) {
        if (isBlank(providerReference)) {
            throw invalidReference();
        }
        var matcher = ORDER_REFERENCE.matcher(providerReference);
        if (matcher.matches()) {
            return Long.valueOf(matcher.group(1));
        }
        try {
            return Long.valueOf(providerReference);
        } catch (NumberFormatException ex) {
            throw invalidReference();
        }
    }

    private BusinessException invalidReference() {
        return new BusinessException("PAYMENT_PROVIDER_REFERENCE_INVALID", "Invalid provider reference");
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
