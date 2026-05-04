package com.furkan.ecommerce.order.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.furkan.ecommerce.common.outbox.ProcessedEvent;
import com.furkan.ecommerce.common.outbox.ProcessedEventRepository;
import com.furkan.ecommerce.order.domain.Order;
import com.furkan.ecommerce.order.domain.OrderStatus;
import com.furkan.ecommerce.order.persistence.OrderRepository;
import com.furkan.ecommerce.payment.event.PaymentFailedEvent;
import com.furkan.ecommerce.payment.event.PaymentSucceededEvent;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

class OrderPaymentEventHandlerIdempotencyTest {
    private FakeOrderRepository orderRepository;
    private FakeProcessedEventRepository processedEventRepository;
    private OrderPaymentEventHandler handler;

    @BeforeEach
    void setUp() {
        orderRepository = new FakeOrderRepository();
        processedEventRepository = new FakeProcessedEventRepository();
        handler = new OrderPaymentEventHandler(orderRepository.proxy(), processedEventRepository.proxy());
    }

    @Test
    void should_not_mark_order_paid_twice_when_payment_succeeded_event_is_replayed() {
        Order order = orderRepository.store(order());
        PaymentSucceededEvent event = new PaymentSucceededEvent(UUID.randomUUID(), order.getId(), "tx-1", Instant.now());

        handler.on(event);
        handler.on(event);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(orderRepository.findByIdCalls).isEqualTo(1);
        assertThat(processedEventRepository.savedEvents).hasSize(1);
    }

    @Test
    void should_not_mark_order_failed_twice_when_payment_failed_event_is_replayed() {
        Order order = orderRepository.store(order());
        PaymentFailedEvent event = new PaymentFailedEvent(UUID.randomUUID(), order.getId(), "tx-1", Instant.now());

        handler.on(event);
        handler.on(event);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_FAILED);
        assertThat(orderRepository.findByIdCalls).isEqualTo(1);
        assertThat(processedEventRepository.savedEvents).hasSize(1);
    }

    private Order order() {
        Order order = Order.create(
                1L,
                List.of(new Order.OrderLineInput(10L, "Kulaklik", null, 1, BigDecimal.TEN)),
                Instant.now().plusSeconds(1800)
        );
        ReflectionTestUtils.setField(order, "id", 100L);
        return order;
    }

    private static class FakeOrderRepository {
        private Order order;
        private int findByIdCalls;

        Order store(Order value) {
            order = value;
            return order;
        }

        OrderRepository proxy() {
            return (OrderRepository) Proxy.newProxyInstance(
                    OrderRepository.class.getClassLoader(),
                    new Class<?>[]{OrderRepository.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "findById" -> {
                            findByIdCalls++;
                            yield Optional.ofNullable(order);
                        }
                        default -> throw new UnsupportedOperationException(method.getName());
                    }
            );
        }
    }

    private static class FakeProcessedEventRepository {
        private final Set<ProcessedEvent.Key> savedEvents = new HashSet<>();

        ProcessedEventRepository proxy() {
            return (ProcessedEventRepository) Proxy.newProxyInstance(
                    ProcessedEventRepository.class.getClassLoader(),
                    new Class<?>[]{ProcessedEventRepository.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "saveAndFlush" -> save((ProcessedEvent) args[0]);
                        default -> throw new UnsupportedOperationException(method.getName());
                    }
            );
        }

        private ProcessedEvent save(ProcessedEvent event) {
            if (!savedEvents.add(new ProcessedEvent.Key(event.getConsumer(), event.getEventId()))) {
                throw new DataIntegrityViolationException("duplicate processed event");
            }
            return event;
        }
    }
}
