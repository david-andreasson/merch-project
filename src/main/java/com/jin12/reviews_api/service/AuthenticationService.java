package com.jin12.reviews_api.service;

import com.jin12.reviews_api.dto.AuthenticationRequest;
import com.jin12.reviews_api.dto.AuthenticationResponse;
import com.jin12.reviews_api.dto.RegisterRequest;
import com.jin12.reviews_api.model.ApiKey;
import com.jin12.reviews_api.model.User;
import com.jin12.reviews_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final ApiKeyService apiKeyService;

    public AuthenticationResponse register(RegisterRequest request) {
        log.info("register – attempt for username={}", request.getUsername());
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);
        log.info("register – user saved username={}", request.getUsername());
        String token = jwtService.generateToken(user);
        log.debug("register – jwt token generated for username={}", request.getUsername());
        return AuthenticationResponse.builder()
                .token(token)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        log.info("authenticate – authType={} identifier={}", request.getAuthType(),
                "API_KEY".equalsIgnoreCase(request.getAuthType()) ? request.getApiKey() : request.getUsername());

        if ("API_KEY".equalsIgnoreCase(request.getAuthType())) {
            Optional<ApiKey> validKey = apiKeyService.findValidKey(request.getApiKey());
            if (validKey.isEmpty()) {
                log.warn("authenticate – invalid API key provided");
                throw new BadCredentialsException("Invalid API key");
            }
            User user = validKey.get().getUser();
            String token = jwtService.generateToken(user);
            log.info("authenticate – api key authentication succeeded for userId={}", user.getId());
            return AuthenticationResponse.builder().token(token).build();
        }

        log.info("authenticate – login attempt username={}", request.getUsername());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        log.debug("authenticate – credentials accepted by AuthenticationManager for username={}", request.getUsername());

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    log.error("authenticate – user not found username={}", request.getUsername());
                    return new UsernameNotFoundException("User not found");
                });

        String token = jwtService.generateToken(user);
        log.info("authenticate – login successful username={}", request.getUsername());
        return AuthenticationResponse.builder()
                .token(token)
                .build();
    }
}