package com.furkan.ecommerce.order.internal.application;

import com.furkan.ecommerce.cart.internal.application.CartCommandService;
import com.furkan.ecommerce.common.exception.ResourceNotFoundException;
import com.furkan.ecommerce.common.outbox.OutboxRecorder;
import com.furkan.ecommerce.order.api.dto.OrderView;
import com.furkan.ecommerce.order.api.event.OrderPlacedEvent;
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
    private final CartCommandService cartCommandService;
    private final OutboxRecorder outboxRecorder;

    @Transactional
    public OrderView createOrder(Long userId) {
        var cart = cartCommandService.getCart(userId);
        if (cart.items().isEmpty()) {
            throw new ResourceNotFoundException("CART_EMPTY", "Cart is empty");
        }
        BigDecimal total = BigDecimal.valueOf(cart.items().size()).multiply(BigDecimal.TEN);
        Order order = orderRepository.save(Order.create(userId, total));
        outboxRecorder.record(new OrderPlacedEvent(UUID.randomUUID(), order.getId(), userId, total, Instant.now()));
        return new OrderView(order.getId(), order.getStatus().name(), order.getTotalAmount());
    }

    public List<OrderView> list(Long userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(o -> new OrderView(o.getId(), o.getStatus().name(), o.getTotalAmount()))
                .toList();
    }
}
