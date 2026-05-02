package com.furkan.ecommerce.order.internal.domain;

import com.furkan.ecommerce.common.domain.BaseEntity;
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

@Getter
@Entity
@Table(name = "order_items")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class OrderItem extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false, length = 255)
    private String productName;

    @Column(length = 500)
    private String productImageUrl;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    static OrderItem of(Order order, Long productId, String productName, String productImageUrl, Integer quantity, BigDecimal unitPrice) {
        OrderItem item = new OrderItem();
        item.order = order;
        item.productId = productId;
        item.productName = productName;
        item.productImageUrl = productImageUrl;
        item.quantity = quantity;
        item.unitPrice = unitPrice;
        return item;
    }

    BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
