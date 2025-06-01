package com.jin12.reviews_api.config;

import com.jin12.reviews_api.security.JwtAuthenticationFilter;
import com.jin12.reviews_api.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * SecurityConfig sets which endpoints are public and which require a valid JWT.
 * Public endpoints:
 * - POST /auth/register, /auth/login
 * - Swagger UI and API docs (/v3/api-docs/**, /swagger-ui/**, /swagger-ui.html)
 * - Static files and health-check (/health, /register.html, /mockshop.html, /register-success.html, /css/**, /js/**)
 * - OPTIONS requests and GET /test
 * All other requests must include a JWT (stateless sessions).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter;

    @Autowired
    private UserService userService;

    /**
     * Configures the HTTP security for the application, including
     * permitted endpoints, stateless session management, and JWT filter placement.
     *
     * @param http the HttpSecurity to configure
     * @return the configured SecurityFilterChain
     * @throws Exception if an error occurs while configuring security
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("Configuring SecurityFilterChain");
        SecurityFilterChain chain = http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Allow registration and login without authentication
                        .requestMatchers(HttpMethod.POST, "/auth/register", "/auth/login").permitAll()
                        // Allow Swagger UI and API docs
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-ui/index.html"
                        ).permitAll()
                        // Allow health check, test page, and static resources
                        .requestMatchers(HttpMethod.GET,
                                "/health",
                                "/register.html",
                                "/mockshop.html",
                                "/register-success.html",
                                "/css/**",
                                "/js/**"
                        ).permitAll()
                        // Allow all CORS preflight requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Allow simple test endpoint
                        .requestMatchers(HttpMethod.GET, "/test").permitAll()
                        // Require authentication for any other request
                        .anyRequest().authenticated()
                )
                // Do not create HTTP session; each request must carry a valid JWT
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Use custom authentication provider for validating user credentials
                .authenticationProvider(authenticationProvider())
                // Insert JWT filter before the UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
        log.info("SecurityFilterChain configured successfully");
        return chain;
    }

    /**
     * Creates a PasswordEncoder bean using the BCrypt algorithm.
     *
     * @return a BCryptPasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        log.debug("Creating PasswordEncoder bean");
        return new BCryptPasswordEncoder();
    }

    /**
     * Exposes the AuthenticationManager bean to be used for authentication processes.
     *
     * @param config the AuthenticationConfiguration to retrieve the manager from
     * @return the AuthenticationManager instance
     * @throws Exception if the manager cannot be created
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        log.debug("Creating AuthenticationManager bean");
        return config.getAuthenticationManager();
    }

    /**
     * Configures the DaoAuthenticationProvider with the custom UserService
     * and password encoder for validating user credentials.
     *
     * @return a configured DaoAuthenticationProvider
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        log.debug("Creating DaoAuthenticationProvider");
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
}