# Smart Network Analyzer 📡⚡

[![FastAPI](https://img.shields.io/badge/FastAPI-0.115+-009688.svg?style=flat&logo=FastAPI&logoColor=white)](https://fastapi.tiangolo.com)
[![React](https://img.shields.io/badge/React-18%2B-61DAFB.svg?style=flat&logo=react&logoColor=black)](https://react.dev)
[![Vite](https://img.shields.io/badge/Vite-6%2B-646CFF.svg?style=flat&logo=vite&logoColor=white)](https://vitejs.dev)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind-3.4+-38B2AC.svg?style=flat&logo=tailwind-css&logoColor=white)](https://tailwindcss.com)
[![Android](https://img.shields.io/badge/Android-Java%2017%20%7C%20SDK%2035-3DDC84.svg?style=flat&logo=android&logoColor=white)](https://developer.android.com)
[![Groq LLM](https://img.shields.io/badge/AI%20Engine-Groq%20Cloud-F05A28.svg?style=flat)](https://groq.com)
[![License](https://img.shields.io/badge/License-MIT-purple.svg)](LICENSE)

A unified, full-stack network intelligence platform featuring:
1. **Python FastAPI Core**: High-throughput REST API with SQLite database, Groq LLM diagnostics, and JWT auth.
2. **Web Dashboard (`WebFrontend`)**: Modern React + Vite + Tailwind CSS web application for your laptop/browser.
3. **Android Mobile App (`Frontend`)**: Native Android app (Java, ViewBinding, Material 3) with custom launcher logo and edge-to-edge system insets.

> **One Central Server & Database**: When you create an account or run tests on your phone, you can log into the exact same account on your laptop via the Web App—all records, scans, heatmaps, and AI health scores sync instantly!

---

## 🏗️ System Architecture

```
                    ┌──────────────────────────────────────────────┐
                    │            Android Phone (Mobile)            │
                    │      (Java 17, Material 3, ViewBinding)      │
                    └──────────────────────┬───────────────────────┘
                                           │
                         USB Reverse Proxy / Wi-Fi IP
                        (http://localhost:8000/api/v1/)
                                           │
                                           ▼
 ┌────────────────────────┐      ┌───────────────────────────┐      ┌─────────────────────────┐
 │   Laptop Web Browser   │ ───► │   FastAPI Backend Core    │ ◄─── │    Groq Cloud LLM AI    │
 │ (React 18 + Vite Web)  │      │   (Python 3.10+, Port 8000│      │   (DeepSeek/Llama-3.3)  │
 └────────────────────────┘      └─────────────┬─────────────┘      └─────────────────────────┘
   http://localhost:5173                       │
                                               ▼
                                 ┌───────────────────────────┐
                                 │   Shared SQLite Database  │
                                 │ (Backend/smart_network.db)│
                                 └───────────────────────────┘
```

---

## ⚡ Super-Fast Quick Start (Launch Everything in 3 Minutes)

### Prerequisites on Your Laptop:
- **Python 3.10+** (with `pip`)
- **Node.js 18+** (with `npm`)
- **Java JDK 17** (or Android Studio)
- An Android phone connected via USB cable (with **Developer Options > USB Debugging** turned ON)

---

### STEP 1: Clone the Repository
```bash
git clone https://github.com/srihari-1711/Smart-Network-Analyzer.git
cd "Smart Network Analyzer"
```

---

### STEP 2: Start the Backend Server (Terminal 1)

#### Windows (One-Click):
Double-click `run_backend.bat` in the root folder, or run:
```powershell
.\run_backend.bat
```

#### macOS / Linux:
```bash
cd Backend
chmod +x start_server.sh
./start_server.sh
```

*(This automatically creates `.venv`, installs Python requirements, runs database migrations, and starts the FastAPI server at `http://localhost:8000`).*
- Health Check: `http://localhost:8000/api/v1/health` -> `{"status":"ok"}`
- Interactive Docs: `http://localhost:8000/docs`

---

### STEP 3: Start the Web Dashboard (Terminal 2)

#### Windows (One-Click):
Double-click `run_web.bat` in the root folder, or run:
```powershell
.\run_web.bat
```

#### macOS / Linux:
```bash
cd WebFrontend
npm install
npm run dev
```

Open your browser at:
👉 **[http://localhost:5173](http://localhost:5173)**

---

### STEP 4: Install & Launch the App on Your Phone (Terminal 3)

1. Connect your Android phone to your laptop using a USB cable.
2. Ensure **USB Debugging** is enabled on the phone (**Settings > Developer Options > USB Debugging**).
3. If your phone shows a popup asking *"Allow USB debugging?"*, check **"Always allow from this computer"** and tap **Allow**.

#### Windows (One-Click):
Double-click `install_phone_app.bat` in the root folder, or run:
```powershell
.\install_phone_app.bat
```

#### macOS / Linux:
```bash
cd Frontend
chmod +x install_and_run_on_phone.sh
./install_and_run_on_phone.sh
```

**What this does automatically:**
1. Detects your connected phone via `adb devices`.
2. Sets up USB port mapping: `adb reverse tcp:8000 tcp:8000`.
3. Builds the Android APK with Gradle.
4. Installs the app onto your phone with the custom radar beacon logo.
5. Launches the app directly on your screen!

---

## 🔄 Cross-Platform Verification (Phone ↔ Web)

Try this out to see the shared database in action:
1. **On your Phone**: Open the app and tap **"Sign Up"**. Enter your name, email (e.g. `alex@example.com`), and password.
2. **On your Laptop**: Go to **[http://localhost:5173/login](http://localhost:5173/login)**.
3. Enter the **same email and password** you just typed on your phone.
4. **BOOM!** You are signed into the exact same account. Any speed test run on the phone immediately appears in the Web Dashboard history and analytics!

---

## 🌟 Feature Breakdown

| Feature | Android Phone App | Web Dashboard | Backend Core |
|---|---|---|---|
| **Auth & Sessions** | JWT Access & Refresh Tokens | Shared JWT Storage & Interceptors | FastAPI + Argon2 Password Hashing |
| **Health Score Ring** | Animated Vector Ring + Pulse | SVG Gradient Arc + Status Badge | Multi-metric composite engine |
| **Wi-Fi Scanner** | Real hardware Wi-Fi RSSI scan | 2.4 / 5 GHz Spectrum Crowding Chart | BSSID / Channel crowd analyzer |
| **Speed Benchmark** | Gauge + Download/Upload throughput | Animated Speedometer + Stats | Historical speed metrics store |
| **Ping & Latency** | ICMP socket ping + jitter | Target Host Picker + RTT chart | Latency & packet loss logger |
| **Signal Heatmaps** | 2D Room canvas with tap-to-record | Interactive 2D Heatmap visualizer | Coordinate points & coverage store |
| **Security Audit** | Rogue AP & ARP spoofing scanner | Posture checklist + Risk score | Rule-based & heuristic threat engine |
| **AI Diagnostics** | Groq LLM integration | Executive synthesis + action plan | Cloud Groq API (`openai/gpt-oss-20b`) |
| **Audit Reports** | PDF preview & intent share | Instant PDF generation & download | HTML to PDF rendering engine |

---

## 🔧 Useful Commands & Troubleshooting

### 1. Phone cannot connect to backend?
If running over USB, ensure port 8000 is forwarded:
```bash
adb reverse tcp:8000 tcp:8000
```
If connecting over Wi-Fi (without USB):
- Find your laptop's local IP: `ipconfig` (Windows) or `ifconfig` (Mac/Linux).
- In the app's Login screen, tap **"Configure Server URL"** and set: `http://<YOUR_LAPTOP_IP>:8000/api/v1/`.

### 2. Check Device Status:
```bash
adb devices
```
If it shows `unauthorized`, unlock your phone and tap **"Allow"** on the USB debugging prompt.

### 3. Reset App State on Phone:
```bash
adb shell pm clear com.simats.smartnetworkanalyzerfrontend
```

---

## 📜 Repository Structure
```
├── Backend/                 # Python FastAPI REST API & SQLite DB
│   ├── app/                 # Routers, models, services, Groq AI
│   ├── start_server.bat     # Windows backend launcher
│   └── start_server.sh      # macOS/Linux backend launcher
│
├── Frontend/                # Native Android Mobile Application
│   ├── app/src/main/        # Java code, XML layouts, custom logo
│   ├── install_and_run_on_phone.bat  # Windows one-click installer
│   └── install_and_run_on_phone.sh   # macOS/Linux installer
│
├── WebFrontend/             # React + Vite + Tailwind Web Application
│   ├── src/                 # Pages, components, API client, contexts
│   └── start_web.bat        # Windows web launcher
│
├── run_backend.bat          # Root shortcut: Start Backend
├── run_web.bat              # Root shortcut: Start Web App
├── install_phone_app.bat    # Root shortcut: Install on Phone
└── README.md                # This complete guide
```

---

## 📄 License
This project is licensed under the [MIT License](LICENSE).
