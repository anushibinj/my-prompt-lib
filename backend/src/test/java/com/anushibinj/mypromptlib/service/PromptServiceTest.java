package com.anushibinj.mypromptlib.service;

import com.anushibinj.mypromptlib.model.Prompt;
import com.anushibinj.mypromptlib.model.PromptVersion;
import com.anushibinj.mypromptlib.repository.PromptRepository;
import com.anushibinj.mypromptlib.repository.PromptVersionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
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

    @Mock
    private PromptVersionRepository promptVersionRepository;

    @InjectMocks
    private PromptService promptService;

    private Prompt prompt;
    private final UUID id = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        prompt = Prompt.builder()
                .id(id)
                .title("Test Prompt")
                .content("Test Content {{var}}")
                .userId(userId)
                .build();
    }

    @Test
    void testGetAllPromptsForUser() {
        when(promptRepository.findByUserId(userId)).thenReturn(List.of(prompt));
        List<Prompt> result = promptService.getAllPromptsForUser(userId);
        assertEquals(1, result.size());
        verify(promptRepository).findByUserId(userId);
    }

    @Test
    void testGetPublicPrompts() {
        when(promptRepository.findByIsPublicTrue()).thenReturn(List.of(prompt));
        List<Prompt> result = promptService.getPublicPrompts();
        assertEquals(1, result.size());
        verify(promptRepository).findByIsPublicTrue();
    }

    @Test
    void testGetPromptByIdAndUserSuccess() {
        when(promptRepository.findById(id)).thenReturn(Optional.of(prompt));
        Prompt result = promptService.getPromptByIdAndUser(id, userId);
        assertNotNull(result);
        assertEquals(id, result.getId());
    }

    @Test
    void testGetPromptByIdAndUserNotFound() {
        when(promptRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> promptService.getPromptByIdAndUser(id, userId));
    }

    @Test
    void testGetPromptByIdAndUserUnauthorized() {
        when(promptRepository.findById(id)).thenReturn(Optional.of(prompt));
        UUID otherUserId = UUID.randomUUID();
        assertThrows(RuntimeException.class, () -> promptService.getPromptByIdAndUser(id, otherUserId));
    }

    @Test
    void testGetSharedPromptSuccess() {
        Prompt publicPrompt = Prompt.builder()
                .id(id).title("T").content("C").userId(userId).isPublic(true).build();
        when(promptRepository.findById(id)).thenReturn(Optional.of(publicPrompt));
        Prompt result = promptService.getSharedPrompt(id);
        assertNotNull(result);
    }

    @Test
    void testGetSharedPromptNotPublic() {
        Prompt privatePrompt = Prompt.builder()
                .id(id).title("T").content("C").userId(userId).isPublic(false).build();
        when(promptRepository.findById(id)).thenReturn(Optional.of(privatePrompt));
        assertThrows(RuntimeException.class, () -> promptService.getSharedPrompt(id));
    }

    @Test
    void testGetSharedPromptNotFound() {
        when(promptRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> promptService.getSharedPrompt(id));
    }

    @Test
    void testCreatePrompt() {
        when(promptRepository.save(any(Prompt.class))).thenReturn(prompt);
        when(promptVersionRepository.findTopByPromptIdOrderByVersionNumberDesc(id)).thenReturn(Optional.empty());
        when(promptVersionRepository.save(any(PromptVersion.class))).thenReturn(PromptVersion.builder().build());

        Prompt result = promptService.createPrompt(prompt, userId);
        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(promptRepository).save(prompt);
        verify(promptVersionRepository).save(any(PromptVersion.class));
    }

    @Test
    void testUpdatePrompt() {
        Prompt updatedDetails = Prompt.builder()
                .id(id).title("New Title").content("New Content").userId(userId).build();
        when(promptRepository.findById(id)).thenReturn(Optional.of(prompt));
        when(promptRepository.save(any(Prompt.class))).thenReturn(updatedDetails);
        when(promptVersionRepository.findTopByPromptIdOrderByVersionNumberDesc(id))
                .thenReturn(Optional.of(PromptVersion.builder().versionNumber(1).build()));
        when(promptVersionRepository.save(any(PromptVersion.class))).thenReturn(PromptVersion.builder().build());

        Prompt result = promptService.updatePrompt(id, updatedDetails, userId);
        assertEquals("New Title", result.getTitle());
        assertEquals("New Content", result.getContent());
        verify(promptVersionRepository).save(argThat(v -> v.getVersionNumber() == 2));
    }

    @Test
    void testDeletePrompt() {
        when(promptRepository.findById(id)).thenReturn(Optional.of(prompt));
        doNothing().when(promptVersionRepository).deleteByPromptId(id);
        doNothing().when(promptRepository).delete(prompt);

        promptService.deletePrompt(id, userId);
        verify(promptVersionRepository).deleteByPromptId(id);
        verify(promptRepository).delete(prompt);
    }

    @Test
    void testGetPromptHistory() {
        when(promptRepository.findById(id)).thenReturn(Optional.of(prompt));
        PromptVersion v1 = PromptVersion.builder()
                .id(UUID.randomUUID()).promptId(id).versionNumber(1)
                .title("T").content("C").createdAt(Instant.now()).build();
        PromptVersion v2 = PromptVersion.builder()
                .id(UUID.randomUUID()).promptId(id).versionNumber(2)
                .title("T2").content("C2").createdAt(Instant.now()).build();
        when(promptVersionRepository.findByPromptIdOrderByVersionNumberDesc(id))
                .thenReturn(List.of(v2, v1));

        List<PromptVersion> result = promptService.getPromptHistory(id, userId);
        assertEquals(2, result.size());
        assertEquals(2, result.get(0).getVersionNumber());
    }

    @Test
    void testGetPromptHistoryUnauthorized() {
        when(promptRepository.findById(id)).thenReturn(Optional.of(prompt));
        UUID otherUserId = UUID.randomUUID();
        assertThrows(RuntimeException.class, () -> promptService.getPromptHistory(id, otherUserId));
    }
}
