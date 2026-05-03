package com.furkan.ecommerce.product.internal;

import com.furkan.ecommerce.product.api.dto.CategoryTreeView;
import com.furkan.ecommerce.product.api.dto.CategoryView;
import com.furkan.ecommerce.product.api.dto.ProductCategorySummary;
import com.furkan.ecommerce.product.api.dto.ProductView;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
interface ProductMapper {
    ProductView toView(Product product);

    ProductCategorySummary toCategorySummary(Category category);

    @Mapping(target = "parentId", source = "parent.id")
    CategoryView toCategoryView(Category category);

    @Mapping(target = "children", source = "children")
    CategoryTreeView toTreeView(Category category, List<CategoryTreeView> children);
}
