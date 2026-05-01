package com.furkan.ecommerce.payment.internal;

import com.furkan.ecommerce.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentStateMachineTest {

    @Test
    void should_return_noop_when_mark_succeeded_twice() {
        Payment payment = Payment.initiate(1L, BigDecimal.TEN, "iyzico", "tx-1", null, null, "1");

        assertThat(payment.markSucceeded("tx-1")).isEqualTo(Payment.TransitionResult.CHANGED);
        assertThat(payment.markSucceeded("tx-1")).isEqualTo(Payment.TransitionResult.NOOP);
    }

    @Test
    void should_throw_when_mark_failed_after_succeeded() {
        Payment payment = Payment.initiate(1L, BigDecimal.TEN, "iyzico", "tx-1", null, null, "1");
        payment.markSucceeded("tx-1");

        assertThatThrownBy(() -> payment.markFailed("tx-2"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Successful payment cannot transition to failed");
    }

    @Test
    void should_throw_when_mark_succeeded_after_failed() {
        Payment payment = Payment.initiate(1L, BigDecimal.TEN, "iyzico", "tx-1", null, null, "1");
        payment.markFailed("tx-1");

        assertThatThrownBy(() -> payment.markSucceeded("tx-2"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Failed payment cannot transition to succeeded");
    }
}
