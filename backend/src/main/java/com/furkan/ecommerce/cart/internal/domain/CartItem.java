package com.furkan.ecommerce.cart.internal.domain;

import com.furkan.ecommerce.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "cart_items")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItem extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Integer quantity;

    public static CartItem create(Cart cart, Long productId, Integer quantity) {
        CartItem item = new CartItem();
        item.cart = cart;
        item.productId = productId;
        item.quantity = quantity;
        return item;
    }

    public void changeQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
