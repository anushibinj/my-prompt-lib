package com.anushibinj.mypromptlib.service;

import com.anushibinj.mypromptlib.model.User;
import com.anushibinj.mypromptlib.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(userId)
                .username("testuser")
                .password("password")
                .token("test-token")
                .build();
    }

    @Test
    void testRegisterUser_success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.registerUser("testuser", "password");
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegisterUser_usernameAlreadyExists() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class, () -> userService.registerUser("testuser", "password"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testLoginUser_success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        Optional<User> result = userService.loginUser("testuser", "password");
        assertTrue(result.isPresent());
        verify(userRepository).save(user);
    }

    @Test
    void testLoginUser_wrongPassword() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        Optional<User> result = userService.loginUser("testuser", "wrongpassword");
        assertFalse(result.isPresent());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testLoginUser_userNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        Optional<User> result = userService.loginUser("unknown", "password");
        assertFalse(result.isPresent());
    }

    @Test
    void testFindByToken_success() {
        when(userRepository.findByToken("test-token")).thenReturn(Optional.of(user));

        Optional<User> result = userService.findByToken("test-token");
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
    }

    @Test
    void testFindByToken_nullToken() {
        Optional<User> result = userService.findByToken(null);
        assertFalse(result.isPresent());
        verify(userRepository, never()).findByToken(any());
    }

    @Test
    void testFindByToken_blankToken() {
        Optional<User> result = userService.findByToken("   ");
        assertFalse(result.isPresent());
        verify(userRepository, never()).findByToken(any());
    }
}
