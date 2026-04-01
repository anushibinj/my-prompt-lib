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

    @Test
    void testLoginUser_googleUserNullPassword() {
        User googleUser = User.builder()
                .id(userId)
                .username("google@example.com")
                .password(null)
                .googleId("google-123")
                .token("test-token")
                .build();
        when(userRepository.findByUsername("google@example.com")).thenReturn(Optional.of(googleUser));

        Optional<User> result = userService.loginUser("google@example.com", "anypassword");
        assertFalse(result.isPresent());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testLoginOrRegisterGoogleUser_existingGoogleUser() {
        when(userRepository.findByGoogleId("google-123")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.loginOrRegisterGoogleUser("google-123", "test@example.com");
        assertNotNull(result);
        verify(userRepository).save(user);
    }

    @Test
    void testLoginOrRegisterGoogleUser_existingUsernameMatchingEmail() {
        when(userRepository.findByGoogleId("google-456")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.loginOrRegisterGoogleUser("google-456", "testuser");
        assertNotNull(result);
        assertEquals("google-456", user.getGoogleId());
        verify(userRepository).save(user);
    }

    @Test
    void testLoginOrRegisterGoogleUser_newUser() {
        when(userRepository.findByGoogleId("google-789")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("new@example.com")).thenReturn(Optional.empty());
        User newUser = User.builder()
                .id(UUID.randomUUID())
                .username("new@example.com")
                .googleId("google-789")
                .token("new-token")
                .build();
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        User result = userService.loginOrRegisterGoogleUser("google-789", "new@example.com");
        assertNotNull(result);
        assertEquals("new@example.com", result.getUsername());
        assertEquals("google-789", result.getGoogleId());
    }
}
