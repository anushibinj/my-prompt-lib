package service

import (
	"encoding/json"
	"errors"
	"fmt"
	"net/http"
)

type GoogleTokenVerifierService struct {
	clientID string
}

func NewGoogleTokenVerifierService(clientID string) *GoogleTokenVerifierService {
	return &GoogleTokenVerifierService{clientID: clientID}
}

type GoogleUserInfo struct {
	GoogleID string
	Email    string
}

func (s *GoogleTokenVerifierService) Verify(idToken string) (*GoogleUserInfo, error) {
	url := fmt.Sprintf("https://oauth2.googleapis.com/tokeninfo?id_token=%s", idToken)

	resp, err := http.Get(url)
	if err != nil {
		return nil, errors.New("Failed to verify Google token")
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return nil, errors.New("Invalid Google token")
	}

	var responseMap map[string]interface{}
	if err := json.NewDecoder(resp.Body).Decode(&responseMap); err != nil {
		return nil, errors.New("Invalid Google token")
	}

	aud, ok := responseMap["aud"].(string)
	if !ok || aud != s.clientID {
		return nil, errors.New("Token not intended for this app")
	}

	email, ok1 := responseMap["email"].(string)
	sub, ok2 := responseMap["sub"].(string)

	if !ok1 || !ok2 || email == "" || sub == "" {
		return nil, errors.New("Invalid Google token: missing user info")
	}

	return &GoogleUserInfo{
		GoogleID: sub,
		Email:    email,
	}, nil
}
