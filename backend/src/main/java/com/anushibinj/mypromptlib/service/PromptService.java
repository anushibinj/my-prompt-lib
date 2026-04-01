package com.anushibinj.mypromptlib.service;

import com.anushibinj.mypromptlib.model.Prompt;
import com.anushibinj.mypromptlib.model.PromptVersion;
import com.anushibinj.mypromptlib.repository.PromptRepository;
import com.anushibinj.mypromptlib.repository.PromptVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PromptService {

    private final PromptRepository promptRepository;
    private final PromptVersionRepository promptVersionRepository;

    public List<Prompt> getAllPromptsForUser(UUID userId) {
        return promptRepository.findByUserId(userId);
    }

    public List<Prompt> getPublicPrompts() {
        return promptRepository.findByIsPublicTrue();
    }

    public Prompt getPromptByIdAndUser(UUID id, UUID userId) {
        Prompt prompt = promptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prompt not found"));
        if (!prompt.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to access this prompt");
        }
        return prompt;
    }

    public Prompt getSharedPrompt(UUID id) {
        Prompt prompt = promptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prompt not found"));
        if (!prompt.isPublic()) {
            throw new RuntimeException("This prompt is not public");
        }
        return prompt;
    }

    @Transactional
    public Prompt createPrompt(Prompt prompt, UUID userId) {
        prompt.setUserId(userId);
        Prompt saved = promptRepository.save(prompt);
        saveVersion(saved);
        return saved;
    }

    @Transactional
    public Prompt updatePrompt(UUID id, Prompt promptDetails, UUID userId) {
        Prompt existing = getPromptByIdAndUser(id, userId);
        existing.setTitle(promptDetails.getTitle());
        existing.setContent(promptDetails.getContent());
        existing.setPublic(promptDetails.isPublic());
        Prompt saved = promptRepository.save(existing);
        saveVersion(saved);
        return saved;
    }

    @Transactional
    public void deletePrompt(UUID id, UUID userId) {
        Prompt existing = getPromptByIdAndUser(id, userId);
        promptVersionRepository.deleteByPromptId(id);
        promptRepository.delete(existing);
    }

    public List<PromptVersion> getPromptHistory(UUID promptId, UUID userId) {
        // Verify ownership
        getPromptByIdAndUser(promptId, userId);
        return promptVersionRepository.findByPromptIdOrderByVersionNumberDesc(promptId);
    }

    private void saveVersion(Prompt prompt) {
        int nextVersion = promptVersionRepository
                .findTopByPromptIdOrderByVersionNumberDesc(prompt.getId())
                .map(v -> v.getVersionNumber() + 1)
                .orElse(1);

        PromptVersion version = PromptVersion.builder()
                .promptId(prompt.getId())
                .versionNumber(nextVersion)
                .title(prompt.getTitle())
                .content(prompt.getContent())
                .isPublic(prompt.isPublic())
                .createdAt(Instant.now())
                .build();
        promptVersionRepository.save(version);
    }
}
