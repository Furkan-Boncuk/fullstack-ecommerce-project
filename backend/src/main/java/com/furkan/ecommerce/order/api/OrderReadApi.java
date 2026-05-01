package com.furkan.ecommerce.order.api;

import com.furkan.ecommerce.order.api.dto.OrderInventoryView;
import com.furkan.ecommerce.order.api.dto.OrderPaymentView;
import java.util.Optional;

public interface OrderReadApi {
    Optional<OrderInventoryView> findInventoryViewById(Long orderId);

    Optional<OrderPaymentView> findPaymentViewById(Long orderId);
}
