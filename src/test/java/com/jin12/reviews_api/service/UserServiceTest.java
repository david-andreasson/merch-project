package com.jin12.reviews_api.service;

import com.jin12.reviews_api.Utils.CryptoUtils;
import com.jin12.reviews_api.model.User;
import com.jin12.reviews_api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepository;
    private UserService userService;

    private final String masterKey = "1234567890123456"; // 16 tecken för AES

    @BeforeEach
    void setUp() throws Exception {
        userRepository = mock(UserRepository.class);
        userService = new UserService();

        // Injicera mocken i private-fältet via reflektion
        Field repoField = UserService.class.getDeclaredField("userRepository");
        repoField.setAccessible(true);
        repoField.set(userService, userRepository);

        // Injicera masterKey i private fältet (för @Value)
        Field keyField = UserService.class.getDeclaredField("masterKey");
        keyField.setAccessible(true);
        keyField.set(userService, masterKey);
    }

    @Test
    void testLoadUserByUsername_UserExists() {
        String username = "testuser";
        User mockUser = new User();
        mockUser.setUsername(username);
        mockUser.setPassword("secret");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));

        UserDetails result = userService.loadUserByUsername(username);

        assertNotNull(result);
        assertEquals(username, result.getUsername());
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void testLoadUserByUsername_UserNotFound() {
        String username = "missinguser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername(username));
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void updateUserApiKey_encryptsAndSaves() throws Exception {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setUsername("mockuser");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        String rawApiKey = "my-secret-api-key";

        // Act
        userService.updateUserApiKey(1L, rawApiKey);

        // Assert
        verify(userRepository).save(user);
        assertNotNull(user.getEncryptedApiKey());
        assertNotEquals(rawApiKey, user.getEncryptedApiKey());

        // Dekryptera och verifiera
        String decrypted = CryptoUtils.decrypt(masterKey, user.getEncryptedApiKey());
        assertEquals(rawApiKey, decrypted);
    }
}
