package com.furkan.ecommerce.product.internal.application;

import com.furkan.ecommerce.common.exception.ResourceNotFoundException;
import com.furkan.ecommerce.common.outbox.ProcessedEvent;
import com.furkan.ecommerce.common.outbox.ProcessedEventRepository;
import com.furkan.ecommerce.order.api.OrderReadApi;
import com.furkan.ecommerce.order.api.event.OrderExpiredEvent;
import com.furkan.ecommerce.payment.api.event.PaymentSucceededEvent;
import com.furkan.ecommerce.product.internal.persistence.ProductRepository;
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
        for (var line : order.lines()) {
            var product = productRepository.findById(line.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("PRODUCT_NOT_FOUND", "Product not found: " + line.productId()));
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
        for (var line : order.lines()) {
            var product = productRepository.findById(line.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("PRODUCT_NOT_FOUND", "Product not found: " + line.productId()));
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
}
