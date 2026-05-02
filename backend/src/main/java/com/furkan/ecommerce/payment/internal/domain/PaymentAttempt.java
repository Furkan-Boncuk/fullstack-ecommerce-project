package com.furkan.ecommerce.payment.internal;

import com.furkan.ecommerce.common.domain.BaseEntity;
import com.furkan.ecommerce.common.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "payment_attempts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class PaymentAttempt extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false, unique = true, length = 120)
    private String attemptReference;

    @Column(unique = true, length = 190)
    private String checkoutToken;

    @Column(length = 1024)
    private String checkoutUrl;

    @Column(length = 120)
    private String transactionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentAttemptStatus status;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(length = 120)
    private String errorCode;

    @Column(nullable = false)
    private Instant expiresAt;

    static PaymentAttempt request(Payment payment, Long orderId, String attemptReference, BigDecimal amount, Instant expiresAt) {
        PaymentAttempt attempt = new PaymentAttempt();
        attempt.payment = payment;
        attempt.orderId = orderId;
        attempt.attemptReference = attemptReference;
        attempt.amount = amount;
        attempt.expiresAt = expiresAt;
        attempt.status = PaymentAttemptStatus.INIT_REQUESTED;
        return attempt;
    }

    boolean isActive(Instant now) {
        return (status == PaymentAttemptStatus.INIT_REQUESTED || status == PaymentAttemptStatus.ACTION_REQUIRED)
                && expiresAt.isAfter(now);
    }

    void markActionRequired(String checkoutUrl, String checkoutToken) {
        ensureNotFinalized();
        this.checkoutUrl = checkoutUrl;
        this.checkoutToken = checkoutToken;
        this.errorCode = null;
        this.status = PaymentAttemptStatus.ACTION_REQUIRED;
    }

    TransitionResult markSucceeded(String transactionId) {
        if (status == PaymentAttemptStatus.SUCCEEDED) {
            return TransitionResult.NOOP;
        }
        if (status == PaymentAttemptStatus.FAILED || status == PaymentAttemptStatus.EXPIRED) {
            throw new BusinessException("PAYMENT_ATTEMPT_INVALID_STATE", "Payment attempt cannot succeed in current state");
        }
        this.transactionId = transactionId;
        this.checkoutUrl = null;
        this.checkoutToken = null;
        this.errorCode = null;
        this.status = PaymentAttemptStatus.SUCCEEDED;
        return TransitionResult.CHANGED;
    }

    TransitionResult markFailed(String transactionId, String errorCode) {
        if (status == PaymentAttemptStatus.FAILED && same(this.errorCode, errorCode)) {
            return TransitionResult.NOOP;
        }
        if (status == PaymentAttemptStatus.SUCCEEDED) {
            throw new BusinessException("PAYMENT_ATTEMPT_INVALID_STATE", "Successful payment attempt cannot fail");
        }
        this.transactionId = transactionId;
        this.checkoutUrl = null;
        this.checkoutToken = null;
        this.errorCode = errorCode;
        this.status = PaymentAttemptStatus.FAILED;
        return TransitionResult.CHANGED;
    }

    TransitionResult markExpired(Instant now) {
        if (status == PaymentAttemptStatus.EXPIRED) {
            return TransitionResult.NOOP;
        }
        if (!isActive(now)) {
            return TransitionResult.NOOP;
        }
        this.checkoutUrl = null;
        this.checkoutToken = null;
        this.errorCode = "PAYMENT_ATTEMPT_EXPIRED";
        this.status = PaymentAttemptStatus.EXPIRED;
        return TransitionResult.CHANGED;
    }

    private void ensureNotFinalized() {
        if (status == PaymentAttemptStatus.SUCCEEDED || status == PaymentAttemptStatus.FAILED || status == PaymentAttemptStatus.EXPIRED) {
            throw new BusinessException("PAYMENT_ATTEMPT_INVALID_STATE", "Payment attempt is finalized");
        }
    }

    private boolean same(String left, String right) {
        return left == null ? right == null : left.equals(right);
    }

    enum TransitionResult {
        CHANGED,
        NOOP
    }
}
