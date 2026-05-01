package com.furkan.ecommerce.product.internal;

import com.furkan.ecommerce.common.exception.ResourceNotFoundException;
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
class ProductOrderEventHandler {
    private static final String CONSUMER = "product-stock-reservation";

    private final ProductRepository productRepository;
    private final ProcessedEventRepository processedEventRepository;

    @EventListener
    @Transactional
    public void on(OrderCreatedEvent event) {
        try {
            processedEventRepository.saveAndFlush(ProcessedEvent.of(CONSUMER, event.eventId()));
        } catch (DataIntegrityViolationException duplicate) {
            return;
        }

        for (OrderCreatedEvent.OrderItemSnapshot item : event.items()) {
            var product = productRepository.findById(item.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("PRODUCT_NOT_FOUND", "Product not found: " + item.productId()));
            product.reserveStock(item.quantity());
        }
    }
}
