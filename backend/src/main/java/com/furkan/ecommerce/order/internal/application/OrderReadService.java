package com.furkan.ecommerce.order.internal.application;

import com.furkan.ecommerce.order.api.OrderReadApi;
import com.furkan.ecommerce.order.api.dto.OrderInventoryView;
import com.furkan.ecommerce.order.api.dto.OrderPaymentView;
import com.furkan.ecommerce.order.internal.persistence.OrderRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class OrderReadService implements OrderReadApi {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Override
    public Optional<OrderInventoryView> findInventoryViewById(Long orderId) {
        return orderRepository.findWithItemsById(orderId)
                .map(orderMapper::toInventoryView);
    }

    @Override
    public Optional<OrderPaymentView> findPaymentViewById(Long orderId) {
        return orderRepository.findWithItemsById(orderId)
                .map(orderMapper::toPaymentView);
    }
}
