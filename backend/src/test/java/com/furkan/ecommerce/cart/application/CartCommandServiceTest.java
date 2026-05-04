package com.furkan.ecommerce.cart.application;

import com.furkan.ecommerce.cart.dto.CartItemRequest;
import com.furkan.ecommerce.cart.domain.Cart;
import com.furkan.ecommerce.cart.persistence.CartRepository;
import com.furkan.ecommerce.common.exception.BusinessException;
import com.furkan.ecommerce.common.exception.ResourceNotFoundException;
import com.furkan.ecommerce.product.ProductReadApi;
import com.furkan.ecommerce.product.dto.ProductCategorySummary;
import com.furkan.ecommerce.product.dto.ProductView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CartCommandServiceTest {
    private FakeCartRepository cartRepository;
    private FakeProductReadApi productReadApi;
    private CartCommandService service;

    @BeforeEach
    void setUp() {
        cartRepository = new FakeCartRepository();
        productReadApi = new FakeProductReadApi();
        CartMapper mapper = Mappers.getMapper(CartMapper.class);
        CartViewAssembler cartViewAssembler = new CartViewAssembler(productReadApi, mapper);
        service = new CartCommandService(cartRepository.proxy(), productReadApi, cartViewAssembler);
    }

    @Test
    void should_return_enriched_cart_after_add_item() {
        productReadApi.add(product(10L, "Kablosuz Kulaklik", BigDecimal.valueOf(100), 5));

        var result = service.addItem(1L, new CartItemRequest(10L, 2));

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().getFirst().name()).isEqualTo("Kablosuz Kulaklik");
        assertThat(result.items().getFirst().unitPrice()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(result.items().getFirst().lineTotal()).isEqualByComparingTo(BigDecimal.valueOf(200));
        assertThat(result.items().getFirst().availableStock()).isEqualTo(5);
        assertThat(result.summary().itemCount()).isEqualTo(2);
        assertThat(result.summary().subtotal()).isEqualByComparingTo(BigDecimal.valueOf(200));
        assertThat(productReadApi.findByIdsCalls).isEqualTo(1);
    }

    @Test
    void should_reject_missing_product() {
        assertThatThrownBy(() -> service.addItem(1L, new CartItemRequest(999L, 1)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    void should_reject_quantity_greater_than_available_stock() {
        productReadApi.add(product(10L, "Kulaklik", BigDecimal.TEN, 1));

        assertThatThrownBy(() -> service.addItem(1L, new CartItemRequest(10L, 2)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    void should_return_updated_summary_after_remove_item() {
        productReadApi.add(product(10L, "Kulaklik", BigDecimal.valueOf(100), 5));
        productReadApi.add(product(20L, "Saat", BigDecimal.valueOf(50), 5));
        service.addItem(1L, new CartItemRequest(10L, 2));
        service.addItem(1L, new CartItemRequest(20L, 1));

        var result = service.removeItem(1L, 10L);

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().getFirst().productId()).isEqualTo(20L);
        assertThat(result.summary().itemCount()).isEqualTo(1);
        assertThat(result.summary().subtotal()).isEqualByComparingTo(BigDecimal.valueOf(50));
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

    private static class FakeCartRepository {
        private final Map<Long, Cart> cartsByUserId = new HashMap<>();
        private long sequence = 1L;

        CartRepository proxy() {
            return (CartRepository) Proxy.newProxyInstance(
                    CartRepository.class.getClassLoader(),
                    new Class<?>[]{CartRepository.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "findByUserId" -> Optional.ofNullable(cartsByUserId.get((Long) args[0]));
                        case "save", "saveAndFlush" -> save((Cart) args[0]);
                        default -> throw new UnsupportedOperationException(method.getName());
                    }
            );
        }

        private Cart save(Cart cart) {
            if (cart.getId() == null) {
                ReflectionTestUtils.setField(cart, "id", sequence++);
            }
            cartsByUserId.put(cart.getUserId(), cart);
            return cart;
        }
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
