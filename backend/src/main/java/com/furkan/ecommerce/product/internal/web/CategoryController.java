package com.furkan.ecommerce.product.internal.web;

import com.furkan.ecommerce.product.api.dto.CategoryTreeView;
import com.furkan.ecommerce.product.api.dto.CategoryView;
import com.furkan.ecommerce.product.internal.CategoryReadService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
class CategoryController {
    private final CategoryReadService categoryReadService;

    @GetMapping
    List<CategoryView> list() {
        return categoryReadService.findAll();
    }

    @GetMapping("/tree")
    List<CategoryTreeView> tree() {
        return categoryReadService.findTree();
    }

    @GetMapping("/{slug}")
    CategoryView detail(@PathVariable String slug) {
        return categoryReadService.getBySlug(slug);
    }
}
