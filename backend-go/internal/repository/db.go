package repository

import (
	"fmt"
	"net/url"
	"strings"
	"time"

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

	sqlDB, err := db.DB()
	if err != nil {
		return fmt.Errorf("failed to get sql db from gorm: %w", err)
	}

	maxOpen := cfg.Database.MaxOpenConns
	if maxOpen <= 0 {
		maxOpen = 3
	}
	maxIdle := cfg.Database.MaxIdleConns
	if maxIdle < 0 {
		maxIdle = 0
	}
	if maxIdle > maxOpen {
		maxIdle = maxOpen
	}
	lifetimeMinutes := cfg.Database.ConnMaxLifetimeMinutes
	if lifetimeMinutes <= 0 {
		lifetimeMinutes = 5
	}
	idleMinutes := cfg.Database.ConnMaxIdleMinutes
	if idleMinutes <= 0 {
		idleMinutes = 1
	}

	sqlDB.SetMaxOpenConns(maxOpen)
	sqlDB.SetMaxIdleConns(maxIdle)
	sqlDB.SetConnMaxLifetime(time.Duration(lifetimeMinutes) * time.Minute)
	sqlDB.SetConnMaxIdleTime(time.Duration(idleMinutes) * time.Minute)

	DB = db
	return nil
}

func CloseDB() error {
	if DB == nil {
		return nil
	}

	sqlDB, err := DB.DB()
	if err != nil {
		return fmt.Errorf("failed to get sql db from gorm for close: %w", err)
	}

	if err := sqlDB.Close(); err != nil {
		return fmt.Errorf("failed to close database: %w", err)
	}

	DB = nil
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
	if ssl := strings.TrimSpace(q.Get("ssl")); ssl != "" {
		// Some URLs use `ssl=...` (JDBC style), but pgx expects `sslmode=...`.
		if q.Get("sslmode") == "" {
			switch strings.ToLower(ssl) {
			case "true", "on", "1", "enabled", "enable", "require":
				q.Set("sslmode", "require")
			case "false", "off", "0", "disabled", "disable":
				q.Set("sslmode", "disable")
			default:
				q.Set("sslmode", ssl)
			}
		}
		q.Del("ssl")
	}
	if q.Get("sslmode") == "" {
		// Managed Postgres providers (e.g. Aiven) typically require encryption.
		// For local Postgres without TLS, set ?sslmode=disable in JDBC_URL.
		q.Set("sslmode", "require")
	}
	if q.Get("connect_timeout") == "" {
		// Keep startup responsive when remote DB is slow/unreachable.
		q.Set("connect_timeout", "10")
	}
	u.RawQuery = q.Encode()

	return u.String()
}
