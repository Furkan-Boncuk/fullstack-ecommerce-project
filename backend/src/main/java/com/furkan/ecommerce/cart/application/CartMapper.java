package com.furkan.ecommerce.cart.application;

import com.furkan.ecommerce.cart.dto.CartView;
import com.furkan.ecommerce.cart.domain.Cart;
import com.furkan.ecommerce.product.dto.ProductView;
import java.math.BigDecimal;
import java.util.Map;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
abstract class CartMapper {
    CartView toViewWithAvailableProducts(Cart cart, Map<Long, ProductView> productsById) {
        var lines = cart.getItems().stream()
                .map(item -> toLineView(item.getProductId(), item.getQuantity(), productsById.get(item.getProductId())))
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

    private CartView.CartLineView toLineView(Long productId, Integer quantity, ProductView product) {
        if (product == null) {
            return null;
        }
        BigDecimal lineTotal = product.price().multiply(BigDecimal.valueOf(quantity));
        return toLineView(productId, quantity, lineTotal, product);
    }

    @Mapping(target = "name", source = "product.name")
    @Mapping(target = "imageUrl", source = "product.imageUrl")
    @Mapping(target = "unitPrice", source = "product.price")
    @Mapping(target = "availableStock", source = "product.stock")
    abstract CartView.CartLineView toLineView(Long productId, Integer quantity, BigDecimal lineTotal, ProductView product);
}
