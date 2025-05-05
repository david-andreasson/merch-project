package com.jin12.reviews_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jin12.reviews_api.dto.AuthenticationRequest;
import com.jin12.reviews_api.dto.AuthenticationResponse;
import com.jin12.reviews_api.dto.RegisterRequest;
import com.jin12.reviews_api.model.User;
import com.jin12.reviews_api.repository.UserRepository;
import com.jin12.reviews_api.service.ApiKeyService;
import com.jin12.reviews_api.service.AuthenticationService;
import com.jin12.reviews_api.service.JwtService;
import com.jin12.reviews_api.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {
    private MockMvc mvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Mock private AuthenticationService authenticationService;
    @Mock private ApiKeyService apiKeyService;
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private UserService userService;
    @InjectMocks private AuthController controller;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("POST /auth/register med authType=API_KEY ska spara user och returnera apiKey")
    void register_withApiKey_generatesAndReturnsApiKey() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("alice");
        req.setPassword("plaintext");
        req.setAuthType("API_KEY");
        User savedUser = User.builder().id(42L).username("alice").password("encodedPwd").build();
        when(passwordEncoder.encode("plaintext")).thenReturn("encodedPwd");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(apiKeyService.createApiKey(any(User.class))).thenReturn("the-api-key-123");

        mvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apiKey").value("the-api-key-123"));
    }

    @Test
    @DisplayName("POST /auth/register med annat authType ska anropa authenticationService.register")
    void register_withNonApiKey_delegatesToService() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("bob");
        req.setPassword("pw");
        req.setAuthType("STANDARD");
        AuthenticationResponse resp = AuthenticationResponse.builder().token("jwt-token-xyz").build();
        when(authenticationService.register(any(RegisterRequest.class))).thenReturn(resp);

        mvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-xyz"));
    }

    @Test
    @DisplayName("POST /auth/login ska anropa authenticationService.authenticate och returnera korrekt svar")
    void authenticate_delegatesToService() throws Exception {
        AuthenticationRequest req = new AuthenticationRequest();
        req.setUsername("charlie");
        req.setPassword("secret");
        req.setAuthType("PASSWORD");
        AuthenticationResponse resp = AuthenticationResponse.builder().token("login-jwt-456").build();
        when(authenticationService.authenticate(any(AuthenticationRequest.class))).thenReturn(resp);

        mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("login-jwt-456"));
    }
}
