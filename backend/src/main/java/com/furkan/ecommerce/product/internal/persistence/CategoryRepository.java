package com.furkan.ecommerce.product.internal.persistence;

import com.furkan.ecommerce.product.internal.domain.Category;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    @EntityGraph(attributePaths = "parent")
    List<Category> findAllByOrderBySortOrderAscNameAsc();

    @EntityGraph(attributePaths = "parent")
    Optional<Category> findBySlug(String slug);
}
