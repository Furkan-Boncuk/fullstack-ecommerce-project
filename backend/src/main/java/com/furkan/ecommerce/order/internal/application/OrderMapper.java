package com.furkan.ecommerce.order.internal.application;

import com.furkan.ecommerce.order.api.dto.AdminOrderView;
import com.furkan.ecommerce.order.api.dto.OrderInventoryView;
import com.furkan.ecommerce.order.api.dto.OrderPaymentView;
import com.furkan.ecommerce.order.api.dto.OrderView;
import com.furkan.ecommerce.order.internal.domain.Order;
import java.util.List;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
abstract class OrderMapper {
    @Mapping(target = "status", source = "status")
    @Mapping(target = "shippingAddress", source = "order", qualifiedByName = "shippingAddressOrNull")
    @Mapping(target = "items", source = "order", qualifiedByName = "orderItems")
    abstract OrderView toView(Order order);

    @Mapping(target = "status", source = "order.status")
    @Mapping(target = "shippingAddress", source = "order", qualifiedByName = "shippingAddressOrNull")
    @Mapping(target = "items", source = "order", qualifiedByName = "orderItems")
    abstract AdminOrderView toAdminView(Order order, String userEmail);

    @Mapping(target = "orderId", source = "id")
    @Mapping(target = "lines", source = "order", qualifiedByName = "inventoryLines")
    abstract OrderInventoryView toInventoryView(Order order);

    @Mapping(target = "orderId", source = "id")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "items", source = "order", qualifiedByName = "paymentLines")
    abstract OrderPaymentView toPaymentView(Order order);

    abstract List<Order.OrderLineInput> toOrderLineInputs(List<com.furkan.ecommerce.cart.api.dto.CartView.CartLineView> lines);

    @Mapping(target = "productName", source = "name")
    @Mapping(target = "productImageUrl", source = "imageUrl")
    abstract Order.OrderLineInput toOrderLineInput(com.furkan.ecommerce.cart.api.dto.CartView.CartLineView line);

    abstract List<com.furkan.ecommerce.order.api.event.OrderCreatedEvent.OrderItemSnapshot> toOrderItemSnapshots(
            List<com.furkan.ecommerce.cart.api.dto.CartView.CartLineView> lines
    );

    abstract com.furkan.ecommerce.order.api.event.OrderCreatedEvent.OrderItemSnapshot toOrderItemSnapshot(
            com.furkan.ecommerce.cart.api.dto.CartView.CartLineView line
    );

    abstract Order.ShippingSnapshot toShippingSnapshot(com.furkan.ecommerce.auth.api.dto.AuthPaymentProfileView profile);

    abstract OrderView.OrderItemView toOrderItemView(Order.OrderLineView line);

    abstract OrderPaymentView.OrderPaymentLineView toPaymentLineView(Order.OrderLineView line);

    abstract OrderInventoryView.OrderInventoryLineView toInventoryLineView(Order.OrderLineSnapshot line);

    @Named("orderItems")
    List<OrderView.OrderItemView> toOrderItemViews(Order order) {
        return order.lineViews().stream()
                .map(this::toOrderItemView)
                .collect(Collectors.toList());
    }

    @Named("paymentLines")
    List<OrderPaymentView.OrderPaymentLineView> toPaymentLineViews(Order order) {
        return order.lineViews().stream()
                .map(this::toPaymentLineView)
                .collect(Collectors.toList());
    }

    @Named("inventoryLines")
    List<OrderInventoryView.OrderInventoryLineView> toInventoryLineViews(Order order) {
        return order.inventoryLines().stream()
                .map(this::toInventoryLineView)
                .collect(Collectors.toList());
    }

    @Mapping(target = "firstName", source = "shippingFirstName")
    @Mapping(target = "lastName", source = "shippingLastName")
    @Mapping(target = "phoneNumber", source = "shippingPhoneNumber")
    @Mapping(target = "address", source = "shippingAddress")
    @Mapping(target = "city", source = "shippingCity")
    @Mapping(target = "country", source = "shippingCountry")
    @Mapping(target = "zipCode", source = "shippingZipCode")
    abstract OrderView.ShippingAddressView mapShippingAddress(Order order);

    @Named("shippingAddressOrNull")
    OrderView.ShippingAddressView toShippingAddressView(Order order) {
        return hasShippingAddress(order) ? mapShippingAddress(order) : null;
    }

    boolean hasShippingAddress(Order order) {
        return !isBlank(order.getShippingFirstName())
                || !isBlank(order.getShippingLastName())
                || !isBlank(order.getShippingAddress());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
