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

@Getter
@Entity
@Table(name = "products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {
    @Column(nullable = false)
    private String name;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;
    @Column(nullable = false)
    private Integer stock;

    public static Product create(String name, BigDecimal price, Integer stock) {
        Product p = new Product();
        p.name = name;
        p.price = price;
        p.stock = stock;
        return p;
    }

    public void decreaseStock(int qty) {
        if (stock < qty) {
            throw new BusinessException("INSUFFICIENT_STOCK", "Insufficient stock");
        }
        stock -= qty;
    }
}
