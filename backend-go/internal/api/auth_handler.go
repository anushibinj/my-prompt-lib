package api

import (
	"net/http"

	"backend-go/internal/service"
	"github.com/gin-gonic/gin"
)

type AuthHandler struct {
	userService        *service.UserService
	googleTokenService *service.GoogleTokenVerifierService
	googleClientID     string
}

func NewAuthHandler(userService *service.UserService, googleTokenService *service.GoogleTokenVerifierService, googleClientID string) *AuthHandler {
	return &AuthHandler{
		userService:        userService,
		googleTokenService: googleTokenService,
		googleClientID:     googleClientID,
	}
}

type AuthRequest struct {
	Username string `json:"username" binding:"required"`
	Password string `json:"password" binding:"required"`
}

type GoogleAuthRequest struct {
	Credential string `json:"credential" binding:"required"`
}

type AuthResponse struct {
	Token    string `json:"token"`
	Username string `json:"username"`
}

func (h *AuthHandler) Register(c *gin.Context) {
	var req AuthRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.Error(err)
		return
	}

	user, err := h.userService.RegisterUser(req.Username, req.Password)
	if err != nil {
		c.String(http.StatusBadRequest, err.Error())
		return
	}

	c.JSON(http.StatusCreated, AuthResponse{
		Token:    user.Token,
		Username: user.Username,
	})
}

func (h *AuthHandler) Login(c *gin.Context) {
	var req AuthRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.Error(err)
		return
	}

	user, err := h.userService.LoginUser(req.Username, req.Password)
	if err != nil {
		c.String(http.StatusBadRequest, err.Error())
		return
	}

	if user != nil {
		c.JSON(http.StatusOK, AuthResponse{
			Token:    user.Token,
			Username: user.Username,
		})
		return
	}

	c.String(http.StatusUnauthorized, "Invalid credentials")
}

func (h *AuthHandler) GoogleLogin(c *gin.Context) {
	var req GoogleAuthRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.Error(err)
		return
	}

	info, err := h.googleTokenService.Verify(req.Credential)
	if err != nil {
		c.String(http.StatusUnauthorized, "Google authentication failed")
		return
	}

	user, err := h.userService.LoginOrRegisterGoogleUser(info.GoogleID, info.Email)
	if err != nil {
		c.String(http.StatusUnauthorized, "Google authentication failed")
		return
	}

	c.JSON(http.StatusOK, AuthResponse{
		Token:    user.Token,
		Username: user.Username,
	})
}

func (h *AuthHandler) GetGoogleClientID(c *gin.Context) {
	c.JSON(http.StatusOK, gin.H{"clientId": h.googleClientID})
}
