package com.furkan.ecommerce.product.web;

import com.furkan.ecommerce.common.dto.PageResponse;
import com.furkan.ecommerce.product.dto.ProductFilterRequest;
import com.furkan.ecommerce.product.dto.ProductView;
import com.furkan.ecommerce.product.application.ProductReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
class ProductController {
    private final ProductReadService readService;

    @GetMapping
    PageResponse<ProductView> list(ProductFilterRequest filter, Pageable pageable) {
        return readService.list(filter, pageable);
    }

    @GetMapping("/{id}")
    ProductView detail(@PathVariable Long id) {
        return readService.getById(id);
    }
}
