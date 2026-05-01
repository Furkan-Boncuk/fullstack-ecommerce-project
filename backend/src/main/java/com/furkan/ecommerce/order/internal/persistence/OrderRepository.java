package com.furkan.ecommerce.order.internal.persistence;

import com.furkan.ecommerce.order.internal.domain.Order;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @EntityGraph(attributePaths = "items")
    Optional<Order> findWithItemsById(Long id);

    List<Order> findByUserId(Long userId);
}
