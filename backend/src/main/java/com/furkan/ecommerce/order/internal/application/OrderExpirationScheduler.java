package com.furkan.ecommerce.order.internal.application;

import com.furkan.ecommerce.common.outbox.OutboxRecorder;
import com.furkan.ecommerce.order.api.event.OrderExpiredEvent;
import com.furkan.ecommerce.order.internal.domain.Order;
import com.furkan.ecommerce.order.internal.domain.OrderStatus;
import com.furkan.ecommerce.order.internal.persistence.OrderRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
class OrderExpirationScheduler {
    private final OrderRepository orderRepository;
    private final OutboxRecorder outboxRecorder;

    @Scheduled(fixedDelayString = "${app.order.expiration-delay-ms:60000}")
    @Transactional
    public void expirePayableOrders() {
        Instant now = Instant.now();
        List<Order> orders = orderRepository.findByStatusInAndExpiresAtBefore(
                List.of(OrderStatus.PENDING, OrderStatus.PAYMENT_FAILED),
                now
        );
        for (Order order : orders) {
            if (order.expire(now) == Order.TransitionResult.CHANGED) {
                outboxRecorder.record(new OrderExpiredEvent(UUID.randomUUID(), order.getId(), order.getUserId(), now));
            }
        }
    }
}
