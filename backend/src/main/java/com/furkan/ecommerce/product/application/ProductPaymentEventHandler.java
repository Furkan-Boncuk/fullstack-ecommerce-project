package com.furkan.ecommerce.product.application;

import com.furkan.ecommerce.common.exception.ResourceNotFoundException;
import com.furkan.ecommerce.common.outbox.ProcessedEvent;
import com.furkan.ecommerce.common.outbox.ProcessedEventRepository;
import com.furkan.ecommerce.order.OrderReadApi;
import com.furkan.ecommerce.order.event.OrderExpiredEvent;
import com.furkan.ecommerce.payment.event.PaymentSucceededEvent;
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
class ProductPaymentEventHandler {
    private static final String SUCCESS_CONSUMER = "product-stock-commit";
    private static final String EXPIRED_CONSUMER = "product-stock-release-on-order-expired";

    private final OrderReadApi orderReadApi;
    private final ProductRepository productRepository;
    private final ProcessedEventRepository processedEventRepository;

    @EventListener
    @Transactional
    public void on(PaymentSucceededEvent event) {
        if (isDuplicate(SUCCESS_CONSUMER, event.eventId())) {
            return;
        }
        var order = orderReadApi.findInventoryViewById(event.orderId())
                .orElseThrow(() -> new ResourceNotFoundException("ORDER_NOT_FOUND", "Order not found"));
        Map<Long, Product> productsById = loadProductsById(order.lines().stream()
                .map(line -> line.productId())
                .collect(Collectors.toSet()));

        for (var line : order.lines()) {
            Product product = productsById.get(line.productId());
            product.commitReservedStock(line.quantity());
        }
    }

    @EventListener
    @Transactional
    public void on(OrderExpiredEvent event) {
        if (isDuplicate(EXPIRED_CONSUMER, event.eventId())) {
            return;
        }
        var order = orderReadApi.findInventoryViewById(event.orderId())
                .orElseThrow(() -> new ResourceNotFoundException("ORDER_NOT_FOUND", "Order not found"));
        Map<Long, Product> productsById = loadProductsById(order.lines().stream()
                .map(line -> line.productId())
                .collect(Collectors.toSet()));

        for (var line : order.lines()) {
            Product product = productsById.get(line.productId());
            product.releaseStock(line.quantity());
        }
    }

    private boolean isDuplicate(String consumer, java.util.UUID eventId) {
        try {
            processedEventRepository.saveAndFlush(ProcessedEvent.of(consumer, eventId));
            return false;
        } catch (DataIntegrityViolationException duplicate) {
            return true;
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
