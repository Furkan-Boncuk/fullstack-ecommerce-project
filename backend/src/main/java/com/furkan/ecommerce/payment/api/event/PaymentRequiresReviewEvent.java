package com.furkan.ecommerce.payment.api.event;

import com.furkan.ecommerce.common.outbox.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record PaymentRequiresReviewEvent(
        UUID eventId,
        Long orderId,
        String reason,
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
