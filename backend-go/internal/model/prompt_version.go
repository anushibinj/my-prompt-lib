package model

import (
	"time"

	"github.com/google/uuid"
)

// PromptVersion corresponds to the prompt_versions table
type PromptVersion struct {
	ID            uuid.UUID `gorm:"type:uuid;primary_key;default:gen_random_uuid()" json:"id"`
	PromptID      uuid.UUID `gorm:"type:uuid;index:idx_prompt_versions_prompt_id;uniqueIndex:uq_prompt_versions_prompt_version,priority:1;not null" json:"promptId"`
	VersionNumber int       `gorm:"uniqueIndex:uq_prompt_versions_prompt_version,priority:2;not null" json:"versionNumber"`
	Title         string    `gorm:"not null" json:"title"`
	Content       string    `gorm:"type:text;not null" json:"content"`
	IsPublic      bool      `gorm:"not null;default:false" json:"isPublic"`
	CreatedAt     time.Time `gorm:"type:timestamp with time zone;not null" json:"createdAt"`
}

// TableName overrides the table name used by GORM
func (PromptVersion) TableName() string {
	return "prompt_versions"
}
