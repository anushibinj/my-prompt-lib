package com.example.mypromptlib.controller;

import com.example.mypromptlib.exception.GlobalExceptionHandler;
import com.example.mypromptlib.model.Prompt;
import com.example.mypromptlib.service.PromptService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
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

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetAllPrompts() throws Exception {
        Prompt prompt = new Prompt(UUID.randomUUID(), "T1", "C1");
        when(promptService.getAllPrompts()).thenReturn(List.of(prompt));

        mockMvc.perform(get("/api/prompts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("T1"));
    }

    @Test
    void testGetPromptById() throws Exception {
        UUID id = UUID.randomUUID();
        Prompt prompt = new Prompt(id, "T1", "C1");
        when(promptService.getPromptById(id)).thenReturn(prompt);

        mockMvc.perform(get("/api/prompts/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("T1"));
    }

    @Test
    void testCreatePrompt() throws Exception {
        UUID id = UUID.randomUUID();
        Prompt prompt = new Prompt(null, "T1", "C1");
        Prompt savedPrompt = new Prompt(id, "T1", "C1");
        
        when(promptService.createPrompt(any(Prompt.class))).thenReturn(savedPrompt);

        mockMvc.perform(post("/api/prompts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(prompt)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("T1"));
    }

    @Test
    void testCreatePromptValidationFail() throws Exception {
        Prompt invalidPrompt = new Prompt(null, "", "");
        
        mockMvc.perform(post("/api/prompts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidPrompt)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.content").exists());
    }

    @Test
    void testUpdatePrompt() throws Exception {
        UUID id = UUID.randomUUID();
        Prompt prompt = new Prompt(null, "T2", "C2");
        Prompt updatedPrompt = new Prompt(id, "T2", "C2");
        
        when(promptService.updatePrompt(eq(id), any(Prompt.class))).thenReturn(updatedPrompt);

        mockMvc.perform(put("/api/prompts/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(prompt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("T2"));
    }

    @Test
    void testDeletePrompt() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(promptService).deletePrompt(id);

        mockMvc.perform(delete("/api/prompts/{id}", id))
                .andExpect(status().isNoContent());

        verify(promptService).deletePrompt(id);
    }
}
