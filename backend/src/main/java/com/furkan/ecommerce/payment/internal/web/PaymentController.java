package com.furkan.ecommerce.payment.internal.web;

import com.furkan.ecommerce.common.exception.UnauthorizedException;
import com.furkan.ecommerce.infrastructure.security.SecurityPrincipal;
import com.furkan.ecommerce.payment.api.dto.PaymentInitRequest;
import com.furkan.ecommerce.payment.api.dto.PaymentInitResponse;
import com.furkan.ecommerce.payment.api.dto.PaymentStatusResponse;
import com.furkan.ecommerce.payment.internal.PaymentCallbackProperties;
import com.furkan.ecommerce.payment.internal.PaymentCommandService;
import com.furkan.ecommerce.payment.internal.PaymentGateway;
import com.furkan.ecommerce.payment.internal.PaymentMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
class PaymentController {
    private final PaymentCommandService paymentCommandService;
    private final PaymentCallbackProperties properties;
    private final PaymentMapper paymentMapper;

    @PostMapping("/init")
    PaymentInitResponse init(
            @AuthenticationPrincipal SecurityPrincipal principal,
            @Valid @RequestBody PaymentInitRequest request,
            HttpServletRequest servletRequest
    ) {
        if (principal == null) {
            throw new UnauthorizedException("Unauthorized");
        }
        PaymentGateway.PaymentResult result = paymentCommandService.init(principal.userId(), request.orderId(), clientIp(servletRequest));
        return paymentMapper.toInitResponse(result);
    }

    @GetMapping("/orders/{orderId}")
    PaymentStatusResponse status(@AuthenticationPrincipal SecurityPrincipal principal, @PathVariable Long orderId) {
        if (principal == null) {
            throw new UnauthorizedException("Unauthorized");
        }
        return paymentCommandService.getStatus(principal.userId(), orderId);
    }

    @PostMapping("/callback")
    ResponseEntity<Void> callback(@RequestParam String token, HttpServletRequest request) {
        log.info("Payment callback received contentType={}", request.getContentType());
        return redirect(paymentCommandService.handleCallback(token));
    }

    private ResponseEntity<Void> redirect(PaymentCommandService.PaymentCallbackResult result) {
        URI location = UriComponentsBuilder.fromUriString(properties.frontendResultUrl())
                .queryParam("orderId", result.orderId())
                .build(true)
                .toUri();
        log.info("Payment callback redirect orderId={} status={} location={}", result.orderId(), result.status(), location);
        return ResponseEntity.status(HttpStatus.SEE_OTHER)
                .header(HttpHeaders.LOCATION, location.toString())
                .build();
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
