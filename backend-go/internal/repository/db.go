package repository

import (
	"fmt"
	"net/url"
	"strings"

	"backend-go/internal/config"
	"gorm.io/driver/postgres"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"
)

var DB *gorm.DB

func InitDB(cfg *config.Config) error {
	dsn := parseJDBCURL(cfg.Database.URL, cfg.Database.Username, cfg.Database.Password)

	logLevel := logger.Info
	if cfg.Logging.Level == "DEBUG" {
		logLevel = logger.Info // GORM doesn't have a DEBUG level, info logs SQL
	} else if cfg.Logging.Level == "INFO" {
		logLevel = logger.Warn
	}

	db, err := gorm.Open(postgres.Open(dsn), &gorm.Config{
		Logger: logger.Default.LogMode(logLevel),
	})

	if err != nil {
		return fmt.Errorf("failed to connect database: %w", err)
	}

	DB = db
	return nil
}

// parseJDBCURL converts a JDBC URL like jdbc:postgresql://localhost:5432/promptdb
// into a DSN format for gorm postgres driver
func parseJDBCURL(jdbcURL, user, password string) string {
	// Convert JDBC URL (jdbc:postgresql://...) to postgres URL and preserve query params.
	dsn := strings.TrimSpace(jdbcURL)
	dsn = strings.Replace(dsn, "jdbc:postgresql://", "postgres://", 1)

	u, err := url.Parse(dsn)
	if err != nil {
		// Fall back to the converted string so caller still gets original parse behavior.
		return dsn
	}

	// Add credentials only when URL does not already include them.
	if u.User == nil {
		u.User = url.UserPassword(user, password)
	}

	q := u.Query()
	if q.Get("sslmode") == "" {
		// Managed Postgres providers (e.g. Aiven) typically require encryption.
		// For local Postgres without TLS, set ?sslmode=disable in JDBC_URL.
		q.Set("sslmode", "require")
	}
	u.RawQuery = q.Encode()

	return u.String()
}
