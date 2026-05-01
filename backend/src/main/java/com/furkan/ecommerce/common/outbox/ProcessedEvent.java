package com.furkan.ecommerce.common.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "processed_events")
@IdClass(ProcessedEvent.Key.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProcessedEvent {
    @Id
    @Column(nullable = false, length = 150)
    private String consumer;

    @Id
    @Column(nullable = false)
    private UUID eventId;

    @Column(nullable = false)
    private Instant processedAt;

    public static ProcessedEvent of(String consumer, UUID eventId) {
        ProcessedEvent e = new ProcessedEvent();
        e.consumer = consumer;
        e.eventId = eventId;
        e.processedAt = Instant.now();
        return e;
    }

    public record Key(String consumer, UUID eventId) implements Serializable {}
}
