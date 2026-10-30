#!/usr/bin/env bash
# Smart Network Analyzer - Backend Server (macOS / Linux)
set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$DIR"

echo "==================================================="
echo "    Smart Network Analyzer - FastAPI Backend"
echo "==================================================="
echo

# Auto-copy .env if missing
if [ ! -f ".env" ] && [ -f ".env.example" ]; then
    echo "[*] Initializing .env from .env.example..."
    cp .env.example .env
fi

# Activate or create virtual environment
if [ -f ".venv/bin/activate" ]; then
    echo "[*] Activating virtual environment (.venv)..."
    source .venv/bin/activate
elif [ -f "venv/bin/activate" ]; then
    echo "[*] Activating virtual environment (venv)..."
    source venv/bin/activate
else
    echo "[*] Virtual environment not found. Creating in .venv..."
    python3 -m venv .venv
    source .venv/bin/activate
    echo "[*] Installing dependencies..."
    pip install -r requirements.txt
fi

echo "[*] Applying database migrations..."
python -m alembic upgrade head

echo "[*] Starting Uvicorn API server on 0.0.0.0:8000..."
echo "[*] Interactive Docs: http://localhost:8000/docs"
echo "[*] Health Check:    http://localhost:8000/api/v1/health"
echo
exec uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload

