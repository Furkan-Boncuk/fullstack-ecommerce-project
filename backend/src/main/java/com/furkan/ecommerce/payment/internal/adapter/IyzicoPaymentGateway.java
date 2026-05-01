package com.furkan.ecommerce.payment.internal.adapter;

import com.furkan.ecommerce.payment.internal.application.PaymentGateway;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class IyzicoPaymentGateway implements PaymentGateway {
    @Value("${app.payment.iyzico.enabled:false}")
    private boolean enabled;

    @Override
    public PaymentResult charge(Long orderId, BigDecimal amount) {
        if (!enabled) {
            return PaymentResult.success("MOCK-" + UUID.randomUUID());
        }
        return PaymentResult.success("IYZ-" + UUID.randomUUID());
    }
}
