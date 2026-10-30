# Smart Network Analyzer — Web Frontend

A modern, Apple-inspired Web Dashboard for the **Smart Network Analyzer**, built with **React**, **Vite**, and **Tailwind CSS**.

It connects to the same central FastAPI backend and shared SQLite database as the Android mobile app, allowing you to seamlessly sign in with the same account and see synchronized telemetry, speed tests, Wi-Fi scans, signal maps, and AI diagnostics.

---

## ⚡ Quick Start

### 1. Ensure the Backend Server is Running
Before launching the web frontend, start the backend server:
```bash
# Windows
cd ..\Backend
start_server.bat

# Linux / macOS
cd ../Backend
./start_server.sh
```
Verify the backend is live at `http://localhost:8000/api/v1/health`.

### 2. Start the Web Frontend
```bash
# Install dependencies (first time only)
npm install

# Start development server
npm run dev
# or double click start_web.bat on Windows
```

Open your browser at:
👉 **`http://localhost:5173`**

---

## 🌐 Unified Multi-Platform Features
- **Shared Authentication**: Register on Android phone, log in on the Web app with the same credentials (or vice versa).
- **Synchronized Telemetry**: Live network health score, download/upload throughput benchmarks, and latency checks.
- **Spectrum & Wi-Fi Scanner**: RF channel crowding distribution across 2.4 GHz and 5 GHz bands.
- **2D Signal Heatmaps**: Visual floor plan coverage with click-to-measure RSSI sampling.
- **Groq LLM AI Diagnostics**: Multi-dimensional network health analysis with actionable remediation recommendations.
- **Security Audit**: Encryption analysis, ARP spoofing detection, and rogue AP verification.
- **Executive Audit Reports**: PDF compilation and export.
