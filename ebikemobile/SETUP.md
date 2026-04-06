# Android Mobile App - Setup Guide

This guide will walk you through setting up the E-Bike Android mobile app in Android Studio.

## Prerequisites

- **Android Studio**: Iguana 2023.2.1 or later
- **JDK 17**: Or later
- **Android SDK**: API level 34 (recommended)
- **Gradle 8.2+**: Already bundled with Android Studio

## Step 1: Open the Project

1. Open Android Studio
2. Select **File → Open**
3. Navigate to `E-bike/ebikemobile` folder
4. Click **Open**

Android Studio will automatically recognize it as a Gradle project and start syncing.

## Step 2: Wait for Gradle Sync

- Let Gradle finish downloading all dependencies (this may take 5-10 minutes on first run)
- Monitor progress in the "Build" panel at the bottom
- Wait for "BUILD SUCCESSFUL" message

If you see errors, try:
```bash
# From ebikemobile folder
./gradlew --refresh-dependencies
./gradlew clean build
```

## Step 3: Configure Backend Connection

1. Open `app/build.gradle`
2. Locate `buildConfigField "String", "BASE_URL"`
3. Update the URL based on your setup:

   **For Android Emulator:**
   ```gradle
   buildConfigField "String", "BASE_URL", "\"http://10.0.2.2:8080/api/\""
   ```

   **For Physical Device (replace with your IP):**
   ```gradle
   buildConfigField "String", "BASE_URL", "\"http://192.168.1.100:8080/api/\""
   ```

4. Make sure backend is running on your machine: `java -jar target/ebike-0.0.1-SNAPSHOT.jar`

## Step 4: Google OAuth Setup (Optional but Recommended)

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing
3. Enable Google Sign-In API
4. Create OAuth 2.0 credentials:
   - Type: Android
   - Package name: `com.ebike.mobile`
   - Get SHA-1 fingerprint:
   ```bash
   # From Android Studio Terminal or system terminal
   keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
   ```
5. Copy the Client ID from Google Cloud Console
6. Open `app/build.gradle` and update:
   ```gradle
   buildConfigField "String", "GOOGLE_CLIENT_ID", "\"YOUR_CLIENT_ID_HERE\""
   ```

## Step 5: Prepare Emulator/Device

### Using Emulator:
1. In Android Studio, go to **Tools → Device Manager**
2. Create a new virtual device (if none exists):
   - Device: Pixel 6 or similar
   - System Image: Android 14 (API 34)
   - Click **Create Device**
3. Click **Play** to start the emulator
4. Wait for it to fully boot (look for lock/home screen)

### Using Physical Device:
1. Enable **Developer Mode**: Settings → About Phone → tap Build Number 7 times
2. Enable **USB Debugging**: Settings → Developer Options → USB Debugging
3. Connect phone via USB cable
4. Accept USB debugging prompt on device
5. In Android Studio, you'll see the device listed

## Step 6: Build and Run

### Option A: From Android Studio
1. Click **Run** → **Run 'app'** (or press Shift+F10)
2. Select your emulator/device
3. Click **OK**
4. Wait for app to build and install

### Option B: From Terminal
```bash
cd ebikemobile

# Build APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Build and run
./gradlew run
```

## Step 7: Test the App

1. **Login Screen** should appear
2. Use test credentials:
   - Email: `user@example.com`
   - Password: `user123` (if registered)
   - Or click **Sign up** to create account

3. If backend is running, you can:
   - Register new account
   - Login with credentials
   - Browse available bikes
   - Create bookings

## Troubleshooting

### Gradle Sync Issues
```bash
# Clean and rebuild
./gradlew clean
./gradlew build

# Or in Android Studio: File > Invalidate Caches > Invalidate and Restart
```

### Cannot Connect to Backend
- Check backend is running: `curl http://localhost:8080/api/bikes`
- Verify `BASE_URL` in build.gradle matches your backend
- For emulator: use `10.0.2.2` not `localhost`
- For device: use your computer's IP (run `ipconfig` on Windows or `ifconfig` on Mac/Linux)
- Check firewall isn't blocking port 8080

### App Crashes on Startup
- Check Android Studio logcat for errors
- Try clean build: `./gradlew clean build`
- Ensure minimum SDK is 24+ on device

### Cannot compile
- Right-click project → **Synchronize**
- **File → Invalidate Caches → Invalidate and Restart**
- Delete `.gradle` folder and rebuild

### Google Sign-In Not Working
- Verify Client ID in `build.gradle`
- Check SHA-1 fingerprint matches in Google Cloud Console
- Ensure internet connection on device
- Review backend auth logs for JWT errors

## Project Structure Overview

```
ebikemobile/
├── app/
│   ├── src/main/
│   │   ├── java/com/ebike/mobile/
│   │   │   ├── api/              (API client & interceptors)
│   │   │   ├── data/             (Models, repositories, token management)
│   │   │   ├── ui/               (UI screens, theme, viewmodels)
│   │   │   └── MainActivity.kt   (Entry point)
│   │   ├── AndroidManifest.xml
│   │   └── res/                  (Resources)
│   ├── build.gradle              (App dependencies & build config)
│   └── proguard-rules.pro        (Obfuscation rules)
├── build.gradle                  (Root build config)
├── settings.gradle               (Project structure)
├── README.md                     (Quick reference)
└── SETUP.md                      (This file)
```

## Key Features

- ✅ Android 14+ support
- ✅ Material Design 3 UI
- ✅ Jetpack Compose
- ✅ JWT Authentication
- ✅ Google OAuth Integration
- ✅ Real-time API sync
- ✅ Secure token storage
- ✅ Offline-ready architecture

## Next Steps

After successful setup:
1. Log in with test account
2. Browse available bikes
3. Create a booking
4. View rental history
5. Test cancellation with reason

## Need Help?

- Check Android logcat (Android Studio > View > Tool Windows > Logcat)
- Look for red error messages indicating the issue
- Verify backend is responding: `curl http://YOUR_IP:8080/api/bikes`
- Check internet connectivity on device
- Review build.gradle configuration

## Contact

For questions or issues, contact the development team.
