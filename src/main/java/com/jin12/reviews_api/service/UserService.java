package com.jin12.reviews_api.service;

import com.jin12.reviews_api.Utils.CryptoUtils;
import com.jin12.reviews_api.exception.ApiKeyUpdateException;
import com.jin12.reviews_api.model.User;
import com.jin12.reviews_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    @Value("${master.key}")
    private String masterKey;

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public void updateUserApiKey(Long userId, String apiKey) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiKeyUpdateException("User with id " + userId + " not found"));

        try {
            String encryptedKey = CryptoUtils.encrypt(masterKey, apiKey);
            user.setEncryptedApiKey(encryptedKey);
            userRepository.save(user);
        } catch (Exception e) {
            throw new ApiKeyUpdateException("Failed to encrypt or save API key for user " + userId, e);
        }
    }
}
