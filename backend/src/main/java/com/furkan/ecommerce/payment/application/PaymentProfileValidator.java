package com.furkan.ecommerce.payment.application;

import com.furkan.ecommerce.auth.dto.AuthPaymentProfileView;
import com.furkan.ecommerce.common.exception.BusinessException;
import org.springframework.stereotype.Component;

@Component
class PaymentProfileValidator {

    void validate(AuthPaymentProfileView profile) {
        if (isBlank(profile.firstName())
                || isBlank(profile.lastName())
                || isBlank(profile.phoneNumber())
                || isBlank(profile.identityNumber())
                || isBlank(profile.address())
                || isBlank(profile.city())
                || isBlank(profile.country())
                || isBlank(profile.zipCode())) {
            throw new BusinessException("PAYMENT_PROFILE_INCOMPLETE", "User profile is incomplete for payment");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
