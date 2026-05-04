# PayMongo GCash Configuration Step-by-Step Guide

## Complete Setup Instructions with Screenshots

### PART 1: Create PayMongo Account

#### Step 1.1: Go to PayMongo Website
1. Open your browser and go to: **https://dashboard.paymongo.com/register**
2. Click on **"Sign Up"** button

#### Step 1.2: Fill Registration Form
Fill in the following information:
- **Email:** Your business email (e.g., admin@ebike.com)
- **Password:** Create a strong password (min 8 characters)
- **First Name:** Your name or business owner name
- **Last Name:** Your last name
- **Phone Number:** Your contact number (+63 for Philippines)
- **Company Name:** Your business name (e.g., E-Bike Rental)
- **Country:** Philippines

#### Step 1.3: Verify Email
1. Check your email for verification link from PayMongo
2. Click the verification link
3. Your email is now verified

---

### PART 2: Complete Business Profile

#### Step 2.1: Login to Dashboard
1. Go to: **https://dashboard.paymongo.com/login**
2. Enter your email and password
3. Click **Sign In**

#### Step 2.2: Set Up Business Information
Once logged in, you'll see a setup wizard:

1. **Business Type:**
   - Select: **"Transportation/Rental Services"** (or closest match)
   - Click **Next**

2. **Business Details:**
   - **Business Name:** E-Bike Rental
   - **Business Address:** Your address
   - **City:** Your city
   - **Province/State:** Your province
   - **Postal Code:** Your postal code
   - Click **Next**

3. **Business Registration:**
   - **Registration Type:** Sole Proprietor / Partnership / Incorporation
   - **Registration Number:** Your business/tax ID
   - Click **Next**

4. **Revenue Information:**
   - **Expected Monthly Revenue:** PHP 10,000 - 100,000 (estimate)
   - **Use Case:** Online rental platform
   - Click **Next**

#### Step 2.3: Complete KYC (Know Your Customer)
This is required for verification:

1. **Document Upload:**
   - Upload valid ID (Passport, Driver's License, National ID)
   - Upload business registration certificate (if applicable)
   - Upload proof of address (utility bill, bank statement)

2. **Bank Account:**
   - **Bank Name:** Your bank
   - **Account Holder Name:** Match your ID
   - **Account Number:** Your account number
   - **Branch Code:** Your bank branch code

3. Click **Submit for Review**
   - PayMongo will verify (usually 1-24 hours)
   - You'll receive email confirmation

---

### PART 3: Get API Keys

#### Step 3.1: Access API Settings
1. Login to PayMongo Dashboard: **https://dashboard.paymongo.com**
2. Click on **Settings** (⚙️ icon) in left sidebar
3. Click **API Keys** from the menu

#### Step 3.2: View Your Keys

You'll see two versions:

**TEST Environment:**
- Secret Key: `pk_test_xxxxxxxxxxxxxxxxxxxxxxxx`
- Public Key: `pk_test_xxxxxxxxxxxxxxxxxxxxxxxx`

**LIVE Environment:**
- Secret Key: `pk_live_xxxxxxxxxxxxxxxxxxxxxxxx` (only after approval)
- Public Key: `pk_live_xxxxxxxxxxxxxxxxxxxxxxxx`

#### Step 3.3: Copy Your Test Keys
For development:
1. Click **Copy** next to **Test Secret Key**
   - Save this somewhere safe (you'll need it for backend)
2. Click **Copy** next to **Test Public Key** 
   - Save this for frontend configuration

**⚠️ IMPORTANT:**
- **SECRET KEY** = Backend only (NEVER share or commit to git)
- **PUBLIC KEY** = Frontend is okay (but keep safe)
- **TEST KEYS** = Use for development/testing
- **LIVE KEYS** = Use for production (after full setup)

---

### PART 4: Enable GCash Payment Method

#### Step 4.1: Go to Payment Methods
1. In Dashboard, click **Settings** (⚙️)
2. Click **Payment Methods** or **Integrations**

#### Step 4.2: Enable GCash
1. Look for **GCash** in the payment methods list
2. If status shows "Disabled":
   - Click **Enable** or **Activate**
   - Read terms and conditions
   - Click **Confirm**
3. Status should now show **"Enabled"** ✅

#### Step 4.3: GCash Settings (Optional)
- Min Amount: PHP 1.00 (default)
- Max Amount: PHP 1,000,000.00 (default)
- Leave defaults if unsure
- Click **Save**

---

### PART 5: Set Up Webhooks for Payment Notifications

#### Step 5.1: Go to Webhooks Section
1. Click **Developers** in left sidebar
2. Click **Webhooks** from submenu

#### Step 5.2: Add Webhook Endpoint

**For Local Development (Testing):**
Use ngrok to expose local server:

```bash
# In a terminal, run ngrok:
ngrok http 8083

# Output will show:
# Forwarding https://abc123.ngrok.io -> http://localhost:8083
```

**For Production:**
Use your actual domain

#### Step 5.3: Create Webhook
1. Click **+ Add Endpoint** button
2. Fill in the form:

   **Webhook URL:**
   - Local: `https://abc123.ngrok.io/api/payments/gcash/callback`
   - Production: `https://your-domain.com/api/payments/gcash/callback`

   **Events to Subscribe To:**
   Check these boxes:
   - ✅ `payment.paid` (Payment successful)
   - ✅ `payment.failed` (Payment failed)
   - ✅ `payment.abandoned` (User cancelled)
   - ✅ `payment.refund.created` (Refund initiated)

3. Click **Create Endpoint**

#### Step 5.4: Verify Webhook
1. PayMongo will send a test webhook
2. Check your backend logs for confirmation
3. If successful, you'll see ✅ status in dashboard

---

### PART 6: Test Payment Integration

#### Step 6.1: Get Test GCash Numbers
PayMongo provides test credentials:

**For Testing (No Real Charge):**
- Amount: PHP 1.00 - PHP 1,000,000.00
- Phone: +639123456789 (any format works)
- These are test-only and won't charge anything

#### Step 6.2: Test the Flow
1. Make sure your backend is running with test keys:
   ```powershell
   $env:PAYMONGO_ENABLED='true'
   $env:PAYMONGO_SECRET_KEY='pk_test_xxx'  # From Step 3.3
   $env:PAYMONGO_PUBLIC_KEY='pk_test_xxx'
   ```

2. Check backend is healthy:
   ```bash
   curl http://localhost:8083/api/payments/gcash/status
   # Should respond: CONFIGURED
   ```

3. Create a test booking and initiate payment:
   ```bash
   curl -X POST http://localhost:8083/api/payments/gcash/initiate?bookingId=1
   ```

4. You'll get checkout URL - open it and complete test transaction

#### Step 6.3: Monitor Webhook
1. In PayMongo Dashboard
2. Go to **Developers → Webhooks**
3. Click on your endpoint
4. View **Recent Deliveries** tab
5. You should see:
   - `payment.paid` webhook (after successful payment)
   - Status: ✅ Success or ⚠️ Failed

---

### PART 7: Switch to Production (When Ready)

#### Step 7.1: Update to Live Keys
**Only after your account is fully approved:**

1. Go to **Settings → API Keys**
2. Copy **Live Secret Key** (pk_live_xxx)
3. Update your environment:
   ```powershell
   $env:PAYMONGO_SECRET_KEY='pk_live_xxx'  # NOT pk_test_!
   $env:PAYMONGO_PUBLIC_KEY='pk_live_xxx'
   ```

#### Step 7.2: Update Production Webhook
1. Go to **Developers → Webhooks**
2. Click your test webhook endpoint
3. Edit the URL:
   ```
   https://your-production-domain.com/api/payments/gcash/callback
   ```
4. Save changes

#### Step 7.3: Verify Production Setup
- Test with real GCash payment (small amount)
- Verify webhook is received
- Check payment status updates in your app

---

## Quick Reference URLs

| Purpose | URL |
|---------|-----|
| Sign Up | https://dashboard.paymongo.com/register |
| Login | https://dashboard.paymongo.com/login |
| Dashboard | https://dashboard.paymongo.com/dashboard |
| API Keys | https://dashboard.paymongo.com/settings/api-keys |
| Payment Methods | https://dashboard.paymongo.com/settings/payment-methods |
| Webhooks | https://dashboard.paymongo.com/developers/webhooks |
| API Docs | https://developers.paymongo.com |

---

## Environment Variables to Set

After getting your keys, set these for your backend:

```powershell
# TEST ENVIRONMENT (Development)
$env:PAYMONGO_ENABLED='true'
$env:PAYMONGO_SECRET_KEY='pk_test_xxxxxxxxxxxxxxxxxxxxxxxx'
$env:PAYMONGO_PUBLIC_KEY='pk_test_xxxxxxxxxxxxxxxxxxxxxxxx'
$env:APP_CALLBACK_URL='http://localhost:5173'
$env:PAYMONGO_API_URL='https://api.paymongo.com/v1'

# Save to file for later use (create .env file in backend/ebike folder)
# Then load with: Get-Content .env | ForEach-Object { if ($_) { $name, $value = $_.split('='); Invoke-Expression "`$env:$name='$value'" } }
```

---

## Troubleshooting During Setup

### Issue: Email verification not received
**Solution:**
- Check spam/junk folder
- Request resend from login page
- Contact: support@paymongo.com

### Issue: KYC approval stuck
**Solution:**
- Ensure documents are clear and valid
- Upload high-quality images
- Check document details match registration
- Contact support if delayed >24 hours

### Issue: GCash shows "Disabled"
**Solution:**
- Contact PayMongo support - may not be available in your region
- Verify your merchant account is approved
- Check that you've completed KYC

### Issue: Webhook not delivering
**Solution:**
- Verify webhook URL is accessible and returns HTTP 200
- For local testing, ensure ngrok is running
- Check firewall/network isn't blocking
- View webhook logs in dashboard

### Issue: Test payments not working
**Solution:**
- Ensure you're using `pk_test_` keys (not `pk_live_`)
- Check GCash is enabled in payment methods
- Verify backend has `PAYMONGO_ENABLED=true`

---

## Key Differences: Test vs Live

| Aspect | Test | Live |
|--------|------|------|
| **Key Prefix** | pk_test_ | pk_live_ |
| **Payment Charged** | No | Yes (real money) |
| **Approval** | Immediate | After KYC |
| **Environment** | Development | Production |
| **Reset** | Anytime | Permanent |

---

## Next Steps After Configuration

1. ✅ Complete PayMongo registration
2. ✅ Verify email
3. ✅ Complete KYC
4. ✅ Copy API keys
5. ✅ Set environment variables
6. ✅ Enable GCash payment method
7. ✅ Configure webhooks
8. ✅ Test with test credentials
9. ✅ Switch to live keys (production)
10. ✅ Deploy to production

---

## Support

- **PayMongo Support:** support@paymongo.com
- **PayMongo Docs:** https://developers.paymongo.com
- **Live Chat:** Available in PayMongo dashboard
- **Community:** https://discord.gg/paymongo

---

## Important Security Notes

🔒 **NEVER:**
- Commit API keys to git/GitHub
- Share secret keys with anyone
- Use live keys in development
- Post keys in public forums

✅ **ALWAYS:**
- Use environment variables for keys
- Keep keys in .env file (add to .gitignore)
- Rotate keys periodically
- Use different keys for test/prod
- Use HTTPS for webhooks (not HTTP)
