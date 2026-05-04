# E-Bike Mobile (Android/Kotlin) - Connection Setup Guide

## Issue You're Experiencing

```
Failed to connect to /10.0.2.2 (port 8080)
from /192.168.254.104 (port 37538) after 3000ms
```

**What this means:**
- Your phone is on network: `192.168.254.104`
- Backend is on different IP: `192.168.254.109`
- App trying to connect to emulator address: `10.0.2.2` (which only works in Android Studio emulator, NOT physical device)

**Solution:** Update the backend IP address in the app configuration

---

## 🔧 Fix: Step by Step

### Step 1: Find Your Computer's IP Address

**On Windows (PowerShell):**

```powershell
# Open PowerShell and run:
ipconfig

# Look for "IPv4 Address" under "Wireless LAN adapter" or "Ethernet adapter"
# Example output:
# IPv4 Address. . . . . . . . . . . : 192.168.254.109
# OR
# IPv4 Address. . . . . . . . . . . : 192.168.1.100

# Copy this IP address
```

**Make sure:**
- ✅ Your phone is on the SAME WiFi network as your computer
- ✅ IP address is NOT 127.0.0.1 (that's localhost, won't work for real phone)

### Step 2: Update App Configuration

The app code has been updated. Now you have **TWO options**:

#### Option A: Quick Fix - Update Code Directly

Edit this file in Android Studio:

**File:** `ebikemobile/app/src/main/java/com/ebike/mobile/api/RetrofitClient.kt`

Find this line (around line 18):
```kotlin
val defaultUrl = "http://192.168.254.104:8083/api/"
```

Replace `192.168.254.104` with YOUR computer IP from Step 1:
```kotlin
val defaultUrl = "http://YOUR_IP_HERE:8083/api/"  // e.g., 192.168.1.100
```

Then rebuild and run the app.

#### Option B: Better - Use Runtime Configuration

The app now has a `ApiConfig` utility that allows changing the URL at runtime:

**In any Activity/Fragment:**
```kotlin
import com.ebike.mobile.api.ApiConfig

// Change API URL
ApiConfig.setApiUrl(context, "http://192.168.1.100:8083/api/")

// Or use preset
ApiConfig.setApiUrl(context, "http://192.168.254.109:8083/api/")
```

This saves to SharedPreferences so you don't have to rebuild.

---

## 🏃 Quick Start Connection

### YOUR IP Address

Based on your error, your computer IP looks like:
- One of: `192.168.254.x` 
- Find exact IP with `ipconfig` command above

### Backend URL Should Be

```
http://[YOUR_IP]:8083/api/

Examples:
- http://192.168.254.109:8083/api/
- http://192.168.1.100:8083/api/
- http://10.0.0.50:8083/api/
```

### Port Numbers

- **Backend Spring Boot:** Port 8083 (Neon database setup)
- **Frontend (Web):** Port 5173
- **Android App:** No specific port needed

---

## 🧪 Test Connection

### Test 1: Can your phone reach your computer?

**On your physical phone:**

1. Open a web browser
2. Go to: `http://192.168.254.109:8083/api/health` (replace with YOUR IP)
3. You should see a JSON response like:
   ```json
   {"status": "UP"}
   ```

If this works → Your network connection is OK  
If this fails → Check IP address, firewall, or network settings

### Test 2: Try login after fix

1. Rebuild app with updated IP
2. Open app on physical phone
3. Try to login
4. Should connect successfully ✅

### Test 3: Check logs

In Android Studio, view Logcat (bottom panel):

```
Look for messages like:
- "RetrofitClient: Retrofit client created with URL: http://192.168.254.109:8083/api/"
- "RetrofitClient: Using saved API URL"
- "AuthRepository: Login successful"
```

---

## 🔍 Common Issues & Solutions

### Issue 1: "Connection refused"
```
Failed to connect after 3000ms
```
**Causes:**
- Wrong IP address
- Backend not running
- Firewall blocking connection
- Different network (phone on WiFi, computer on Ethernet)

**Solution:**
- Verify IP with `ipconfig`
- Check backend is running: Spring Boot console should show "Started"
- Check firewall allows port 8083
- Verify both on same network: `ipconfig` on computer, WiFi settings on phone

### Issue 2: "Network unreachable"
**Causes:**
- Phone is not on WiFi
- Phone is on different WiFi than computer

**Solution:**
- Make sure phone is connected to WiFi
- Verify phone and computer on SAME WiFi network
- Check SSID matches (WiFi network name)

### Issue 3: Google Login Not Working
**Causes:**
- Network issue (fix above first)
- Google Client ID not configured in backend
- CORS issues

**Solution:**
- First, fix the network connection (Steps 1-2 above)
- Verify backend has Google credentials set
- Check backend logs for CORS errors

### Issue 4: Data not loading after login
**Causes:**
- API URL wrong for specific endpoint
- Backend endpoint not returning data
- Response format mismatch

**Solution:**
- Check Logcat for actual error messages
- Test API with Postman: `curl http://YOUR_IP:8083/api/auth/login`
- Print response body: Logcat shows full JSON

---

## 📱 Android Phone Configuration

### Required Permissions

Ensure your `AndroidManifest.xml` has:
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

### Emulator vs Physical Device

| Aspect | Emulator | Physical Device |
|--------|----------|-----------------|
| API URL | `http://10.0.2.2:8083/api/` | `http://192.168.x.x:8083/api/` |
| Port Access | Automatic | Requires same network |
| Network | Virtual | Real WiFi |
| Testing | Faster | Real-world |

---

## 🛠️ Rebuild & Test

After updating IP:

```bash
# In Android Studio terminal:

# 1. Clean build
./gradlew clean

# 2. Rebuild
./gradlew build

# 3. Deploy to device
# Select device: Tools → Device Manager (or Ctrl+Shift+A → Device Manager)
# Click "Run" or press Shift+F10

# 4. Monitor logs
# View → Tool Windows → Logcat (or Alt+6)
```

Or in Android Studio GUI:
1. Click "Run" (▶ button)
2. Select your physical phone device
3. Click "OK"

---

## 💻 Backend Configuration Checklist

Make sure your backend is properly configured for phone access:

- ✅ Backend running on port 8083
- ✅ CORS enabled for mobile requests
- ✅ Backend using Neon database (not local MySQL)
- ✅ Google OAuth credentials configured
- ✅ PayMongo setup (if using GCash payments)

### Check Backend CORS

In `backend/ebike/src/main/java/com/ebike/rental/config/SecurityConfig.java` or similar:

```java
@CrossOrigin(origins = "*", maxAge = 3600)
```

Should be present on controllers.

---

## 🚀 For Future: Add Settings Screen

You can add a Settings screen to the app where users can:
1. Change API URL without rebuilding
2. Select preset URLs (Local, Emulator, Production)
3. Test connection
4. View current settings

### Using ApiConfig Helper

```kotlin
// Get current URL
val currentUrl = ApiConfig.getApiUrl(context)

// Get preset URLs
val presets = ApiConfig.getPresetUrls()
// Returns: ["Local (Physical Device)", "Emulator", "Production", "Custom"]

// Set from preset
ApiConfig.setApiUrl(context, ApiConfig.getUrlForPreset("Local (Physical Device)"))

// Set custom URL
ApiConfig.setApiUrl(context, "http://192.168.1.100:8083/api/")
```

---

## ✅ Fixed Issues Checklist

After applying the fix:

- [ ] Found your computer IP with `ipconfig`
- [ ] Updated `RetrofitClient.kt` with correct IP
- [ ] Rebuilt Android app
- [ ] Installed on physical phone
- [ ] Verified network: Browser test to `http://YOUR_IP:8083/api/health`
- [ ] Tried login - should work now ✅
- [ ] Tried Google login - should work now ✅
- [ ] Can see bikes list - connected! 🎉

---

## 🔗 Useful Connections

### Check Backend Health
```
http://192.168.254.109:8083/api/health
```

### Test API Endpoint
```bash
# Login test
curl -X POST http://192.168.254.109:8083/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@ebike.com","password":"admin123"}'
```

### Alternative: Use ngrok for External Testing
```bash
# If need to test from outside local network:
ngrok http 8083

# Gets you a public URL like:
# https://abc123.ngrok.io/api/

# Use in app instead of local IP
```

---

## 📞 Need Help?

**Check:**
1. ✅ Is backend running? (Spring Boot console)
2. ✅ Is phone on WiFi? (WiFi settings)
3. ✅ Is IP address correct? (ipconfig)
4. ✅ Can you ping? (Test URL in browser)

**If still stuck:**
1. Check Logcat for actual error message
2. Test curl command to verify API works
3. Check firewall isn't blocking port 8083
4. Verify phone and computer on same network

---

## Summary

**The Fix:**
1. Find your computer IP with `ipconfig`
2. Update `RetrofitClient.kt` line 18 with your IP
3. Rebuild app
4. Test connection
5. Login should work! ✅

**IP Examples:**
- `192.168.254.109` - Your IP (from error message)
- `192.168.1.100` - Common home network
- `10.0.2.2` - Android emulator only (NOT for physical phone)

**Remember:**
- Replace `192.168.254.104` in error message with YOUR actual computer IP
- Both devices must be on same WiFi network
- Backend must be running on port 8083

---

**Ready to fix? Start with Step 1 above! 🚀**
