package com.anushibinj.mypromptlib.controller;

import com.anushibinj.mypromptlib.model.Prompt;
import com.anushibinj.mypromptlib.model.PromptVersion;
import com.anushibinj.mypromptlib.model.User;
import com.anushibinj.mypromptlib.service.PromptService;
import com.anushibinj.mypromptlib.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PromptController.class)
public class PromptControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PromptService promptService;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TEST_TOKEN = "test-token";
    private UUID userId;
    private User testUser;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testUser = User.builder()
                .id(userId)
                .username("testuser")
                .password("password")
                .token(TEST_TOKEN)
                .build();
        when(userService.findByToken(TEST_TOKEN)).thenReturn(Optional.of(testUser));
    }

    @Test
    void testGetAllPrompts() throws Exception {
        Prompt prompt = Prompt.builder()
                .id(UUID.randomUUID()).title("T1").content("C1").userId(userId).build();
        when(promptService.getAllPromptsForUser(any(UUID.class))).thenReturn(List.of(prompt));

        mockMvc.perform(get("/api/prompts")
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("T1"));
    }

    @Test
    void testGetPublicPrompts() throws Exception {
        Prompt prompt = Prompt.builder()
                .id(UUID.randomUUID()).title("SharedT").content("SharedC").isPublic(true).build();
        when(promptService.getPublicPrompts()).thenReturn(List.of(prompt));

        mockMvc.perform(get("/api/prompts/shared")
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("SharedT"));
    }

    @Test
    void testGetSharedPromptById() throws Exception {
        UUID id = UUID.randomUUID();
        Prompt prompt = Prompt.builder()
                .id(id).title("SharedT").content("SharedC").isPublic(true).build();
        when(promptService.getSharedPrompt(id)).thenReturn(prompt);

        mockMvc.perform(get("/api/prompts/shared/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("SharedT"));
    }

    @Test
    void testGetPromptById() throws Exception {
        UUID id = UUID.randomUUID();
        Prompt prompt = Prompt.builder()
                .id(id).title("T1").content("C1").userId(userId).build();
        when(promptService.getPromptByIdAndUser(eq(id), any(UUID.class))).thenReturn(prompt);

        mockMvc.perform(get("/api/prompts/{id}", id)
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("T1"));
    }

    @Test
    void testGetPromptById_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(promptService.getPromptByIdAndUser(eq(id), any(UUID.class)))
                .thenThrow(new RuntimeException("Prompt not found"));

        mockMvc.perform(get("/api/prompts/{id}", id)
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Prompt not found"));
    }

    @Test
    void testCreatePrompt() throws Exception {
        UUID id = UUID.randomUUID();
        Prompt payload = Prompt.builder().title("T1").content("C1").build();
        Prompt saved = Prompt.builder().id(id).title("T1").content("C1").userId(userId).build();

        when(promptService.createPrompt(any(Prompt.class), any(UUID.class))).thenReturn(saved);

        mockMvc.perform(post("/api/prompts")
                        .header("Authorization", "Bearer " + TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("T1"));
    }

    @Test
    void testCreatePromptValidationFail() throws Exception {
        Prompt invalid = Prompt.builder().title("").content("").build();

        mockMvc.perform(post("/api/prompts")
                        .header("Authorization", "Bearer " + TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.content").exists());
    }

    @Test
    void testUpdatePrompt() throws Exception {
        UUID id = UUID.randomUUID();
        Prompt payload = Prompt.builder().title("T2").content("C2").build();
        Prompt updated = Prompt.builder().id(id).title("T2").content("C2").userId(userId).build();

        when(promptService.updatePrompt(eq(id), any(Prompt.class), any(UUID.class))).thenReturn(updated);

        mockMvc.perform(put("/api/prompts/{id}", id)
                        .header("Authorization", "Bearer " + TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("T2"));
    }

    @Test
    void testDeletePrompt() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(promptService).deletePrompt(eq(id), any(UUID.class));

        mockMvc.perform(delete("/api/prompts/{id}", id)
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andExpect(status().isNoContent());

        verify(promptService).deletePrompt(eq(id), any(UUID.class));
    }

    @Test
    void testUnauthorizedRequest() throws Exception {
        mockMvc.perform(get("/api/prompts"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testInvalidToken() throws Exception {
        when(userService.findByToken("invalid-token")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/prompts")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetPromptHistory() throws Exception {
        UUID promptId = UUID.randomUUID();
        PromptVersion v1 = PromptVersion.builder()
                .id(UUID.randomUUID()).promptId(promptId).versionNumber(1)
                .title("T1").content("C1").createdAt(Instant.now()).build();
        PromptVersion v2 = PromptVersion.builder()
                .id(UUID.randomUUID()).promptId(promptId).versionNumber(2)
                .title("T2").content("C2").createdAt(Instant.now()).build();
        when(promptService.getPromptHistory(eq(promptId), any(UUID.class)))
                .thenReturn(List.of(v2, v1));

        mockMvc.perform(get("/api/prompts/{id}/history", promptId)
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].versionNumber").value(2))
                .andExpect(jsonPath("$[1].versionNumber").value(1));
    }
}
