package com.anushibinj.mypromptlib.controller;

import com.anushibinj.mypromptlib.model.User;
import com.anushibinj.mypromptlib.service.UserService;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @Data
    public static class AuthResponse {
        private String token;
        private String username;
        public AuthResponse(String token, String username) {
            this.token = token;
            this.username = username;
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody AuthRequest request) {
        try {
            User user = userService.registerUser(request.getUsername(), request.getPassword());
            return new ResponseEntity<>(new AuthResponse(user.getToken(), user.getUsername()), HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {
        Optional<User> user = userService.loginUser(request.getUsername(), request.getPassword());
        if (user.isPresent()) {
            return ResponseEntity.ok(new AuthResponse(user.get().getToken(), user.get().getUsername()));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
    }
}
