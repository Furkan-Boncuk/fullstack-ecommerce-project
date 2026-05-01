package com.furkan.ecommerce.product.internal;

import com.furkan.ecommerce.common.dto.PageResponse;
import com.furkan.ecommerce.common.exception.ResourceNotFoundException;
import com.furkan.ecommerce.product.api.ProductReadApi;
import com.furkan.ecommerce.product.api.dto.ProductCategorySummary;
import com.furkan.ecommerce.product.api.dto.ProductFilterRequest;
import com.furkan.ecommerce.product.api.dto.ProductView;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductReadService implements ProductReadApi {
    private final ProductRepository repository;

    public PageResponse<ProductView> list(ProductFilterRequest filter, Pageable pageable) {
        var page = repository.findAll(ProductSpecifications.from(filter), pageable)
                .map(this::toView);
        return PageResponse.of(page);
    }

    public ProductView getById(Long id) {
        return findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PRODUCT_NOT_FOUND", "Product not found"));
    }

    @Override
    public Optional<ProductView> findById(Long id) {
        return repository.findById(id).map(this::toView);
    }

    @Override
    public List<ProductView> findByIds(Collection<Long> ids) {
        return repository.findByIdIn(ids).stream().map(this::toView).toList();
    }

    private ProductView toView(Product product) {
        return new ProductView(
                product.getId(),
                product.getName(),
                product.getDescription(),
                toCategorySummary(product.getCategory()),
                product.getImageUrl(),
                product.getPrice(),
                product.getStock()
        );
    }

    private ProductCategorySummary toCategorySummary(Category category) {
        return new ProductCategorySummary(category.getId(), category.getName(), category.getSlug());
    }
}
