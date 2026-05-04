# PayMongo & GCash Quick Setup Checklist

## ⏱️ Total Setup Time: ~30-60 minutes

### PHASE 1: PayMongo Account Setup (10 minutes)

- [ ] Go to https://dashboard.paymongo.com/register
- [ ] Fill registration form with your details
- [ ] Verify email (check inbox + spam folder)
- [ ] Login to dashboard: https://dashboard.paymongo.com/login

### PHASE 2: Complete Business Profile (10 minutes)

- [ ] Select business type (Transportation/Rental Services)
- [ ] Enter business details:
  - [ ] Business Name: E-Bike Rental
  - [ ] Address: Your address
  - [ ] City/Province/Postal Code
- [ ] Select business registration type
- [ ] Select expected monthly revenue
- [ ] Click Next/Submit

### PHASE 3: Complete KYC Verification (5-10 minutes)

- [ ] Upload valid ID (Passport/License/National ID)
- [ ] Upload business registration (if applicable)
- [ ] Upload proof of address (utility bill/bank statement)
- [ ] Enter bank account details:
  - [ ] Bank Name
  - [ ] Account Holder Name
  - [ ] Account Number
  - [ ] Branch Code
- [ ] Submit for review
- ⏳ Wait for approval (usually 1-24 hours)

### PHASE 4: Get API Keys (2 minutes)

Once approved, get your keys:

1. Login: https://dashboard.paymongo.com
2. Click Settings (⚙️) → API Keys
3. Copy Test Keys:
   - [ ] **Test Secret Key:** `pk_test_xxxxx` → SAVE THIS
   - [ ] **Test Public Key:** `pk_test_xxxxx` → SAVE THIS

**📋 Save these in a safe text file!**

### PHASE 5: Enable GCash Payment Method (1 minute)

- [ ] Settings → Payment Methods
- [ ] Find **GCash**
- [ ] Click **Enable**
- [ ] Confirm terms
- [ ] Status should show ✅ Enabled

### PHASE 6: Configure Webhooks (5 minutes)

#### For Local Testing (Development):

First, set up ngrok:

```bash
# Download from https://ngrok.com or use chocolatey:
choco install ngrok

# Run ngrok (in separate terminal):
ngrok http 8083

# Copy the forwarding URL, e.g.:
# https://abc123def456.ngrok.io
```

- [ ] Developers → Webhooks
- [ ] Click **+ Add Endpoint**
- [ ] **Webhook URL:**
  - Local: `https://abc123def456.ngrok.io/api/payments/gcash/callback`
  - (Replace abc123def456 with your ngrok URL)
- [ ] Check these events:
  - [ ] ✅ payment.paid
  - [ ] ✅ payment.failed
  - [ ] ✅ payment.abandoned
  - [ ] ✅ payment.refund.created
- [ ] Click **Create Endpoint**
- [ ] Verify receives test webhook ✅

---

## 🔑 Configuration: Environment Variables

### On Your Machine (Backend Startup)

**FOR DEVELOPMENT:**

```powershell
# Open PowerShell and paste this before running backend:

$env:PAYMONGO_ENABLED='true'
$env:PAYMONGO_SECRET_KEY='pk_test_xxxxxxxxxxxxxxxxxxxxxxxx'
$env:PAYMONGO_PUBLIC_KEY='pk_test_xxxxxxxxxxxxxxxxxxxxxxxx'
$env:APP_CALLBACK_URL='http://localhost:5173'
$env:PAYMONGO_API_URL='https://api.paymongo.com/v1'

# Then run your backend normally
Set-Location backend/ebike
& .\mvnw.cmd spring-boot:run
```

**Replace `pk_test_xxxx` with your actual keys from Step 4!**

### Create .env File (Optional but Recommended)

Create file: `backend/ebike/.env`

```
PAYMONGO_ENABLED=true
PAYMONGO_SECRET_KEY=pk_test_xxxxxxxxxxxxxxxxxxxxxxxx
PAYMONGO_PUBLIC_KEY=pk_test_xxxxxxxxxxxxxxxxxxxxxxxx
APP_CALLBACK_URL=http://localhost:5173
PAYMONGO_API_URL=https://api.paymongo.com/v1
```

Then add `backend/ebike/.env` to `.gitignore`

---

## ✅ Verify Everything Works

### Test 1: Check Backend Configuration

```bash
# Test if backend recognizes PayMongo config
curl http://localhost:8083/api/payments/gcash/status

# Expected response:
# { "success": true, "data": "CONFIGURED" }
```

### Test 2: Initiate Payment

```bash
# Start a test payment (create booking first in your app)
curl -X POST "http://localhost:8083/api/payments/gcash/initiate?bookingId=1"

# You should get back a checkoutUrl
```

### Test 3: Check Webhooks

1. Go to PayMongo Dashboard
2. Developers → Webhooks
3. Click your endpoint
4. View "Recent Deliveries" tab
5. You should see test webhook ✅

---

## 🧪 Test Payment Flow

1. Make sure both backend and frontend are running:
   ```powershell
   # Terminal 1: Backend (with PayMongo env vars set above)
   Set-Location backend/ebike
   & .\mvnw.cmd spring-boot:run

   # Terminal 2: Frontend
   Set-Location web
   npm run dev
   ```

2. Open your app: http://localhost:5173

3. Create a test booking (if you have booking UI)

4. Click "Pay with GCash" button

5. You'll be redirected to PayMongo checkout

6. Complete test transaction:
   - Amount: PHP 1.00 - PHP 1,000.00
   - Phone: +639123456789 (any format)
   - This is a TEST transaction (no real charge)

7. After completion, webhook should arrive at your backend

8. Payment status should update ✅

---

## 📊 Check Webhook Status

**In PayMongo Dashboard:**

1. Developers → Webhooks
2. Click your endpoint name
3. **Recent Deliveries** tab shows:
   - `payment.paid` webhook
   - Timestamp
   - Status: ✅ Success (200)

**In Your Backend Logs:**

Look for messages like:
```
✓ Default admin user created: admin@ebike.com
INFO: GCash payment initiated
INFO: Webhook received and verified
```

---

## 🚀 Move to Production (Later)

When ready to go live:

- [ ] Get Live API Keys (different from test keys!)
  - Settings → API Keys
  - **Live Secret Key:** `pk_live_xxxxx`
  - **Live Public Key:** `pk_live_xxxxx`

- [ ] Update environment variables:
  ```powershell
  $env:PAYMONGO_SECRET_KEY='pk_live_xxxxx'  # NOT pk_test_
  $env:PAYMONGO_PUBLIC_KEY='pk_live_xxxxx'
  ```

- [ ] Update webhook URL to production:
  ```
  https://your-production-domain.com/api/payments/gcash/callback
  ```

- [ ] Deploy backend and frontend to production

- [ ] Test with real GCash payment (small amount like PHP 10)

---

## 🆘 Troubleshooting

### PayMongo Status shows "NOT_CONFIGURED"
```bash
# This means environment variables are not set

# Solution: Re-run with proper env vars
$env:PAYMONGO_ENABLED='true'
$env:PAYMONGO_SECRET_KEY='pk_test_xxx'
$env:PAYMONGO_PUBLIC_KEY='pk_test_xxx'

# Restart backend
Set-Location backend/ebike
& .\mvnw.cmd spring-boot:run
```

### No Checkout URL returned
```bash
# Possible causes:
# 1. GCash not enabled in PayMongo dashboard
# 2. Account not fully approved
# 3. Wrong API keys

# Solution:
# 1. Go to PayMongo → Settings → Payment Methods
# 2. Verify GCash shows ✅ Enabled
# 3. Go to Settings → API Keys
# 4. Copy exact key again (no spaces)
```

### Webhook not arriving
```bash
# For local development:
# 1. Ensure ngrok is running in separate terminal
# 2. Copy exact ngrok URL (e.g., https://abc123.ngrok.io)
# 3. Check webhook URL in PayMongo dashboard is correct

# For production:
# 1. Ensure URL uses HTTPS (not HTTP)
# 2. Ensure URL is publicly accessible
# 3. Test with: curl https://your-domain.com/api/payments/gcash/callback
```

---

## 📝 Important Notes

⚠️ **SECURITY:**
- Never commit API keys to git
- Use environment variables only
- Different keys for test/prod
- Keep secret key PRIVATE

✅ **TESTING:**
- Test mode uses `pk_test_` keys
- No real charges in test mode
- Multiple test transactions allowed
- Test data is separate from production

📱 **GCash SPECIFIC:**
- Minimum amount: PHP 1.00
- Maximum daily limit: PHP 1,000,000
- Settlement: 1-3 business days
- Phone number format: +63 or 09 format

---

## 📞 Support Resources

| Question | Where to Get Help |
|----------|-------------------|
| PayMongo API Help | https://developers.paymongo.com |
| Account Issues | support@paymongo.com |
| Technical Questions | https://discord.gg/paymongo |
| GCash Info | https://www.globe.com.ph/gcash |
| E-Bike Rental Code | Check PAYMONGO_GCASH_SETUP.md |

---

## ✨ Quick Copy-Paste Commands

### Set Environment Variables (Copy & Paste)
```powershell
$env:PAYMONGO_ENABLED='true'
$env:PAYMONGO_SECRET_KEY='pk_test_YOUR_KEY_HERE'
$env:PAYMONGO_PUBLIC_KEY='pk_test_YOUR_KEY_HERE'
$env:APP_CALLBACK_URL='http://localhost:5173'
$env:PAYMONGO_API_URL='https://api.paymongo.com/v1'
```

### Test Backend Configuration
```powershell
curl http://localhost:8083/api/payments/gcash/status
```

### Start Backend with PayMongo
```powershell
$env:PAYMONGO_ENABLED='true'; $env:PAYMONGO_SECRET_KEY='pk_test_xxx'; $env:PAYMONGO_PUBLIC_KEY='pk_test_xxx'; Set-Location backend/ebike; & .\mvnw.cmd spring-boot:run
```

### Start ngrok for Webhooks
```bash
ngrok http 8083
# Copy the https:// URL shown
```

---

**Ready to start? Follow the checklist above step by step! 🎉**
