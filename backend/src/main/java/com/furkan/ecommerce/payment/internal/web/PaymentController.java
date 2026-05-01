package com.furkan.ecommerce.payment.internal.web;

import com.furkan.ecommerce.payment.api.dto.PaymentCallbackRequest;
import com.furkan.ecommerce.payment.api.dto.PaymentInitRequest;
import com.furkan.ecommerce.payment.api.dto.PaymentInitResponse;
import com.furkan.ecommerce.payment.internal.PaymentCommandService;
import com.furkan.ecommerce.payment.internal.PaymentGateway;
import com.furkan.ecommerce.common.exception.UnauthorizedException;
import com.furkan.ecommerce.infrastructure.security.SecurityPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
class PaymentController {
    private final PaymentCommandService paymentCommandService;

    @PostMapping("/init")
    PaymentInitResponse init(
            @AuthenticationPrincipal SecurityPrincipal principal,
            @Valid @RequestBody PaymentInitRequest request
    ) {
        if (principal == null) {
            throw new UnauthorizedException("Unauthorized");
        }
        PaymentGateway.PaymentResult result = paymentCommandService.init(principal.userId(), request.orderId());
        return new PaymentInitResponse(
                result.success(),
                result.transactionId(),
                result.errorCode(),
                result.checkoutUrl(),
                result.checkoutToken(),
                result.providerReference()
        );
    }

    @PostMapping(value = "/callback", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> callbackJson(@Valid @RequestBody PaymentCallbackRequest request) {
        paymentCommandService.handleCallback(request.token());
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/callback", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    ResponseEntity<Void> callbackForm(@RequestParam String token) {
        paymentCommandService.handleCallback(token);
        return ResponseEntity.noContent().build();
    }
}
