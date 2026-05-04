package com.furkan.ecommerce.cart.application;

import com.furkan.ecommerce.cart.CartReadApi;
import com.furkan.ecommerce.cart.dto.CartView;
import com.furkan.ecommerce.cart.domain.Cart;
import com.furkan.ecommerce.cart.persistence.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class CartReadService implements CartReadApi {
    private final CartRepository cartRepository;
    private final CartViewAssembler cartViewAssembler;

    @Override
    public CartView getCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId).orElseGet(() -> Cart.create(userId));
        return cartViewAssembler.toView(cart);
    }
}
