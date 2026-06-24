FROM golang:1.25.1-alpine AS builder

WORKDIR /app

# Copy and download dependencies
COPY go.mod go.sum ./
RUN go mod download

# Copy source code
COPY . .

# Build the binary
RUN CGO_ENABLED=0 GOOS=linux go build -o /app/backend-go ./cmd/api/main.go

# Setup production image
FROM alpine:3.19

# Timezone support
RUN apk add --no-cache tzdata ca-certificates

WORKDIR /app

COPY --from=builder /app/backend-go .

# Set default env vars
ENV GIN_MODE=release

# Expose port
EXPOSE 8080

CMD ["/app/backend-go"]
