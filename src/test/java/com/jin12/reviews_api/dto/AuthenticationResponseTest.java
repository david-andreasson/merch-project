package com.jin12.reviews_api.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AuthenticationResponseTest {
    @Test
    void testGettersAndSetters() {
        AuthenticationResponse dto = new AuthenticationResponse();
        dto.setToken("token");
        assertEquals("token", dto.getToken());
    }

    @Test
    void testEqualsAndHashCode() {
        AuthenticationResponse dto1 = new AuthenticationResponse();
        dto1.setToken("t");
        AuthenticationResponse dto2 = new AuthenticationResponse();
        dto2.setToken("t");
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testToString() {
        AuthenticationResponse dto = new AuthenticationResponse();
        dto.setToken("t");
        assertTrue(dto.toString().contains("t"));
    }
}
