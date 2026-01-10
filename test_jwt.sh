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

