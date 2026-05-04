package com.furkan.ecommerce.order.application;

import com.furkan.ecommerce.auth.AuthReadApi;
import com.furkan.ecommerce.common.dto.PageResponse;
import com.furkan.ecommerce.common.exception.BusinessException;
import com.furkan.ecommerce.order.dto.AdminOrderView;
import com.furkan.ecommerce.order.domain.Order;
import com.furkan.ecommerce.order.domain.OrderStatus;
import com.furkan.ecommerce.order.persistence.OrderRepository;
import jakarta.persistence.criteria.Predicate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminOrderService {
    private final OrderRepository orderRepository;
    private final AuthReadApi authReadApi;
    private final OrderMapper orderMapper;

    public PageResponse<AdminOrderView> list(String status, Long userId, String email, Pageable pageable) {
        List<Long> emailUserIds = email == null || email.isBlank()
                ? null
                : authReadApi.findUserIdsByEmailContaining(email);
        if (emailUserIds != null && emailUserIds.isEmpty()) {
            return PageResponse.of(Page.empty(pageable));
        }

        OrderStatus parsedStatus = parseStatus(status);
        Page<Order> orders = orderRepository.findAll(specification(parsedStatus, userId, emailUserIds), pageable);
        Map<Long, String> emailsByUserId = userEmails(orders.getContent());
        Page<AdminOrderView> mapped = new PageImpl<>(
                orders.getContent().stream()
                        .map(order -> orderMapper.toAdminView(order, emailsByUserId.get(order.getUserId())))
                        .toList(),
                pageable,
                orders.getTotalElements()
        );
        return PageResponse.of(mapped);
    }

    private Specification<Order> specification(OrderStatus status, Long userId, List<Long> emailUserIds) {
        return (root, query, cb) -> {
            query.orderBy(cb.desc(root.get("createdAt")));
            Predicate predicate = cb.conjunction();
            if (status != null) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), status));
            }
            if (userId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("userId"), userId));
            }
            if (emailUserIds != null) {
                predicate = cb.and(predicate, root.get("userId").in(emailUserIds));
            }
            return predicate;
        };
    }

    private OrderStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return OrderStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("ORDER_INVALID_STATE", "Order status filter is invalid");
        }
    }

    private Map<Long, String> userEmails(List<Order> orders) {
        List<Long> userIds = orders.stream().map(Order::getUserId).distinct().toList();
        Map<Long, String> result = new HashMap<>();
        authReadApi.findUserSummariesByIds(userIds)
                .forEach(user -> result.put(user.id(), user.email()));
        return result;
    }

}
