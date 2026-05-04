package com.furkan.ecommerce.payment.application;

import com.furkan.ecommerce.common.exception.ResourceNotFoundException;
import com.furkan.ecommerce.order.OrderReadApi;
import com.furkan.ecommerce.order.dto.OrderPaymentView;
import com.furkan.ecommerce.payment.domain.Payment;
import com.furkan.ecommerce.payment.domain.PaymentAttempt;
import com.furkan.ecommerce.payment.dto.PaymentStatusResponse;
import com.furkan.ecommerce.payment.persistence.PaymentAttemptRepository;
import com.furkan.ecommerce.payment.persistence.PaymentRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
class PaymentStatusService {
    private final OrderReadApi orderReadApi;
    private final PaymentRepository paymentRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final TransactionTemplate transactionTemplate;
    private final PaymentMapper paymentMapper;

    PaymentStatusResponse getStatus(Long userId, Long orderId) {
        OrderPaymentView order = orderReadApi.findPaymentViewById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("ORDER_NOT_FOUND", "Order not found"));
        if (!order.userId().equals(userId)) {
            throw new AccessDeniedException("Order does not belong to authenticated user");
        }
        return transactionTemplate.execute(status -> paymentRepository.findByOrderId(orderId)
                .map(payment -> {
                    PaymentAttempt attempt = latestAttempt(payment).orElse(null);
                    return paymentMapper.toStatusResponse(
                            orderId,
                            payment,
                            attemptStatus(attempt),
                            checkoutUrl(payment, attempt),
                            attempt == null ? null : attempt.getExpiresAt()
                    );
                })
                .orElse(paymentMapper.notStartedStatus(orderId, order)));
    }

    private Optional<PaymentAttempt> latestAttempt(Payment payment) {
        return paymentAttemptRepository.findFirstByPaymentOrderByIdDesc(payment);
    }

    private String attemptStatus(PaymentAttempt attempt) {
        return attempt == null ? null : attempt.getStatus().name();
    }

    private String checkoutUrl(Payment payment, PaymentAttempt attempt) {
        return attempt == null ? payment.getCheckoutUrl() : attempt.getCheckoutUrl();
    }
}
