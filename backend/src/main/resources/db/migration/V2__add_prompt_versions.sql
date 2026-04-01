-- V2: Add prompt_versions table for version history

CREATE TABLE prompt_versions (
    id UUID NOT NULL,
    prompt_id UUID NOT NULL,
    version_number INTEGER NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    is_public BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_prompt_versions PRIMARY KEY (id),
    CONSTRAINT fk_prompt_versions_prompt_id FOREIGN KEY (prompt_id) REFERENCES prompts (id) ON DELETE CASCADE
);

CREATE INDEX idx_prompt_versions_prompt_id ON prompt_versions (prompt_id);
CREATE UNIQUE INDEX uq_prompt_versions_prompt_version ON prompt_versions (prompt_id, version_number);
