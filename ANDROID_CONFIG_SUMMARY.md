# Android App Configuration Summary 

## Current Problem
```
Failed to connect to /10.0.2.2 (port 8080)
```

**Root Cause:** 
- App was using **emulator IP** (10.0.2.2) instead of **your computer's IP** on the network
- App was trying port 8080 instead of 8083 where backend actually runs

---

## What's Been Fixed ✅

### 1. RetrofitClient.kt Updated
- **File:** `ebikemobile/app/src/main/java/com/ebike/mobile/api/RetrofitClient.kt`
- **Change:** Updated default base URL to `http://192.168.254.104:8083/api/`
- **Feature:** Now reads from SharedPreferences so you can change URL without rebuilding
- **Method:** `getBaseUrl(context)` - retrieves from SharedPreferences or uses default

### 2. ApiConfig.kt Created
- **File:** `ebikemobile/app/src/main/java/com/ebike/mobile/api/ApiConfig.kt`
- **Purpose:** Centralized configuration management for API URLs
- **Features:**
  - Preset URLs for different environments
  - Runtime URL switching capability
  - Network diagnostics helper
  - Device IP lookup function

### 3. SharedPreferences Integration
- **Storage Key:** `ebike_settings` / `api_base_url`
- **Behavior:** 
  - First launch: Uses default `192.168.254.104:8083`
  - Saved URL: Uses whatever was last saved
  - Changeable: Can update via `ApiConfig.setApiUrl(context, newUrl)`

---

## Quick Start: What You Need to Do

### ✅ Step 1: Find Your Computer's IP

**Windows PowerShell:**
```powershell
ipconfig | Select-String "IPv4 Address"
```

**Look for something like:**
```
IPv4 Address. . . . . . . . . . . : 192.168.254.109
```

💾 **COPY THIS IP - YOU'LL NEED IT**

---

### ✅ Step 2: Update Default IP (if different from 192.168.254.104)

**If your IP is different**, edit this file:

📄 `ebikemobile/app/src/main/java/com/ebike/mobile/api/RetrofitClient.kt`

**Line 28:**
```kotlin
val defaultUrl = "http://192.168.254.104:8083/api/"
```

**Change to your IP:**
```kotlin
val defaultUrl = "http://192.168.254.109:8083/api/"  // Use YOUR IP
```

---

### ✅ Step 3: Rebuild App

**In Android Studio:**
1. Click **"Run"** (▶ button) or press **Shift+F10**
2. Select your physical Android phone
3. Click **"OK"**

**Or terminal:**
```bash
cd ebikemobile
./gradlew clean
./gradlew build
```

---

### ✅ Step 4: Test Connection

**Before opening app, verify backend:**

```powershell
# Test if backend is reachable
$ip = "192.168.254.109"  # Your IP from Step 1
curl "http://$ip`:8083/api/health"
```

**Should return:**
```json
{"status":"UP"}
```

If this works, your network is good! ✅

---

### ✅ Step 5: Open App on Phone

1. Launch app on your physical Android phone
2. Try to login
3. Should work now! 🎉

---

## File Locations Reference

| File | Purpose | What to Change |
|------|---------|-----------------|
| `RetrofitClient.kt` | Main API client | Default URL (if IP differs) |
| `ApiConfig.kt` | Configuration utility | Preset URLs (optional) |
| `AuthInterceptor.kt` | Authentication | No changes needed |
| `BikeRentalApi.kt` | API endpoints | No changes needed |

---

## Environment Details

### Backend Configuration
```
Server:     Spring Boot 3.x
Port:       8083 (prod profile)
Database:   PostgreSQL (Neon cloud)
URL:        http://YOUR_IP:8083/api/
Profile:    prod
```

### Frontend Configuration
```
Server:     React + Vite
Port:       5173
Type:       Web browser
URL:        http://localhost:5173/
```

### Mobile Configuration
```
App:        Android Kotlin
API Client: Retrofit 2.x
Storage:    SharedPreferences (ebike_settings)
Default:    http://192.168.254.104:8083/api/
```

---

## How to Change API URL Without Rebuilding

### Method 1: Code Change (Current)
```kotlin
// In any Activity or Fragment
import com.ebike.mobile.api.ApiConfig

ApiConfig.setApiUrl(context, "http://192.168.254.109:8083/api/")
```

### Method 2: Use Presets (Current)
```kotlin
val urls = ApiConfig.getPresetUrls()
// ["Local (Physical Device)", "Emulator", "Production", "Custom"]

ApiConfig.setApiUrl(context, ApiConfig.getUrlForPreset("Local (Physical Device)"))
```

### Method 3: Settings Screen (Add Later)
Could create a Settings Activity that:
- Shows current URL
- Allows switching presets
- Allows entering custom URL
- Tests connectivity

---

## Debugging Checklist

### 🔴 App Can't Connect
- [ ] Is backend running? (Check Spring Boot console)
- [ ] Is phone on same WiFi as computer?
- [ ] Is IP address correct? (Run `ipconfig`)
- [ ] Is port 8083 open? (Not blocked by firewall)
- [ ] Did you rebuild after code changes?

### 🔴 Logcat Shows Wrong URL
- [ ] Check SharedPreferences value
- [ ] Clear app cache: Settings → Apps → E-Bike Mobile → Clear Cache
- [ ] Uninstall and reinstall app
- [ ] Verify RetrofitClient.kt has correct default

### 🔴 Login Works but Data Doesn't Load
- [ ] Check backend logs for errors
- [ ] Verify JWT token is being sent
- [ ] Check API response format matches expected types
- [ ] View Logcat for parsing errors

---

## Current Default

**Current Hardcoded Default in RetrofitClient.kt:**
```kotlin
val defaultUrl = "http://192.168.254.104:8083/api/"
```

**If your computer IP is 192.168.254.104:**
- ✅ No changes needed, just rebuild
- ✅ App will work immediately

**If your computer IP is different (e.g., 192.168.1.100):**
- 🔨 Update RetrofitClient.kt with your IP
- ✅ Rebuild
- ✅ App will work

---

## Network Setup Verification

### Check 1: Can Terminal Reach Backend?

```powershell
# Windows PowerShell
Test-NetConnection -ComputerName 192.168.254.104 -Port 8083

# Mac/Linux Terminal
nc -zv 192.168.254.104 8083
```

**Expected:** `TCPTestSucceeded : True`

### Check 2: Can Phone's Browser Reach Backend?

1. On phone, open **any browser**
2. Go to: `http://192.168.254.104:8083/api/health`
3. Should see JSON: `{"status":"UP"}`

If this works in browser but not app → App configuration issue  
If this fails → Network or backend issue

### Check 3: Is Firewall Blocking?

```powershell
# Check Windows Firewall
Get-NetFirewallRule -DisplayName "*8083*"

# Or use Windows Defender GUI:
# Settings → Firewall → Allow apps through firewall → Check Java/Maven
```

---

## Next Steps After Connection Works

1. ✅ Test login/register on app
2. ✅ Verify bikes list loads
3. ✅ Test booking workflow
4. ✅ Implement Google OAuth for mobile (if needed)
5. ✅ Test GCash payment (once frontend integration done)
6. ✅ Create Settings screen for easy URL selection

---

## Useful Commands Reference

```bash
# Android Studio
Shift+F10          Run app on device
Ctrl+Alt+L         Format code
Alt+6              Show Logcat
Ctrl+F5            Rebuild

# Gradle
./gradlew clean                 # Clean build
./gradlew build                 # Build
./gradlew build --info          # Build with verbose output
./gradlew installDebug          # Install to device
```

---

## File Size Check

Ensure these files exist and aren't empty:

```bash
# In Android Studio Terminal
dir ebikemobile\app\src\main\java\com\ebike\mobile\api\

# Should show:
# - ApiConfig.kt (✅ NEW)
# - AuthInterceptor.kt
# - BikeRentalApi.kt
# - RetrofitClient.kt (✅ UPDATED)
```

---

## Summary

| What | Before | After |
|------|--------|-------|
| **Default URL** | `http://192.168.254.109:8080/api/` ❌ | `http://192.168.254.104:8083/api/` ✅ |
| **Configuration** | Hardcoded ❌ | Dynamic via SharedPreferences ✅ |
| **Runtime Changes** | Impossible ❌ | Possible with ApiConfig ✅ |
| **Logging** | None ❌ | Detailed logging ✅ |
| **Presets** | None ❌ | 4 presets available ✅ |

---

## Ready to Deploy?

1. ✅ Ensure computer IP in RetrofitClient.kt matches your actual IP
2. ✅ Rebuild Android app
3. ✅ Install to physical phone
4. ✅ Run connection test: `curl http://YOUR_IP:8083/api/health`
5. ✅ Open app and login
6. ✅ Done! 🚀

---

**Questions? Check ANDROID_CONNECTION_FIX.md or ANDROID_DEBUG_COMMANDS.md**
