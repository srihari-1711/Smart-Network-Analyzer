# Smart Network Analyzer 📡⚡

[![FastAPI](https://img.shields.io/badge/FastAPI-0.115+-009688.svg?style=flat&logo=FastAPI&logoColor=white)](https://fastapi.tiangolo.com)
[![Python](https://img.shields.io/badge/Python-3.10%2B-blue.svg?style=flat&logo=python&logoColor=white)](https://python.org)
[![Android](https://img.shields.io/badge/Android-Java%2017%20%7C%20SDK%2035-3DDC84.svg?style=flat&logo=android&logoColor=white)](https://developer.android.com)
[![Material 3](https://img.shields.io/badge/UI%2FUX-Apple--Level%20Material%203-0066FF.svg?style=flat)](Frontend/DESIGN_SYSTEM.md)
[![License](https://img.shields.io/badge/License-MIT-purple.svg)](LICENSE)

A production-grade, full-stack network intelligence platform combining a high-performance **Python FastAPI** backend with a native **Android (Java, ViewBinding, Material 3)** mobile application. Designed to Apple Human Interface Guidelines standards with a deep obsidian palette, spring animations, glowing gauges, and 2D signal heatmaps.

---

## 📱 Quick Start: Run on Your Phone via USB (Easiest Way!)

You can set this up on any laptop and run it directly on your Android phone using a standard USB cable. No complex Wi-Fi IP configuration or router firewall adjustments needed—port 8000 is automatically mapped over the USB cable via `adb reverse`.

### Step 1: Prepare Your Phone (1 minute)
1. On your Android phone, go to **Settings > About Phone**.
2. Tap **Build Number** 7 times to enable **Developer Options**.
3. Go to **Settings > System > Developer Options** (or search "Developer Options"):
   - Turn **ON** **Developer Options**.
   - Turn **ON** **USB Debugging**.
   - Turn **ON** **Install via USB** (if shown on Xiaomi/Realme/Oppo/OnePlus).
4. Connect your phone to your laptop using a USB cable.
5. A popup will appear on your phone: tap **"Always allow from this computer"** and tap **Allow**.

---

### Step 2: Start the Backend Server (Terminal 1)

#### On Windows:
Double-click `Backend\start_server.bat` or run:
```powershell
cd Backend
.\start_server.bat
```

#### On macOS / Linux:
```bash
cd Backend
chmod +x start_server.sh
./start_server.sh
```

*(This automatically creates `.venv`, installs requirements, applies SQLite database migrations, and boots the FastAPI server at `http://0.0.0.0:8000`)*.

---

### Step 3: Install & Launch App on Your Phone (Terminal 2)

#### On Windows:
Double-click `Frontend\install_and_run_on_phone.bat` or run:
```powershell
cd Frontend
.\install_and_run_on_phone.bat
```

#### On macOS / Linux:
```bash
cd Frontend
chmod +x install_and_run_on_phone.sh
./install_and_run_on_phone.sh
```

**That's it!** The script will:
1. Locate your Android SDK / `adb` tool automatically.
2. Build the Android debug APK via Gradle.
3. Establish the USB reverse proxy: `adb reverse tcp:8000 tcp:8000`.
4. Install the app onto your connected phone.
5. Launch the app directly on your screen!

---

### Step 4: Sign In (Pre-seeded Demo Account)

The database includes a pre-seeded account with realistic historical network telemetry:
- **Email:** `demo@example.com`
- **Password:** `password123` (or `Demo@1234`)
- **Server URL:** `http://localhost:8000/api/v1/` (working automatically over USB)

---

## 🛠️ Manual Setup & Architecture Details

If you prefer building each component manually or exploring the codebase, follow the instructions below.

### System Architecture

```
 ┌────────────────────────────────────────────────────────────┐
 │                 Android Mobile Application                 │
 │       (Java 17, Material 3, ViewBinding, DynamicAnimation) │
 └─────────────────────────────┬──────────────────────────────┘
                               │
                       USB Reverse Proxy
                  (http://localhost:8000/api/v1/)
                               │
                               ▼
 ┌────────────────────────────────────────────────────────────┐
 │                    FastAPI Backend Router                  │
 │      (Pydantic v2, SQLAlchemy 2.0, Alembic, SQLite/Postgres)│
 └──────┬──────────────────────┬──────────────────────┬───────┘
        │                      │                      │
        ▼                      ▼                      ▼
 ┌──────────────┐       ┌──────────────┐       ┌──────────────┐
 │ Deterministic│       │  Pluggable   │       │  ReportLab   │
 │ Rules Engine │       │   Groq LLM   │       │  PDF Engine  │
 │ (Offline OK) │       │ (Auto-fail)  │       │ (Vector Doc) │
 └──────────────┘       └──────────────┘       └──────────────┘
```

---

### Prerequisites
- **Laptop / PC:**
  - Python 3.10+ (Tested on Python 3.10 to 3.14)
  - Java JDK 17 or JDK 21
  - Android SDK Platform-Tools (`adb`)
- **Phone:**
  - Any Android device running Android 7.0+ (API 24 to API 35)

---

### Backend Manual Setup

1. **Navigate to the Backend directory:**
   ```bash
   cd Backend
   ```

2. **Create and activate a virtual environment:**
   ```bash
   # Windows:
   python -m venv .venv
   .\.venv\Scripts\activate

   # macOS / Linux:
   python3 -m venv .venv
   source .venv/bin/activate
   ```

3. **Install dependencies:**
   ```bash
   pip install -r requirements.txt
   ```

4. **Initialize environment configuration:**
   ```bash
   # Windows:
   copy .env.example .env

   # macOS / Linux:
   cp .env.example .env
   ```

   *(Optional: If you want cloud AI diagnostics, add your free key to `GROQ_API_KEY=gsk_...` in `.env`. If left empty, the deterministic offline rule engine will run automatically).*

5. **Apply database migrations:**
   ```bash
   python -m alembic upgrade head
   ```

6. **Seed realistic demo telemetry:**
   ```bash
   python scripts/seed_demo.py
   ```

7. **Run backend test suite (25/25 tests):**
   ```bash
   python -m pytest tests -v
   ```

8. **Start API server:**
   ```bash
   uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
   ```
   - Interactive Swagger API Docs: `http://localhost:8000/docs`
   - Health check endpoint: `http://localhost:8000/api/v1/health`

---

### Android Frontend Manual Setup

1. **Open `Frontend` in Android Studio** (or build from command line):
   ```bash
   cd Frontend
   # Windows:
   .\gradlew.bat assembleDebug

   # macOS / Linux:
   ./gradlew assembleDebug
   ```

2. **Connect phone via USB and verify connection:**
   ```bash
   adb devices
   # Should output: <device-id> device
   ```

3. **Map the backend port over USB:**
   ```bash
   adb reverse tcp:8000 tcp:8000
   ```

4. **Install the built APK:**
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

---

## 🌐 Alternative: Running over Local Wi-Fi (Without USB)

If you prefer to disconnect the USB cable and test over your home/office Wi-Fi network:

1. Connect both your laptop and phone to the **same Wi-Fi network**.
2. Find your laptop's local IP address:
   - Windows: Run `ipconfig` (look for `IPv4 Address`, e.g., `192.168.1.15`).
   - macOS / Linux: Run `ifconfig` or `ip a`.
3. If using Windows, allow port 8000 through Windows Defender Firewall:
   ```powershell
   New-NetFirewallRule -DisplayName "FastAPI Smart Network" -Direction Inbound -LocalPort 8000 -Protocol TCP -Action Allow
   ```
4. Open the app on your phone, navigate to **More Tools > Profile & Settings > Server Connection**, and change the URL to:
   ```
   http://192.168.1.15:8000/api/v1/
   ```
5. Tap **Save Server URL**.

---

## ✨ Features & Capabilities

| Feature | Description |
|---|---|
| 🎛️ **Dashboard & Health Score** | Live animated circular health ring, real-time Wi-Fi connection info (SSID, gateway, IP), quick action pods. |
| 🚀 **Speed Test** | Multi-stage download & upload throughput test, glowing gradient sweep gauge with tabular digits and smooth easing. |
| ⏱️ **Latency & Jitter** | 20-probe ICMP/TCP ping engine, RFC 3550 jitter calculation (`J = J + (\|D\| - J)/16`), live probe streaming. |
| 📶 **Wi-Fi Scanner** | Multi-band discovery (2.4 GHz & 5 GHz), channel crowding analysis, radar scanning animation, security protocol audits. |
| 🗺️ **Signal Strength Heatmap** | 2D floor plan coordinate mapping, radial ambient falloff, one-tap live RSSI auto-sampling, zone pill counters (Strong, Fair, Weak). |
| 📊 **Data Usage Monitor** | Daily, weekly, and monthly cellular vs. Wi-Fi consumption tracking with theme-adaptive stacked bar charts. |
| 🛡️ **Security Audit** | Rogue AP detection, ARP spoofing detection, encryption strength evaluation (WPA3/WPA2/WEP/Open). |
| 🧠 **AI Diagnostics** | Hybrid engine: cloud Groq LLM enriched synthesis with automatic, 100% offline deterministic rule engine fallback. |
| 📄 **PDF Export** | Vector-quality diagnostic report generation and export via Android FileProvider sharing. |

---

## 🎨 Design System

The mobile application follows modern Apple Human Interface Guidelines translated to native Android Material 3:
- **Palette:** Deep Obsidian canvas (`#0A0E17`), Electric Sapphire (`#0066FF`), Cyan (`#0EA5E9`), and AI Iris (`#8B5CF6`).
- **Motion:** Physical `SpringAnimation` with damping ratio `0.75` and stiffness `400` for tactile press responses.
- **Controls:** 52dp pill buttons (`26dp` radius), 20dp squircle cards, and floating outlined text inputs.
- Read the full design specification in [`Frontend/DESIGN_SYSTEM.md`](Frontend/DESIGN_SYSTEM.md).

---

## ❓ Troubleshooting

### 1. `adb: command not found`
- **Fix:** Make sure Android platform-tools is installed. If Android Studio is installed, `adb` is located at:
  - Windows: `%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe`
  - macOS: `~/Library/Android/sdk/platform-tools/adb`
  - Linux: `~/Android/Sdk/platform-tools/adb`
  Add this directory to your system `PATH`.

### 2. `adb devices` shows `unauthorized`
- **Fix:** Look at your phone's screen. A dialog will prompt you to "Allow USB debugging". Check "Always allow from this computer" and tap **Allow**.

### 3. App displays "Network Error" or "Cannot connect to server"
- **Fix:** Re-run the USB reverse port forward:
  ```bash
  adb reverse tcp:8000 tcp:8000
  ```
  Ensure the backend is running (`http://localhost:8000/api/v1/health` responds in your laptop browser).

### 4. Permission Denied when scanning Wi-Fi or Signal
- **Fix:** On Android 10+, Wi-Fi scanning requires Location permissions. When prompted, tap **"While using the app"** and ensure **Location / GPS is toggled ON** on the phone.

---

## 📄 License
This project is open-source and available under the [MIT License](LICENSE).
