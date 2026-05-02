package com.furkan.ecommerce.order.internal.web;

import com.furkan.ecommerce.common.exception.UnauthorizedException;
import com.furkan.ecommerce.infrastructure.security.SecurityPrincipal;
import com.furkan.ecommerce.order.api.dto.OrderView;
import com.furkan.ecommerce.order.internal.application.OrderCommandService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
class OrderController {
    private final OrderCommandService service;

    @PostMapping
    OrderView create(@AuthenticationPrincipal SecurityPrincipal principal) {
        if (principal == null) {
            throw new UnauthorizedException("Unauthorized");
        }
        return service.createOrder(principal.userId());
    }

    @GetMapping
    List<OrderView> list(@AuthenticationPrincipal SecurityPrincipal principal) {
        if (principal == null) {
            throw new UnauthorizedException("Unauthorized");
        }
        return service.list(principal.userId());
    }
}
