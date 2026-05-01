package com.furkan.ecommerce.order.api.event;

import com.furkan.ecommerce.common.outbox.DomainEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderPlacedEvent(UUID eventId, Long orderId, Long userId, BigDecimal totalAmount, Instant occurredAt)
        implements DomainEvent {
    @Override
    public String aggregateType() {
        return "Order";
    }

    @Override
    public String aggregateId() {
        return String.valueOf(orderId);
    }
}
