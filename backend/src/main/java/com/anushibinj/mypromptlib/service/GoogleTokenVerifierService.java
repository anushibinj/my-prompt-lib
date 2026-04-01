package com.anushibinj.mypromptlib.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class GoogleTokenVerifierService {

    private final RestTemplate restTemplate;
    private final String clientId;

    public GoogleTokenVerifierService(RestTemplate restTemplate, @Value("${google.client-id}") String clientId) {
        this.restTemplate = restTemplate;
        this.clientId = clientId;
    }

    @SuppressWarnings("unchecked")
    public GoogleUserInfo verify(String idToken) {
        String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
        Map<String, Object> response;
        try {
            response = restTemplate.getForObject(url, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify Google token");
        }
        if (response == null) {
            throw new RuntimeException("Invalid Google token");
        }
        String aud = (String) response.get("aud");
        if (!clientId.equals(aud)) {
            throw new RuntimeException("Token not intended for this app");
        }
        String email = (String) response.get("email");
        String sub = (String) response.get("sub");
        if (email == null || sub == null) {
            throw new RuntimeException("Invalid Google token: missing user info");
        }
        return new GoogleUserInfo(sub, email);
    }

    @Data
    @AllArgsConstructor
    public static class GoogleUserInfo {
        private String googleId;
        private String email;
    }
}
