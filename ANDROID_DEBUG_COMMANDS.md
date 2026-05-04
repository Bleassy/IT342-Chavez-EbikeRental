# Test Script for E-Bike Mobile Connection

## Windows PowerShell - Quick Test Commands

Copy & paste each line into PowerShell (one at a time):

### 1️⃣ Find Your Computer's IP Address

```powershell
ipconfig | Select-String "IPv4 Address"
```

**Expected output:**
```
IPv4 Address. . . . . . . . . . . : 192.168.254.109
```

💡 **Copy this IP address - you'll need it!**

---

### 2️⃣ Check if Backend is Running

```powershell
Test-NetConnection -ComputerName 192.168.254.109 -Port 8083 -InformationLevel Quiet
```

Replace `192.168.254.109` with YOUR IP from step 1.

**Expected output:**
```
True
```

❌ If you get `False` → Backend not running or wrong IP

---

### 3️⃣ Test Backend Health Endpoint

```powershell
$ip = "192.168.254.109"
$port = "8083"
curl "http://$ip`:$port/api/health"
```

Replace IP with yours.

**Expected output:**
```json
{"status":"UP"}
```

---

### 4️⃣ Full Network Connection Test

```powershell
# This will test everything
$ip = "192.168.254.109"
$port = "8083"
$url = "http://$ip`:$port/api/health"

Write-Host "🔍 Testing connection to: $url" -ForegroundColor Cyan

try {
    $response = Invoke-WebRequest -Uri $url -TimeoutSec 5
    Write-Host "✅ SUCCESS! Backend is running" -ForegroundColor Green
    Write-Host "Response: $($response.StatusCode)"
    Write-Host "Content: $($response.Content)"
} catch {
    Write-Host "❌ FAILED! Cannot reach backend" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)"
}
```

---

### 5️⃣ Find All Network Interfaces

```powershell
Get-NetAdapter | Where-Object Status -eq "Up" | Format-Table Name, InterfaceDescription, Status
```

**Expected output:**
```
Name     InterfaceDescription               Status
----     ---------------------               ------
WiFi     Realtek WiFi 6E Wireless Adapter    Up
Ethernet Gigabit Ethernet Connection         Disconnected
```

Make note of which one is active.

---

### 6️⃣ Test Ping to Your Phone

**First, find your phone's IP on the WiFi:**

1. On your phone: Go to Settings → WiFi → Connected Network
2. Look for "IP Address" or similar
3. Copy it (e.g., `192.168.254.105`)

**Then in PowerShell:**

```powershell
Test-NetConnection -ComputerName 192.168.254.105 -Hops 1
```

Replace `192.168.254.105` with YOUR PHONE'S IP.

**Expected output:**
```
ComputerName     : 192.168.254.105
RemoteAddress    : 192.168.254.105
InterfaceAlias   : WiFi
SourceAddress    : 192.168.254.109
PingSucceeded    : True
```

---

## One-Command Full Test

<br>

Copy the entire block and paste into PowerShell:

```powershell
Clear-Host
Write-Host "
╔═══════════════════════════════════════╗
║  E-Bike Mobile Connection Test        ║
╚═══════════════════════════════════════╝
" -ForegroundColor Cyan

# Get IP
$ipOutput = ipconfig | Select-String "IPv4 Address"
Write-Host "📍 Your Computer IP:" -ForegroundColor Yellow
Write-Host $ipOutput -ForegroundColor Green

# Extract IP
$ip = ($ipOutput -split ": ")[1].Trim()

# Test connection
Write-Host "`n🧪 Testing backend connection..." -ForegroundColor Yellow
$url = "http://$ip`:8083/api/health"
Write-Host "URL: $url" -ForegroundColor Cyan

try {
    $response = Invoke-WebRequest -Uri $url -TimeoutSec 5
    Write-Host "✅ SUCCESS! Backend is running on port 8083" -ForegroundColor Green
    Write-Host "Status: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Response: $($response.Content)" -ForegroundColor Green
    Write-Host "`n📱 Use this IP in your app: http://$ip`:8083/api/" -ForegroundColor Cyan
} catch {
    Write-Host "❌ FAILED! Could not connect" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "`nPossible issues:" -ForegroundColor Yellow
    Write-Host "  ❌ Backend Spring Boot not running" 
    Write-Host "  ❌ Firewall blocking port 8083"
    Write-Host "  ❌ Connected to different network"
    Write-Host "  ❌ Wrong IP address"
}

Write-Host "`n═══════════════════════════════════════" -ForegroundColor Cyan
```

Just copy everything and run it at once!

---

## What Each Test Means

| Test | Success | Failure |
|------|---------|---------|
| `ipconfig` | Shows IP like `192.168.x.x` | Shows `127.0.0.1` (localhost - won't work) |
| `Test-NetConnection` | Returns `True` | Returns `False` (can't reach backend) |
| Curl health | Returns `{"status":"UP"}` | Connection timeout or error |
| App login | Works without network errors | "Failed to connect" errors |

---

## Troubleshooting Checklist

- [ ] Backend Spring Boot is running
  ```
  Check: Terminal should show "Started Application in X seconds"
  ```

- [ ] Correct IP address
  ```
  Run: ipconfig | Select-String "IPv4 Address"
  ```

- [ ] Correct port (8083, NOT 8080)
  ```
  Note: Check backend startup logs for listening port
  ```

- [ ] Same WiFi network
  ```
  Check: Phone WiFi name matches computer WiFi
  ```

- [ ] Firewall allows port 8083
  ```
  PowerShell: Test-NetConnection -ComputerName [IP] -Port 8083
  ```

- [ ] App rebuilt with new IP
  ```
  Check: RetrofitClient.kt has YOUR IP
  Rebuild: gradle clean && gradle build
  ```

---

## Save This Test Command

Create a file `test-connection.ps1` in your project:

```powershell
# E-Bike Mobile Backend Test
$ip = Read-Host "Enter your computer IP (or press Enter for 192.168.254.109)"
if ($ip -eq "") { $ip = "192.168.254.109" }

$url = "http://$ip`:8083/api/health"
Write-Host "Testing: $url" -ForegroundColor Cyan

try {
    $response = Invoke-WebRequest -Uri $url -TimeoutSec 5
    Write-Host "✅ Connected!" -ForegroundColor Green
    Write-Host $response.Content
} catch {
    Write-Host "❌ Failed: $($_.Exception.Message)" -ForegroundColor Red
}
```

Then run:
```powershell
.\test-connection.ps1
```

---

## For Android Developers: ADB Commands

If using Android Debug Bridge:

```bash
# List connected devices
adb devices

# View live logs
adb logcat | grep "RetrofitClient"

# Test from phone (via adb shell)
adb shell am start -a android.intent.action.VIEW -d "http://192.168.254.109:8083/api/health"
```

---

## Need More Help?

**If connection test passes but app still fails:**
1. Check Logcat in Android Studio (Alt+6)
2. Look for "RetrofitClient: Using saved API URL"
3. Verify the URL shown matches your computer IP

**Common Logcat Messages:**
```
✅ Good: "Retrofit client created with URL: http://192.168.254.109:8083/api/"
❌ Bad: "Using saved API URL: http://10.0.2.2:8083/api/"
❌ Bad: "Request to http://192.168.254.109:8083/api/auth/login failed"
```

---

**Ready to test? Start with the One-Command Full Test above! 🚀**
