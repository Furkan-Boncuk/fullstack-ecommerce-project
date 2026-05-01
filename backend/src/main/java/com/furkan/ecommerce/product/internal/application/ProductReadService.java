package com.furkan.ecommerce.product.internal.application;

import com.furkan.ecommerce.common.dto.PageResponse;
import com.furkan.ecommerce.product.api.ProductReadApi;
import com.furkan.ecommerce.product.api.dto.ProductFilterRequest;
import com.furkan.ecommerce.product.api.dto.ProductView;
import com.furkan.ecommerce.product.internal.persistence.ProductRepository;
import com.furkan.ecommerce.product.internal.specification.ProductSpecifications;
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
                .map(p -> new ProductView(p.getId(), p.getName(), p.getPrice(), p.getStock()));
        return PageResponse.of(page);
    }

    @Override
    public Optional<ProductView> findById(Long id) {
        return repository.findById(id).map(p -> new ProductView(p.getId(), p.getName(), p.getPrice(), p.getStock()));
    }

    @Override
    public List<ProductView> findByIds(Collection<Long> ids) {
        return repository.findAllById(ids).stream().map(p -> new ProductView(p.getId(), p.getName(), p.getPrice(), p.getStock())).toList();
    }
}
