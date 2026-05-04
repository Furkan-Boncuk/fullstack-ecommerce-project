package com.furkan.ecommerce.cart.application;

import com.furkan.ecommerce.cart.domain.Cart;
import com.furkan.ecommerce.cart.dto.CartView;
import com.furkan.ecommerce.product.ProductReadApi;
import com.furkan.ecommerce.product.dto.ProductView;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class CartViewAssembler {
    private final ProductReadApi productReadApi;
    private final CartMapper cartMapper;

    CartView toView(Cart cart) {
        Set<Long> productIds = cart.getItems().stream()
                .map(item -> item.getProductId())
                .collect(Collectors.toSet());

        Map<Long, ProductView> productsById = productIds.isEmpty()
                ? Map.of()
                : productReadApi.findByIds(productIds).stream()
                        .collect(Collectors.toMap(ProductView::id, Function.identity()));

        return cartMapper.toViewWithAvailableProducts(cart, productsById);
    }
}
