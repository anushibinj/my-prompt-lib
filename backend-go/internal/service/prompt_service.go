package service

import (
	"errors"
	"time"

	"backend-go/internal/model"
	"backend-go/internal/repository"

	"github.com/google/uuid"
)

type PromptService struct {
	promptRepo        *repository.PromptRepository
	promptVersionRepo *repository.PromptVersionRepository
}

func NewPromptService(promptRepo *repository.PromptRepository, promptVersionRepo *repository.PromptVersionRepository) *PromptService {
	return &PromptService{
		promptRepo:        promptRepo,
		promptVersionRepo: promptVersionRepo,
	}
}

func (s *PromptService) GetAllPromptsForUser(userId uuid.UUID) ([]model.Prompt, error) {
	return s.promptRepo.FindByUserID(userId)
}

func (s *PromptService) GetPublicPrompts() ([]model.Prompt, error) {
	return s.promptRepo.FindByIsPublicTrue()
}

func (s *PromptService) GetPromptByIdAndUser(id uuid.UUID, userId uuid.UUID) (*model.Prompt, error) {
	prompt, err := s.promptRepo.FindByID(id)
	if err != nil {
		return nil, err
	}
	if prompt == nil {
		return nil, errors.New("Prompt not found")
	}
	if prompt.UserID != userId {
		return nil, errors.New("Unauthorized to access this prompt")
	}
	return prompt, nil
}

func (s *PromptService) GetSharedPrompt(id uuid.UUID) (*model.Prompt, error) {
	prompt, err := s.promptRepo.FindByID(id)
	if err != nil {
		return nil, err
	}
	if prompt == nil {
		return nil, errors.New("Prompt not found")
	}
	if !prompt.IsPublic {
		return nil, errors.New("This prompt is not public")
	}
	return prompt, nil
}

func (s *PromptService) CreatePrompt(prompt *model.Prompt, userId uuid.UUID) (*model.Prompt, error) {
	// ID generation handles by default or model
	if prompt.ID == uuid.Nil {
		prompt.ID = uuid.New()
	}
	prompt.UserID = userId
	saved, err := s.promptRepo.Save(prompt)
	if err != nil {
		return nil, err
	}
	err = s.saveVersion(saved)
	if err != nil {
		return nil, err
	}
	return saved, nil
}

func (s *PromptService) UpdatePrompt(id uuid.UUID, promptDetails *model.Prompt, userId uuid.UUID) (*model.Prompt, error) {
	existing, err := s.GetPromptByIdAndUser(id, userId)
	if err != nil {
		return nil, err
	}

	existing.Title = promptDetails.Title
	existing.Content = promptDetails.Content
	existing.IsPublic = promptDetails.IsPublic

	saved, err := s.promptRepo.Save(existing)
	if err != nil {
		return nil, err
	}

	err = s.saveVersion(saved)
	if err != nil {
		return nil, err
	}

	return saved, nil
}

func (s *PromptService) DeletePrompt(id uuid.UUID, userId uuid.UUID) error {
	existing, err := s.GetPromptByIdAndUser(id, userId)
	if err != nil {
		return err
	}

	err = s.promptVersionRepo.DeleteByPromptID(id)
	if err != nil {
		return err
	}

	return s.promptRepo.Delete(existing)
}

func (s *PromptService) GetPromptHistory(promptId uuid.UUID, userId uuid.UUID) ([]model.PromptVersion, error) {
	// Verify ownership
	_, err := s.GetPromptByIdAndUser(promptId, userId)
	if err != nil {
		return nil, err
	}

	return s.promptVersionRepo.FindByPromptIDOrderByVersionNumberDesc(promptId)
}

func (s *PromptService) saveVersion(prompt *model.Prompt) error {
	nextVersion := 1
	topVersion, err := s.promptVersionRepo.FindTopByPromptIDOrderByVersionNumberDesc(prompt.ID)
	if err != nil {
		return err
	}
	if topVersion != nil {
		nextVersion = topVersion.VersionNumber + 1
	}

	version := &model.PromptVersion{
		ID:            uuid.New(),
		PromptID:      prompt.ID,
		VersionNumber: nextVersion,
		Title:         prompt.Title,
		Content:       prompt.Content,
		IsPublic:      prompt.IsPublic,
		CreatedAt:     time.Now(), // time.Now().UTC() matches Instant.now() depending on setup, but time.Now is standard
	}

	_, err = s.promptVersionRepo.Save(version)
	return err
}
