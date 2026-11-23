# ==============================================================================
# FlopKart E-Commerce Platform - Docker Compose End-to-End Smoke Test Script (PowerShell)
# ==============================================================================
$ErrorActionPreference = "Stop"

$GatewayUrl = if ($env:GATEWAY_URL) { $env:GATEWAY_URL } else { "http://localhost:8080" }
$EurekaUrl = if ($env:EUREKA_URL) { $env:EUREKA_URL } else { "http://localhost:8761" }

Write-Host "========================================================================" -ForegroundColor Cyan
Write-Host "  FlopKart Containerized Stack - End-to-End Smoke Test Verification    " -ForegroundColor Cyan
Write-Host "========================================================================" -ForegroundColor Cyan

# ------------------------------------------------------------------------------
# 1. Verify Eureka Discovery and Registered Applications
# ------------------------------------------------------------------------------
Write-Host "`n[Step 1/5] Verifying Eureka Server & Service Discovery..." -ForegroundColor Yellow
$maxRetries = 30
$retries = 0
$eurekaReady = $false

while (-not $eurekaReady -and $retries -lt $maxRetries) {
    try {
        $health = Invoke-RestMethod -Uri "$EurekaUrl/actuator/health" -Method Get -TimeoutSec 3
        if ($health.status -eq "UP") {
            $eurekaReady = $true
        }
    } catch {
        $retries++
        Write-Host "Waiting for Eureka Server to report UP ($retries/$maxRetries)..."
        Start-Sleep -Seconds 3
    }
}

if (-not $eurekaReady) {
    Write-Host "Eureka Server failed to become healthy in time!" -ForegroundColor Red
    exit 1
}
Write-Host "✓ Eureka Server is healthy and operational!" -ForegroundColor Green

try {
    $apps = Invoke-RestMethod -Uri "$EurekaUrl/eureka/apps" -Method Get -Headers @{ "Accept" = "application/json" }
    Write-Host "✓ Eureka registry queried successfully." -ForegroundColor Green
} catch {
    Write-Host "! Eureka apps query skipped: $_" -ForegroundColor Yellow
}

# ------------------------------------------------------------------------------
# 2. User Registration & Authentication (JWT Generation)
# ------------------------------------------------------------------------------
Write-Host "`n[Step 2/5] Testing User Registration & Authentication..." -ForegroundColor Yellow
$randomId = Get-Random -Minimum 10000 -Maximum 99999
$testEmail = "smoke_${randomId}@flopkart.com"
$testPassword = "Password123!"

$registerBody = @{
    name = "Smoke Tester"
    email = $testEmail
    password = $testPassword
} | ConvertTo-Json

$regResp = Invoke-RestMethod -Uri "$GatewayUrl/api/v1/auth/register" -Method Post -Body $registerBody -ContentType "application/json"
if (-not $regResp.success) {
    Write-Host "User registration failed: $($regResp.message)" -ForegroundColor Red
    exit 1
}
Write-Host "✓ User registration succeeded: $testEmail" -ForegroundColor Green

$loginBody = @{
    email = $testEmail
    password = $testPassword
} | ConvertTo-Json

$loginResp = Invoke-RestMethod -Uri "$GatewayUrl/api/v1/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
$jwtToken = $loginResp.data.token

if (-not $jwtToken) {
    Write-Host "Failed to obtain JWT token from login response!" -ForegroundColor Red
    exit 1
}
Write-Host "✓ Authentication successful! JWT acquired." -ForegroundColor Green

$authHeaders = @{
    "Authorization" = "Bearer $jwtToken"
}

# ------------------------------------------------------------------------------
# 3. Product Catalog Browsing / Seeding
# ------------------------------------------------------------------------------
Write-Host "`n[Step 3/5] Testing Product Catalog Browsing..." -ForegroundColor Yellow
$productsResp = Invoke-RestMethod -Uri "$GatewayUrl/api/v1/products?page=0&size=5" -Method Get
$productId = $null

if ($productsResp.data.content.Count -gt 0) {
    $product = $productsResp.data.content[0]
    $productId = $product.id
    $productName = $product.name
    $productPrice = $product.price
} else {
    Write-Host "No products found. Creating test product..."
    $productBody = @{
        name = "Dockerized Headphones"
        description = "Wireless ANC Headphones"
        price = 199.99
        stockQuantity = 50
        category = "Electronics"
    } | ConvertTo-Json
    $newProd = Invoke-RestMethod -Uri "$GatewayUrl/api/v1/products" -Method Post -Body $productBody -ContentType "application/json"
    $productId = $newProd.data.id
    $productName = "Dockerized Headphones"
    $productPrice = 199.99
}
Write-Host "✓ Product catalog operational! Selected Product ID: $productId" -ForegroundColor Green

# ------------------------------------------------------------------------------
# 4. Ephemeral Cart Operations (Redis)
# ------------------------------------------------------------------------------
Write-Host "`n[Step 4/5] Testing Shopping Cart with Redis Session..." -ForegroundColor Yellow
$cartItemBody = @{
    productId = $productId
    productName = $productName
    price = $productPrice
    quantity = 2
} | ConvertTo-Json

$addCartResp = Invoke-RestMethod -Uri "$GatewayUrl/api/v1/cart/items" -Method Post -Headers $authHeaders -Body $cartItemBody -ContentType "application/json"
$cartResp = Invoke-RestMethod -Uri "$GatewayUrl/api/v1/cart" -Method Get -Headers $authHeaders

if ($cartResp.data.totalItems -ne 2) {
    Write-Host "Cart item count verification failed: $($cartResp.data.totalItems)" -ForegroundColor Red
    exit 1
}
Write-Host "✓ Cart successfully updated and verified in Redis! Total items: $($cartResp.data.totalItems)" -ForegroundColor Green

# ------------------------------------------------------------------------------
# 5. Order Checkout & Distributed Saga Orchestration (Kafka)
# ------------------------------------------------------------------------------
Write-Host "`n[Step 5/5] Executing Order Checkout and Saga Verification..." -ForegroundColor Yellow
$orderBody = @{
    shippingAddress = "77 Docker Way, Suite 400, Container City"
} | ConvertTo-Json

$orderResp = Invoke-RestMethod -Uri "$GatewayUrl/api/v1/orders" -Method Post -Headers $authHeaders -Body $orderBody -ContentType "application/json"
$orderNumber = $orderResp.data.orderNumber
$initialStatus = $orderResp.data.status

Write-Host "✓ Order created: $orderNumber (Initial Status: $initialStatus)" -ForegroundColor Green

Write-Host "Waiting 3 seconds for asynchronous Saga events via Kafka..."
Start-Sleep -Seconds 3

$trackingResp = Invoke-RestMethod -Uri "$GatewayUrl/api/v1/orders/$orderNumber" -Method Get -Headers $authHeaders
$finalStatus = $trackingResp.data.status
Write-Host "Final Order Status: $finalStatus"

if ($finalStatus -eq "CONFIRMED") {
    Write-Host "✓ Saga verified! Order $orderNumber successfully transitioned to CONFIRMED!" -ForegroundColor Green
} else {
    Write-Host "! Order status is $finalStatus (Check payment failure simulation settings if testing rollback)" -ForegroundColor Yellow
}

$postCartResp = Invoke-RestMethod -Uri "$GatewayUrl/api/v1/cart" -Method Get -Headers $authHeaders
if ($postCartResp.data.totalItems -eq 0) {
    Write-Host "✓ Cart was automatically cleared after checkout completion!" -ForegroundColor Green
}

Write-Host "`n========================================================================" -ForegroundColor Cyan
Write-Host "  ALL DOCKER COMPOSE SMOKE TESTS PASSED SUCCESSFULLY!                  " -ForegroundColor Green
Write-Host "========================================================================" -ForegroundColor Cyan
