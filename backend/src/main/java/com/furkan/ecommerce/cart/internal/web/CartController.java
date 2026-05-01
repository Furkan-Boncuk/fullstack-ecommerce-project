package com.furkan.ecommerce.cart.internal.web;

import com.furkan.ecommerce.cart.api.CartReadApi;
import com.furkan.ecommerce.cart.api.dto.CartItemRequest;
import com.furkan.ecommerce.cart.api.dto.CartView;
import com.furkan.ecommerce.cart.internal.application.CartCommandService;
import com.furkan.ecommerce.infrastructure.security.SecurityPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartCommandService commandService;
    private final CartReadApi cartReadApi;

    @GetMapping
    CartView getCart(@AuthenticationPrincipal SecurityPrincipal principal) {
        return cartReadApi.getCart(principal.userId());
    }

    @PostMapping("/items")
    CartView addItem(@AuthenticationPrincipal SecurityPrincipal principal, @Valid @RequestBody CartItemRequest request) {
        return commandService.addItem(principal.userId(), request);
    }

    @DeleteMapping("/items/{productId}")
    CartView removeItem(@AuthenticationPrincipal SecurityPrincipal principal, @PathVariable Long productId) {
        return commandService.removeItem(principal.userId(), productId);
    }
}
