package com.furkan.ecommerce.cart.domain;

import com.furkan.ecommerce.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CartDomainTest {
    @Test
    void should_add_new_item() {
        Cart cart = Cart.create(1L);

        cart.addOrUpdate(10L, 2);

        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().getFirst().getProductId()).isEqualTo(10L);
        assertThat(cart.getItems().getFirst().getQuantity()).isEqualTo(2);
    }

    @Test
    void should_update_existing_item_quantity() {
        Cart cart = Cart.create(1L);

        cart.addOrUpdate(10L, 2);
        cart.addOrUpdate(10L, 4);

        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().getFirst().getQuantity()).isEqualTo(4);
    }

    @Test
    void should_reject_invalid_quantity() {
        Cart cart = Cart.create(1L);

        assertThatThrownBy(() -> cart.addOrUpdate(10L, 0))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Quantity must be greater than zero");
    }

    @Test
    void should_remove_and_clear_items() {
        Cart cart = Cart.create(1L);
        cart.addOrUpdate(10L, 2);
        cart.addOrUpdate(20L, 1);

        cart.remove(10L);
        assertThat(cart.getItems()).extracting("productId").containsExactly(20L);

        cart.clear();
        assertThat(cart.getItems()).isEmpty();
    }
}
