package com.furkan.ecommerce.product.internal.specification;

import com.furkan.ecommerce.product.api.dto.ProductFilterRequest;
import com.furkan.ecommerce.product.internal.domain.Product;
import org.springframework.data.jpa.domain.Specification;

public final class ProductSpecifications {
    private ProductSpecifications() {}

    public static Specification<Product> from(ProductFilterRequest filter) {
        return Specification.where(search(filter.search()))
                .and(minPrice(filter.minPrice()))
                .and(maxPrice(filter.maxPrice()));
    }

    private static Specification<Product> search(String s) {
        return (root, q, cb) -> s == null ? null : cb.like(cb.lower(root.get("name")), "%" + s.toLowerCase() + "%");
    }

    private static Specification<Product> minPrice(java.math.BigDecimal min) {
        return (root, q, cb) -> min == null ? null : cb.greaterThanOrEqualTo(root.get("price"), min);
    }

    private static Specification<Product> maxPrice(java.math.BigDecimal max) {
        return (root, q, cb) -> max == null ? null : cb.lessThanOrEqualTo(root.get("price"), max);
    }
}
