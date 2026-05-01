package com.furkan.ecommerce.product.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class CategoryReadServiceTest {
    private FakeCategoryRepository categoryRepository;
    private CategoryReadService service;

    @BeforeEach
    void setUp() {
        categoryRepository = new FakeCategoryRepository();
        service = new CategoryReadService(categoryRepository.proxy());
    }

    @Test
    void should_return_category_views() {
        Category electronics = category(1L, "Elektronik", "elektronik", null, 10);
        categoryRepository.categories = List.of(electronics);

        var result = service.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().slug()).isEqualTo("elektronik");
        assertThat(result.getFirst().parentId()).isNull();
    }

    @Test
    void should_return_category_tree() {
        Category electronics = category(1L, "Elektronik", "elektronik", null, 10);
        Category headphones = category(2L, "Kulaklik", "kulaklik", electronics, 20);
        categoryRepository.categories = List.of(electronics, headphones);

        var result = service.findTree();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().slug()).isEqualTo("elektronik");
        assertThat(result.getFirst().children()).extracting("slug").containsExactly("kulaklik");
    }

    @Test
    void should_find_category_by_slug() {
        Category sports = category(4L, "Spor", "spor", null, 40);
        categoryRepository.categories = List.of(sports);

        var result = service.findBySlug("spor");

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().name()).isEqualTo("Spor");
    }

    private Category category(Long id, String name, String slug, Category parent, Integer sortOrder) {
        Category category = Category.create(name, slug, name + " kategori aciklamasi", null, parent, sortOrder);
        ReflectionTestUtils.setField(category, "id", id);
        return category;
    }

    private static class FakeCategoryRepository {
        List<Category> categories = List.of();

        CategoryRepository proxy() {
            return (CategoryRepository) Proxy.newProxyInstance(
                    CategoryRepository.class.getClassLoader(),
                    new Class<?>[]{CategoryRepository.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "findAllByOrderBySortOrderAscNameAsc" -> categories;
                        case "findBySlug" -> categories.stream()
                                .filter(category -> category.getSlug().equals(args[0]))
                                .findFirst();
                        default -> throw new UnsupportedOperationException(method.getName());
                    }
            );
        }
    }
}
