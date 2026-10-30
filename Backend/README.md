# Smart Network Analyzer - Backend API 🚀

Production-ready FastAPI backend for network monitoring, speed diagnostics, signal strength mapping, security auditing, AI-powered health analysis, and automated PDF reporting.

---

## 1. Quick Start

### 1.1 Prerequisites
- Python 3.10+ (tested with Python 3.10 through 3.14)
- Git

### 1.2 Automated Launch (Fastest)
- **Windows:** Double-click or run `start_server.bat`
- **macOS / Linux:** Run `chmod +x start_server.sh && ./start_server.sh`

### 1.3 Manual Setup
```bash
# 1. Navigate to the Backend directory
cd Backend

# 2. Create and activate a virtual environment
# Windows:
python -m venv .venv
.\.venv\Scripts\activate

# macOS / Linux:
python3 -m venv .venv
source .venv/bin/activate

# 3. Install dependencies
pip install -r requirements.txt

# 4. Copy environment configuration
# Windows:
copy .env.example .env
# macOS / Linux:
cp .env.example .env

# 5. Apply database migrations
python -m alembic upgrade head

# 6. Seed rich 3-week historical demo dataset
python -m scripts.seed_demo

# 7. Start the development server
python -m uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

Interactive OpenAPI documentation is live at:
- **Swagger UI:** [http://localhost:8000/docs](http://localhost:8000/docs)
- **Health check:** [http://localhost:8000/api/v1/health](http://localhost:8000/api/v1/health)

---

## 2. Environment Variables

| Variable | Default | Description |
|---|---|---|
| `APP_ENV` | `development` | Environment mode (`development` or `production`). In dev mode, password reset token is returned in forgot-password response. |
| `DATABASE_URL` | `sqlite:///./smart_network.db` | SQLAlchemy database connection string. |
| `SECRET_KEY` | (32+ chars string) | Cryptographic secret for signing JWT access tokens. |
| `ACCESS_TOKEN_EXPIRE_MINUTES` | `30` | Access token lifetime in minutes. |
| `REFRESH_TOKEN_EXPIRE_DAYS` | `30` | Refresh token lifetime in days. |
| `CORS_ORIGINS` | `*` | Comma-separated list of allowed origins. |
| `GROQ_API_KEY` | `""` (Empty) | Optional diagnostic model authorization token. |
| `GROQ_MODEL` | `llama-3.3-70b-versatile` | Neural diagnostic model identifier. |
| `GROQ_TIMEOUT_SECONDS` | `30.0` | Request timeout for diagnostic engine. |

### Switching to PostgreSQL
To use PostgreSQL in production instead of SQLite, only change `DATABASE_URL` in `.env`:
```env
DATABASE_URL=postgresql+psycopg2://user:password@localhost:5432/smart_network_db
```
Then run `python -m alembic upgrade head`. No application code changes are needed.

---

## 3. AI Diagnostic Engine & Deterministic Fallback
The backend features an intelligent diagnostic engine to evaluate network performance and generate clear health summaries:
1. **Autonomous Operation:** The backend includes an advanced pure-Python deterministic analysis engine (`source: "rules"`). It automatically audits telemetry, RF channel overlap, latency jitter, and encryption standards.
2. **Seamless Fallback:** The application runs 100% locally and offline without requiring any external cloud setup.

---

## 4. Connecting Android Devices

### Method A: USB Cable (Recommended & Zero Configuration)
1. Plug your phone into the laptop via USB and enable USB Debugging.
2. Run port forwarding:
   ```bash
   adb reverse tcp:8000 tcp:8000
   ```
3. The phone connects directly to `http://localhost:8000/api/v1/` through the USB cable!

### Method B: Over Local Wi-Fi
1. Ensure your PC and phone are on the **same Wi-Fi network**.
2. Find your PC's local LAN IP:
   ```powershell
   ipconfig
   # Look for IPv4 Address, e.g., 192.168.1.50
   ```
3. Allow incoming connections on port 8000 in Windows Firewall:
   ```powershell
   New-NetFirewallRule -DisplayName "FastAPI Smart Network" -Direction Inbound -LocalPort 8000 -Protocol TCP -Action Allow
   ```
4. In the Android app Settings, set the Server URL to `http://<YOUR_PC_LAN_IP>:8000/api/v1/` and tap **Save Server URL**.

---

## 5. Running Automated Tests
Run the automated pytest suite (25/25 tests passing):
```bash
python -m pytest tests -v
```
All tests run against an isolated in-memory SQLite database (`sqlite:///:memory:`) and test all endpoints, cascading deletions, rate limiting, and business logic.
