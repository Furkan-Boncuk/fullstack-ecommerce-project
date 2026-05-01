package com.furkan.ecommerce.cart.api;

import com.furkan.ecommerce.cart.api.dto.CartView;

public interface CartReadApi {
    CartView getCart(Long userId);
}

