package com.jin12.reviews_api.service;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class JwtServiceTest {

    private JwtService jwtService;
    private final String rawSecret = "supersecretkey12345678901234567890"; // minst 32 tecken
    private final String encodedSecret = Base64.getEncoder().encodeToString(rawSecret.getBytes());
    private final long expiration = 1000 * 60 * 10; // 10 minuter

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(encodedSecret, expiration);

        userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("testuser");
    }

    @Test
    void testGenerateAndValidateToken() {
        String token = jwtService.generateToken(userDetails);
        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void testExtractUsername() {
        String token = jwtService.generateToken(userDetails);
        String username = jwtService.extractUsername(token);
        assertEquals("testuser", username);
    }

    @Test
    void testIsTokenValid_InvalidUsername() {
        String token = jwtService.generateToken(userDetails);

        UserDetails otherUser = mock(UserDetails.class);
        when(otherUser.getUsername()).thenReturn("otheruser");

        assertFalse(jwtService.isTokenValid(token, otherUser));
    }

    @Test
    void testIsTokenExpired_WithExpiredToken() {
        // Skapa en utgången token manuellt, med samma nyckel
        Date issuedAt = new Date(System.currentTimeMillis() - 10_000);  // 10 sekunder sen
        Date expiredAt = new Date(System.currentTimeMillis() - 5_000);  // gick ut för 5 sekunder sen

        SecretKey key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(encodedSecret));

        String expiredToken = Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(issuedAt)
                .expiration(expiredAt)
                .signWith(key)
                .compact();

        // Här tillåter vi en liten fördröjning för att simulera "clock skew"
        assertThrows(ExpiredJwtException.class, () -> jwtService.extractUsername(expiredToken));
    }

    @Test
    void testInvalidToken_ThrowsException() {
        String invalidToken = "not.a.valid.token";
        assertThrows(JwtException.class, () -> jwtService.extractUsername(invalidToken));
    }
}
