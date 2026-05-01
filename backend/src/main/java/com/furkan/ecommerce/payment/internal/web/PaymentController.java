package com.furkan.ecommerce.payment.internal.web;

import com.furkan.ecommerce.payment.internal.application.PaymentGateway;
import java.math.BigDecimal;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
class PaymentController {
    private final PaymentGateway gateway;

    @PostMapping("/init")
    Map<String, Object> init(@RequestBody Map<String, String> request) {
        Long orderId = Long.parseLong(request.get("orderId"));
        BigDecimal amount = new BigDecimal(request.get("amount"));
        var result = gateway.charge(orderId, amount);
        return Map.of("success", result.success(), "transactionId", result.transactionId(), "errorCode", result.errorCode());
    }
}
