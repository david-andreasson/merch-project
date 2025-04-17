package com.jin12.reviews_api.controller;

import com.jin12.reviews_api.dto.AuthenticationRequest;
import com.jin12.reviews_api.dto.AuthenticationResponse;
import com.jin12.reviews_api.dto.RegisterRequest;
import com.jin12.reviews_api.service.AuthenticationService;
import com.jin12.reviews_api.service.ApiKeyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;
    private final ApiKeyService apiKeyService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request) {
        if ("API_KEY".equalsIgnoreCase(request.getAuthType())) {
            String apiKey = apiKeyService.createApiKey();
            return ResponseEntity.ok(AuthenticationResponse.builder()
                    .apiKey(apiKey)
                    .build());
        } else {
            AuthenticationResponse response = authenticationService.register(request);
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        AuthenticationResponse response = authenticationService.authenticate(request);
        return ResponseEntity.ok(response);
    }
}