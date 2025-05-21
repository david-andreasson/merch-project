package com.jin12.reviews_api.service;

import com.jin12.reviews_api.Utils.CryptoUtils;
import com.jin12.reviews_api.model.User;
import com.jin12.reviews_api.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Value("${master.key}")
    private String masterKey;

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("loadUserByUsername – attempt for username={}", username);
        UserDetails user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("loadUserByUsername – user not found username={}", username);
                    return new UsernameNotFoundException("User not found");
                });
        log.debug("loadUserByUsername – found user username={}", username);
        return user;
    }

    public void updateUserApiKey(Long userId, String apiKey) throws Exception {
        log.info("updateUserApiKey – start for userId={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("updateUserApiKey – user not found userId={}", userId);
                    return new IllegalArgumentException("User with id " + userId + " not found");
                });
        String encryptedKey = CryptoUtils.encrypt(masterKey, apiKey);
        user.setEncryptedApiKey(encryptedKey);
        userRepository.save(user);
        log.info("updateUserApiKey – updated encrypted API key for userId={}", userId);
    }
}