package com.furkan.ecommerce.cart.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.furkan.ecommerce.cart.domain.Cart;
import com.furkan.ecommerce.product.ProductReadApi;
import com.furkan.ecommerce.product.dto.ProductCategorySummary;
import com.furkan.ecommerce.product.dto.ProductView;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class CartViewAssemblerTest {
    private FakeProductReadApi productReadApi;
    private CartViewAssembler assembler;

    @BeforeEach
    void setUp() {
        productReadApi = new FakeProductReadApi();
        assembler = new CartViewAssembler(productReadApi, Mappers.getMapper(CartMapper.class));
    }

    @Test
    void should_load_products_in_one_batch_when_cart_has_multiple_items() {
        productReadApi.add(product(10L, "Kulaklik", BigDecimal.valueOf(100), 5));
        productReadApi.add(product(20L, "Saat", BigDecimal.valueOf(50), 3));
        Cart cart = Cart.create(1L);
        cart.addOrUpdate(10L, 2);
        cart.addOrUpdate(20L, 1);

        var result = assembler.toView(cart);

        assertThat(productReadApi.findByIdsCalls).isEqualTo(1);
        assertThat(result.items()).extracting("productId").containsExactlyInAnyOrder(10L, 20L);
        assertThat(result.summary().itemCount()).isEqualTo(3);
        assertThat(result.summary().subtotal()).isEqualByComparingTo(BigDecimal.valueOf(250));
    }

    @Test
    void should_skip_cart_line_when_product_snapshot_is_missing() {
        productReadApi.add(product(10L, "Kulaklik", BigDecimal.valueOf(100), 5));
        Cart cart = Cart.create(1L);
        cart.addOrUpdate(10L, 2);
        cart.addOrUpdate(999L, 1);

        var result = assembler.toView(cart);

        assertThat(result.items()).extracting("productId").containsExactly(10L);
        assertThat(result.summary().itemCount()).isEqualTo(2);
        assertThat(result.summary().subtotal()).isEqualByComparingTo(BigDecimal.valueOf(200));
    }

    private ProductView product(Long id, String name, BigDecimal price, int stock) {
        return new ProductView(
                id,
                name,
                name + " description",
                new ProductCategorySummary(1L, "Elektronik", "elektronik"),
                "https://picsum.photos/id/180/900/700",
                price,
                stock
        );
    }

    private static class FakeProductReadApi implements ProductReadApi {
        private final Map<Long, ProductView> products = new HashMap<>();
        private int findByIdsCalls;

        void add(ProductView product) {
            products.put(product.id(), product);
        }

        @Override
        public Optional<ProductView> findById(Long id) {
            return Optional.ofNullable(products.get(id));
        }

        @Override
        public List<ProductView> findByIds(Collection<Long> ids) {
            findByIdsCalls++;
            return ids.stream()
                    .map(products::get)
                    .filter(java.util.Objects::nonNull)
                    .toList();
        }
    }
}
