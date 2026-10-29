# Smart Network Analyzer - Android Mobile App 📱

Native Android mobile application built with **Java 17, ViewBinding, Material 3, and DynamicAnimation springs**, following modern Apple Human Interface Guidelines standards.

---

## 🚀 Quick Setup: Deploy to Phone via USB

The easiest and fastest way to build and run the app on your physical phone:

### 1. Enable USB Debugging on your Phone
1. Go to **Settings > About Phone** and tap **Build Number** 7 times.
2. In **Developer Options**, toggle **ON**:
   - **USB Debugging**
   - **Install via USB** (on devices that prompt for it)
3. Connect the phone to your laptop using a USB cable and tap **Allow** on the screen.

### 2. Run the Deployment Script
- **Windows:**
  Double-click `install_and_run_on_phone.bat` or run:
  ```powershell
  .\install_and_run_on_phone.bat
  ```
- **macOS / Linux:**
  ```bash
  chmod +x install_and_run_on_phone.sh
  ./install_and_run_on_phone.sh
  ```

This script automatically locates `adb`, builds the APK, maps `adb reverse tcp:8000 tcp:8000`, installs the APK, and launches the app directly on your phone!

---

## 🛠️ Manual Build & Install

```bash
# 1. Build the debug APK with Gradle
# Windows:
.\gradlew.bat assembleDebug

# macOS / Linux:
./gradlew assembleDebug

# 2. Forward the FastAPI backend port over USB
adb reverse tcp:8000 tcp:8000

# 3. Install onto connected Android device
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 4. Launch the app
adb shell am start -n com.simats.smartnetworkanalyzerfrontend/.ui.SplashActivity
```

---

## 🔑 Demo Account Credentials
- **Email:** `demo@example.com`
- **Password:** `password123` (or `Demo@1234` if seeded via `seed_demo.py`)
- **Server URL:** `http://localhost:8000/api/v1/` (connects automatically through USB reverse proxy)

---

## 🎨 Design System
For a deep dive into semantic color tokens, typography scales, squircle geometry, and physical spring animations, see [`DESIGN_SYSTEM.md`](DESIGN_SYSTEM.md).
