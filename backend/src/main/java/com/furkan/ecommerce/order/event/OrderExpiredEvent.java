package com.furkan.ecommerce.order.event;

import com.furkan.ecommerce.common.outbox.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record OrderExpiredEvent(
        UUID eventId,
        Long orderId,
        Long userId,
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
}
