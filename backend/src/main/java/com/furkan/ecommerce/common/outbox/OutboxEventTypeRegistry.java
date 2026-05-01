package com.furkan.ecommerce.common.outbox;

import com.furkan.ecommerce.order.api.event.OrderPlacedEvent;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class OutboxEventTypeRegistry {
    private final Map<String, Class<?>> types = Map.of(
            OrderPlacedEvent.class.getSimpleName(), OrderPlacedEvent.class
    );

    public Class<?> resolve(String eventType) {
        return types.getOrDefault(eventType, OrderPlacedEvent.class);
    }
}
