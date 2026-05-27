# My Prompt Lib - Go Backend

This is the Go implementation of the My Prompt Lib backend, fully API compatible with the original Spring Boot implementation.

## Architecture

* **Framework:** Gin Web Framework
* **Database/ORM:** GORM with PostgreSQL driver
* **Configuration:** Viper (supporting YAML and environment variables)
* **Structure:** Standard Go Project Layout (cmd, internal/api, internal/service, internal/repository, internal/model)

## Environment Variables

| Variable | Description | Default |
| --- | --- | --- |
| `JDBC_URL` | The PostgreSQL connection string | `jdbc:postgresql://localhost:5432/promptdb` |
| `JDBC_USERNAME` | Database username | `postgres` |
| `JDBC_PASSWORD` | Database password | `password` |
| `GOOGLE_CLIENT_ID` | Your Google OAuth Client ID | `your-google-client-id-here` |

## Local Development

1. Ensure PostgreSQL is running.
2. Run `go run cmd/api/main.go` from the `backend-go` directory.

## Docker

A multi-stage Dockerfile is included for production usage.

```bash
docker build -t my-prompt-lib-go .
docker run -p 8080:8080 -e JDBC_URL=jdbc:postgresql://db:5432/promptdb ... my-prompt-lib-go
```

## API Compatibility Guarantees

This Go implementation guarantees exactly matched API contracts, preserving:
- All HTTP status codes for success and error conditions
- Identical JSON responses and input payload definitions
- JWT bearer token handling logic identical to `AuthInterceptor`
- Validation logic reporting structures and specific error phrasing identical to `GlobalExceptionHandler` and `MethodArgumentNotValidException` logic.
