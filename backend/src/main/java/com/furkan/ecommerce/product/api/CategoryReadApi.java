package com.furkan.ecommerce.product.api;

import com.furkan.ecommerce.product.api.dto.CategoryTreeView;
import com.furkan.ecommerce.product.api.dto.CategoryView;
import java.util.List;
import java.util.Optional;

public interface CategoryReadApi {
    List<CategoryView> findAll();
    List<CategoryTreeView> findTree();
    Optional<CategoryView> findBySlug(String slug);
}
