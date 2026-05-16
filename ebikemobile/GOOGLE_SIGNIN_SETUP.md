# Google Sign-In Setup Guide for E-Bike Mobile App

## Overview
This guide will help you configure Google Sign-In for the E-Bike Rental Android app. **This is a REQUIRED setup step** for Google authentication to work.

## Problem
If you're seeing the error **"Sign-In cancelled or not configured properly"**, it means the app hasn't been registered in Google Cloud Console yet.

## Step 1: Create Google Cloud Project

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project:
   - Click "Select a Project" → "NEW PROJECT"
   - Name: `E-Bike Rental Mobile`
   - Click "CREATE"

## Step 2: Enable Google Sign-In API

1. In the Google Cloud Console, search for "Google+ API" in the search bar
2. Click **"Google+ API"** from the results
3. Click the **"ENABLE"** button
4. Wait for it to enable (takes a few seconds)

## Step 3: Create OAuth Client ID

1. Go to **APIs & Services** → **Credentials** (left sidebar)
2. Click **"+ CREATE CREDENTIALS"** button
3. Select **"OAuth Client ID"** from the dropdown
4. You'll see a warning: "You need to configure an OAuth consent screen first"
5. Click **"CONFIGURE CONSENT SCREEN"**

### Configure OAuth Consent Screen

1. Choose **"External"** as User Type
2. Click **"CREATE"**
3. Fill in the form:
   - **App name**: `E-Bike Rental Mobile`
   - **User support email**: Your email
   - **Developer contact information**: Your email
4. Click **"SAVE AND CONTINUE"**
5. On "Scopes" page: Click **"SAVE AND CONTINUE"** (default scopes are fine)
6. On "Test users" page: Click **"SAVE AND CONTINUE"** (no test users needed)
7. Review and click **"BACK TO DASHBOARD"**

### Create Android OAuth Client ID

1. Go back to **APIs & Services** → **Credentials**
2. Click **"+ CREATE CREDENTIALS"** → **"OAuth Client ID"**
3. Select **"Android"** from Application type
4. Fill in the form:
   - **Name**: `E-Bike Mobile App`
   - **Package name**: `com.ebike.mobile`
   - **SHA-1 certificate fingerprint**: Get this from Step 4 below

## Step 4: Get Your App's SHA-1 Fingerprint

The SHA-1 fingerprint is unique to your debug/release signing key. You need to register this with Google.

### Option A: Get from Android Studio (Easiest)

1. Open Android Studio
2. Open the terminal at the bottom
3. Run this command:

```bash
./gradlew signingReport
```

4. Look for `SHA1` in the output. It should look like:
   ```
   SHA1: AB:CD:EF:12:34:56:78:9A:BC:DE:F1:23:45:67:89:AB:CD:EF:12:34
   ```

### Option B: Get from Logcat (During App Launch)

1. Build and run the app in Android Studio
2. Open **Logcat** (View → Tool Windows → Logcat)
3. Look for logs from the app, you should see:
   ```
   📱 App SHA-1 Fingerprint: ABCDEF123456789...
   Use this fingerprint in Google Cloud Console
   Package Name: com.ebike.mobile
   ```

### Option C: Manual Generation (Advanced)

```bash
# For debug key (usually located at ~/.android/debug.keystore on Mac/Linux or 
# C:\Users\YourUsername\.android\debug.keystore on Windows)

keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

Look for `SHA1` in the output.

## Step 5: Register App with Google

1. Go to **APIs & Services** → **Credentials**
2. Click **"+ CREATE CREDENTIALS"** → **"OAuth Client ID"**
3. Select **"Android"** from Application type
4. Fill in:
   - **Name**: `E-Bike Mobile Debug`
   - **Package name**: `com.ebike.mobile`
   - **SHA-1 certificate fingerprint**: Paste the SHA-1 you got from Step 4
5. Click **"CREATE"**
6. Copy the **OAuth Client ID** that appears
7. Update `app/build.gradle`:

```gradle
buildConfigField "String", "GOOGLE_CLIENT_ID", "\"YOUR_OAUTH_CLIENT_ID_HERE\""
```

Replace `YOUR_OAUTH_CLIENT_ID_HERE` with the actual Client ID from Google Cloud Console.

## Step 6: Rebuild and Test

```bash
# Sync Gradle files
# In Android Studio: File → Sync Now

# Clean build
./gradlew clean

# Run the app
# Click Run 'app' in Android Studio or:
./gradlew installDebug
```

Then test by:
1. Opening the Login screen
2. Clicking "Continue with Google"
3. Sign in with your Google account

## Troubleshooting

### Error: "Sign-In cancelled or not configured properly"

**Root Causes:**
1. **SHA-1 fingerprint mismatch**: The fingerprint you registered in Google Cloud Console doesn't match your app's actual fingerprint
2. **Wrong Client ID**: The `GOOGLE_CLIENT_ID` in `build.gradle` is incorrect or for a different package
3. **Package name mismatch**: Make sure it's exactly `com.ebike.mobile`
4. **OAuth consent screen not configured**: Complete the OAuth consent screen setup (see Step 3)

**Solutions:**
1. Verify SHA-1 fingerprint again using `./gradlew signingReport`
2. Check the logcat output for the app's SHA-1 when it launches:
   ```
   📱 App SHA-1 Fingerprint: [Your fingerprint]
   ```
3. Make sure it exactly matches what's in Google Cloud Console
4. Verify the `GOOGLE_CLIENT_ID` in `build.gradle` matches Google Cloud Console

### Error: "Google Play Services not available"

- Update Google Play Services on the emulator/device
- Or use a physical device with Google Play Services installed

### Error: Code 12502 - "Google Play Services update required"

- Update Google Play Services through the Play Store

### Logcat shows "App SHA-1 Fingerprint:"

This is **debug information**, not an error. It helps you verify you're using the correct fingerprint in Google Cloud Console.

## For Release Build

When you're ready to release your app, you'll need to:

1. Generate a release signing key
2. Get the SHA-1 from that release key
3. Create a separate OAuth Client ID in Google Cloud Console with the release SHA-1
4. Update your build.gradle with the release Client ID for release builds

Example:
```gradle
buildTypes {
    debug {
        buildConfigField "String", "GOOGLE_CLIENT_ID", "\"DEBUG_CLIENT_ID\""
    }
    release {
        buildConfigField "String", "GOOGLE_CLIENT_ID", "\"RELEASE_CLIENT_ID\""
    }
}
```

## Google Cloud Console References

- **Console URL**: https://console.cloud.google.com/
- **OAuth Credentials**: https://console.cloud.google.com/apis/credentials
- **Google+ API**: https://console.cloud.google.com/apis/library/plus.googleapis.com

## Additional Resources

- [Google Sign-In for Android](https://developers.google.com/identity/sign-in/android/start-integrating)
- [OAuth 2.0 for Mobile & Desktop Apps](https://developers.google.com/identity/protocols/oauth2/native-app)
- [Android Debug Keystore](https://developer.android.com/tools/publishing/app-signing#debug-signing)
