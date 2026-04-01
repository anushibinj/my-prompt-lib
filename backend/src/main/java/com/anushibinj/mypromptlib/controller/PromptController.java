package com.anushibinj.mypromptlib.controller;

import com.anushibinj.mypromptlib.model.Prompt;
import com.anushibinj.mypromptlib.service.PromptService;
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
    public List<Prompt> getAllPrompts(jakarta.servlet.http.HttpServletRequest request) {
        com.anushibinj.mypromptlib.model.User user = (com.anushibinj.mypromptlib.model.User) request.getAttribute("user");
        return promptService.getAllPromptsForUser(user.getId());
    }

    @GetMapping("/shared")
    public List<Prompt> getPublicPrompts() {
        return promptService.getPublicPrompts();
    }

    @GetMapping("/shared/{id}")
    public ResponseEntity<Prompt> getSharedPrompt(@PathVariable UUID id) {
        return ResponseEntity.ok(promptService.getSharedPrompt(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Prompt> getPromptById(@PathVariable UUID id, jakarta.servlet.http.HttpServletRequest request) {
        com.anushibinj.mypromptlib.model.User user = (com.anushibinj.mypromptlib.model.User) request.getAttribute("user");
        return ResponseEntity.ok(promptService.getPromptByIdAndUser(id, user.getId()));
    }

    @PostMapping
    public ResponseEntity<Prompt> createPrompt(@Valid @RequestBody Prompt prompt, jakarta.servlet.http.HttpServletRequest request) {
        com.anushibinj.mypromptlib.model.User user = (com.anushibinj.mypromptlib.model.User) request.getAttribute("user");
        return new ResponseEntity<>(promptService.createPrompt(prompt, user.getId()), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Prompt> updatePrompt(@PathVariable UUID id, @Valid @RequestBody Prompt prompt, jakarta.servlet.http.HttpServletRequest request) {
        com.anushibinj.mypromptlib.model.User user = (com.anushibinj.mypromptlib.model.User) request.getAttribute("user");
        return ResponseEntity.ok(promptService.updatePrompt(id, prompt, user.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePrompt(@PathVariable UUID id, jakarta.servlet.http.HttpServletRequest request) {
        com.anushibinj.mypromptlib.model.User user = (com.anushibinj.mypromptlib.model.User) request.getAttribute("user");
        promptService.deletePrompt(id, user.getId());
        return ResponseEntity.noContent().build();
    }
}
