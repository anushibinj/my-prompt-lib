package com.anushibinj.mypromptlib.repository;

import com.anushibinj.mypromptlib.model.PromptVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PromptVersionRepository extends JpaRepository<PromptVersion, UUID> {
    List<PromptVersion> findByPromptIdOrderByVersionNumberDesc(UUID promptId);

    Optional<PromptVersion> findTopByPromptIdOrderByVersionNumberDesc(UUID promptId);

    void deleteByPromptId(UUID promptId);
}
