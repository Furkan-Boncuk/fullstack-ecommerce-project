package com.furkan.ecommerce.order.internal.domain;

import com.furkan.ecommerce.common.domain.BaseEntity;
import com.furkan.ecommerce.common.exception.BusinessException;
import com.furkan.ecommerce.infrastructure.crypto.PiiStringAttributeConverter;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
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

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(length = 512)
    @Convert(converter = PiiStringAttributeConverter.class)
    private String shippingFirstName;

    @Column(length = 512)
    @Convert(converter = PiiStringAttributeConverter.class)
    private String shippingLastName;

    @Column(length = 512)
    @Convert(converter = PiiStringAttributeConverter.class)
    private String shippingPhoneNumber;

    @Column(length = 512)
    @Convert(converter = PiiStringAttributeConverter.class)
    private String shippingAddress;

    @Column(length = 512)
    @Convert(converter = PiiStringAttributeConverter.class)
    private String shippingCity;

    @Column(length = 512)
    @Convert(converter = PiiStringAttributeConverter.class)
    private String shippingCountry;

    @Column(length = 512)
    @Convert(converter = PiiStringAttributeConverter.class)
    private String shippingZipCode;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    public static Order create(Long userId, List<OrderLineInput> lines, Instant expiresAt) {
        return create(userId, lines, null, expiresAt);
    }

    public static Order create(Long userId, List<OrderLineInput> lines, ShippingSnapshot shippingSnapshot, Instant expiresAt) {
        if (lines == null || lines.isEmpty()) {
            throw new BusinessException("ORDER_LINES_EMPTY", "Order must contain at least one item");
        }
        Order order = new Order();
        order.userId = userId;
        order.applyShippingSnapshot(shippingSnapshot);
        order.expiresAt = expiresAt;
        order.totalAmount = lines.stream()
                .map(OrderLineInput::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.status = OrderStatus.PENDING;
        lines.forEach(line -> order.items.add(OrderItem.of(
                order,
                line.productId(),
                line.productName(),
                line.productImageUrl(),
                line.quantity(),
                line.unitPrice()
        )));
        return order;
    }

    private void applyShippingSnapshot(ShippingSnapshot snapshot) {
        if (snapshot == null) {
            return;
        }
        this.shippingFirstName = snapshot.firstName();
        this.shippingLastName = snapshot.lastName();
        this.shippingPhoneNumber = snapshot.phoneNumber();
        this.shippingAddress = snapshot.address();
        this.shippingCity = snapshot.city();
        this.shippingCountry = snapshot.country();
        this.shippingZipCode = snapshot.zipCode();
    }

    public TransitionResult markPaid() {
        if (status == OrderStatus.PAID) {
            return TransitionResult.NOOP;
        }
        if (status != OrderStatus.PENDING && status != OrderStatus.PAYMENT_FAILED) {
            throw new BusinessException("ORDER_INVALID_STATE", "Order cannot be paid in current state");
        }
        status = OrderStatus.PAID;
        return TransitionResult.CHANGED;
    }

    public TransitionResult markPaymentFailed() {
        if (status == OrderStatus.PAYMENT_FAILED) {
            return TransitionResult.NOOP;
        }
        if (status != OrderStatus.PENDING) {
            throw new BusinessException("ORDER_INVALID_STATE", "Order cannot be marked payment failed in current state");
        }
        status = OrderStatus.PAYMENT_FAILED;
        return TransitionResult.CHANGED;
    }

    public TransitionResult markRequiresReview() {
        if (status == OrderStatus.REQUIRES_REVIEW) {
            return TransitionResult.NOOP;
        }
        if (status == OrderStatus.PAID || status == OrderStatus.CANCELLED) {
            throw new BusinessException("ORDER_INVALID_STATE", "Order cannot require review in current state");
        }
        status = OrderStatus.REQUIRES_REVIEW;
        return TransitionResult.CHANGED;
    }

    public TransitionResult expire(Instant now) {
        if (status == OrderStatus.EXPIRED) {
            return TransitionResult.NOOP;
        }
        if (status != OrderStatus.PENDING && status != OrderStatus.PAYMENT_FAILED) {
            return TransitionResult.NOOP;
        }
        if (expiresAt.isAfter(now)) {
            return TransitionResult.NOOP;
        }
        status = OrderStatus.EXPIRED;
        return TransitionResult.CHANGED;
    }

    public List<OrderLineSnapshot> inventoryLines() {
        return items.stream()
                .map(item -> new OrderLineSnapshot(item.getProductId(), item.getQuantity()))
                .toList();
    }

    public List<OrderLineView> lineViews() {
        return items.stream()
                .map(item -> new OrderLineView(
                        item.getProductId(),
                        item.getProductName(),
                        item.getProductImageUrl(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.lineTotal()
                ))
                .toList();
    }

    public record OrderLineInput(
            Long productId,
            String productName,
            String productImageUrl,
            Integer quantity,
            BigDecimal unitPrice
    ) {
        public OrderLineInput {
            if (quantity == null || quantity <= 0) {
                throw new BusinessException("INVALID_QUANTITY", "Quantity must be greater than zero");
            }
            if (unitPrice == null || unitPrice.signum() < 0) {
                throw new BusinessException("INVALID_PRICE", "Unit price cannot be negative");
            }
        }

        public BigDecimal lineTotal() {
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    public record OrderLineSnapshot(Long productId, Integer quantity) {}

    public record ShippingSnapshot(
            String firstName,
            String lastName,
            String phoneNumber,
            String address,
            String city,
            String country,
            String zipCode
    ) {}

    public record OrderLineView(
            Long productId,
            String productName,
            String productImageUrl,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal lineTotal
    ) {}

    public enum TransitionResult {
        CHANGED,
        NOOP
    }
}
