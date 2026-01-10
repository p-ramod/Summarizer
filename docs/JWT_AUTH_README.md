# JWT Authentication Implementation

This Spring Boot application demonstrates JWT-based authentication with PostgreSQL integration.

## Features Implemented

### 1. JWT Authentication System
- **POST /login** - Accepts username/password and returns JWT token
- **GET /api/public** - Anonymous endpoint (no authentication required)
- **GET /api/protected** - Protected endpoint (requires valid JWT token)

### 2. Database Integration
- PostgreSQL database with user management
- JPA/Hibernate with auto-DDL enabled
- BCrypt password hashing
- Single role column (ROLE_USER, ROLE_ADMIN)

### 3. Security Configuration
- Stateless session management
- CSRF disabled for REST API
- JWT filter for token validation
- Spring Security 7 integration

## Prerequisites

- Java 21
- Docker & Docker Compose (for PostgreSQL)
- Maven wrapper included

## Setup & Run

### 1. Start PostgreSQL Database

```bash
docker compose up -d
```

This starts PostgreSQL with:
- Database: `demo`
- Username: `demo`
- Password: `demo`
- Port: `5432`

### 2. Start the Application

```bash
./mvnw spring-boot:run
```

The application will:
- Connect to PostgreSQL
- Create the `users` table automatically
- Insert two demo users:
  - Username: `demo`, Password: `demo`, Role: `ROLE_USER`
  - Username: `admin`, Password: `admin`, Role: `ROLE_ADMIN`
- Start on port `8080`

## API Testing

### 1. Test Public Endpoint (No Authentication Required)

```bash
curl -X GET http://localhost:8080/api/public
```

**Expected Response:**
```json
{
  "message": "This is a public endpoint - no authentication required",
  "timestamp": "2026-01-04T10:45:00",
  "authenticated": false
}
```

### 2. Test Protected Endpoint Without Token (Should Fail)

```bash
curl -X GET http://localhost:8080/api/protected
```

**Expected Response:** `401 Unauthorized`

### 3. Login to Get JWT Token

```bash
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "demo",
    "password": "demo"
  }'
```

**Expected Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiUk9MRV9VU0VSIiwic3ViIjoiZGVtbyIsImlhdCI6MTcwNDM1MTkwMCwiZXhwIjoxNzA0MzU1NTAwfQ...",
  "tokenType": "Bearer",
  "expiresAt": "2026-01-04T11:45:00",
  "role": "ROLE_USER",
  "username": "demo"
}
```

### 4. Test Protected Endpoint With JWT Token

```bash
# Save the token from previous response
TOKEN="<your-jwt-token-here>"

curl -X GET http://localhost:8080/api/protected \
  -H "Authorization: Bearer $TOKEN"
```

**Expected Response:**
```json
{
  "message": "This is a protected endpoint - authentication required",
  "timestamp": "2026-01-04T10:46:00",
  "authenticated": true,
  "username": "demo",
  "authorities": [
    {
      "authority": "ROLE_USER"
    }
  ]
}
```

### 5. Login as Admin

```bash
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin"
  }'
```

## Complete Test Script

Save this as `test_jwt.sh`:

```bash
#!/bin/bash

echo "=== Testing JWT Authentication ==="

echo -e "\n1. Testing public endpoint (no auth)..."
curl -s http://localhost:8080/api/public | jq .

echo -e "\n2. Testing protected endpoint without token (should fail)..."
curl -s -w "\nHTTP Status: %{http_code}\n" http://localhost:8080/api/protected

echo -e "\n3. Login with demo user..."
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"demo"}')

echo "$LOGIN_RESPONSE" | jq .

TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r .token)
echo -e "\nToken: $TOKEN"

echo -e "\n4. Testing protected endpoint with valid token..."
curl -s http://localhost:8080/api/protected \
  -H "Authorization: Bearer $TOKEN" | jq .

echo -e "\n5. Login with admin user..."
ADMIN_RESPONSE=$(curl -s -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}')

echo "$ADMIN_RESPONSE" | jq .

ADMIN_TOKEN=$(echo "$ADMIN_RESPONSE" | jq -r .token)

echo -e "\n6. Testing protected endpoint with admin token..."
curl -s http://localhost:8080/api/protected \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .

echo -e "\n=== Tests Complete ==="
```

Run with:
```bash
chmod +x test_jwt.sh
./test_jwt.sh
```

## Architecture

### Key Components

1. **Entity Layer**
   - `UserEntity` - JPA entity for users table

2. **Repository Layer**
   - `UserRepository` - Spring Data JPA repository

3. **Service Layer**
   - `JwtService` - JWT token generation and validation
   - `DbUserDetailsService` - Load users from database

4. **Security Layer**
   - `JwtAuthenticationFilter` - Intercept requests and validate JWT
   - `SecurityConfig` - Spring Security configuration

5. **Controller Layer**
   - `AuthController` - Handle login requests
   - `DemoController` - Public and protected endpoints

6. **Configuration**
   - `DataBootstrap` - Initialize demo users on startup

### JWT Token Structure

The JWT token contains:
- **sub** (subject): username
- **role**: User's role (ROLE_USER, ROLE_ADMIN)
- **iat** (issued at): Token creation timestamp
- **exp** (expiration): Token expiry timestamp (60 minutes from creation)

### Security Flow

1. User sends credentials to `/login`
2. Spring Security authenticates against database
3. If valid, JWT token is generated and returned
4. Client includes token in `Authorization: Bearer <token>` header
5. `JwtAuthenticationFilter` validates token and sets security context
6. Controller methods execute with authenticated principal

## Configuration

### application.yaml

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/demo
    username: demo
    password: demo
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

security:
  jwt:
    secret: 404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
    expiration-minutes: 60
```

### Important Notes

- **JWT Secret**: In production, use a strong secret from environment variables
- **Token Expiration**: Set to 60 minutes, adjust as needed
- **Password Encoding**: BCrypt with default strength (10 rounds)
- **Session Management**: Stateless (no server-side sessions)

## Troubleshooting

### Application won't start
- Ensure PostgreSQL is running: `docker compose ps`
- Check logs for connection errors
- Verify port 8080 is not in use: `lsof -i :8080`

### Login fails with 401
- Verify username/password are correct
- Check database has users: `docker exec -it <container> psql -U demo -d demo -c "SELECT * FROM users;"`

### Protected endpoint returns 401 with token
- Verify token is not expired
- Check token format: `Authorization: Bearer <token>`
- Ensure no extra spaces in header

## Database Schema

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

## Dependencies

- Spring Boot 4.0.1
- Spring Security 7.0.2
- Spring Data JPA
- PostgreSQL Driver
- JJWT 0.12.3 (JWT library)
- Lombok (code generation)

## Next Steps / Enhancements

- [ ] Add refresh token mechanism
- [ ] Implement token blacklist for logout
- [ ] Add role-based authorization (@PreAuthorize)
- [ ] Implement user registration endpoint
- [ ] Add password reset functionality
- [ ] Add rate limiting for login attempts
- [ ] Implement refresh token rotation
- [ ] Add API documentation (Swagger/OpenAPI)
- [ ] Add integration tests
- [ ] Move JWT secret to environment variable

