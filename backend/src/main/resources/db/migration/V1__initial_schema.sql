-- V1: Initial schema for My Prompt Lib
-- All constraints and indices are explicitly named for future migration reference.

CREATE TABLE users (
    id UUID NOT NULL,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255),
    token VARCHAR(255),
    google_id VARCHAR(255),
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_username UNIQUE (username)
);

CREATE INDEX idx_users_token ON users (token);
CREATE INDEX idx_users_google_id ON users (google_id);

CREATE TABLE prompts (
    id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    user_id UUID,
    is_public BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_prompts PRIMARY KEY (id),
    CONSTRAINT fk_prompts_user_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_prompts_user_id ON prompts (user_id);
CREATE INDEX idx_prompts_is_public ON prompts (is_public);
