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

    @Override
    public Optional<OrderInventoryView> findInventoryViewById(Long orderId) {
        return orderRepository.findWithItemsById(orderId)
                .map(order -> new OrderInventoryView(
                        order.getId(),
                        order.inventoryLines().stream()
                                .map(item -> new OrderInventoryView.OrderInventoryLineView(item.productId(), item.quantity()))
                                .toList()
                ));
    }

    @Override
    public Optional<OrderPaymentView> findPaymentViewById(Long orderId) {
        return orderRepository.findWithItemsById(orderId)
                .map(order -> new OrderPaymentView(
                        order.getId(),
                        order.getUserId(),
                        order.getStatus().name(),
                        order.getTotalAmount(),
                        order.getExpiresAt(),
                        order.lineViews().stream()
                                .map(line -> new OrderPaymentView.OrderPaymentLineView(
                                        line.productId(),
                                        line.productName(),
                                        line.productImageUrl(),
                                        line.unitPrice(),
                                        line.quantity()
                                ))
                                .toList()
                ));
    }
}
