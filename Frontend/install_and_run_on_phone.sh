#!/usr/bin/env bash
# Smart Network Analyzer - Direct Phone Deployment Tool (macOS / Linux)
set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$DIR"

echo "================================================================"
echo "   Smart Network Analyzer - Direct Phone Deployment Tool"
echo "================================================================"
echo

# 1. Locate ADB
if command -v adb >/dev/null 2>&1; then
    ADB="adb"
elif [ -n "$ANDROID_HOME" ] && [ -x "$ANDROID_HOME/platform-tools/adb" ]; then
    ADB="$ANDROID_HOME/platform-tools/adb"
elif [ -n "$ANDROID_SDK_ROOT" ] && [ -x "$ANDROID_SDK_ROOT/platform-tools/adb" ]; then
    ADB="$ANDROID_SDK_ROOT/platform-tools/adb"
elif [ -x "$HOME/Library/Android/sdk/platform-tools/adb" ]; then
    ADB="$HOME/Library/Android/sdk/platform-tools/adb"
elif [ -x "$HOME/Android/Sdk/platform-tools/adb" ]; then
    ADB="$HOME/Android/Sdk/platform-tools/adb"
else
    echo "[ERROR] adb could not be found. Please install Android platform-tools or add it to PATH."
    exit 1
fi

echo "[*] Using ADB from: $ADB"
echo

APK="app/build/outputs/apk/debug/app-debug.apk"
PKG="com.simats.smartnetworkanalyzerfrontend"
ACTIVITY=".ui.SplashActivity"

# 2. Build APK if missing
if [ ! -f "$APK" ]; then
    echo "[*] Building debug APK with Gradle..."
    ./gradlew assembleDebug
fi

# 3. Detect USB device
echo "[*] Checking connected Android devices..."
"$ADB" devices
echo
echo "[*] Waiting for device to be detected..."
echo "[*] Ensure USB Debugging is ON in Developer Options and accepted on screen."
"$ADB" wait-for-device

echo "[*] Forwarding backend port 8000 over USB..."
"$ADB" reverse tcp:8000 tcp:8000

# 4. Install APK
echo "[*] Installing APK on phone..."
"$ADB" install -r -d "$APK"

# 5. Launch App
echo "[*] Launching Smart Network Analyzer on phone..."
"$ADB" shell am start -n "$PKG/$ACTIVITY"

echo
echo "================================================================"
echo "   SUCCESS! App is running on your phone."
echo "   Backend port 8000 is mapped via USB reverse proxy."
echo "================================================================"

