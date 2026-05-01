package com.furkan.ecommerce.product.internal.domain;

import com.furkan.ecommerce.common.domain.BaseEntity;
import com.furkan.ecommerce.common.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
public class Product extends BaseEntity {
    @Column(nullable = false)
    private String name;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false)
    private Integer reservedStock;

    @Column(nullable = false)
    private boolean active;

    public static Product create(String name, BigDecimal price, Integer stock) {
        Product p = new Product();
        p.name = name;
        p.price = price;
        p.stock = stock;
        p.reservedStock = 0;
        p.active = true;
        return p;
    }

    public void reserveStock(int qty) {
        if (qty <= 0) {
            throw new BusinessException("INVALID_QUANTITY", "Quantity must be greater than zero");
        }
        if (stock < qty) {
            throw new BusinessException("INSUFFICIENT_STOCK", "Insufficient stock");
        }
        stock -= qty;
        reservedStock += qty;
    }

    public void releaseStock(int qty) {
        if (qty <= 0) {
            throw new BusinessException("INVALID_QUANTITY", "Quantity must be greater than zero");
        }
        if (reservedStock < qty) {
            throw new BusinessException("INSUFFICIENT_RESERVED_STOCK", "Insufficient reserved stock");
        }
        reservedStock -= qty;
        stock += qty;
    }

    public void commitReservedStock(int qty) {
        if (qty <= 0) {
            throw new BusinessException("INVALID_QUANTITY", "Quantity must be greater than zero");
        }
        if (reservedStock < qty) {
            throw new BusinessException("INSUFFICIENT_RESERVED_STOCK", "Insufficient reserved stock");
        }
        reservedStock -= qty;
    }

    public void deactivate() {
        this.active = false;
    }
}
