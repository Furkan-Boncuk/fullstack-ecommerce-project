package com.furkan.ecommerce.cart.application;

import com.furkan.ecommerce.cart.dto.CartItemRequest;
import com.furkan.ecommerce.cart.dto.CartView;
import com.furkan.ecommerce.cart.domain.Cart;
import com.furkan.ecommerce.cart.persistence.CartRepository;
import com.furkan.ecommerce.common.exception.BusinessException;
import com.furkan.ecommerce.common.exception.ResourceNotFoundException;
import com.furkan.ecommerce.product.ProductReadApi;
import com.furkan.ecommerce.product.dto.ProductView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartCommandService {
    private final CartRepository cartRepository;
    private final ProductReadApi productReadApi;
    private final CartMapper cartMapper;

    @Transactional
    public CartView addItem(Long userId, CartItemRequest request) {
        ProductView product = productReadApi.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("PRODUCT_NOT_FOUND", "Product not found"));
        if (product.stock() < request.quantity()) {
            throw new BusinessException("INSUFFICIENT_STOCK", "Insufficient stock");
        }

        Cart cart = cartRepository.findByUserId(userId).orElseGet(() -> Cart.create(userId));
        cart.addOrUpdate(request.productId(), request.quantity());
        Cart savedCart = cartRepository.save(cart);
        return cartMapper.toView(savedCart);
    }

    @Transactional
    public CartView removeItem(Long userId, Long productId) {
        Cart cart = cartRepository.findByUserId(userId).orElseGet(() -> Cart.create(userId));
        cart.remove(productId);
        if (cart.getId() == null) {
            return cartMapper.toView(cart);
        }
        return cartMapper.toView(cartRepository.save(cart));
    }

    @Transactional
    void clearCart(Long userId) {
        cartRepository.findByUserId(userId).ifPresent(Cart::clear);
    }
}
