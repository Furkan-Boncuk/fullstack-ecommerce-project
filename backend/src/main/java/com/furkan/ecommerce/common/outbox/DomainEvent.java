package com.furkan.ecommerce.common.outbox;

import java.time.Instant;
import java.util.UUID;

public interface DomainEvent {
    UUID eventId();
    Instant occurredAt();
    String aggregateType();
    String aggregateId();
}
