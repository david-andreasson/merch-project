package com.jin12.reviews_api.service;

import com.jin12.reviews_api.dto.AuthenticationRequest;
import com.jin12.reviews_api.dto.AuthenticationResponse;
import com.jin12.reviews_api.dto.RegisterRequest;
import com.jin12.reviews_api.exception.BadRequestException;
import com.jin12.reviews_api.model.ApiKey;
import com.jin12.reviews_api.model.User;
import com.jin12.reviews_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * AuthenticationService handles user registration and login logic.
 * Supports two authentication flows:
 * - API_KEY: validates a provided API key and returns a JWT for the associated user.
 * - PASSWORD: authenticates username+password via AuthenticationManager and returns a JWT.
 */
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final ApiKeyService apiKeyService;

    /**
     * Registers a new user by saving to the database with an encoded password,
     * then generates and returns a JWT token.
     *
     * @param request contains username and password for registration
     * @return AuthenticationResponse containing a newly generated JWT
     */
    public AuthenticationResponse register(RegisterRequest request) {
        log.info("register – attempt for username={}", request.getUsername());
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword())) // hash the password
                .build();

        userRepository.save(user);
        log.info("register – user saved username={}", request.getUsername());

        // Generate JWT for the new user
        String token = jwtService.generateToken(user);
        log.debug("register – jwt token generated for username={}", request.getUsername());

        return AuthenticationResponse.builder()
                .token(token)
                .build();
    }

    /**
     * Authenticates a user based on the provided AuthenticationRequest.
     * If authType = "API_KEY", validates the raw API key and issues a JWT.
     * Otherwise, uses the AuthenticationManager to validate username/password and issues a JWT.
     *
     * @param request contains authType, username, password, or apiKey
     * @return AuthenticationResponse containing a generated JWT
     * @throws BadRequestException if credentials are invalid or user not found
     */
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        log.info("authenticate – authType={} identifier={}",
                request.getAuthType(),
                "API_KEY".equalsIgnoreCase(request.getAuthType())
                        ? request.getApiKey()
                        : request.getUsername());

        // API key flow: check for a valid, unexpired API key
        if ("API_KEY".equalsIgnoreCase(request.getAuthType())) {
            Optional<ApiKey> validKey = apiKeyService.findValidKey(request.getApiKey());
            if (validKey.isEmpty()) {
                log.warn("authenticate – invalid API key provided");
                throw new BadRequestException("Invalid API key");
            }
            User user = validKey.get().getUser();
            // Generate JWT for the user associated with the API key
            String token = jwtService.generateToken(user);
            log.info("authenticate – api key authentication succeeded for userId={}", user.getId());
            return AuthenticationResponse.builder().token(token).build();
        }

        // Password flow: authenticate against username and password
        try {
            log.info("authenticate – login attempt username={}", request.getUsername());
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
            log.debug("authenticate – credentials accepted by AuthenticationManager for username={}", request.getUsername());
        } catch (AuthenticationException e) {
            // Thrown if authentication fails (bad credentials)
            throw new BadRequestException("Invalid username or password");
        }

        // Fetch the user entity to generate a JWT
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    log.error("authenticate – user not found username={}", request.getUsername());
                    return new BadRequestException("User not found");
                });

        // Generate JWT after successful authentication
        String token = jwtService.generateToken(user);
        log.info("authenticate – login successful username={}", request.getUsername());
        return AuthenticationResponse.builder()
                .token(token)
                .build();
    }
}