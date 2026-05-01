package com.furkan.ecommerce.order.internal.application;

import com.furkan.ecommerce.cart.api.CartReadApi;
import com.furkan.ecommerce.common.exception.ResourceNotFoundException;
import com.furkan.ecommerce.common.outbox.OutboxRecorder;
import com.furkan.ecommerce.order.api.dto.OrderView;
import com.furkan.ecommerce.order.api.event.OrderCreatedEvent;
import com.furkan.ecommerce.order.internal.domain.Order;
import com.furkan.ecommerce.order.internal.persistence.OrderRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderCommandService {
    private final OrderRepository orderRepository;
    private final CartReadApi cartReadApi;
    private final OutboxRecorder outboxRecorder;

    @Transactional
    public OrderView createOrder(Long userId) {
        var cart = cartReadApi.getCart(userId);
        if (cart.items().isEmpty()) {
            throw new ResourceNotFoundException("CART_EMPTY", "Cart is empty");
        }
        List<Order.OrderLineInput> lines = cart.items().stream()
                .map(line -> new Order.OrderLineInput(line.productId(), line.quantity(), BigDecimal.TEN))
                .toList();
        Order order = orderRepository.save(Order.create(userId, lines));
        List<OrderCreatedEvent.OrderItemSnapshot> items = cart.items().stream()
                .map(line -> new OrderCreatedEvent.OrderItemSnapshot(line.productId(), line.quantity()))
                .toList();
        outboxRecorder.record(new OrderCreatedEvent(
                UUID.randomUUID(),
                order.getId(),
                userId,
                order.getTotalAmount(),
                items,
                Instant.now()
        ));
        return new OrderView(order.getId(), order.getStatus().name(), order.getTotalAmount());
    }

    public List<OrderView> list(Long userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(o -> new OrderView(o.getId(), o.getStatus().name(), o.getTotalAmount()))
                .toList();
    }
}
