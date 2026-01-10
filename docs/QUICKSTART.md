# Quick Start Guide - JWT Authentication

## 🚀 Get Started in 3 Steps

### Step 1: Start Database
```bash
docker compose up -d
```

### Step 2: Run Application  
```bash
./mvnw spring-boot:run
```

Wait for: `Started SummarizerApplication in X.XXX seconds`

### Step 3: Test It!

#### Option A: Use the test script
```bash
./test_jwt.sh
```

#### Option B: Manual testing

**1. Get a JWT token:**
```bash
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"demo"}'
```

Save the `token` value from the response.

**2. Test public endpoint (no token needed):**
```bash
curl http://localhost:8080/api/public
```

**3. Test protected endpoint (needs token):**
```bash
curl http://localhost:8080/api/protected \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

## 📋 What You Get

✅ `/login` - Login endpoint that returns JWT token  
✅ `/api/public` - Anonymous endpoint (no auth)  
✅ `/api/protected` - Protected endpoint (requires JWT)  
✅ PostgreSQL database with user management  
✅ BCrypt password hashing  
✅ Two demo users: `demo/demo` and `admin/admin`

## 📖 More Info

- **Full Documentation**: See `JWT_AUTH_README.md`
- **Implementation Details**: See `IMPLEMENTATION_SUMMARY.md`

## 🔑 Demo Credentials

| Username | Password | Role |
|----------|----------|------|
| demo     | demo     | ROLE_USER |
| admin    | admin    | ROLE_ADMIN |

## ⚙️ Configuration

- **Application Port**: 8080
- **Database**: PostgreSQL on localhost:5432
- **Token Expiry**: 60 minutes
- **Database**: auto-created with Hibernate

## 🛑 Troubleshooting

**Port already in use?**
```bash
# Check what's using port 8080
lsof -i :8080
# Kill it or change the port in application.yaml
```

**Database connection failed?**
```bash
# Check if PostgreSQL is running
docker compose ps
# Restart if needed
docker compose restart
```

**Token expired?**
```bash
# Just login again to get a new token
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"demo"}'
```

---

**That's it! You're ready to go!** 🎉

