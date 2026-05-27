package repository

import (
	"fmt"
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
	// Simple parsing for expected jdbc url format
	// jdbc:postgresql://localhost:5432/promptdb

	dsn := jdbcURL
	dsn = strings.Replace(dsn, "jdbc:postgresql://", "postgres://", 1)

	// Add credentials if they are missing from the URL
	if !strings.Contains(dsn, "@") {
		// insert user:password@ before host
		hostStart := strings.Index(dsn, "://") + 3
		dsn = dsn[:hostStart] + user + ":" + password + "@" + dsn[hostStart:]
	}

	// append sslmode=disable for local development default if not present
	if !strings.Contains(dsn, "sslmode=") {
		if strings.Contains(dsn, "?") {
			dsn += "&sslmode=disable"
		} else {
			dsn += "?sslmode=disable"
		}
	}

	return dsn
}
