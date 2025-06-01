package com.jin12.reviews_api.service;

import com.jin12.reviews_api.Utils.CryptoUtils;
import com.jin12.reviews_api.exception.ApiKeyException;
import com.jin12.reviews_api.model.ApiKey;
import com.jin12.reviews_api.model.User;
import com.jin12.reviews_api.repository.ApiKeyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

/**
 * Service for managing user API keys.
 * Handles generation, hashing, storage, and decryption of API keys.
 */
@Service
public class ApiKeyService {

    private static final Logger log = LoggerFactory.getLogger(ApiKeyService.class);

    @Value("${master.key}")
    private String masterKey;
    private final ApiKeyRepository apiKeyRepository;

    public ApiKeyService(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    /**
     * Decrypts the stored encrypted API key for a given user.
     *
     * @param user the user whose API key to decrypt
     * @return the decrypted API key, or null if none exists
     * @throws ApiKeyException if decryption fails
     */
    public String getDecryptedApiKey(User user) throws ApiKeyException {
        log.debug("getDecryptedApiKey – start for userId={}", user.getId());
        String encryptedKey = user.getEncryptedApiKey();
        if (encryptedKey == null) {
            log.warn("getDecryptedApiKey – no encrypted key for userId={}", user.getId());
            return null;
        }
        try {
            // Decrypt using masterKey from configuration
            String decrypted = CryptoUtils.decrypt(masterKey, encryptedKey);
            log.debug("getDecryptedApiKey – decrypted key for userId={}", user.getId());
            return decrypted;
        } catch (Exception e) {
            // Wrap any exception in ApiKeyException to signal invalid key
            throw new ApiKeyException("Invalid API key", e);
        }
    }

    /**
     * Generates a new API key for the user, hashes it, and saves it to the database.
     * The raw (plaintext) key is returned so it can be given to the user.
     *
     * @param user the user for whom to create an API key
     * @return the raw (plaintext) API key
     */
    public String createApiKey(User user) {
        log.info("createApiKey – generating new API key for userId={}", user.getId());
        // Generate a secure random key string
        String rawKey = generateApiKey();
        // Hash the plaintext key before storing
        String hashedKey = BCrypt.hashpw(rawKey, BCrypt.gensalt());

        ApiKey apiKey = new ApiKey();
        apiKey.setKeyHash(hashedKey);
        apiKey.setCreatedAt(LocalDateTime.now());
        apiKey.setExpiresAt(LocalDateTime.now().plusMonths(6));
        apiKey.setUser(user);

        apiKeyRepository.save(apiKey);
        log.info("createApiKey – saved ApiKey id={} for userId={}, expiresAt={}",
                apiKey.getId(), user.getId(), apiKey.getExpiresAt());
        return rawKey;
    }

    /**
     * Finds a valid (not expired) API key entity matching the provided raw key.
     *
     * @param rawKey the plaintext API key to validate
     * @return an Optional containing the matching ApiKey if valid, or empty if none found
     */
    public Optional<ApiKey> findValidKey(String rawKey) {
        log.debug("findValidKey – start, rawKey present={}", rawKey != null && !rawKey.isBlank());
        if (rawKey == null || rawKey.isBlank()) {
            log.warn("findValidKey – invalid rawKey provided");
            return Optional.empty();
        }
        // Stream through stored keys, filter out expired, then check hash match
        Optional<ApiKey> result = apiKeyRepository.findAll().stream()
                .filter(k -> k.getExpiresAt().isAfter(LocalDateTime.now()))
                .filter(k -> BCrypt.checkpw(rawKey, k.getKeyHash()))
                .findFirst();
        log.debug("findValidKey – key {}found", result.isPresent() ? "" : "not ");
        return result;
    }

    /**
     * Generates a secure random API key string.
     * Uses 32 bytes of randomness, then encodes to URL-safe Base64 without padding.
     *
     * @return a new random API key string
     */
    private String generateApiKey() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes); // Fill with cryptographically strong random bytes
        String key = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        log.debug("generateApiKey – generated key of length {}", key.length());
        return key;
    }
}