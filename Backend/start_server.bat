@echo off
title Smart Network Analyzer - Backend Server
cd /d "%~dp0"

echo ===================================================
echo     Smart Network Analyzer - FastAPI Backend
echo ===================================================
echo.

:: Auto-copy .env if missing
if not exist ".env" (
    if exist ".env.example" (
        echo [*] .env not found. Initializing from .env.example...
        copy ".env.example" ".env" >nul
    )
)

:: Activate virtual environment (.venv or venv)
if exist ".venv\Scripts\activate.bat" (
    echo [*] Activating virtual environment (.venv)...
    call .venv\Scripts\activate.bat
) else if exist "venv\Scripts\activate.bat" (
    echo [*] Activating virtual environment (venv)...
    call venv\Scripts\activate.bat
) else (
    echo [*] Virtual environment not found. Creating one in .venv...
    python -m venv .venv
    call .venv\Scripts\activate.bat
    echo [*] Installing requirements...
    pip install -r requirements.txt
)

echo [*] Checking and applying database migrations...
python -m alembic upgrade head
if errorlevel 1 (
    echo [ERROR] Migration failed!
    pause
    exit /b 1
)

echo [*] Starting Uvicorn API server on 0.0.0.0:8000...
echo [*] Interactive Docs: http://localhost:8000/docs
echo [*] Health Check:    http://localhost:8000/api/v1/health
echo.
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
pause
