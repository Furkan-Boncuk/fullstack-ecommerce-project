package com.furkan.ecommerce.order.internal.application;

import com.furkan.ecommerce.common.exception.ResourceNotFoundException;
import com.furkan.ecommerce.common.outbox.ProcessedEvent;
import com.furkan.ecommerce.common.outbox.ProcessedEventRepository;
import com.furkan.ecommerce.order.internal.persistence.OrderRepository;
import com.furkan.ecommerce.payment.api.event.PaymentFailedEvent;
import com.furkan.ecommerce.payment.api.event.PaymentRequiresReviewEvent;
import com.furkan.ecommerce.payment.api.event.PaymentSucceededEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
class OrderPaymentEventHandler {
    private static final String PAID_CONSUMER = "order-payment-succeeded";
    private static final String FAILED_CONSUMER = "order-payment-failed";
    private static final String REVIEW_CONSUMER = "order-payment-review";

    private final OrderRepository orderRepository;
    private final ProcessedEventRepository processedEventRepository;

    @EventListener
    @Transactional
    public void on(PaymentSucceededEvent event) {
        if (isDuplicate(PAID_CONSUMER, event.eventId())) {
            return;
        }
        var order = orderRepository.findById(event.orderId())
                .orElseThrow(() -> new ResourceNotFoundException("ORDER_NOT_FOUND", "Order not found"));
        order.markPaid();
    }

    @EventListener
    @Transactional
    public void on(PaymentFailedEvent event) {
        if (isDuplicate(FAILED_CONSUMER, event.eventId())) {
            return;
        }
        var order = orderRepository.findById(event.orderId())
                .orElseThrow(() -> new ResourceNotFoundException("ORDER_NOT_FOUND", "Order not found"));
        order.markPaymentFailed();
    }

    @EventListener
    @Transactional
    public void on(PaymentRequiresReviewEvent event) {
        if (isDuplicate(REVIEW_CONSUMER, event.eventId())) {
            return;
        }
        var order = orderRepository.findById(event.orderId())
                .orElseThrow(() -> new ResourceNotFoundException("ORDER_NOT_FOUND", "Order not found"));
        order.markRequiresReview();
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
