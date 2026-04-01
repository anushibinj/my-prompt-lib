package com.example.mypromptlib.service;

import com.example.mypromptlib.model.Prompt;
import com.example.mypromptlib.repository.PromptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PromptService {

    private final PromptRepository promptRepository;

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

    public Prompt createPrompt(Prompt prompt, UUID userId) {
        prompt.setUserId(userId);
        return promptRepository.save(prompt);
    }

    public Prompt updatePrompt(UUID id, Prompt promptDetails, UUID userId) {
        Prompt existing = getPromptByIdAndUser(id, userId);
        existing.setTitle(promptDetails.getTitle());
        existing.setContent(promptDetails.getContent());
        existing.setPublic(promptDetails.isPublic());
        return promptRepository.save(existing);
    }

    public void deletePrompt(UUID id, UUID userId) {
        Prompt existing = getPromptByIdAndUser(id, userId);
        promptRepository.delete(existing);
    }
}
