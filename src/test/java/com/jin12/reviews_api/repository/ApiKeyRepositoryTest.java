package com.jin12.reviews_api.repository;

import com.jin12.reviews_api.model.ApiKey;
import com.jin12.reviews_api.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ApiKeyRepositoryTest {
    @Autowired
    private ApiKeyRepository apiKeyRepository;

    @Autowired
    private UserRepository userRepository; // Du behöver detta om ApiKey har en relation till User

    @Test
    @DisplayName("Should find ApiKey by keyHash")
    void testFindByKeyHash() {
        // Arrange – spara först en User (eftersom ApiKey kräver det)
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password123");
        user = userRepository.save(user);

        ApiKey apiKey = new ApiKey();
        apiKey.setKeyHash("my-key-hash");
        apiKey.setCreatedAt(LocalDateTime.now());
        apiKey.setExpiresAt(LocalDateTime.now().plusDays(30));
        apiKey.setUser(user);

        apiKeyRepository.save(apiKey);

        // Act
        Optional<ApiKey> result = apiKeyRepository.findByKeyHash("my-key-hash");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getKeyHash()).isEqualTo("my-key-hash");
        assertThat(result.get().getUser().getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Should return empty when keyHash not found")
    void testFindByKeyHash_NotFound() {
        Optional<ApiKey> result = apiKeyRepository.findByKeyHash("non-existent-key");
        assertThat(result).isEmpty();
    }
}