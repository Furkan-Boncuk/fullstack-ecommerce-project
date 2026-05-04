package com.furkan.ecommerce.order.event;

import com.furkan.ecommerce.common.outbox.DomainEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderCreatedEvent(
        UUID eventId,
        Long orderId,
        Long userId,
        BigDecimal totalAmount,
        List<OrderItemSnapshot> items,
        Instant occurredAt
) implements DomainEvent {
    @Override
    public String aggregateType() {
        return "Order";
    }

    @Override
    public String aggregateId() {
        return String.valueOf(orderId);
    }

    public record OrderItemSnapshot(Long productId, Integer quantity) {}
}

