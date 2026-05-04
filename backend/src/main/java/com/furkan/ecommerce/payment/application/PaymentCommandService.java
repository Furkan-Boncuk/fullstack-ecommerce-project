package com.furkan.ecommerce.payment.application;

import com.furkan.ecommerce.payment.dto.PaymentStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentCommandService {
    private final PaymentInitService initService;
    private final PaymentCallbackService callbackService;
    private final PaymentStatusService statusService;

    public PaymentGateway.PaymentResult init(Long userId, Long orderId, String customerIp) {
        return initService.init(userId, orderId, customerIp);
    }

    public PaymentCallbackResult handleCallback(String token) {
        return callbackService.handleCallback(token);
    }

    public PaymentStatusResponse getStatus(Long userId, Long orderId) {
        return statusService.getStatus(userId, orderId);
    }

    public record PaymentCallbackResult(Long orderId, String status) {}
}
