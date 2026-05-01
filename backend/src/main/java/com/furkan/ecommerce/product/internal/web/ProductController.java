package com.furkan.ecommerce.product.internal.web;

import com.furkan.ecommerce.common.dto.PageResponse;
import com.furkan.ecommerce.product.api.dto.ProductFilterRequest;
import com.furkan.ecommerce.product.api.dto.ProductView;
import com.furkan.ecommerce.product.internal.application.ProductReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductReadService readService;

    @GetMapping
    PageResponse<ProductView> list(ProductFilterRequest filter, Pageable pageable) {
        return readService.list(filter, pageable);
    }
}
