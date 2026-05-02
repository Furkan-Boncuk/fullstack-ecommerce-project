package com.furkan.ecommerce.order.internal.persistence;

import com.furkan.ecommerce.order.internal.domain.Order;
import com.furkan.ecommerce.order.internal.domain.OrderStatus;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @EntityGraph(attributePaths = "items")
    Optional<Order> findWithItemsById(Long id);

    @EntityGraph(attributePaths = "items")
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    @EntityGraph(attributePaths = "items")
    List<Order> findByStatusInAndExpiresAtBefore(Collection<OrderStatus> statuses, Instant now);
}
