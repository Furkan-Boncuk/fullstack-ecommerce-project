package com.furkan.ecommerce.order;

import com.furkan.ecommerce.common.exception.BusinessException;
import com.furkan.ecommerce.order.domain.Order;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderStateMachineTest {
    @Test
    void should_mark_pending_order_as_paid() {
        Order order = order();

        order.markPaid();

        assertThat(order.getStatus().name()).isEqualTo("PAID");
    }

    @Test
    void should_allow_payment_failed_order_to_be_paid_after_retry() {
        Order order = order();
        order.markPaymentFailed();

        order.markPaid();

        assertThat(order.getStatus().name()).isEqualTo("PAID");
    }

    @Test
    void should_reject_paid_order_becoming_payment_failed() {
        Order order = order();
        order.markPaid();

        assertThatThrownBy(order::markPaymentFailed).isInstanceOf(BusinessException.class);
    }

    private Order order() {
        return Order.create(1L, List.of(new Order.OrderLineInput(10L, "Test Ürün", null, 1, BigDecimal.TEN)), Instant.now().plusSeconds(1800));
    }
}
