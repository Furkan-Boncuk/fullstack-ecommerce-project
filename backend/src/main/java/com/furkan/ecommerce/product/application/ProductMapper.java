package com.furkan.ecommerce.product.application;

import com.furkan.ecommerce.product.dto.CategoryTreeView;
import com.furkan.ecommerce.product.dto.CategoryView;
import com.furkan.ecommerce.product.dto.ProductCategorySummary;
import com.furkan.ecommerce.product.dto.ProductView;
import com.furkan.ecommerce.product.domain.Category;
import com.furkan.ecommerce.product.domain.Product;
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
