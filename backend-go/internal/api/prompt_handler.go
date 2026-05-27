package api

import (
	"net/http"

	"backend-go/internal/model"
	"backend-go/internal/service"
	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
)

type PromptHandler struct {
	promptService *service.PromptService
}

func NewPromptHandler(promptService *service.PromptService) *PromptHandler {
	return &PromptHandler{promptService: promptService}
}

func (h *PromptHandler) GetAllPrompts(c *gin.Context) {
	user, exists := c.Get("user")
	if !exists {
		c.String(http.StatusUnauthorized, "Unauthorized")
		return
	}
	u := user.(*model.User)

	prompts, err := h.promptService.GetAllPromptsForUser(u.ID)
	if err != nil {
		c.String(http.StatusNotFound, err.Error())
		return
	}

	// Make sure we return [] instead of null when empty
	if prompts == nil {
		prompts = []model.Prompt{}
	}

	c.JSON(http.StatusOK, prompts)
}

func (h *PromptHandler) GetPublicPrompts(c *gin.Context) {
	prompts, err := h.promptService.GetPublicPrompts()
	if err != nil {
		c.String(http.StatusNotFound, err.Error())
		return
	}

	if prompts == nil {
		prompts = []model.Prompt{}
	}

	c.JSON(http.StatusOK, prompts)
}

func (h *PromptHandler) GetSharedPrompt(c *gin.Context) {
	idStr := c.Param("id")
	id, err := uuid.Parse(idStr)
	if err != nil {
		c.String(http.StatusNotFound, "Invalid UUID")
		return
	}

	prompt, err := h.promptService.GetSharedPrompt(id)
	if err != nil {
		c.String(http.StatusNotFound, err.Error())
		return
	}

	c.JSON(http.StatusOK, prompt)
}

func (h *PromptHandler) GetPromptById(c *gin.Context) {
	user, exists := c.Get("user")
	if !exists {
		c.String(http.StatusUnauthorized, "Unauthorized")
		return
	}
	u := user.(*model.User)

	idStr := c.Param("id")
	id, err := uuid.Parse(idStr)
	if err != nil {
		c.String(http.StatusNotFound, "Invalid UUID")
		return
	}

	prompt, err := h.promptService.GetPromptByIdAndUser(id, u.ID)
	if err != nil {
		c.String(http.StatusNotFound, err.Error())
		return
	}

	c.JSON(http.StatusOK, prompt)
}

func (h *PromptHandler) GetPromptHistory(c *gin.Context) {
	user, exists := c.Get("user")
	if !exists {
		c.String(http.StatusUnauthorized, "Unauthorized")
		return
	}
	u := user.(*model.User)

	idStr := c.Param("id")
	id, err := uuid.Parse(idStr)
	if err != nil {
		c.String(http.StatusNotFound, "Invalid UUID")
		return
	}

	history, err := h.promptService.GetPromptHistory(id, u.ID)
	if err != nil {
		c.String(http.StatusNotFound, err.Error())
		return
	}

	if history == nil {
		history = []model.PromptVersion{}
	}

	c.JSON(http.StatusOK, history)
}

func (h *PromptHandler) CreatePrompt(c *gin.Context) {
	user, exists := c.Get("user")
	if !exists {
		c.String(http.StatusUnauthorized, "Unauthorized")
		return
	}
	u := user.(*model.User)

	var prompt model.Prompt
	if err := c.ShouldBindJSON(&prompt); err != nil {
		// Specific binding handled by global error handler structure later
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	saved, err := h.promptService.CreatePrompt(&prompt, u.ID)
	if err != nil {
		c.String(http.StatusNotFound, err.Error())
		return
	}

	c.JSON(http.StatusCreated, saved)
}

func (h *PromptHandler) UpdatePrompt(c *gin.Context) {
	user, exists := c.Get("user")
	if !exists {
		c.String(http.StatusUnauthorized, "Unauthorized")
		return
	}
	u := user.(*model.User)

	idStr := c.Param("id")
	id, err := uuid.Parse(idStr)
	if err != nil {
		c.String(http.StatusNotFound, "Invalid UUID")
		return
	}

	var prompt model.Prompt
	if err := c.ShouldBindJSON(&prompt); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	saved, err := h.promptService.UpdatePrompt(id, &prompt, u.ID)
	if err != nil {
		c.String(http.StatusNotFound, err.Error())
		return
	}

	c.JSON(http.StatusOK, saved)
}

func (h *PromptHandler) DeletePrompt(c *gin.Context) {
	user, exists := c.Get("user")
	if !exists {
		c.String(http.StatusUnauthorized, "Unauthorized")
		return
	}
	u := user.(*model.User)

	idStr := c.Param("id")
	id, err := uuid.Parse(idStr)
	if err != nil {
		c.String(http.StatusNotFound, "Invalid UUID")
		return
	}

	err = h.promptService.DeletePrompt(id, u.ID)
	if err != nil {
		c.String(http.StatusNotFound, err.Error())
		return
	}

	c.Status(http.StatusNoContent)
}
