package com.furkan.ecommerce.product.internal;

import com.furkan.ecommerce.common.dto.PageResponse;
import com.furkan.ecommerce.common.exception.ResourceNotFoundException;
import com.furkan.ecommerce.product.api.ProductReadApi;
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
    private final ProductMapper productMapper;

    public PageResponse<ProductView> list(ProductFilterRequest filter, Pageable pageable) {
        var page = repository.findAll(ProductSpecifications.from(filter), pageable)
                .map(productMapper::toView);
        return PageResponse.of(page);
    }

    public ProductView getById(Long id) {
        return findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PRODUCT_NOT_FOUND", "Product not found"));
    }

    @Override
    public Optional<ProductView> findById(Long id) {
        return repository.findById(id).map(productMapper::toView);
    }

    @Override
    public List<ProductView> findByIds(Collection<Long> ids) {
        return repository.findByIdIn(ids).stream().map(productMapper::toView).toList();
    }
}
