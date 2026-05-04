package com.furkan.ecommerce.cart;

import com.furkan.ecommerce.cart.dto.CartView;

public interface CartReadApi {
    CartView getCart(Long userId);
}

