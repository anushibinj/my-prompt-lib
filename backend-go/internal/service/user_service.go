package service

import (
	"errors"
	"backend-go/internal/model"
	"backend-go/internal/repository"
	"github.com/google/uuid"
)

type UserService struct {
	repo *repository.UserRepository
}

func NewUserService(repo *repository.UserRepository) *UserService {
	return &UserService{repo: repo}
}

func (s *UserService) RegisterUser(username, password string) (*model.User, error) {
	existingUser, err := s.repo.FindByUsername(username)
	if err != nil {
		return nil, err
	}
	if existingUser != nil {
		return nil, errors.New("Username already exists")
	}

	user := &model.User{
		ID:       uuid.New(),
		Username: username,
		Password: password,
		Token:    uuid.New().String(),
	}

	return s.repo.Save(user)
}

func (s *UserService) LoginUser(username, password string) (*model.User, error) {
	user, err := s.repo.FindByUsername(username)
	if err != nil {
		return nil, err
	}
	if user != nil && password != "" && user.Password == password {
		// Assign new token
		user.Token = uuid.New().String()
		return s.repo.Save(user)
	}
	return nil, nil // Return nil, nil when login fails (like Optional.empty())
}

func (s *UserService) LoginOrRegisterGoogleUser(googleID, email string) (*model.User, error) {
	// Check if user already linked to this Google account
	existingGoogleUser, err := s.repo.FindByGoogleID(googleID)
	if err != nil {
		return nil, err
	}
	if existingGoogleUser != nil {
		existingGoogleUser.Token = uuid.New().String()
		return s.repo.Save(existingGoogleUser)
	}

	// Check if user exists with same email as username — link Google ID
	emailUser, err := s.repo.FindByUsername(email)
	if err != nil {
		return nil, err
	}
	if emailUser != nil {
		emailUser.GoogleID = googleID
		emailUser.Token = uuid.New().String()
		return s.repo.Save(emailUser)
	}

	// Create new Google user
	newUser := &model.User{
		ID:       uuid.New(),
		Username: email,
		GoogleID: googleID,
		Token:    uuid.New().String(),
	}
	return s.repo.Save(newUser)
}

func (s *UserService) FindByToken(token string) (*model.User, error) {
	if token == "" {
		return nil, nil
	}
	return s.repo.FindByToken(token)
}
