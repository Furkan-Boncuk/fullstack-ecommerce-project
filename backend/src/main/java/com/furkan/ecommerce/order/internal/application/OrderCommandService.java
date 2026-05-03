package com.furkan.ecommerce.order.internal.application;

import com.furkan.ecommerce.cart.api.CartReadApi;
import com.furkan.ecommerce.auth.api.AuthReadApi;
import com.furkan.ecommerce.auth.api.dto.AuthPaymentProfileView;
import com.furkan.ecommerce.common.exception.BusinessException;
import com.furkan.ecommerce.common.exception.ResourceNotFoundException;
import com.furkan.ecommerce.common.outbox.OutboxRecorder;
import com.furkan.ecommerce.order.api.dto.OrderView;
import com.furkan.ecommerce.order.api.event.OrderCreatedEvent;
import com.furkan.ecommerce.order.internal.domain.Order;
import com.furkan.ecommerce.order.internal.persistence.OrderRepository;
import java.time.Duration;
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
    private static final Duration PAYMENT_WINDOW = Duration.ofMinutes(30);

    private final OrderRepository orderRepository;
    private final CartReadApi cartReadApi;
    private final AuthReadApi authReadApi;
    private final OutboxRecorder outboxRecorder;
    private final OrderMapper orderMapper;

    @Transactional
    public OrderView createOrder(Long userId) {
        var cart = cartReadApi.getCart(userId);
        if (cart.items().isEmpty()) {
            throw new ResourceNotFoundException("CART_EMPTY", "Cart is empty");
        }
        AuthPaymentProfileView profile = authReadApi.findPaymentProfileById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "User not found"));
        validateShippingProfile(profile);
        Instant expiresAt = Instant.now().plus(PAYMENT_WINDOW);
        Order order = orderRepository.save(Order.create(userId, orderMapper.toOrderLineInputs(cart.items()), orderMapper.toShippingSnapshot(profile), expiresAt));
        outboxRecorder.record(new OrderCreatedEvent(
                UUID.randomUUID(),
                order.getId(),
                userId,
                order.getTotalAmount(),
                orderMapper.toOrderItemSnapshots(cart.items()),
                Instant.now()
        ));
        return orderMapper.toView(order);
    }

    public List<OrderView> list(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(orderMapper::toView)
                .toList();
    }

    private void validateShippingProfile(AuthPaymentProfileView profile) {
        if (isBlank(profile.firstName())
                || isBlank(profile.lastName())
                || isBlank(profile.phoneNumber())
                || isBlank(profile.address())
                || isBlank(profile.city())
                || isBlank(profile.country())
                || isBlank(profile.zipCode())) {
            throw new BusinessException("PAYMENT_PROFILE_INCOMPLETE", "Payment profile is incomplete");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
