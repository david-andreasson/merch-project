package com.jin12.reviews_api.controller;

import com.jin12.reviews_api.dto.AuthenticationRequest;
import com.jin12.reviews_api.dto.AuthenticationResponse;
import com.jin12.reviews_api.dto.RegisterRequest;
import com.jin12.reviews_api.exception.BadRequestException;
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

/**
 * AuthController handles user registration and login.
 * Supports two authentication flows:
 * - API_KEY: creates a new user and returns a generated API key.
 * - PASSWORD: delegates to AuthenticationService to register/login with JWT.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationService authenticationService;
    private final ApiKeyService apiKeyService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Registers a new user based on the authType in the request.
     * If authType = "API_KEY", create user, generate and return API key.
     * Otherwise, delegate to AuthenticationService for JWT registration.
     *
     * @param request contains username, password, and authType (API_KEY or PASSWORD)
     * @return AuthenticationResponse with either apiKey or JWT token
     * @throws BadRequestException if username already exists
     */
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request) {
        log.info("register – authType={} username={}", request.getAuthType(), request.getUsername());

        // Check if username is already taken
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new BadRequestException("Username already exists");
        }

        // If client requests API_KEY flow, create user and generate API key
        if ("API_KEY".equalsIgnoreCase(request.getAuthType())) {
            User user = User.builder()
                    .username(request.getUsername())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .build();
            userRepository.save(user);
            log.info("register – new user saved username={}", request.getUsername());

            // Generate and return API key
            String apiKey = apiKeyService.createApiKey(user);
            log.info("register – apiKey created for username={}", request.getUsername());

            AuthenticationResponse response = AuthenticationResponse.builder()
                    .apiKey(apiKey)
                    .build();
            log.debug("register – returning API_KEY response for username={}", request.getUsername());
            return ResponseEntity.ok(response);

        } else {
            // Otherwise, use password-based flow via AuthenticationService (returns JWT)
            log.info("register – delegate to AuthenticationService for username={}", request.getUsername());
            AuthenticationResponse response = authenticationService.register(request);
            log.debug("register – returning register response for username={}", request.getUsername());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Authenticates a user based on the provided credentials.
     * Delegates to AuthenticationService, which handles both API_KEY and PASSWORD flows.
     *
     * @param request contains username, password, and optional apiKey
     * @return AuthenticationResponse with JWT token or API key confirmation
     */
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        log.info("authenticate – login attempt username={}", request.getUsername());
        AuthenticationResponse response = authenticationService.authenticate(request);
        log.info("authenticate – login successful username={}", request.getUsername());
        return ResponseEntity.ok(response);
    }
}