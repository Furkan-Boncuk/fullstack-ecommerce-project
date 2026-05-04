package com.furkan.ecommerce.common.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class OutboxDispatcher {
    private final OutboxEventRepository repository;
    private final ApplicationEventPublisher publisher;
    private final ObjectMapper objectMapper;
    private final OutboxEventTypeRegistry registry;
    private final int batchSize;

    public OutboxDispatcher(
            OutboxEventRepository repository,
            ApplicationEventPublisher publisher,
            ObjectMapper objectMapper,
            OutboxEventTypeRegistry registry,
            @Value("${app.outbox.batch-size:50}") int batchSize
    ) {
        this.repository = repository;
        this.publisher = publisher;
        this.objectMapper = objectMapper;
        this.registry = registry;
        this.batchSize = batchSize <= 0 ? 50 : batchSize;
    }

    @Scheduled(fixedDelayString = "${app.outbox.delay-ms:1000}")
    @Transactional
    public void dispatch() {
        List<OutboxEvent> events = repository.findPendingForDispatch(batchSize);
        for (OutboxEvent event : events) {
            try {
                var type = registry.resolve(event.getEventType());
                var payload = objectMapper.readValue(event.getPayload(), type);
                publisher.publishEvent(payload);
                event.markPublished();
            } catch (Exception ex) {
                event.markFailed(ex.getMessage());
                log.error("Outbox dispatch failed id={}", event.getId(), ex);
            }
        }
    }
}
