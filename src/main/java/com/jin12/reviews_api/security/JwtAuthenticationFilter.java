package com.jin12.reviews_api.security;

import com.jin12.reviews_api.service.JwtService;
import com.jin12.reviews_api.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that intercepts each HTTP request to validate a JWT token.
 * If a valid JWT is found in the Authorization header, this filter
 * sets the corresponding user authentication in the security context.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserService userService;

    /**
     * Filters incoming requests to check the Authorization header for a Bearer JWT.
     * - Skips token validation for /auth/register and /auth/login paths.
     * - Extracts the username from the JWT and validates the token.
     * - If valid, sets the Authentication in the SecurityContextHolder.
     *
     * @param request     the incoming HttpServletRequest
     * @param response    the HttpServletResponse
     * @param filterChain the FilterChain to pass control along if validation is skipped or completed
     * @throws ServletException if an error occurs during filtering
     * @throws IOException      if an I/O error occurs during filtering
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();
        log.debug("doFilterInternal – request path={}", path);

        // Skip JWT validation for registration and login endpoints
        if (path.equals("/auth/register") || path.equals("/auth/login")) {
            log.debug("doFilterInternal – skipping auth for path={}", path);
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        log.debug("doFilterInternal – Authorization header={}", authHeader);

        // If Authorization header is missing or not starting with "Bearer ", continue filter chain without authentication
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("doFilterInternal – missing or invalid Authorization header for path={}", path);
            filterChain.doFilter(request, response);
            return;
        }

        // Extract token and username
        final String jwt = authHeader.substring(7);
        final String username = jwtService.extractUsername(jwt);
        log.debug("doFilterInternal – extracted username={} from JWT", username);

        // If username is present and no authentication is yet set, validate token
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            var userDetails = userService.loadUserByUsername(username);
            if (jwtService.isTokenValid(jwt, userDetails)) {
                log.info("doFilterInternal – JWT is valid, setting authentication for username={}", username);
                var authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
                log.warn("doFilterInternal – JWT is invalid for username={}", username);
            }
        }

        // Continue the filter chain after processing
        filterChain.doFilter(request, response);
    }
}