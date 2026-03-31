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

    public List<Prompt> getAllPrompts() {
        return promptRepository.findAll();
    }

    public Prompt getPromptById(UUID id) {
        return promptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prompt not found with id: " + id));
    }

    public Prompt createPrompt(Prompt prompt) {
        return promptRepository.save(prompt);
    }

    public Prompt updatePrompt(UUID id, Prompt promptDetails) {
        Prompt existing = getPromptById(id);
        existing.setTitle(promptDetails.getTitle());
        existing.setContent(promptDetails.getContent());
        return promptRepository.save(existing);
    }

    public void deletePrompt(UUID id) {
        Prompt existing = getPromptById(id);
        promptRepository.delete(existing);
    }
}
