package com.furkan.ecommerce.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static java.time.Duration.ofSeconds;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

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
    }

    @Autowired
    MockMvc mockMvc;

    @Test
    void should_rotate_refresh_token_and_reject_old_token() throws Exception {
        String registerBody = """
                {
                  "email": "auth1@test.com",
                  "password": "password123"
                }
                """;

        var registerResponse = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("HttpOnly")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("Secure")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("SameSite=Lax")))
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn();

        String firstCookie = registerResponse.getResponse().getHeader(HttpHeaders.SET_COOKIE);
        assertThat(firstCookie).isNotBlank();

        var refreshResponse = mockMvc.perform(post("/api/v1/auth/refresh")
                        .header(HttpHeaders.COOKIE, firstCookie))
                .andExpect(status().isOk())
                .andReturn();

        String rotatedCookie = refreshResponse.getResponse().getHeader(HttpHeaders.SET_COOKIE);
        assertThat(rotatedCookie).isNotBlank();
        assertThat(rotatedCookie).isNotEqualTo(firstCookie);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .header(HttpHeaders.COOKIE, firstCookie))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void should_logout_and_invalidate_refresh_token() throws Exception {
        String registerBody = """
                {
                  "email": "auth2@test.com",
                  "password": "password123"
                }
                """;

        var registerResponse = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isOk())
                .andReturn();

        String cookie = registerResponse.getResponse().getHeader(HttpHeaders.SET_COOKIE);

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header(HttpHeaders.COOKIE, cookie))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .header(HttpHeaders.COOKIE, cookie))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void should_expire_refresh_token_with_short_ttl() throws Exception {
        String registerBody = """
                {
                  "email": "auth3@test.com",
                  "password": "password123"
                }
                """;

        var registerResponse = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isOk())
                .andReturn();

        String cookie = registerResponse.getResponse().getHeader(HttpHeaders.SET_COOKIE);

        await().atMost(ofSeconds(20)).untilAsserted(() ->
                mockMvc.perform(post("/api/v1/auth/refresh")
                                .header(HttpHeaders.COOKIE, cookie))
                        .andExpect(status().isUnauthorized())
        );
    }
}
