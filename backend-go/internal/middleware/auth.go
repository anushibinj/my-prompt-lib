package middleware

import (
	"net/http"
	"strings"

	"backend-go/internal/service"
	"github.com/gin-gonic/gin"
)

func AuthMiddleware(userService *service.UserService) gin.HandlerFunc {
	return func(c *gin.Context) {
		// Bypass paths logic matching Java
		path := c.Request.URL.Path
		if strings.HasPrefix(path, "/api/auth/") || strings.HasPrefix(path, "/api/prompts/shared") || path == "/api/health" {
			c.Next()
			return
		}

		authHeader := c.GetHeader("Authorization")
		if authHeader != "" && strings.HasPrefix(authHeader, "Bearer ") {
			token := authHeader[7:]
			user, err := userService.FindByToken(token)
			if err == nil && user != nil {
				c.Set("user", user)
				c.Next()
				return
			}
		}

		c.AbortWithStatus(http.StatusUnauthorized)
	}
}
