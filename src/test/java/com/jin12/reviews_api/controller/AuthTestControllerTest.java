package com.jin12.reviews_api.controller;

import com.jin12.reviews_api.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthTestControllerTest {
    @Test
    void testAccess_returnsExpectedMessage() {
        AuthTestController controller = new AuthTestController();
        User user = User.builder()
                .username("alice")
                .password("pw")
                .build();
        ResponseEntity<String> resp = controller.testAccess(user);
        assertEquals("✅ Authenticated as: alice", resp.getBody());
    }
}
