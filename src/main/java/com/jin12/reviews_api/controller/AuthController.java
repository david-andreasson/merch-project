package com.jin12.reviews_api.controller;

import com.jin12.reviews_api.dto.AuthenticationRequest;
import com.jin12.reviews_api.dto.AuthenticationResponse;
import com.jin12.reviews_api.dto.RegisterRequest;
import com.jin12.reviews_api.model.User;
import com.jin12.reviews_api.repository.UserRepository;
import com.jin12.reviews_api.service.ApiKeyService;
import com.jin12.reviews_api.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationService authenticationService;
    private final ApiKeyService apiKeyService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request) {
        log.info("register – authType={} username={}", request.getAuthType(), request.getUsername());
        if ("API_KEY".equalsIgnoreCase(request.getAuthType())) {
            User user = User.builder()
                    .username(request.getUsername())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .build();
            userRepository.save(user);
            log.info("register – new user saved username={}", request.getUsername());
            String apiKey = apiKeyService.createApiKey(user);
            log.info("register – apiKey created for username={}", request.getUsername());
            AuthenticationResponse response = AuthenticationResponse.builder()
                    .apiKey(apiKey)
                    .build();
            log.debug("register – returning API_KEY response for username={}", request.getUsername());
            return ResponseEntity.ok(response);
        } else {
            log.info("register – delegate to AuthenticationService for username={}", request.getUsername());
            AuthenticationResponse response = authenticationService.register(request);
            log.debug("register – returning register response for username={}", request.getUsername());
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        log.info("authenticate – login attempt username={}", request.getUsername());
        AuthenticationResponse response = authenticationService.authenticate(request);
        log.info("authenticate – login successful username={}", request.getUsername());
        return ResponseEntity.ok(response);
    }
}