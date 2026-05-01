package com.furkan.ecommerce.cart.internal.application;

import com.furkan.ecommerce.cart.api.CartReadApi;
import com.furkan.ecommerce.cart.api.dto.CartView;
import com.furkan.ecommerce.cart.internal.domain.Cart;
import com.furkan.ecommerce.cart.internal.persistence.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class CartReadService implements CartReadApi {
    private final CartRepository cartRepository;

    @Override
    public CartView getCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId).orElseGet(() -> Cart.create(userId));
        return toView(cart);
    }

    private CartView toView(Cart cart) {
        return new CartView(cart.getUserId(), cart.getItems().stream().map(i -> new CartView.CartLineView(i.getProductId(), i.getQuantity())).toList());
    }
}

