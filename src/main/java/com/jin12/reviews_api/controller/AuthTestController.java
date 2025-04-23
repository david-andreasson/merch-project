package com.jin12.reviews_api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.jin12.reviews_api.model.User;

@RestController
@RequestMapping("/test-auth")
public class AuthTestController {

    @GetMapping
    public ResponseEntity<String> testAccess(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok("✅ Authenticated as: " + user.getUsername());
    }
}