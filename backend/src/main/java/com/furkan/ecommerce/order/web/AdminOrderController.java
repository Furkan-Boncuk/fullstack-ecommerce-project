package com.furkan.ecommerce.order.web;

import com.furkan.ecommerce.common.dto.PageResponse;
import com.furkan.ecommerce.infrastructure.security.AdminOnly;
import com.furkan.ecommerce.order.dto.AdminOrderView;
import com.furkan.ecommerce.order.application.AdminOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@AdminOnly
@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
class AdminOrderController {
    private final AdminOrderService service;

    @GetMapping
    PageResponse<AdminOrderView> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String email,
            Pageable pageable
    ) {
        return service.list(status, userId, email, pageable);
    }
}
