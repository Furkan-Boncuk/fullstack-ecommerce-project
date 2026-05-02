package com.furkan.ecommerce.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import jakarta.servlet.http.Cookie;

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
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
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
                        .cookie(refreshCookieFrom(firstCookie)))
                .andExpect(status().isOk())
                .andReturn();

        String rotatedCookie = refreshResponse.getResponse().getHeader(HttpHeaders.SET_COOKIE);
        assertThat(rotatedCookie).isNotBlank();
        assertThat(rotatedCookie).isNotEqualTo(firstCookie);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(refreshCookieFrom(firstCookie)))
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
                        .cookie(refreshCookieFrom(cookie)))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(refreshCookieFrom(cookie)))
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
                                .cookie(refreshCookieFrom(cookie)))
                        .andExpect(status().isUnauthorized())
        );
    }

    private Cookie refreshCookieFrom(String setCookie) {
        assertThat(setCookie).isNotBlank();

        String cookieName = "refresh_token";
        String cookiePrefix = cookieName + "=";
        int valueStart = setCookie.indexOf(cookiePrefix);
        assertThat(valueStart).isGreaterThanOrEqualTo(0);
        valueStart += cookiePrefix.length();

        int valueEnd = setCookie.indexOf(';', valueStart);
        String value = valueEnd == -1 ? setCookie.substring(valueStart) : setCookie.substring(valueStart, valueEnd);
        assertThat(value).isNotBlank();

        Cookie cookie = new Cookie(cookieName, value);
        cookie.setPath("/api/v1/auth");
        return cookie;
    }
}
