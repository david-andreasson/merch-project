package com.jin12.reviews_api.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AuthenticationRequestTest {
    @Test
    void testGettersAndSetters() {
        AuthenticationRequest dto = new AuthenticationRequest();
        dto.setUsername("testuser");
        dto.setPassword("password");
        dto.setApiKey("api-key-123");
        dto.setAuthType("API_KEY");
        assertEquals("testuser", dto.getUsername());
        assertEquals("password", dto.getPassword());
        assertEquals("api-key-123", dto.getApiKey());
        assertEquals("API_KEY", dto.getAuthType());
    }

    @Test
    void testEqualsAndHashCode() {
        AuthenticationRequest dto1 = new AuthenticationRequest();
        dto1.setUsername("a");
        dto1.setPassword("b");
        dto1.setApiKey("key");
        dto1.setAuthType("type");
        AuthenticationRequest dto2 = new AuthenticationRequest();
        dto2.setUsername("a");
        dto2.setPassword("b");
        dto2.setApiKey("key");
        dto2.setAuthType("type");
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testToString() {
        AuthenticationRequest dto = new AuthenticationRequest();
        dto.setUsername("a");
        dto.setPassword("b");
        dto.setApiKey("key");
        dto.setAuthType("type");
        assertTrue(dto.toString().contains("a"));
        assertTrue(dto.toString().contains("key"));
        assertTrue(dto.toString().contains("type"));
    }
}
