package com.example.mypromptlib.service;

import com.example.mypromptlib.model.Prompt;
import com.example.mypromptlib.repository.PromptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PromptServiceTest {

    @Mock
    private PromptRepository promptRepository;

    @InjectMocks
    private PromptService promptService;

    private Prompt prompt;
    private final UUID id = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        prompt = Prompt.builder()
                .id(id)
                .title("Test Prompt")
                .content("Test Content {{var}}")
                .build();
    }

    @Test
    void testGetAllPrompts() {
        when(promptRepository.findAll()).thenReturn(List.of(prompt));
        List<Prompt> result = promptService.getAllPrompts();
        assertEquals(1, result.size());
        verify(promptRepository).findAll();
    }

    @Test
    void testGetPromptByIdSuccess() {
        when(promptRepository.findById(id)).thenReturn(Optional.of(prompt));
        Prompt result = promptService.getPromptById(id);
        assertNotNull(result);
        assertEquals(id, result.getId());
    }

    @Test
    void testGetPromptByIdNotFound() {
        when(promptRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> promptService.getPromptById(id));
    }

    @Test
    void testCreatePrompt() {
        when(promptRepository.save(any(Prompt.class))).thenReturn(prompt);
        Prompt result = promptService.createPrompt(prompt);
        assertNotNull(result);
        assertEquals(id, result.getId());
    }

    @Test
    void testUpdatePrompt() {
        Prompt updatedDetails = new Prompt(id, "New Title", "New Content");
        when(promptRepository.findById(id)).thenReturn(Optional.of(prompt));
        when(promptRepository.save(any(Prompt.class))).thenReturn(updatedDetails);
        
        Prompt result = promptService.updatePrompt(id, updatedDetails);
        assertEquals("New Title", result.getTitle());
        assertEquals("New Content", result.getContent());
    }

    @Test
    void testDeletePrompt() {
        when(promptRepository.findById(id)).thenReturn(Optional.of(prompt));
        doNothing().when(promptRepository).delete(prompt);
        
        promptService.deletePrompt(id);
        verify(promptRepository).delete(prompt);
    }
}
