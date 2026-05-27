package model

import (
	"github.com/google/uuid"
)

// User corresponds to the users table
type User struct {
	ID       uuid.UUID `gorm:"type:uuid;primary_key;default:gen_random_uuid()"`
	Username string    `gorm:"uniqueIndex:uq_users_username;not null"`
	Password string
	Token    string    `gorm:"index:idx_users_token"`
	GoogleID string    `gorm:"index:idx_users_google_id"`
}
