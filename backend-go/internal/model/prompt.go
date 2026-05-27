package model

import (
	"github.com/google/uuid"
)

// Prompt corresponds to the prompts table
type Prompt struct {
	ID       uuid.UUID `gorm:"type:uuid;primary_key;default:gen_random_uuid()" json:"id"`
	Title    string    `gorm:"not null" json:"title"`
	Content  string    `gorm:"type:text;not null" json:"content"`
	UserID   uuid.UUID `gorm:"type:uuid;index:idx_prompts_user_id" json:"userId"`
	IsPublic bool      `gorm:"not null;default:false;index:idx_prompts_is_public" json:"isPublic"`
}

// TableName overrides the table name used by GORM
func (Prompt) TableName() string {
	return "prompts"
}
