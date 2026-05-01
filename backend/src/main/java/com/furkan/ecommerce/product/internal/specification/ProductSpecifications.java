package com.furkan.ecommerce.product.internal;

import com.furkan.ecommerce.product.api.dto.ProductFilterRequest;
import org.springframework.data.jpa.domain.Specification;

final class ProductSpecifications {
    private ProductSpecifications() {}

    static Specification<Product> from(ProductFilterRequest filter) {
        return Specification.where(search(filter.search()))
                .and(minPrice(filter.minPrice()))
                .and(maxPrice(filter.maxPrice()))
                .and(categorySlug(filter.categorySlug()))
                .and(inStock(filter.inStock()));
    }

    private static Specification<Product> search(String s) {
        return (root, q, cb) -> {
            if (isBlank(s)) {
                return null;
            }
            String pattern = "%" + s.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }

    private static Specification<Product> minPrice(java.math.BigDecimal min) {
        return (root, q, cb) -> min == null ? null : cb.greaterThanOrEqualTo(root.get("price"), min);
    }

    private static Specification<Product> maxPrice(java.math.BigDecimal max) {
        return (root, q, cb) -> max == null ? null : cb.lessThanOrEqualTo(root.get("price"), max);
    }

    private static Specification<Product> categorySlug(String categorySlug) {
        return (root, q, cb) -> isBlank(categorySlug)
                ? null
                : cb.equal(cb.lower(root.join("category").get("slug")), categorySlug.trim().toLowerCase());
    }

    private static Specification<Product> inStock(Boolean inStock) {
        return (root, q, cb) -> Boolean.TRUE.equals(inStock) ? cb.greaterThan(root.get("stock"), 0) : null;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
