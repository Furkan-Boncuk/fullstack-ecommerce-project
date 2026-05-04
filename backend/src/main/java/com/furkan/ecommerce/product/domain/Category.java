package com.furkan.ecommerce.product.domain;

import com.furkan.ecommerce.common.domain.BaseEntity;
import com.furkan.ecommerce.common.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity
@Table(name = "categories")
@SQLRestriction("active = true")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends BaseEntity {
    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, unique = true, length = 140)
    private String slug;

    @Column(length = 500)
    private String description;

    @Column(length = 500)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @Column(nullable = false)
    private Integer sortOrder;

    @Column(nullable = false)
    private boolean active;

    public static Category create(String name, String slug, String description, String imageUrl, Category parent, Integer sortOrder) {
        Category category = new Category();
        category.name = requireText(name, "CATEGORY_NAME_REQUIRED", "Category name is required");
        category.slug = requireText(slug, "CATEGORY_SLUG_REQUIRED", "Category slug is required");
        category.description = description;
        category.imageUrl = imageUrl;
        category.parent = parent;
        category.sortOrder = sortOrder == null ? 0 : sortOrder;
        category.active = true;
        return category;
    }

    void rename(String name, String slug) {
        this.name = requireText(name, "CATEGORY_NAME_REQUIRED", "Category name is required");
        this.slug = requireText(slug, "CATEGORY_SLUG_REQUIRED", "Category slug is required");
    }

    void updateMetadata(String description, String imageUrl, Integer sortOrder) {
        this.description = description;
        this.imageUrl = imageUrl;
        this.sortOrder = sortOrder == null ? this.sortOrder : sortOrder;
    }

    void moveUnder(Category parent) {
        if (parent != null && getId() != null && getId().equals(parent.getId())) {
            throw new BusinessException("CATEGORY_INVALID_PARENT", "Category cannot be its own parent");
        }
        this.parent = parent;
    }

    void activate() {
        this.active = true;
    }

    void deactivate() {
        this.active = false;
    }

    private static String requireText(String value, String code, String message) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(code, message);
        }
        return value.trim();
    }
}
