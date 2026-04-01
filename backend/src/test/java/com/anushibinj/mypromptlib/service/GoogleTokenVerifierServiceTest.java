package com.anushibinj.mypromptlib.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GoogleTokenVerifierServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private GoogleTokenVerifierService service;

    private static final String CLIENT_ID = "test-client-id";

    @BeforeEach
    void setUp() {
        service = new GoogleTokenVerifierService(restTemplate, CLIENT_ID);
    }

    @Test
    void testVerify_success() {
        Map<String, Object> response = new HashMap<>();
        response.put("aud", CLIENT_ID);
        response.put("email", "user@example.com");
        response.put("sub", "google-user-123");

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(response);

        GoogleTokenVerifierService.GoogleUserInfo info = service.verify("valid-token");
        assertEquals("google-user-123", info.getGoogleId());
        assertEquals("user@example.com", info.getEmail());
    }

    @Test
    void testVerify_restCallFails() {
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenThrow(new RestClientException("Connection error"));

        assertThrows(RuntimeException.class, () -> service.verify("bad-token"));
    }

    @Test
    void testVerify_nullResponse() {
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(null);

        assertThrows(RuntimeException.class, () -> service.verify("token"));
    }

    @Test
    void testVerify_wrongAudience() {
        Map<String, Object> response = new HashMap<>();
        response.put("aud", "wrong-client-id");
        response.put("email", "user@example.com");
        response.put("sub", "google-user-123");

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(response);

        assertThrows(RuntimeException.class, () -> service.verify("token"));
    }

    @Test
    void testVerify_missingEmail() {
        Map<String, Object> response = new HashMap<>();
        response.put("aud", CLIENT_ID);
        response.put("sub", "google-user-123");
        // email is missing

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(response);

        assertThrows(RuntimeException.class, () -> service.verify("token"));
    }

    @Test
    void testVerify_missingSub() {
        Map<String, Object> response = new HashMap<>();
        response.put("aud", CLIENT_ID);
        response.put("email", "user@example.com");
        // sub is missing

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(response);

        assertThrows(RuntimeException.class, () -> service.verify("token"));
    }
}
