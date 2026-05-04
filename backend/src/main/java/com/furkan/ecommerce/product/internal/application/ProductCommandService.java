package com.furkan.ecommerce.product.internal.application;

import com.furkan.ecommerce.common.dto.PageResponse;
import com.furkan.ecommerce.common.exception.ResourceNotFoundException;
import com.furkan.ecommerce.product.api.dto.AdminProductRequest;
import com.furkan.ecommerce.product.api.dto.ProductView;
import com.furkan.ecommerce.product.internal.domain.Category;
import com.furkan.ecommerce.product.internal.domain.Product;
import com.furkan.ecommerce.product.internal.persistence.CategoryRepository;
import com.furkan.ecommerce.product.internal.persistence.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductCommandService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    public PageResponse<ProductView> list(Pageable pageable) {
        return PageResponse.of(productRepository.findAll((Specification<Product>) null, pageable).map(productMapper::toView));
    }

    @Transactional
    public ProductView create(AdminProductRequest request) {
        Product product = productRepository.save(Product.create(
                request.name(),
                request.description(),
                request.price(),
                request.stock(),
                request.imageUrl(),
                category(request.categoryId())
        ));
        return productMapper.toView(product);
    }

    @Transactional
    public ProductView update(Long id, AdminProductRequest request) {
        Product product = product(id);
        product.updateDetails(
                request.name(),
                request.description(),
                request.price(),
                request.stock(),
                request.imageUrl(),
                category(request.categoryId())
        );
        return productMapper.toView(product);
    }

    @Transactional
    public void deactivate(Long id) {
        product(id).deactivate();
    }

    private Product product(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PRODUCT_NOT_FOUND", "Product not found"));
    }

    private Category category(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CATEGORY_NOT_FOUND", "Category not found"));
    }

}
