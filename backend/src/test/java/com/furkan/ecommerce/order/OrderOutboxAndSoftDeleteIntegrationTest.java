package com.furkan.ecommerce.order;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.furkan.ecommerce.auth.internal.domain.User;
import com.furkan.ecommerce.auth.internal.persistence.UserRepository;
import com.furkan.ecommerce.product.internal.domain.Product;
import com.furkan.ecommerce.product.internal.persistence.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderOutboxAndSoftDeleteIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        registry.add("app.security.refresh-cookie-secure", () -> false);
    }

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void should_write_order_created_event_to_outbox_on_order_creation() throws Exception {
        Product product = productRepository.save(Product.create("Phone", BigDecimal.valueOf(1000), 20));

        String token = registerAndExtractAccessToken("order-flow@test.com");

        mockMvc.perform(post("/api/v1/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":" + product.getId() + ",\"quantity\":2}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        Integer outboxCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM outbox_events WHERE event_type = 'OrderCreatedEvent'",
                Integer.class
        );
        assertThat(outboxCount).isNotNull();
        assertThat(outboxCount).isGreaterThan(0);
    }

    @Test
    void should_hide_soft_deleted_product_and_user_from_default_queries() throws Exception {
        Product activeProduct = productRepository.save(Product.create("Visible", BigDecimal.valueOf(50), 5));
        Product hiddenProduct = productRepository.save(Product.create("Hidden", BigDecimal.valueOf(70), 5));
        hiddenProduct.deactivate();
        productRepository.save(hiddenProduct);

        String productListResponse = mockMvc.perform(get("/api/v1/products?page=0&size=20"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode productJson = objectMapper.readTree(productListResponse);
        List<Long> productIds = new ArrayList<>();
        for (JsonNode node : productJson.get("content")) {
            productIds.add(node.get("id").asLong());
        }

        assertThat(productIds).contains(activeProduct.getId());
        assertThat(productIds).doesNotContain(hiddenProduct.getId());

        String email = "inactive-user@test.com";
        registerAndExtractAccessToken(email);
        User user = userRepository.findByEmail(email).orElseThrow();
        user.deactivate();
        userRepository.save(user);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "inactive-user@test.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    private String registerAndExtractAccessToken(String email) throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "password123"
                                }
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("accessToken").asText();
    }
}
