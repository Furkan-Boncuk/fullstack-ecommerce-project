package com.furkan.ecommerce.common.outbox;

import com.furkan.ecommerce.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "outbox_events")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEvent extends BaseEntity {
    @Column(nullable = false, length = 100)
    private String aggregateType;

    @Column(nullable = false, length = 100)
    private String aggregateId;

    @Column(nullable = false, length = 150)
    private String eventType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    private Instant publishedAt;

    @Column(nullable = false)
    private int attempts;

    private String lastError;

    static OutboxEvent create(String aggregateType, String aggregateId, String eventType, String payload) {
        OutboxEvent event = new OutboxEvent();
        event.aggregateType = aggregateType;
        event.aggregateId = aggregateId;
        event.eventType = eventType;
        event.payload = payload;
        event.attempts = 0;
        return event;
    }

    void markPublished() {
        publishedAt = Instant.now();
    }

    void markFailed(String err) {
        attempts++;
        lastError = err;
    }
}
