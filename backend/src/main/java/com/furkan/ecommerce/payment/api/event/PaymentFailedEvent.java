package com.furkan.ecommerce.payment.api.event;

import com.furkan.ecommerce.common.outbox.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record PaymentFailedEvent(
        UUID eventId,
        Long orderId,
        String transactionId,
        Instant occurredAt
) implements DomainEvent {
    @Override
    public String aggregateType() {
        return "Payment";
    }

    @Override
    public String aggregateId() {
        return String.valueOf(orderId);
    }
}

