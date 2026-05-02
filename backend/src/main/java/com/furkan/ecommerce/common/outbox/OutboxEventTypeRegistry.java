package com.furkan.ecommerce.common.outbox;

import com.furkan.ecommerce.common.exception.BusinessException;
import com.furkan.ecommerce.order.api.event.OrderCreatedEvent;
import com.furkan.ecommerce.order.api.event.OrderExpiredEvent;
import com.furkan.ecommerce.payment.api.event.PaymentFailedEvent;
import com.furkan.ecommerce.payment.api.event.PaymentRequiresReviewEvent;
import com.furkan.ecommerce.payment.api.event.PaymentSucceededEvent;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class OutboxEventTypeRegistry {
    private final Map<String, Class<?>> types = Map.of(
            OrderCreatedEvent.class.getSimpleName(), OrderCreatedEvent.class,
            OrderExpiredEvent.class.getSimpleName(), OrderExpiredEvent.class,
            PaymentSucceededEvent.class.getSimpleName(), PaymentSucceededEvent.class,
            PaymentFailedEvent.class.getSimpleName(), PaymentFailedEvent.class,
            PaymentRequiresReviewEvent.class.getSimpleName(), PaymentRequiresReviewEvent.class
    );

    public Class<?> resolve(String eventType) {
        Class<?> type = types.get(eventType);
        if (type == null) {
            throw new BusinessException("OUTBOX_EVENT_TYPE_NOT_REGISTERED", "Unregistered outbox event type: " + eventType);
        }
        return type;
    }
}
