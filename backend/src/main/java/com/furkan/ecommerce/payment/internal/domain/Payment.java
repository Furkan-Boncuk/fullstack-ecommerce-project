package com.furkan.ecommerce.payment.internal;

import com.furkan.ecommerce.common.domain.BaseEntity;
import com.furkan.ecommerce.common.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "payments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class Payment extends BaseEntity {
    @Column(nullable = false, unique = true)
    private Long orderId;

    @Column(nullable = false, length = 50)
    private String provider;

    @Column(length = 120)
    private String transactionId;

    @Column(length = 255)
    private String checkoutUrl;

    @Column(length = 150)
    private String checkoutToken;

    @Column(length = 120)
    private String providerReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    public static Payment initiate(
            Long orderId,
            BigDecimal amount,
            String provider,
            String transactionId,
            String checkoutUrl,
            String checkoutToken,
            String providerReference
    ) {
        Payment payment = new Payment();
        payment.orderId = orderId;
        payment.amount = amount;
        payment.provider = provider;
        payment.transactionId = transactionId;
        payment.checkoutUrl = checkoutUrl;
        payment.checkoutToken = checkoutToken;
        payment.providerReference = providerReference;
        payment.status = PaymentStatus.INITIATED;
        return payment;
    }

    public TransitionResult markSucceeded(String transactionId) {
        if (status == PaymentStatus.SUCCEEDED) {
            return TransitionResult.NOOP;
        }
        if (status == PaymentStatus.FAILED) {
            throw new BusinessException("PAYMENT_INVALID_STATE", "Failed payment cannot transition to succeeded");
        }
        this.transactionId = transactionId;
        this.checkoutToken = null;
        this.checkoutUrl = null;
        this.status = PaymentStatus.SUCCEEDED;
        return TransitionResult.CHANGED;
    }

    public TransitionResult markFailed(String transactionId) {
        if (status == PaymentStatus.FAILED) {
            return TransitionResult.NOOP;
        }
        if (status == PaymentStatus.SUCCEEDED) {
            throw new BusinessException("PAYMENT_INVALID_STATE", "Successful payment cannot transition to failed");
        }
        this.transactionId = transactionId;
        this.checkoutToken = null;
        this.checkoutUrl = null;
        this.status = PaymentStatus.FAILED;
        return TransitionResult.CHANGED;
    }

    public enum TransitionResult {
        CHANGED,
        NOOP
    }
}
