@echo off
echo ========================================================
echo   Starting Smart Network Analyzer Web Frontend (Vite)
echo ========================================================
cd /d "%~dp0\WebFrontend"
if not exist node_modules (
    echo Installing node dependencies...
    call npm install
)
call npm run dev
