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

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    @PostMapping("/api-key")
    public ResponseEntity<String> setApiKey(@RequestBody String rawKey, @AuthenticationPrincipal User user) {
            userService.updateUserApiKey(user.getId(), rawKey);
            return ResponseEntity.ok("API key updated");
    }
}
