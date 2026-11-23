@echo off
setlocal

:: Switch working directory to the repository root where this script resides
cd /d "%~dp0"

echo ========================================================================
echo   FlopKart E-Commerce Platform - Starting Application
echo ========================================================================
echo.

:: Ensure .env file exists
if not exist "%~dp0.env" (
    echo [INFO] .env file not found. Initializing from .env.example...
    if exist "%~dp0.env.example" (
        copy "%~dp0.env.example" "%~dp0.env" >nul
        echo [OK] Initialized .env with default settings.
    )
)

:: 1. Verify Docker Engine is running
echo [1/3] Checking Docker engine status...
docker info >nul 2>&1
if %ERRORLEVEL% equ 0 goto docker_ready

echo [INFO] Docker daemon is not responding. Attempting to launch Docker Desktop...
if exist "C:\Program Files\Docker\Docker\Docker Desktop.exe" (
    start "" "C:\Program Files\Docker\Docker\Docker Desktop.exe"
)
echo Waiting for Docker daemon to become responsive (up to 45 seconds)...
set retries=0

:wait_docker
timeout /t 3 /nobreak >nul
docker info >nul 2>&1
if %ERRORLEVEL% equ 0 goto docker_ready
set /a retries+=1
if %retries% lss 15 (
    echo Waiting for Docker daemon... (%retries%/15)
    goto wait_docker
)

echo.
echo [ERROR] Docker engine could not be contacted.
echo Please make sure Docker Desktop is launched and the Docker engine is running,
echo then re-run this script.
echo.
echo %cmdcmdline% | findstr /i /c:"/c" >nul && pause
exit /b 1

:docker_ready
echo [OK] Docker engine is active and ready.
echo.

:: 2. Launch Backend Microservices & Infrastructure
echo [2/3] Starting backend microservices and databases via Docker Compose...
docker compose up -d
if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERROR] Docker Compose failed to start the containers.
    echo %cmdcmdline% | findstr /i /c:"/c" >nul && pause
    exit /b 1
)
echo [OK] Backend containers started successfully.
echo.

:: 3. Launch Frontend Development Server
echo [3/3] Checking Frontend development server...
powershell -NoProfile -Command "Get-NetTCPConnection -LocalPort 5173 -State Listen -ErrorAction SilentlyContinue" >nul 2>&1
if %ERRORLEVEL% equ 0 (
    echo [INFO] Frontend server is already active on port 5173.
) else (
    echo Launching Frontend dev server in a dedicated window...
    start "FlopKart Frontend" cmd /c "cd /d ""%~dp0frontend"" && npm run dev"
    echo [OK] Frontend dev server started.
)

echo.
echo ========================================================================
echo   FlopKart Application is Running!
echo ========================================================================
echo.
echo Access URLs:
echo   - Storefront (Web UI):       http://localhost:5173
echo   - API Gateway:               http://localhost:8080
echo   - Eureka Service Registry:   http://localhost:8761
echo   - Kafka UI Dashboard:        http://localhost:8090
echo.
echo To run the automated end-to-end smoke test:
echo   powershell -ExecutionPolicy Bypass -File "%~dp0scripts\smoke-test-docker.ps1"
echo.
echo To stop everything, run: stop.bat (or drag stop.bat into terminal)
echo ========================================================================
echo.

exit /b 0
