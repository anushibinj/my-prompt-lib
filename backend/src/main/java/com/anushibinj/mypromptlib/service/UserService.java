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
        if (userOptional.isPresent() && userOptional.get().getPassword().equals(password)) {
            User user = userOptional.get();
            // Assign new token
            user.setToken(UUID.randomUUID().toString());
            userRepository.save(user);
            return Optional.of(user);
        }
        return Optional.empty();
    }

    public Optional<User> findByToken(String token) {
        if (token == null || token.isBlank()) return Optional.empty();
        return userRepository.findByToken(token);
    }
}
