package com.furkan.ecommerce.cart.persistence;

import com.furkan.ecommerce.cart.domain.Cart;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {
    @EntityGraph(attributePaths = "items")
    Optional<Cart> findByUserId(Long userId);
}
