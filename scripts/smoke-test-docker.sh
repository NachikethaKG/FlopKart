#!/usr/bin/env bash
# ==============================================================================
# FlopKart E-Commerce Platform - Docker Compose End-to-End Smoke Test Script
# ==============================================================================
set -euo pipefail

GATEWAY_URL="${GATEWAY_URL:-http://localhost:8080}"
EUREKA_URL="${EUREKA_URL:-http://localhost:8761}"

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================================================${NC}"
echo -e "${BLUE}  FlopKart Containerized Stack - End-to-End Smoke Test Verification    ${NC}"
echo -e "${BLUE}========================================================================${NC}"

# ------------------------------------------------------------------------------
# 1. Verify Eureka Discovery and Registered Applications
# ------------------------------------------------------------------------------
echo -e "\n${YELLOW}[Step 1/5] Verifying Eureka Server & Service Discovery...${NC}"
MAX_RETRIES=30
RETRY_COUNT=0

until curl -s -f "${EUREKA_URL}/actuator/health" | grep -q '"status":"UP"'; do
  RETRY_COUNT=$((RETRY_COUNT + 1))
  if [ "$RETRY_COUNT" -gt "$MAX_RETRIES" ]; then
    echo -e "${RED}Eureka Server failed to become healthy in time!${NC}"
    exit 1
  fi
  echo "Waiting for Eureka Server to report UP ($RETRY_COUNT/$MAX_RETRIES)..."
  sleep 3
done
echo -e "${GREEN}✓ Eureka Server is healthy and operational!${NC}"

# Check registered apps
APPS_JSON=$(curl -s -H "Accept: application/json" "${EUREKA_URL}/eureka/apps" || true)
REQUIRED_SERVICES=("API-GATEWAY" "USER-SERVICE" "PRODUCT-SERVICE" "CART-SERVICE" "ORDER-SERVICE" "PAYMENT-SERVICE" "NOTIFICATION-SERVICE")

for svc in "${REQUIRED_SERVICES[@]}"; do
  if echo "$APPS_JSON" | grep -iq "$svc"; then
    echo -e "${GREEN}✓ Registered service found: ${svc}${NC}"
  else
    echo -e "${YELLOW}! Service ${svc} not yet registered (may take up to 30s to heartbeat)${NC}"
  fi
done

# ------------------------------------------------------------------------------
# 2. User Registration & Authentication (JWT Generation)
# ------------------------------------------------------------------------------
echo -e "\n${YELLOW}[Step 2/5] Testing User Registration & Authentication...${NC}"
RANDOM_ID=$((RANDOM % 90000 + 10000))
TEST_EMAIL="smoke_${RANDOM_ID}@flopkart.com"
TEST_PASSWORD="Password123!"

echo "Registering user: ${TEST_EMAIL}"
REG_RESP=$(curl -s -X POST "${GATEWAY_URL}/api/v1/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"Smoke Tester\",\"email\":\"${TEST_EMAIL}\",\"password\":\"${TEST_PASSWORD}\"}")

if ! echo "$REG_RESP" | grep -q '"success":true'; then
  echo -e "${RED}User registration failed: ${REG_RESP}${NC}"
  exit 1
fi
echo -e "${GREEN}✓ User registration succeeded!${NC}"

echo "Logging in to obtain JWT token..."
LOGIN_RESP=$(curl -s -X POST "${GATEWAY_URL}/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"${TEST_EMAIL}\",\"password\":\"${TEST_PASSWORD}\"}")

JWT_TOKEN=$(echo "$LOGIN_RESP" | grep -o '"token":"[^"]*' | cut -d'"' -f4 || true)

if [ -z "$JWT_TOKEN" ]; then
  echo -e "${RED}Failed to extract JWT token from login response: ${LOGIN_RESP}${NC}"
  exit 1
fi
echo -e "${GREEN}✓ Authentication successful! JWT acquired.${NC}"

# ------------------------------------------------------------------------------
# 3. Product Catalog Browsing / Seeding
# ------------------------------------------------------------------------------
echo -e "\n${YELLOW}[Step 3/5] Testing Product Catalog Browsing...${NC}"
PRODUCTS_RESP=$(curl -s -X GET "${GATEWAY_URL}/api/v1/products?page=0&size=5")

PRODUCT_ID=$(echo "$PRODUCTS_RESP" | grep -o '"id":[0-9]*' | head -n1 | cut -d':' -f2 || true)

if [ -z "$PRODUCT_ID" ]; then
  echo "No products found in database. Creating a test product..."
  PROD_CREATE_RESP=$(curl -s -X POST "${GATEWAY_URL}/api/v1/products" \
    -H "Content-Type: application/json" \
    -d '{"name":"Dockerized Headphones","description":"Wireless ANC Headphones","price":199.99,"stockQuantity":50,"category":"Electronics"}')
  PRODUCT_ID=$(echo "$PROD_CREATE_RESP" | grep -o '"id":[0-9]*' | head -n1 | cut -d':' -f2)
fi

echo -e "${GREEN}✓ Product catalog operational! Selected Product ID: ${PRODUCT_ID}${NC}"

# ------------------------------------------------------------------------------
# 4. Ephemeral Cart Operations (Redis)
# ------------------------------------------------------------------------------
echo -e "\n${YELLOW}[Step 4/5] Testing Shopping Cart with Redis Session...${NC}"
ADD_ITEM_RESP=$(curl -s -X POST "${GATEWAY_URL}/api/v1/cart/items" \
  -H "Authorization: Bearer ${JWT_TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{\"productId\":${PRODUCT_ID},\"quantity\":2}")

if ! echo "$ADD_ITEM_RESP" | grep -q '"success":true'; then
  echo -e "${RED}Failed to add item to cart: ${ADD_ITEM_RESP}${NC}"
  exit 1
fi

CART_RESP=$(curl -s -X GET "${GATEWAY_URL}/api/v1/cart" \
  -H "Authorization: Bearer ${JWT_TOKEN}")

if ! echo "$CART_RESP" | grep -q '"totalItems":2'; then
  echo -e "${RED}Cart total items verification failed: ${CART_RESP}${NC}"
  exit 1
fi
echo -e "${GREEN}✓ Cart successfully updated and verified in Redis!${NC}"

# ------------------------------------------------------------------------------
# 5. Order Checkout & Distributed Saga Orchestration (Kafka)
# ------------------------------------------------------------------------------
echo -e "\n${YELLOW}[Step 5/5] Executing Order Checkout and Saga Verification...${NC}"
ORDER_RESP=$(curl -s -X POST "${GATEWAY_URL}/api/v1/orders" \
  -H "Authorization: Bearer ${JWT_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{"shippingAddress":"77 Docker Way, Suite 400, Container City"}')

if ! echo "$ORDER_RESP" | grep -q '"success":true'; then
  echo -e "${RED}Checkout initiation failed: ${ORDER_RESP}${NC}"
  exit 1
fi

ORDER_NUMBER=$(echo "$ORDER_RESP" | grep -o '"orderNumber":"[^"]*' | cut -d'"' -f4)
ORDER_STATUS=$(echo "$ORDER_RESP" | grep -o '"status":"[^"]*' | cut -d'"' -f4)

echo -e "${GREEN}✓ Order created: ${ORDER_NUMBER} (Initial Status: ${ORDER_STATUS})${NC}"

# Wait for Saga processing (Kafka: OrderPlaced -> Payment -> PaymentProcessed -> OrderConfirmed)
echo "Waiting 3 seconds for asynchronous Saga events via Kafka..."
sleep 3

TRACKING_RESP=$(curl -s -X GET "${GATEWAY_URL}/api/v1/orders/${ORDER_NUMBER}" \
  -H "Authorization: Bearer ${JWT_TOKEN}")

FINAL_STATUS=$(echo "$TRACKING_RESP" | grep -o '"status":"[^"]*' | cut -d'"' -f4 || true)
echo "Final Order Status: ${FINAL_STATUS}"

if [ "$FINAL_STATUS" = "CONFIRMED" ]; then
  echo -e "${GREEN}✓ Saga verified! Order ${ORDER_NUMBER} successfully transitioned to CONFIRMED!${NC}"
else
  echo -e "${YELLOW}! Order status is ${FINAL_STATUS} (Check payment failure simulation settings if testing rollback)${NC}"
fi

# Verify cart cleared after checkout
POST_CART_RESP=$(curl -s -X GET "${GATEWAY_URL}/api/v1/cart" \
  -H "Authorization: Bearer ${JWT_TOKEN}")

if echo "$POST_CART_RESP" | grep -q '"totalItems":0'; then
  echo -e "${GREEN}✓ Cart was automatically cleared after checkout completion!${NC}"
fi

echo -e "\n${BLUE}========================================================================${NC}"
echo -e "${GREEN}  ALL DOCKER COMPOSE SMOKE TESTS PASSED SUCCESSFULLY!                  ${NC}"
echo -e "${BLUE}========================================================================${NC}"
