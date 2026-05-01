package com.furkan.ecommerce.cart.internal.application;

import com.furkan.ecommerce.cart.api.dto.CartItemRequest;
import com.furkan.ecommerce.cart.api.dto.CartView;
import com.furkan.ecommerce.cart.internal.domain.Cart;
import com.furkan.ecommerce.cart.internal.persistence.CartRepository;
import com.furkan.ecommerce.common.exception.ResourceNotFoundException;
import com.furkan.ecommerce.product.api.ProductReadApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartCommandService {
    private final CartRepository cartRepository;
    private final ProductReadApi productReadApi;

    @Transactional
    public CartView addItem(Long userId, CartItemRequest request) {
        productReadApi.findById(request.productId()).orElseThrow(() -> new ResourceNotFoundException("PRODUCT_NOT_FOUND", "Product not found"));
        Cart cart = cartRepository.findByUserId(userId).orElseGet(() -> Cart.create(userId));
        cart.addOrUpdate(request.productId(), request.quantity());
        cartRepository.save(cart);
        return toView(cart);
    }

    @Transactional
    public void removeItem(Long userId, Long productId) {
        Cart cart = cartRepository.findByUserId(userId).orElseThrow(() -> new ResourceNotFoundException("CART_NOT_FOUND", "Cart not found"));
        cart.remove(productId);
    }

    @Transactional
    void clearCart(Long userId) {
        cartRepository.findByUserId(userId).ifPresent(Cart::clear);
    }

    private CartView toView(Cart cart) {
        return new CartView(cart.getUserId(), cart.getItems().stream().map(i -> new CartView.CartLineView(i.getProductId(), i.getQuantity())).toList());
    }
}
