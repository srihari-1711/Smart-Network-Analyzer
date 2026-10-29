@echo off
title Push and Run Smart Network Analyzer on Connected Phone
cd /d "%~dp0"

echo ================================================================
echo    Smart Network Analyzer - Direct Phone Deployment Tool
echo ================================================================
echo.

:: 1. Auto-detect ADB
set "ADB=adb"
where adb >nul 2>&1
if %ERRORLEVEL% equ 0 (
    goto :adb_found
)

if exist "%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" (
    set "ADB=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"
    goto :adb_found
)

if defined ANDROID_HOME (
    if exist "%ANDROID_HOME%\platform-tools\adb.exe" (
        set "ADB=%ANDROID_HOME%\platform-tools\adb.exe"
        goto :adb_found
    )
)

if defined ANDROID_SDK_ROOT (
    if exist "%ANDROID_SDK_ROOT%\platform-tools\adb.exe" (
        set "ADB=%ANDROID_SDK_ROOT%\platform-tools\adb.exe"
        goto :adb_found
    )
)

echo [ERROR] adb.exe could not be found automatically.
echo Please make sure Android platform-tools is installed or added to your PATH.
echo Expected location: %%LOCALAPPDATA%%\Android\Sdk\platform-tools\adb.exe
pause
exit /b 1

:adb_found
echo [*] Using ADB from: %ADB%
echo.

set "APK=app\build\outputs\apk\debug\app-debug.apk"
set "PKG=com.simats.smartnetworkanalyzerfrontend"
set "ACTIVITY=.ui.SplashActivity"

:: 2. Check / Build APK
if not exist "%APK%" (
    echo [*] Debug APK not found. Building APK with Gradle...
    call gradlew.bat assembleDebug
    if errorlevel 1 (
        echo [ERROR] Gradle build failed!
        pause
        exit /b 1
    )
)

:: 3. Detect Phone via USB
echo [*] Checking for connected Android devices via USB...
"%ADB%" devices
echo.

echo [*] Waiting for device to be detected...
echo [*] Tip: Make sure USB Debugging is ON in Developer Options and tap "Allow USB Debugging" on phone.
"%ADB%" wait-for-device

echo.
echo [*] Device detected!
echo [*] Forwarding backend port 8000 over USB (phone can now reach http://localhost:8000/)...
"%ADB%" reverse tcp:8000 tcp:8000

:: 4. Install APK
echo [*] Installing app to connected device...
"%ADB%" install -r -d "%APK%"
if errorlevel 1 (
    echo.
    echo [*] Checking if phone prompt requires confirmation, retrying in 3 seconds...
    timeout /t 3 >nul
    "%ADB%" install -r -d "%APK%"
)
if errorlevel 1 (
    echo [ERROR] Installation failed! Please check if "Install via USB" is allowed on your phone.
    pause
    exit /b 1
)

:: 5. Launch App
echo.
echo [*] Launching Smart Network Analyzer...
"%ADB%" shell am start -n "%PKG%/%ACTIVITY%"

echo.
echo ================================================================
echo    SUCCESS! App is running on your phone.
echo    Backend port 8000 is mapped via USB reverse proxy.
echo ================================================================
echo.
pause
