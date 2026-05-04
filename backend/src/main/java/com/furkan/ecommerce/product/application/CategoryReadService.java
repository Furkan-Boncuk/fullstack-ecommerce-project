package com.furkan.ecommerce.product.application;

import com.furkan.ecommerce.common.exception.ResourceNotFoundException;
import com.furkan.ecommerce.product.CategoryReadApi;
import com.furkan.ecommerce.product.dto.CategoryTreeView;
import com.furkan.ecommerce.product.dto.CategoryView;
import com.furkan.ecommerce.product.domain.Category;
import com.furkan.ecommerce.product.persistence.CategoryRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryReadService implements CategoryReadApi {
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    @Override
    public List<CategoryView> findAll() {
        return categoryRepository.findAllByOrderBySortOrderAscNameAsc().stream()
                .map(productMapper::toCategoryView)
                .toList();
    }

    @Override
    public List<CategoryTreeView> findTree() {
        List<Category> categories = categoryRepository.findAllByOrderBySortOrderAscNameAsc();
        Map<Long, List<Category>> byParentId = categories.stream()
                .collect(Collectors.groupingBy(category -> category.getParent() == null ? 0L : category.getParent().getId()));

        return byParentId.getOrDefault(0L, List.of()).stream()
                .map(category -> toTreeView(category, byParentId))
                .toList();
    }

    @Override
    public Optional<CategoryView> findBySlug(String slug) {
        return categoryRepository.findBySlug(slug).map(productMapper::toCategoryView);
    }

    public CategoryView getBySlug(String slug) {
        return findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("CATEGORY_NOT_FOUND", "Category not found"));
    }

    private CategoryTreeView toTreeView(Category category, Map<Long, List<Category>> byParentId) {
        List<CategoryTreeView> children = byParentId.getOrDefault(category.getId(), List.of()).stream()
                .map(child -> toTreeView(child, byParentId))
                .toList();
        return productMapper.toTreeView(category, children);
    }
}
