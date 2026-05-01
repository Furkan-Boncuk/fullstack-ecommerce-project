package com.furkan.ecommerce.order;

import com.furkan.ecommerce.common.exception.BusinessException;
import com.furkan.ecommerce.order.internal.domain.Order;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderStateMachineTest {
    @Test
    void should_mark_pending_order_as_paid() {
        Order order = Order.create(1L, BigDecimal.TEN);
        order.markPaid();
        assertThat(order.getStatus().name()).isEqualTo("PAID");
    }

    @Test
    void should_reject_paid_transition_when_not_pending() {
        Order order = Order.create(1L, BigDecimal.TEN);
        order.markFailed();
        assertThatThrownBy(order::markPaid).isInstanceOf(BusinessException.class);
    }
}
