package com.furkan.ecommerce.product;

import com.furkan.ecommerce.product.dto.ProductView;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductReadApi {
    Optional<ProductView> findById(Long id);
    List<ProductView> findByIds(Collection<Long> ids);
}
