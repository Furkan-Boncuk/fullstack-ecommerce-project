package com.furkan.ecommerce.payment.internal;

import com.furkan.ecommerce.common.outbox.ProcessedEvent;
import com.furkan.ecommerce.common.outbox.ProcessedEventRepository;
import com.furkan.ecommerce.order.api.event.OrderExpiredEvent;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
class PaymentOrderEventHandler {
    private static final String EXPIRED_CONSUMER = "payment-order-expired";
    private static final List<PaymentAttemptStatus> ACTIVE_ATTEMPT_STATUSES = List.of(
            PaymentAttemptStatus.INIT_REQUESTED,
            PaymentAttemptStatus.ACTION_REQUIRED
    );

    private final PaymentRepository paymentRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final ProcessedEventRepository processedEventRepository;

    @EventListener
    @Transactional
    public void on(OrderExpiredEvent event) {
        if (isDuplicate(EXPIRED_CONSUMER, event.eventId())) {
            return;
        }
        paymentRepository.findByOrderId(event.orderId()).ifPresent(payment -> {
            paymentAttemptRepository.findByPaymentAndStatusIn(payment, ACTIVE_ATTEMPT_STATUSES)
                    .forEach(attempt -> attempt.markExpired(Instant.now()));
            payment.markFailed(null, "ORDER_EXPIRED");
        });
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
