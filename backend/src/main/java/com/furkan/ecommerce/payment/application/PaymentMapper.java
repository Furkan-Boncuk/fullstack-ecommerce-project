package com.furkan.ecommerce.payment.application;

import com.furkan.ecommerce.order.dto.OrderPaymentView;
import com.furkan.ecommerce.payment.dto.PaymentInitResponse;
import com.furkan.ecommerce.payment.dto.PaymentStatusResponse;
import com.furkan.ecommerce.payment.domain.Payment;
import java.time.Instant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PaymentMapper {
    PaymentInitResponse toInitResponse(PaymentGateway.PaymentResult result);

    @Mapping(target = "orderId", source = "orderId")
    @Mapping(target = "paymentStatus", source = "payment.status")
    @Mapping(target = "transactionId", source = "payment.transactionId")
    @Mapping(target = "errorCode", source = "payment.errorCode")
    PaymentStatusResponse toStatusResponse(
            Long orderId,
            Payment payment,
            String latestAttemptStatus,
            String checkoutUrl,
            Instant expiresAt
    );

    @Mapping(target = "orderId", source = "orderId")
    @Mapping(target = "paymentStatus", constant = "NOT_STARTED")
    @Mapping(target = "latestAttemptStatus", ignore = true)
    @Mapping(target = "transactionId", ignore = true)
    @Mapping(target = "errorCode", ignore = true)
    @Mapping(target = "checkoutUrl", ignore = true)
    @Mapping(target = "expiresAt", source = "order.expiresAt")
    PaymentStatusResponse notStartedStatus(Long orderId, OrderPaymentView order);

    PaymentGateway.CheckoutLine toCheckoutLine(OrderPaymentView.OrderPaymentLineView line);
}
