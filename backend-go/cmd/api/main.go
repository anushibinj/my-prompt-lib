package main

import (
	"context"
	"errors"
	"log"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"backend-go/internal/api"
	"backend-go/internal/config"
	"backend-go/internal/middleware"
	"backend-go/internal/repository"
	"backend-go/internal/service"

	"github.com/gin-contrib/cors"
	"github.com/gin-gonic/gin"
)

func main() {
	cfg, err := config.LoadConfig()
	if err != nil {
		log.Fatalf("Failed to load config: %v", err)
	}

	err = repository.InitDB(cfg)
	if err != nil {
		log.Fatalf("Failed to initialize database: %v", err)
	}

	// Repositories
	userRepo := repository.NewUserRepository(repository.DB)
	promptRepo := repository.NewPromptRepository(repository.DB)
	promptVersionRepo := repository.NewPromptVersionRepository(repository.DB)

	// Services
	userService := service.NewUserService(userRepo)
	promptService := service.NewPromptService(promptRepo, promptVersionRepo)
	googleTokenService := service.NewGoogleTokenVerifierService(cfg.Google.ClientID)

	// Handlers
	authHandler := api.NewAuthHandler(userService, googleTokenService, cfg.Google.ClientID)
	promptHandler := api.NewPromptHandler(promptService)
	healthHandler := api.NewHealthHandler()

	router := gin.Default()

	// CORS matching Java WebConfig
	config := cors.DefaultConfig()
	config.AllowOrigins = []string{"http://localhost:5173", "http://127.0.0.1:5173", "https://my-prompt-lib-online.web.app", "https://mpl.fastorial.dev"}
	config.AllowMethods = []string{"GET", "POST", "PUT", "DELETE", "OPTIONS"}
	config.AllowHeaders = []string{"*"}
	router.Use(cors.New(config))

	router.Use(middleware.ErrorHandler())
	router.Use(middleware.AuthMiddleware(userService))

	apiGroup := router.Group("/api")
	{
		auth := apiGroup.Group("/auth")
		{
			auth.POST("/register", authHandler.Register)
			auth.POST("/login", authHandler.Login)
			auth.POST("/google", authHandler.GoogleLogin)
			auth.GET("/google-client-id", authHandler.GetGoogleClientID)
		}

		prompts := apiGroup.Group("/prompts")
		{
			prompts.GET("", promptHandler.GetAllPrompts)
			prompts.GET("/shared", promptHandler.GetPublicPrompts)
			prompts.GET("/shared/:id", promptHandler.GetSharedPrompt)
			prompts.GET("/:id", promptHandler.GetPromptById)
			prompts.GET("/:id/history", promptHandler.GetPromptHistory)
			prompts.POST("", promptHandler.CreatePrompt)
			prompts.PUT("/:id", promptHandler.UpdatePrompt)
			prompts.DELETE("/:id", promptHandler.DeletePrompt)
		}

		apiGroup.GET("/health", healthHandler.Health)
	}

	server := &http.Server{
		Addr:              ":8080",
		Handler:           router,
		ReadHeaderTimeout: 5 * time.Second,
	}

	serverErrCh := make(chan error, 1)
	go func() {
		if err := server.ListenAndServe(); err != nil && !errors.Is(err, http.ErrServerClosed) {
			serverErrCh <- err
		}
	}()

	ctx, stop := signal.NotifyContext(context.Background(), os.Interrupt, syscall.SIGTERM)
	defer stop()

	select {
	case err := <-serverErrCh:
		log.Printf("Server exited with error: %v", err)
	case <-ctx.Done():
		log.Printf("Shutdown signal received, stopping server...")
	}

	shutdownCtx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()
	if err := server.Shutdown(shutdownCtx); err != nil {
		log.Printf("HTTP server shutdown error: %v", err)
	}

	if err := repository.CloseDB(); err != nil {
		log.Printf("Database close error: %v", err)
	}
}
