package com.furkan.ecommerce.order.internal.domain;

import com.furkan.ecommerce.common.domain.BaseEntity;
import com.furkan.ecommerce.common.exception.BusinessException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.util.List;
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

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    public static Order create(Long userId, List<OrderLineInput> lines) {
        Order order = new Order();
        order.userId = userId;
        order.totalAmount = lines.stream()
                .map(line -> line.unitPrice().multiply(BigDecimal.valueOf(line.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.status = OrderStatus.PENDING;
        lines.forEach(line -> order.items.add(OrderItem.of(order, line.productId(), line.quantity(), line.unitPrice())));
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

    public List<OrderLineSnapshot> inventoryLines() {
        return items.stream()
                .map(item -> new OrderLineSnapshot(item.getProductId(), item.getQuantity()))
                .toList();
    }

    public record OrderLineInput(Long productId, Integer quantity, BigDecimal unitPrice) {}
    public record OrderLineSnapshot(Long productId, Integer quantity) {}
}
