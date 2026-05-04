package com.furkan.ecommerce.payment.application;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.furkan.ecommerce.auth.dto.AuthPaymentProfileView;
import com.furkan.ecommerce.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PaymentProfileValidatorTest {
    private PaymentProfileValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PaymentProfileValidator();
    }

    @Test
    void should_accept_complete_payment_profile() {
        assertThatCode(() -> validator.validate(profile("Istanbul")))
                .doesNotThrowAnyException();
    }

    @Test
    void should_reject_incomplete_payment_profile() {
        assertThatThrownBy(() -> validator.validate(profile(" ")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("User profile is incomplete for payment");
    }

    private AuthPaymentProfileView profile(String city) {
        return new AuthPaymentProfileView(
                1L,
                "user@test.com",
                "Furkan",
                "Boncuk",
                "+905551112233",
                "12345678901",
                "Test Mahallesi Test Sokak",
                city,
                "Turkey",
                "34000"
        );
    }
}
