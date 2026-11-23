@echo off
setlocal enabledelayedexpansion

:: Switch working directory to the repository root where this script resides
cd /d "%~dp0"

echo ========================================================================
echo   FlopKart E-Commerce Platform - Stopping Application
echo ========================================================================
echo.

:: 1. Stop Frontend Server (listening on port 5173)
echo [1/2] Stopping Frontend development server...
powershell -NoProfile -Command "$conn = Get-NetTCPConnection -LocalPort 5173 -State Listen -ErrorAction SilentlyContinue; if ($conn) { Stop-Process -Id $conn.OwningProcess -Force -ErrorAction SilentlyContinue; Write-Host 'Stopped frontend process (PID: ' $conn.OwningProcess ')' } else { Write-Host 'Frontend was not running.' }"

:: Also close any window titled "FlopKart Frontend"
taskkill /fi "WINDOWTITLE eq FlopKart Frontend*" /f >nul 2>&1
echo [OK] Frontend cleanup complete.
echo.

:: 2. Stop Docker Compose Backend Stack
echo [2/2] Stopping Docker Compose backend microservices and infrastructure...
docker info >nul 2>&1
if %ERRORLEVEL% equ 0 (
    docker compose down
    if !ERRORLEVEL! equ 0 (
        echo [OK] All backend containers and networks stopped.
    ) else (
        echo [WARNING] Docker compose down reported an issue.
    )
) else (
    echo [INFO] Docker daemon is not running; no containers to stop.
)

echo.
echo ========================================================================
echo   FlopKart Application Stopped Successfully!
echo ========================================================================
echo.

exit /b 0
