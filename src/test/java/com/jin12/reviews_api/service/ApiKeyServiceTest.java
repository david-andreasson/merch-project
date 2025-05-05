package com.jin12.reviews_api.service;

import com.jin12.reviews_api.model.ApiKey;
import com.jin12.reviews_api.model.User;
import com.jin12.reviews_api.repository.ApiKeyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApiKeyServiceTest {

    private ApiKeyRepository apiKeyRepository;
    private ApiKeyService apiKeyService;
    private User testUser;

    @BeforeEach
    void setUp() {
        apiKeyRepository = mock(ApiKeyRepository.class);
        apiKeyService = new ApiKeyService(apiKeyRepository);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
    }

    @Test
    void testCreateApiKey() {
        // Act
        String rawKey = apiKeyService.createApiKey(testUser);

        // Assert
        assertNotNull(rawKey);
        verify(apiKeyRepository, times(1)).save(any(ApiKey.class));
    }

    @Test
    void testFindValidKey_ValidKey() {
        String rawKey = "validKey";
        String hashedKey = org.springframework.security.crypto.bcrypt.BCrypt.hashpw(rawKey, org.springframework.security.crypto.bcrypt.BCrypt.gensalt());

        ApiKey validApiKey = new ApiKey();
        validApiKey.setKeyHash(hashedKey);
        validApiKey.setExpiresAt(LocalDateTime.now().plusDays(10));
        validApiKey.setUser(testUser);

        when(apiKeyRepository.findAll()).thenReturn(List.of(validApiKey));

        Optional<ApiKey> result = apiKeyService.findValidKey(rawKey);
        assertTrue(result.isPresent());
        assertEquals(validApiKey, result.get());
    }

    @Test
    void testFindValidKey_InvalidKey() {
        String rawKey = "wrongKey";
        String hashedKey = org.springframework.security.crypto.bcrypt.BCrypt.hashpw("someOtherKey", org.springframework.security.crypto.bcrypt.BCrypt.gensalt());

        ApiKey apiKey = new ApiKey();
        apiKey.setKeyHash(hashedKey);
        apiKey.setExpiresAt(LocalDateTime.now().plusDays(10));
        apiKey.setUser(testUser);

        when(apiKeyRepository.findAll()).thenReturn(List.of(apiKey));

        Optional<ApiKey> result = apiKeyService.findValidKey(rawKey);
        assertFalse(result.isPresent());
    }

    @Test
    void testFindValidKey_KeyExpired() {
        String rawKey = "expiredKey";
        String hashedKey = org.springframework.security.crypto.bcrypt.BCrypt.hashpw(rawKey, org.springframework.security.crypto.bcrypt.BCrypt.gensalt());

        ApiKey expiredApiKey = new ApiKey();
        expiredApiKey.setKeyHash(hashedKey);
        expiredApiKey.setExpiresAt(LocalDateTime.now().minusDays(1));
        expiredApiKey.setUser(testUser);

        when(apiKeyRepository.findAll()).thenReturn(List.of(expiredApiKey));

        Optional<ApiKey> result = apiKeyService.findValidKey(rawKey);
        assertFalse(result.isPresent());
    }

    @Test
    void testFindValidKey_NullOrEmptyKey() {
        Optional<ApiKey> result1 = apiKeyService.findValidKey(null);
        Optional<ApiKey> result2 = apiKeyService.findValidKey("");

        assertTrue(result1.isEmpty());
        assertTrue(result2.isEmpty());
        verify(apiKeyRepository, never()).findAll();
    }
}
