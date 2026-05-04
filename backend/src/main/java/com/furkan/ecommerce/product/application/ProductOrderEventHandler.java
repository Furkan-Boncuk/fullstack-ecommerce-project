package com.furkan.ecommerce.product.application;

import com.furkan.ecommerce.common.exception.ResourceNotFoundException;
import com.furkan.ecommerce.common.outbox.ProcessedEvent;
import com.furkan.ecommerce.common.outbox.ProcessedEventRepository;
import com.furkan.ecommerce.order.event.OrderCreatedEvent;
import com.furkan.ecommerce.product.domain.Product;
import com.furkan.ecommerce.product.persistence.ProductRepository;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
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

        Map<Long, Product> productsById = loadProductsById(event.items().stream()
                .map(OrderCreatedEvent.OrderItemSnapshot::productId)
                .collect(Collectors.toSet()));

        for (OrderCreatedEvent.OrderItemSnapshot item : event.items()) {
            Product product = productsById.get(item.productId());
            product.reserveStock(item.quantity());
        }
    }

    private Map<Long, Product> loadProductsById(Collection<Long> productIds) {
        Map<Long, Product> productsById = productRepository.findByIdIn(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        productIds.stream()
                .filter(productId -> !productsById.containsKey(productId))
                .findFirst()
                .ifPresent(productId -> {
                    throw new ResourceNotFoundException("PRODUCT_NOT_FOUND", "Product not found: " + productId);
                });

        return productsById;
    }
}
