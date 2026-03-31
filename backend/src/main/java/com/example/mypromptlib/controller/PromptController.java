package com.example.mypromptlib.controller;

import com.example.mypromptlib.model.Prompt;
import com.example.mypromptlib.service.PromptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/prompts")
@RequiredArgsConstructor
public class PromptController {

    private final PromptService promptService;

    @GetMapping
    public List<Prompt> getAllPrompts() {
        return promptService.getAllPrompts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Prompt> getPromptById(@PathVariable UUID id) {
        return ResponseEntity.ok(promptService.getPromptById(id));
    }

    @PostMapping
    public ResponseEntity<Prompt> createPrompt(@Valid @RequestBody Prompt prompt) {
        return new ResponseEntity<>(promptService.createPrompt(prompt), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Prompt> updatePrompt(@PathVariable UUID id, @Valid @RequestBody Prompt prompt) {
        return ResponseEntity.ok(promptService.updatePrompt(id, prompt));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePrompt(@PathVariable UUID id) {
        promptService.deletePrompt(id);
        return ResponseEntity.noContent().build();
    }
}
