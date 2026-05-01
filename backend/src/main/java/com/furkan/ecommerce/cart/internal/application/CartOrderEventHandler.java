package com.furkan.ecommerce.cart.internal.application;

import com.furkan.ecommerce.common.outbox.ProcessedEvent;
import com.furkan.ecommerce.common.outbox.ProcessedEventRepository;
import com.furkan.ecommerce.order.api.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
class CartOrderEventHandler {
    private static final String CONSUMER = "cart-clear-on-order-created";

    private final CartCommandService cartCommandService;
    private final ProcessedEventRepository processedEventRepository;

    @EventListener
    @Transactional
    public void on(OrderCreatedEvent event) {
        try {
            processedEventRepository.saveAndFlush(ProcessedEvent.of(CONSUMER, event.eventId()));
        } catch (DataIntegrityViolationException duplicate) {
            return;
        }

        cartCommandService.clearCart(event.userId());
    }
}

