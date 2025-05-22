package com.jin12.reviews_api.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final String secret;
    private final long expiration;

    public JwtService(@Value("${JWT_SECRET}") String secret,
                      @Value("${JWT_EXPIRATION}") long expiration) {
        this.secret = secret;
        this.expiration = expiration;
        log.info("JwtService initialized with expiration={}ms", expiration);
    }

    public String generateToken(UserDetails userDetails) {
        log.debug("generateToken – start for username={}", userDetails.getUsername());
        SecretKey key = getSigningKey();
        String token = Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
        log.info("generateToken – token generated for username={}, expiresAt={}", userDetails.getUsername(),
                new Date(System.currentTimeMillis() + expiration));
        return token;
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        log.debug("isTokenValid – validating token for username={}", userDetails.getUsername());
        final String username = extractUsername(token);
        boolean valid = (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        log.debug("isTokenValid – validation result for username={}: {}", userDetails.getUsername(), valid);
        return valid;
    }

    public String extractUsername(String token) {
        log.debug("extractUsername – extracting username from token");
        String username = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
        log.debug("extractUsername – extracted username={}", username);
        return username;
    }

    private boolean isTokenExpired(String token) {
        log.debug("isTokenExpired – checking expiration for token");
        Date exp = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();
        boolean expired = exp.before(new Date());
        log.debug("isTokenExpired – token expired={}", expired);
        return expired;
    }

    private SecretKey getSigningKey() {
        log.trace("getSigningKey – decoding secret and generating key");
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);
        log.trace("getSigningKey – key generated");
        return key;
    }
}