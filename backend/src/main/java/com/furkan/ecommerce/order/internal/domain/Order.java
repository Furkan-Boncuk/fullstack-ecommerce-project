package com.furkan.ecommerce.order.internal.domain;

import com.furkan.ecommerce.common.domain.BaseEntity;
import com.furkan.ecommerce.common.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {
    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    public static Order create(Long userId, BigDecimal totalAmount) {
        Order order = new Order();
        order.userId = userId;
        order.totalAmount = totalAmount;
        order.status = OrderStatus.PENDING;
        return order;
    }

    public void markPaid() {
        if (status != OrderStatus.PENDING) {
            throw new BusinessException("ORDER_INVALID_STATE", "Order cannot be paid in current state");
        }
        status = OrderStatus.PAID;
    }

    public void markFailed() {
        if (status == OrderStatus.PAID) {
            throw new BusinessException("ORDER_INVALID_STATE", "Paid order cannot fail");
        }
        status = OrderStatus.FAILED;
    }
}
