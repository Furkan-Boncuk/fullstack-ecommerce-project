package com.furkan.ecommerce.order;

import com.furkan.ecommerce.order.dto.OrderInventoryView;
import com.furkan.ecommerce.order.dto.OrderPaymentView;
import java.util.Optional;

public interface OrderReadApi {
    Optional<OrderInventoryView> findInventoryViewById(Long orderId);

    Optional<OrderPaymentView> findPaymentViewById(Long orderId);
}
