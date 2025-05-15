package com.jin12.reviews_api.repository;

import com.jin12.reviews_api.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should find user by username")
    void testFindByUsername() {
        // Arrange
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("securePassword123"); // lösenord är irrelevanta i testet
        user = userRepository.save(user);

        // Act
        Optional<User> result = userRepository.findByUsername("testuser");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Should return empty when username not found")
    void testFindByUsername_NotFound() {
        Optional<User> result = userRepository.findByUsername("nonexistent");
        assertThat(result).isEmpty();
    }
}