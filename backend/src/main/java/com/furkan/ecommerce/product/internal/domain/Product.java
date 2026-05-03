package com.furkan.ecommerce.product.internal;

import com.furkan.ecommerce.common.domain.BaseEntity;
import com.furkan.ecommerce.common.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity
@Table(name = "products")
@SQLRestriction("active = true")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class Product extends BaseEntity {
    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(length = 500)
    private String imageUrl;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false)
    private Integer reservedStock;

    @Column(nullable = false)
    private boolean active;

    static Product create(String name, String description, BigDecimal price, Integer stock, String imageUrl, Category category) {
        Product p = new Product();
        p.updateDetails(name, description, price, stock, imageUrl, category);
        p.reservedStock = 0;
        p.active = true;
        return p;
    }

    void updateDetails(String name, String description, BigDecimal price, Integer stock, String imageUrl, Category category) {
        if (name == null || name.isBlank()) {
            throw new BusinessException("PRODUCT_NAME_REQUIRED", "Product name is required");
        }
        if (price == null || price.signum() <= 0) {
            throw new BusinessException("INVALID_PRICE", "Product price must be greater than zero");
        }
        if (stock == null || stock < 0) {
            throw new BusinessException("INVALID_STOCK", "Product stock cannot be negative");
        }
        if (category == null) {
            throw new BusinessException("CATEGORY_NOT_FOUND", "Category not found");
        }
        this.name = name.trim();
        this.description = normalize(description);
        this.price = price;
        this.stock = stock;
        this.imageUrl = normalize(imageUrl);
        this.category = category;
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    void reserveStock(int qty) {
        if (qty <= 0) {
            throw new BusinessException("INVALID_QUANTITY", "Quantity must be greater than zero");
        }
        if (stock < qty) {
            throw new BusinessException("INSUFFICIENT_STOCK", "Insufficient stock");
        }
        stock -= qty;
        reservedStock += qty;
    }

    void releaseStock(int qty) {
        if (qty <= 0) {
            throw new BusinessException("INVALID_QUANTITY", "Quantity must be greater than zero");
        }
        if (reservedStock < qty) {
            throw new BusinessException("INSUFFICIENT_RESERVED_STOCK", "Insufficient reserved stock");
        }
        reservedStock -= qty;
        stock += qty;
    }

    void commitReservedStock(int qty) {
        if (qty <= 0) {
            throw new BusinessException("INVALID_QUANTITY", "Quantity must be greater than zero");
        }
        if (reservedStock < qty) {
            throw new BusinessException("INSUFFICIENT_RESERVED_STOCK", "Insufficient reserved stock");
        }
        reservedStock -= qty;
    }

    void deactivate() {
        this.active = false;
    }
}
