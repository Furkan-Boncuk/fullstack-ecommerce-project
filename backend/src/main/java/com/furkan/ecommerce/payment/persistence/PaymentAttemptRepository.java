package com.furkan.ecommerce.payment.persistence;

import com.furkan.ecommerce.payment.domain.Payment;
import com.furkan.ecommerce.payment.domain.PaymentAttempt;
import com.furkan.ecommerce.payment.domain.PaymentAttemptStatus;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentAttemptRepository extends JpaRepository<PaymentAttempt, Long> {
    Optional<PaymentAttempt> findByAttemptReference(String attemptReference);

    Optional<PaymentAttempt> findByCheckoutToken(String checkoutToken);

    Optional<PaymentAttempt> findFirstByPaymentAndStatusInAndExpiresAtAfterOrderByIdDesc(
            Payment payment,
            Collection<PaymentAttemptStatus> statuses,
            Instant now
    );

    Optional<PaymentAttempt> findFirstByPaymentOrderByIdDesc(Payment payment);

    List<PaymentAttempt> findByPaymentAndStatusIn(Payment payment, Collection<PaymentAttemptStatus> statuses);
}
