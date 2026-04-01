package com.anushibinj.mypromptlib.service;

import com.anushibinj.mypromptlib.model.User;
import com.anushibinj.mypromptlib.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User registerUser(String username, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        User user = User.builder()
                .username(username)
                .password(password)
                .token(UUID.randomUUID().toString())
                .build();
        return userRepository.save(user);
    }

    public Optional<User> loginUser(String username, String password) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent() && password != null && password.equals(userOptional.get().getPassword())) {
            User user = userOptional.get();
            // Assign new token
            user.setToken(UUID.randomUUID().toString());
            userRepository.save(user);
            return Optional.of(user);
        }
        return Optional.empty();
    }

    public User loginOrRegisterGoogleUser(String googleId, String email) {
        // Check if user already linked to this Google account
        Optional<User> existingGoogleUser = userRepository.findByGoogleId(googleId);
        if (existingGoogleUser.isPresent()) {
            User user = existingGoogleUser.get();
            user.setToken(UUID.randomUUID().toString());
            return userRepository.save(user);
        }
        // Check if user exists with same email as username — link Google ID
        Optional<User> emailUser = userRepository.findByUsername(email);
        if (emailUser.isPresent()) {
            User user = emailUser.get();
            user.setGoogleId(googleId);
            user.setToken(UUID.randomUUID().toString());
            return userRepository.save(user);
        }
        // Create new Google user
        User newUser = User.builder()
                .username(email)
                .googleId(googleId)
                .token(UUID.randomUUID().toString())
                .build();
        return userRepository.save(newUser);
    }

    public Optional<User> findByToken(String token) {
        if (token == null || token.isBlank()) return Optional.empty();
        return userRepository.findByToken(token);
    }
}
