package com.anushibinj.mypromptlib.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "prompt_versions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromptVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID promptId;

    private int versionNumber;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @JsonProperty("isPublic")
    private boolean isPublic;

    private Instant createdAt;
}
