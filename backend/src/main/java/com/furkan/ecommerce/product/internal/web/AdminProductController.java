package com.furkan.ecommerce.product.internal.web;

import com.furkan.ecommerce.common.dto.PageResponse;
import com.furkan.ecommerce.infrastructure.security.AdminOnly;
import com.furkan.ecommerce.product.api.dto.AdminProductRequest;
import com.furkan.ecommerce.product.api.dto.ProductView;
import com.furkan.ecommerce.product.internal.application.ProductCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AdminOnly
@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
class AdminProductController {
    private final ProductCommandService service;

    @GetMapping
    PageResponse<ProductView> list(Pageable pageable) {
        return service.list(pageable);
    }

    @PostMapping
    ProductView create(@Valid @RequestBody AdminProductRequest request) {
        return service.create(request);
    }

    @PatchMapping("/{id}")
    ProductView update(@PathVariable Long id, @Valid @RequestBody AdminProductRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> deactivate(@PathVariable Long id) {
        service.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
