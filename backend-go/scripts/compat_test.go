package scripts

import (
	"bytes"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"

	"backend-go/internal/api"
	"backend-go/internal/middleware"
	"github.com/gin-gonic/gin"
)

// A simplistic test demonstrating how to verify exactly compatibility with handlers
// In a real environment we'd use mock DB, but here we just ensure the router correctly formats responses.

func setupRouter() *gin.Engine {
	router := gin.Default()
	router.Use(middleware.ErrorHandler())

	healthHandler := api.NewHealthHandler()
	router.GET("/api/health", healthHandler.Health)

	return router
}

func TestHealthEndpoint(t *testing.T) {
	router := setupRouter()

	w := httptest.NewRecorder()
	req, _ := http.NewRequest("GET", "/api/health", nil)
	router.ServeHTTP(w, req)

	if w.Code != http.StatusOK {
		t.Fatalf("Expected 200 OK, got %d", w.Code)
	}

	expected := `{"status":"ok"}`
	if w.Body.String() != expected {
		t.Fatalf("Expected %s, got %s", expected, w.Body.String())
	}
}

func TestValidationErrors(t *testing.T) {
	router := setupRouter()

	authGroup := router.Group("/api/auth")
	// Using nil for services just to test binding failure format
	handler := api.NewAuthHandler(nil, nil, "test")
	authGroup.POST("/register", handler.Register)

	w := httptest.NewRecorder()
	// Empty body should trigger required bindings
	req, _ := http.NewRequest("POST", "/api/auth/register", bytes.NewBuffer([]byte(`{}`)))
	req.Header.Set("Content-Type", "application/json")
	router.ServeHTTP(w, req)

	if w.Code != http.StatusBadRequest {
		t.Fatalf("Expected 400 Bad Request, got %d", w.Code)
	}

	var response map[string]string
	err := json.Unmarshal(w.Body.Bytes(), &response)
	if err != nil {
		t.Fatalf("Expected JSON response, got error: %v", err)
	}

	if response["username"] != "Username cannot be empty" {
		t.Errorf("Expected 'Username cannot be empty', got %s", response["username"])
	}

	if response["password"] != "Password cannot be empty" {
		t.Errorf("Expected 'Password cannot be empty', got %s", response["password"])
	}
}
