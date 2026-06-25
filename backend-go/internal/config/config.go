package config

import (
	"fmt"
	"os"
	"strings"
	"github.com/joho/godotenv"
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
	URL                    string
	Username               string
	Password               string
	MaxOpenConns           int
	MaxIdleConns           int
	ConnMaxLifetimeMinutes int
	ConnMaxIdleMinutes     int
}

type GoogleConfig struct {
	ClientID string `mapstructure:"client-id"`
}

type LoggingConfig struct {
	Level string
}

func LoadConfig() (*Config, error) {
	godotenv.Load() // Load .env file if it exists

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

	// Cloud Run can scale to many instances; keep per-instance pool conservative by default.
	// K_SERVICE is automatically injected by Cloud Run at runtime.
	if os.Getenv("K_SERVICE") != "" {
		v.SetDefault("db.max-open-conns", 1)
		v.SetDefault("db.max-idle-conns", 0)
		v.SetDefault("db.conn-max-lifetime-minutes", 2)
		v.SetDefault("db.conn-max-idle-minutes", 1)
	} else {
		v.SetDefault("db.max-open-conns", 3)
		v.SetDefault("db.max-idle-conns", 1)
		v.SetDefault("db.conn-max-lifetime-minutes", 5)
		v.SetDefault("db.conn-max-idle-minutes", 1)
	}
	v.SetDefault("google.client-id", "your-google-client-id-here")
	v.SetDefault("logging.level.root", "INFO")

	// Map env vars
	v.BindEnv("spring.datasource.url", "JDBC_URL")
	v.BindEnv("spring.datasource.username", "JDBC_USERNAME")
	v.BindEnv("spring.datasource.password", "JDBC_PASSWORD")
	v.BindEnv("db.max-open-conns", "DB_MAX_OPEN_CONNS")
	v.BindEnv("db.max-idle-conns", "DB_MAX_IDLE_CONNS")
	v.BindEnv("db.conn-max-lifetime-minutes", "DB_CONN_MAX_LIFETIME_MINUTES")
	v.BindEnv("db.conn-max-idle-minutes", "DB_CONN_MAX_IDLE_MINUTES")
	v.BindEnv("google.client-id", "GOOGLE_CLIENT_ID")

	if err := v.ReadInConfig(); err != nil {
		fmt.Printf("Warning: Could not read config file: %v. Using defaults and environment variables.\n", err)
	}

	config := &Config{
		App: AppConfig{
			Name: v.GetString("spring.application.name"),
		},
		Database: DatabaseConfig{
			URL:                    v.GetString("spring.datasource.url"),
			Username:               v.GetString("spring.datasource.username"),
			Password:               v.GetString("spring.datasource.password"),
			MaxOpenConns:           v.GetInt("db.max-open-conns"),
			MaxIdleConns:           v.GetInt("db.max-idle-conns"),
			ConnMaxLifetimeMinutes: v.GetInt("db.conn-max-lifetime-minutes"),
			ConnMaxIdleMinutes:     v.GetInt("db.conn-max-idle-minutes"),
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
