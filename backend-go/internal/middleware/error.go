package middleware

import (
	"fmt"
	"net/http"
	"strings"

	"github.com/gin-gonic/gin"
	"github.com/go-playground/validator/v10"
)

// GlobalErrorHandler matches the exact behavior of the Java GlobalExceptionHandler
func ErrorHandler() gin.HandlerFunc {
	return func(c *gin.Context) {
		c.Next()

		// Error checking for validation errors
		for _, err := range c.Errors {
			if validationErrors, ok := err.Err.(validator.ValidationErrors); ok {
				errorsMap := make(map[string]string)
				for _, fieldError := range validationErrors {
					field := strings.ToLower(fieldError.Field()[:1]) + fieldError.Field()[1:] // Convert to camelCase
					message := getValidationErrorMessage(fieldError)
					errorsMap[field] = message
				}
				c.JSON(http.StatusBadRequest, errorsMap)
				return
			}
			// General error matching RuntimeException format -> 404 string body
			if c.Writer.Status() == http.StatusOK || c.Writer.Status() == 0 {
				c.String(http.StatusNotFound, fmt.Sprintf("%v", err.Err))
			} else {
				c.String(c.Writer.Status(), fmt.Sprintf("%v", err.Err))
			}
			return
		}
	}
}

func getValidationErrorMessage(fe validator.FieldError) string {
	switch fe.Tag() {
	case "required":
		// Customizing specific validation strings matching Java exceptions
		if fe.Field() == "Username" || fe.Field() == "Password" || fe.Field() == "Credential" || fe.Field() == "Title" || fe.Field() == "Content" {
			return fe.Field() + " cannot be empty"
		}
		return "This field is required"
	}
	return fe.Error()
}
