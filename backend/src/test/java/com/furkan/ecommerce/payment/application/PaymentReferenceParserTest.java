package com.furkan.ecommerce.payment.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.furkan.ecommerce.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PaymentReferenceParserTest {
    private PaymentReferenceParser parser;

    @BeforeEach
    void setUp() {
        parser = new PaymentReferenceParser();
    }

    @Test
    void should_parse_order_id_from_gateway_conversation_reference() {
        Long result = parser.parseOrderId("order-42-9e2f");

        assertThat(result).isEqualTo(42L);
    }

    @Test
    void should_parse_order_id_from_plain_numeric_reference() {
        Long result = parser.parseOrderId("42");

        assertThat(result).isEqualTo(42L);
    }

    @Test
    void should_reject_blank_or_unknown_reference() {
        assertThatThrownBy(() -> parser.parseOrderId(" "))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Invalid provider reference");

        assertThatThrownBy(() -> parser.parseOrderId("checkout-token"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Invalid provider reference");
    }
}
