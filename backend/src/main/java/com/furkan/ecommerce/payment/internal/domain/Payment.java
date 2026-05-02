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

    @Column(length = 1024)
    private String checkoutUrl;

    @Column(length = 190)
    private String checkoutToken;

    @Column(length = 120)
    private String providerReference;

    @Column(length = 120)
    private String errorCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    public static Payment initiate(Long orderId, BigDecimal amount, String provider) {
        Payment payment = new Payment();
        payment.orderId = orderId;
        payment.amount = amount;
        payment.provider = provider;
        payment.status = PaymentStatus.INITIATED;
        return payment;
    }

    static Payment initiate(
            Long orderId,
            BigDecimal amount,
            String provider,
            String transactionId,
            String checkoutUrl,
            String checkoutToken,
            String providerReference
    ) {
        Payment payment = initiate(orderId, amount, provider);
        payment.transactionId = transactionId;
        payment.checkoutUrl = checkoutUrl;
        payment.checkoutToken = checkoutToken;
        payment.providerReference = providerReference;
        payment.status = PaymentStatus.ACTION_REQUIRED;
        return payment;
    }

    public TransitionResult markActionRequired(String checkoutUrl, String checkoutToken, String providerReference) {
        if (status == PaymentStatus.SUCCEEDED || status == PaymentStatus.REQUIRES_REVIEW) {
            throw new BusinessException("PAYMENT_INVALID_STATE", "Payment cannot be re-initialized in current state");
        }
        this.checkoutUrl = checkoutUrl;
        this.checkoutToken = checkoutToken;
        this.providerReference = providerReference;
        this.errorCode = null;
        this.status = PaymentStatus.ACTION_REQUIRED;
        return TransitionResult.CHANGED;
    }

    public TransitionResult markSucceeded(String transactionId) {
        if (status == PaymentStatus.SUCCEEDED) {
            return TransitionResult.NOOP;
        }
        if (status == PaymentStatus.REQUIRES_REVIEW) {
            throw new BusinessException("PAYMENT_INVALID_STATE", "Payment in review cannot transition automatically");
        }
        this.transactionId = transactionId;
        this.checkoutToken = null;
        this.checkoutUrl = null;
        this.errorCode = null;
        this.status = PaymentStatus.SUCCEEDED;
        return TransitionResult.CHANGED;
    }

    public TransitionResult markFailed(String transactionId, String errorCode) {
        if (status == PaymentStatus.FAILED && same(this.errorCode, errorCode)) {
            return TransitionResult.NOOP;
        }
        if (status == PaymentStatus.SUCCEEDED) {
            throw new BusinessException("PAYMENT_INVALID_STATE", "Successful payment cannot transition to failed");
        }
        if (status == PaymentStatus.REQUIRES_REVIEW) {
            return TransitionResult.NOOP;
        }
        this.transactionId = transactionId;
        this.checkoutToken = null;
        this.checkoutUrl = null;
        this.errorCode = errorCode;
        this.status = PaymentStatus.FAILED;
        return TransitionResult.CHANGED;
    }

    public TransitionResult markRequiresReview(String reason) {
        if (status == PaymentStatus.REQUIRES_REVIEW) {
            return TransitionResult.NOOP;
        }
        if (status == PaymentStatus.SUCCEEDED) {
            throw new BusinessException("PAYMENT_INVALID_STATE", "Successful payment cannot require review automatically");
        }
        this.checkoutToken = null;
        this.checkoutUrl = null;
        this.errorCode = reason;
        this.status = PaymentStatus.REQUIRES_REVIEW;
        return TransitionResult.CHANGED;
    }

    private boolean same(String left, String right) {
        return left == null ? right == null : left.equals(right);
    }

    public enum TransitionResult {
        CHANGED,
        NOOP
    }
}
