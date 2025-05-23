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

@Service
public class ApiKeyService {

    private static final Logger log = LoggerFactory.getLogger(ApiKeyService.class);

    @Value("${master.key}")
    private String masterKey;
    private final ApiKeyRepository apiKeyRepository;

    public ApiKeyService(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    public String getDecryptedApiKey(User user) throws ApiKeyException {
        log.debug("getDecryptedApiKey – start for userId={}", user.getId());
        String encryptedKey = user.getEncryptedApiKey();
        if (encryptedKey == null) {
            log.warn("getDecryptedApiKey – no encrypted key for userId={}", user.getId());
            return null;
        }
        try {
            String decrypted = CryptoUtils.decrypt(masterKey, encryptedKey);
            log.debug("getDecryptedApiKey – decrypted key for userId={}", user.getId());
            return decrypted;
        } catch (Exception e) {
            throw new ApiKeyException("Invalid API key", e);
        }
    }

    public String createApiKey(User user) {
        log.info("createApiKey – generating new API key for userId={}", user.getId());
        String rawKey = generateApiKey();
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

    public Optional<ApiKey> findValidKey(String rawKey) {
        log.debug("findValidKey – start, rawKey present={}", rawKey != null && !rawKey.isBlank());
        if (rawKey == null || rawKey.isBlank()) {
            log.warn("findValidKey – invalid rawKey provided");
            return Optional.empty();
        }
        Optional<ApiKey> result = apiKeyRepository.findAll().stream()
                .filter(k -> k.getExpiresAt().isAfter(LocalDateTime.now()))
                .filter(k -> BCrypt.checkpw(rawKey, k.getKeyHash()))
                .findFirst();
        log.debug("findValidKey – key {}found", result.isPresent() ? "" : "not ");
        return result;
    }

    private String generateApiKey() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        String key = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        log.debug("generateApiKey – generated key of length {}", key.length());
        return key;
    }
}