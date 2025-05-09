package com.jin12.reviews_api.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ApiKeyTest {
    @Test
    void testSettersAndGetters() {
        ApiKey apiKey = new ApiKey();

        User user = new User();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiry = now.plusDays(30);

        apiKey.setId(1L);
        apiKey.setKeyHash("hashed-key-123");
        apiKey.setCreatedAt(now);
        apiKey.setExpiresAt(expiry);
        apiKey.setUser(user);

        assertThat(apiKey.getId()).isEqualTo(1L);
        assertThat(apiKey.getKeyHash()).isEqualTo("hashed-key-123");
        assertThat(apiKey.getCreatedAt()).isEqualTo(now);
        assertThat(apiKey.getExpiresAt()).isEqualTo(expiry);
        assertThat(apiKey.getUser()).isSameAs(user);
    }

    @Test
    void testEqualsAndHashCode() {
        ApiKey apiKey1 = new ApiKey();
        ApiKey apiKey2 = new ApiKey();

        LocalDateTime time = LocalDateTime.now();
        User user = new User();

        apiKey1.setId(10L);
        apiKey1.setKeyHash("key-abc");
        apiKey1.setCreatedAt(time);
        apiKey1.setExpiresAt(time.plusDays(1));
        apiKey1.setUser(user);

        apiKey2.setId(10L);
        apiKey2.setKeyHash("key-abc");
        apiKey2.setCreatedAt(time);
        apiKey2.setExpiresAt(time.plusDays(1));
        apiKey2.setUser(user);

        assertThat(apiKey1).isEqualTo(apiKey2);
        assertThat(apiKey1.hashCode()).isEqualTo(apiKey2.hashCode());
    }

    @Test
    void testToStringContainsExpectedFields() {
        ApiKey apiKey = new ApiKey();
        LocalDateTime now = LocalDateTime.now();

        apiKey.setId(99L);
        apiKey.setKeyHash("abc123");
        apiKey.setCreatedAt(now);
        apiKey.setExpiresAt(now.plusHours(5));
        apiKey.setUser(null); // Relationsfältet null här

        String toString = apiKey.toString();

        assertThat(toString).contains("abc123");
        assertThat(toString).contains("99");
        assertThat(toString).contains("createdAt");
    }
}