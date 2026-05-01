package com.furkan.ecommerce.auth.internal.persistence;

import com.furkan.ecommerce.auth.internal.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
