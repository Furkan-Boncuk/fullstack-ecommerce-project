package com.furkan.ecommerce.payment.internal;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface PaymentAttemptRepository extends JpaRepository<PaymentAttempt, Long> {
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
