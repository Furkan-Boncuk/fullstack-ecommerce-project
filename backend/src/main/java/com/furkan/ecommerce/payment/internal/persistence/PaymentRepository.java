package com.furkan.ecommerce.payment.internal.persistence;

import com.furkan.ecommerce.payment.internal.domain.Payment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(Long orderId);
}
