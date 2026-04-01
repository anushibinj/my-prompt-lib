package com.anushibinj.mypromptlib.controller;

import com.anushibinj.mypromptlib.model.User;
import com.anushibinj.mypromptlib.service.GoogleTokenVerifierService;
import com.anushibinj.mypromptlib.service.UserService;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final GoogleTokenVerifierService googleTokenVerifierService;

    @Value("${google.client-id}")
    private String googleClientId;

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

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@Valid @RequestBody GoogleAuthRequest request) {
        try {
            GoogleTokenVerifierService.GoogleUserInfo info = googleTokenVerifierService.verify(request.getCredential());
            User user = userService.loginOrRegisterGoogleUser(info.getGoogleId(), info.getEmail());
            return ResponseEntity.ok(new AuthResponse(user.getToken(), user.getUsername()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Google authentication failed");
        }
    }

    @GetMapping("/google-client-id")
    public ResponseEntity<Map<String, String>> getGoogleClientId() {
        return ResponseEntity.ok(Map.of("clientId", googleClientId));
    }
}
