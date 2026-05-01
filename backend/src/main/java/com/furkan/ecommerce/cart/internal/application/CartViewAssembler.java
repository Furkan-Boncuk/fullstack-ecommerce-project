package com.furkan.ecommerce.cart.internal.application;

import com.furkan.ecommerce.cart.api.dto.CartView;
import com.furkan.ecommerce.cart.internal.domain.Cart;
import com.furkan.ecommerce.product.api.ProductReadApi;
import com.furkan.ecommerce.product.api.dto.ProductView;
import java.math.BigDecimal;
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

    CartView toView(Cart cart) {
        Set<Long> productIds = cart.getItems().stream()
                .map(item -> item.getProductId())
                .collect(Collectors.toSet());

        Map<Long, ProductView> products = productIds.isEmpty()
                ? Map.of()
                : productReadApi.findByIds(productIds).stream()
                        .collect(Collectors.toMap(ProductView::id, Function.identity()));

        var lines = cart.getItems().stream()
                .map(item -> {
                    ProductView product = products.get(item.getProductId());
                    if (product == null) {
                        return null;
                    }
                    BigDecimal lineTotal = product.price().multiply(BigDecimal.valueOf(item.getQuantity()));
                    return new CartView.CartLineView(
                            item.getProductId(),
                            product.name(),
                            product.imageUrl(),
                            product.price(),
                            item.getQuantity(),
                            lineTotal,
                            product.stock()
                    );
                })
                .filter(java.util.Objects::nonNull)
                .toList();

        int itemCount = lines.stream()
                .mapToInt(CartView.CartLineView::quantity)
                .sum();
        BigDecimal subtotal = lines.stream()
                .map(CartView.CartLineView::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartView(cart.getUserId(), lines, new CartView.CartSummaryView(itemCount, subtotal));
    }
}
