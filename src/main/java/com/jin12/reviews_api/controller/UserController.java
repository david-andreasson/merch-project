package com.jin12.reviews_api.controller;

import com.jin12.reviews_api.model.User;
import com.jin12.reviews_api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * UserController handles user-specific operations such as updating the API key.
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    /**
     * Updates the authenticated user's API key.
     * The raw (plaintext) key is received, then encrypted and stored via UserService.
     *
     * @param rawKey the plaintext API key to set for the user
     * @param user   the authenticated user principal
     * @return a ResponseEntity confirming the update
     */
    @PostMapping("/api-key")
    public ResponseEntity<String> setApiKey(@RequestBody String rawKey, @AuthenticationPrincipal User user) {
        userService.updateUserApiKey(user.getId(), rawKey); // Encrypt and save the new API key
        return ResponseEntity.ok("API key updated");
    }
}