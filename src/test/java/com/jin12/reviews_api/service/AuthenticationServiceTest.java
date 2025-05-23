package com.jin12.reviews_api.service;

import com.jin12.reviews_api.dto.AuthenticationRequest;
import com.jin12.reviews_api.dto.AuthenticationResponse;
import com.jin12.reviews_api.dto.RegisterRequest;
import com.jin12.reviews_api.exception.BadRequestException;
import com.jin12.reviews_api.model.ApiKey;
import com.jin12.reviews_api.model.User;
import com.jin12.reviews_api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private ApiKeyService apiKeyService;

    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authenticationService = new AuthenticationService(userRepository, passwordEncoder, jwtService, authenticationManager, apiKeyService);
    }

    @Test
    void testRegister_Success() {
        // Mock the request and user repository
        RegisterRequest request = new RegisterRequest("testuser", "password123", "USERNAME_PASSWORD");
        User user = User.builder()
                .username("testuser")
                .password("encodedpassword")
                .build();

        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedpassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn("jwtToken");

        AuthenticationResponse response = authenticationService.register(request);

        assertNotNull(response);
        assertEquals("jwtToken", response.getToken());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testAuthenticate_WithUsernamePassword_Success() {
        // Prepare AuthenticationRequest
        AuthenticationRequest request = new AuthenticationRequest("testuser", "password123", null, "USERNAME_PASSWORD");
        User user = User.builder()
                .username("testuser")
                .password("encodedpassword")
                .build();
        when(userRepository.findByUsername(request.getUsername())).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwtToken");

        AuthenticationResponse response = authenticationService.authenticate(request);

        assertNotNull(response);
        assertEquals("jwtToken", response.getToken());
    }

    @Test
    void testAuthenticate_WithInvalidUsernamePassword_Failure() {
        AuthenticationRequest request = new AuthenticationRequest();
        request.setUsername("invalidUser");
        request.setPassword("invalidPassword");

        // Mocka authenticationManager så att den kastar ett BadCredentialsException
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Verifiera att ett BadCredentialsException kastas
        assertThrows(BadRequestException.class, () -> {
            authenticationService.authenticate(request);
        });
    }

    @Test
    void testApiKeyAuthentication() {
        // Förbered användaren och API-nyckeln
        User user = User.builder().username("testuser").password("encodedpassword").build();

        ApiKey apiKey = new ApiKey();
        apiKey.setKeyHash("valid-api-key");  // Här sätter vi det hashade värdet
        apiKey.setCreatedAt(LocalDateTime.now());
        apiKey.setExpiresAt(LocalDateTime.now().plusHours(1)); // API-nyckeln är giltig i en timme
        apiKey.setUser(user); // Koppla användaren till API-nyckeln

        // Mocka att apiKeyService returnerar en giltig nyckel
        when(apiKeyService.findValidKey("valid-api-key")).thenReturn(Optional.of(apiKey));

        // Simulera API-key autentisering
        AuthenticationRequest request = new AuthenticationRequest();
        request.setAuthType("API_KEY");
        request.setApiKey("valid-api-key");

        // Mocka JWT-token generering
        when(jwtService.generateToken(user)).thenReturn("mocked-jwt-token");

        AuthenticationResponse response = authenticationService.authenticate(request);

        // Verifiera resultatet
        assertNotNull(response);
        assertEquals("mocked-jwt-token", response.getToken()); // Verifiera att rätt token genereras
        verify(apiKeyService, times(1)).findValidKey("valid-api-key"); // Verifiera att findValidKey() anropas en gång
    }

    @Test
    void testAuthenticate_WithInvalidApiKey_Failure() {
        // Prepare AuthenticationRequest for API key authentication
        AuthenticationRequest request = new AuthenticationRequest(null, null, "invalid-api-key", "API_KEY");
        when(apiKeyService.findValidKey(request.getApiKey())).thenReturn(java.util.Optional.empty());

        assertThrows(BadRequestException.class, () -> authenticationService.authenticate(request));
    }

    @Test
    void testAuthenticate_WithUsernamePassword_UserNotFound() {
        // Prepare AuthenticationRequest
        AuthenticationRequest request = new AuthenticationRequest("nonexistentuser", "password123", null, "USERNAME_PASSWORD");
        when(userRepository.findByUsername(request.getUsername())).thenReturn(java.util.Optional.empty());

        assertThrows(BadRequestException.class, () -> authenticationService.authenticate(request));
    }
}
