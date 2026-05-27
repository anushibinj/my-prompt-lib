package config

import (
	"fmt"
	"strings"

	"github.com/spf13/viper"
)

type Config struct {
	App      AppConfig
	Database DatabaseConfig
	Google   GoogleConfig
	Logging  LoggingConfig
}

type AppConfig struct {
	Name string
}

type DatabaseConfig struct {
	URL      string
	Username string
	Password string
}

type GoogleConfig struct {
	ClientID string `mapstructure:"client-id"`
}

type LoggingConfig struct {
	Level string
}

func LoadConfig() (*Config, error) {
	v := viper.New()

	v.SetConfigName("application")
	v.SetConfigType("yml")
	v.AddConfigPath(".")
	v.AddConfigPath("./config")
	v.AddConfigPath("../backend/src/main/resources") // For local dev compatibility

	v.SetEnvKeyReplacer(strings.NewReplacer(".", "_", "-", "_"))
	v.AutomaticEnv()

	// Default values matching Java application.yml
	v.SetDefault("spring.application.name", "my-prompt-lib")
	v.SetDefault("spring.datasource.url", "jdbc:postgresql://localhost:5432/promptdb")
	v.SetDefault("spring.datasource.username", "postgres")
	v.SetDefault("spring.datasource.password", "password")
	v.SetDefault("google.client-id", "your-google-client-id-here")
	v.SetDefault("logging.level.root", "INFO")

	// Map env vars
	v.BindEnv("spring.datasource.url", "JDBC_URL")
	v.BindEnv("spring.datasource.username", "JDBC_USERNAME")
	v.BindEnv("spring.datasource.password", "JDBC_PASSWORD")
	v.BindEnv("google.client-id", "GOOGLE_CLIENT_ID")

	if err := v.ReadInConfig(); err != nil {
		fmt.Printf("Warning: Could not read config file: %v. Using defaults and environment variables.\n", err)
	}

	config := &Config{
		App: AppConfig{
			Name: v.GetString("spring.application.name"),
		},
		Database: DatabaseConfig{
			URL:      v.GetString("spring.datasource.url"),
			Username: v.GetString("spring.datasource.username"),
			Password: v.GetString("spring.datasource.password"),
		},
		Google: GoogleConfig{
			ClientID: v.GetString("google.client-id"),
		},
		Logging: LoggingConfig{
			Level: v.GetString("logging.level.root"),
		},
	}

	return config, nil
}
