package repository

import (
	"backend-go/internal/model"
	"github.com/google/uuid"
	"gorm.io/gorm"
)

type PromptVersionRepository struct {
	db *gorm.DB
}

func NewPromptVersionRepository(db *gorm.DB) *PromptVersionRepository {
	return &PromptVersionRepository{db: db}
}

func (r *PromptVersionRepository) FindByPromptIDOrderByVersionNumberDesc(promptId uuid.UUID) ([]model.PromptVersion, error) {
	var versions []model.PromptVersion
	err := r.db.Where("prompt_id = ?", promptId).Order("version_number desc").Find(&versions).Error
	return versions, err
}

func (r *PromptVersionRepository) FindTopByPromptIDOrderByVersionNumberDesc(promptId uuid.UUID) (*model.PromptVersion, error) {
	var version model.PromptVersion
	err := r.db.Where("prompt_id = ?", promptId).Order("version_number desc").First(&version).Error
	if err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil, nil
		}
		return nil, err
	}
	return &version, nil
}

func (r *PromptVersionRepository) Save(version *model.PromptVersion) (*model.PromptVersion, error) {
	err := r.db.Save(version).Error
	if err != nil {
		return nil, err
	}
	return version, nil
}

func (r *PromptVersionRepository) DeleteByPromptID(promptId uuid.UUID) error {
	return r.db.Where("prompt_id = ?", promptId).Delete(&model.PromptVersion{}).Error
}
