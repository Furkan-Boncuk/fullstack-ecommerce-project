package com.furkan.ecommerce.order.internal.persistence;

import com.furkan.ecommerce.order.internal.domain.Order;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);
}
