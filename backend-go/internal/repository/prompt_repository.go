package repository

import (
	"backend-go/internal/model"
	"github.com/google/uuid"
	"gorm.io/gorm"
)

type PromptRepository struct {
	db *gorm.DB
}

func NewPromptRepository(db *gorm.DB) *PromptRepository {
	return &PromptRepository{db: db}
}

func (r *PromptRepository) FindByUserID(userId uuid.UUID) ([]model.Prompt, error) {
	var prompts []model.Prompt
	err := r.db.Where("user_id = ?", userId).Find(&prompts).Error
	return prompts, err
}

func (r *PromptRepository) FindByIsPublicTrue() ([]model.Prompt, error) {
	var prompts []model.Prompt
	err := r.db.Where("is_public = ?", true).Find(&prompts).Error
	return prompts, err
}

func (r *PromptRepository) FindByID(id uuid.UUID) (*model.Prompt, error) {
	var prompt model.Prompt
	err := r.db.Where("id = ?", id).First(&prompt).Error
	if err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil, nil
		}
		return nil, err
	}
	return &prompt, nil
}

func (r *PromptRepository) Save(prompt *model.Prompt) (*model.Prompt, error) {
	err := r.db.Save(prompt).Error
	if err != nil {
		return nil, err
	}
	return prompt, nil
}

func (r *PromptRepository) Delete(prompt *model.Prompt) error {
	return r.db.Delete(prompt).Error
}
