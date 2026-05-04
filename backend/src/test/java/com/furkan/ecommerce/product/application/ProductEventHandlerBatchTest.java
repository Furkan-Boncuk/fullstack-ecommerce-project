package com.furkan.ecommerce.product.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.furkan.ecommerce.common.outbox.ProcessedEventRepository;
import com.furkan.ecommerce.order.OrderReadApi;
import com.furkan.ecommerce.order.dto.OrderInventoryView;
import com.furkan.ecommerce.order.dto.OrderPaymentView;
import com.furkan.ecommerce.order.event.OrderCreatedEvent;
import com.furkan.ecommerce.order.event.OrderExpiredEvent;
import com.furkan.ecommerce.payment.event.PaymentSucceededEvent;
import com.furkan.ecommerce.product.domain.Category;
import com.furkan.ecommerce.product.domain.Product;
import com.furkan.ecommerce.product.persistence.ProductRepository;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ProductEventHandlerBatchTest {
    private FakeProductRepository productRepository;
    private FakeProcessedEventRepository processedEventRepository;
    private FakeOrderReadApi orderReadApi;

    @BeforeEach
    void setUp() {
        productRepository = new FakeProductRepository();
        processedEventRepository = new FakeProcessedEventRepository();
        orderReadApi = new FakeOrderReadApi();
    }

    @Test
    void should_reserve_stock_with_one_batch_load_when_order_created_has_multiple_lines() {
        Product first = productRepository.add(product(10L, 10));
        Product second = productRepository.add(product(20L, 5));
        var handler = new ProductOrderEventHandler(productRepository.proxy(), processedEventRepository.proxy());

        handler.on(new OrderCreatedEvent(
                UUID.randomUUID(),
                100L,
                1L,
                BigDecimal.valueOf(200),
                List.of(
                        new OrderCreatedEvent.OrderItemSnapshot(10L, 2),
                        new OrderCreatedEvent.OrderItemSnapshot(20L, 1)
                ),
                Instant.now()
        ));

        assertThat(productRepository.findByIdInCalls).isEqualTo(1);
        assertThat(productRepository.findByIdCalls).isZero();
        assertThat(first.getStock()).isEqualTo(8);
        assertThat(first.getReservedStock()).isEqualTo(2);
        assertThat(second.getStock()).isEqualTo(4);
        assertThat(second.getReservedStock()).isEqualTo(1);
    }

    @Test
    void should_commit_reserved_stock_with_one_batch_load_when_payment_succeeds() {
        Product first = productRepository.add(product(10L, 10));
        Product second = productRepository.add(product(20L, 5));
        first.reserveStock(2);
        second.reserveStock(1);
        orderReadApi.inventoryView = Optional.of(inventoryView());
        var handler = new ProductPaymentEventHandler(
                orderReadApi,
                productRepository.proxy(),
                processedEventRepository.proxy()
        );

        handler.on(new PaymentSucceededEvent(UUID.randomUUID(), 100L, "tx-1", Instant.now()));

        assertThat(productRepository.findByIdInCalls).isEqualTo(1);
        assertThat(productRepository.findByIdCalls).isZero();
        assertThat(first.getStock()).isEqualTo(8);
        assertThat(first.getReservedStock()).isZero();
        assertThat(second.getStock()).isEqualTo(4);
        assertThat(second.getReservedStock()).isZero();
    }

    @Test
    void should_release_reserved_stock_with_one_batch_load_when_order_expires() {
        Product first = productRepository.add(product(10L, 10));
        Product second = productRepository.add(product(20L, 5));
        first.reserveStock(2);
        second.reserveStock(1);
        orderReadApi.inventoryView = Optional.of(inventoryView());
        var handler = new ProductPaymentEventHandler(
                orderReadApi,
                productRepository.proxy(),
                processedEventRepository.proxy()
        );

        handler.on(new OrderExpiredEvent(UUID.randomUUID(), 100L, 1L, Instant.now()));

        assertThat(productRepository.findByIdInCalls).isEqualTo(1);
        assertThat(productRepository.findByIdCalls).isZero();
        assertThat(first.getStock()).isEqualTo(10);
        assertThat(first.getReservedStock()).isZero();
        assertThat(second.getStock()).isEqualTo(5);
        assertThat(second.getReservedStock()).isZero();
    }

    private OrderInventoryView inventoryView() {
        return new OrderInventoryView(
                100L,
                List.of(
                        new OrderInventoryView.OrderInventoryLineView(10L, 2),
                        new OrderInventoryView.OrderInventoryLineView(20L, 1)
                )
        );
    }

    private Product product(Long id, int stock) {
        Category category = Category.create("Elektronik", "elektronik", null, null, null, 0);
        ReflectionTestUtils.setField(category, "id", 1L);
        Product product = Product.create("Urun " + id, null, BigDecimal.TEN, stock, null, category);
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }

    private static class FakeProductRepository {
        private final Map<Long, Product> products = new HashMap<>();
        private int findByIdCalls;
        private int findByIdInCalls;

        Product add(Product product) {
            products.put(product.getId(), product);
            return product;
        }

        ProductRepository proxy() {
            return (ProductRepository) Proxy.newProxyInstance(
                    ProductRepository.class.getClassLoader(),
                    new Class<?>[]{ProductRepository.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "findById" -> {
                            findByIdCalls++;
                            throw new AssertionError("Use findByIdIn for batch stock loading");
                        }
                        case "findByIdIn" -> {
                            findByIdInCalls++;
                            Collection<?> ids = (Collection<?>) args[0];
                            yield ids.stream()
                                    .map(products::get)
                                    .filter(java.util.Objects::nonNull)
                                    .toList();
                        }
                        default -> throw new UnsupportedOperationException(method.getName());
                    }
            );
        }
    }

    private static class FakeProcessedEventRepository {
        ProcessedEventRepository proxy() {
            return (ProcessedEventRepository) Proxy.newProxyInstance(
                    ProcessedEventRepository.class.getClassLoader(),
                    new Class<?>[]{ProcessedEventRepository.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "saveAndFlush" -> args[0];
                        default -> throw new UnsupportedOperationException(method.getName());
                    }
            );
        }
    }

    private static class FakeOrderReadApi implements OrderReadApi {
        Optional<OrderInventoryView> inventoryView = Optional.empty();

        @Override
        public Optional<OrderInventoryView> findInventoryViewById(Long orderId) {
            return inventoryView;
        }

        @Override
        public Optional<OrderPaymentView> findPaymentViewById(Long orderId) {
            return Optional.empty();
        }
    }
}
