package com.jin12.reviews_api.service;

import com.jin12.reviews_api.Utils.CryptoUtils;
import com.jin12.reviews_api.model.ApiKey;
import com.jin12.reviews_api.model.User;
import com.jin12.reviews_api.repository.ApiKeyRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
public class ApiKeyService {

    @Value("${master.key}")
    private String masterKey;
    private final ApiKeyRepository apiKeyRepository;

    public ApiKeyService(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    public String getDecryptedApiKey(User user) throws Exception {
        String encryptedKey = user.getEncryptedApiKey();
        if (encryptedKey == null) return null;
        return CryptoUtils.decrypt(masterKey, encryptedKey);
    }

    public String createApiKey(User user) {
        String rawKey = generateApiKey();
        String hashedKey = BCrypt.hashpw(rawKey, BCrypt.gensalt());

        ApiKey apiKey = new ApiKey();
        apiKey.setKeyHash(hashedKey);
        apiKey.setCreatedAt(LocalDateTime.now());
        apiKey.setExpiresAt(LocalDateTime.now().plusMonths(6));
        apiKey.setUser(user);

        apiKeyRepository.save(apiKey);
        return rawKey;
    }

    public Optional<ApiKey> findValidKey(String rawKey) {
        if (rawKey == null || rawKey.isBlank()) return Optional.empty();
        return apiKeyRepository.findAll().stream()
                .filter(k -> k.getExpiresAt().isAfter(LocalDateTime.now()))
                .filter(k -> BCrypt.checkpw(rawKey, k.getKeyHash()))
                .findFirst();
    }

    private String generateApiKey() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}