package com.furkan.ecommerce.common.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxRecorder {
    private final OutboxEventRepository repository;
    private final ObjectMapper objectMapper;

    public void record(DomainEvent event) {
        try {
            repository.save(OutboxEvent.create(
                    event.aggregateType(),
                    event.aggregateId(),
                    event.getClass().getSimpleName(),
                    objectMapper.writeValueAsString(event)
            ));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize event", e);
        }
    }
}
