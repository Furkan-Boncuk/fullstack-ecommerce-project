package com.furkan.ecommerce.auth.token;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RedisRefreshTokenStoreTest {
    @Test
    void buildsRefreshTokenKeysWithConfiguredEnvironmentPrefix() {
        RedisRefreshTokenStore store = new RedisRefreshTokenStore(null, "staging:");

        assertThat(store.userKey(42L)).isEqualTo("staging:auth:refresh:user:42");
        assertThat(store.indexKey("token-hash")).isEqualTo("staging:auth:refresh:index:token-hash");
    }
}
