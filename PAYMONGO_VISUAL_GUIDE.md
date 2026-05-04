# PayMongo GCash Integration - Visual Guide

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                     YOUR E-BIKE RENTAL APP                      │
├─────────────────┬──────────────────────────────┬────────────────┤
│   FRONTEND      │        BACKEND               │  DATABASE      │
│ (React/Vite)    │    (Spring Boot)             │  (PostgreSQL)  │
│                 │                              │                │
│ Port: 5173      │    Port: 8083               │                │
└─────────────────┴──────────────────────────────┴────────────────┘
         │                      │
         │                      │
         └──────────┬───────────┘
                    │
                    ▼
    ┌───────────────────────────────┐
    │   PAYMONGO (Payment Gateway)  │
    │   ├─ Checkout Sessions        │
    │   ├─ GCash Processing         │
    │   └─ Webhooks                 │
    └───────────────────────────────┘
                    │
                    ▼
          ┌─────────────────┐
          │     GCash       │
          │  (Mobile App)   │
          └─────────────────┘
```

---

## 🔄 Payment Flow Diagram

```
USER JOURNEY:

1. User selects bike and books
   └─→ Creates Booking with TotalPrice

2. User clicks "Pay with GCash"
   │
   ▼ (POST /api/payments/gcash/initiate)
   
3. Frontend sends bookingId to Backend
   │
   ▼
   
4. Backend creates PayMongo Checkout Session
   │ Uses: Secret Key (Basic Auth)
   │ Sends: Amount, Description, Return URLs
   ▼
   
5. PayMongo returns Checkout URL
   │ Example: https://checkout.paymongo.com/cs_test_xxx
   ▼
   
6. Backend returns URL to Frontend
   │
   ▼
   
7. Frontend redirects user to PayMongo checkout
   │ window.location.href = checkoutUrl
   ▼
   
8. User selects GCash as payment method
   ▼
   
9. User completes GCash transaction in GCash app
   ▼
   
10. PayMongo processes payment
    ├─ If successful → Sends webhook: payment.paid
    └─ If failed → Sends webhook: payment.failed
    
11. Backend receives webhook
    │ (/api/payments/gcash/callback)
    ├─ Verifies webhook data
    ├─ Updates payment status in database
    ▼
    
12. PayMongo redirects user to success page
    │ http://localhost:5173/booking/success?booking=1
    ▼
    
13. Frontend shows "Payment Successful!"
    └─→ Booking is confirmed ✅
```

---

## 📋 Setup Process Flow

```
START: PayMongo Account Setup

1️⃣  CREATE ACCOUNT
    ├─ Go to: https://dashboard.paymongo.com/register
    ├─ Fill registration form
    ├─ Verify email
    └─ Login

2️⃣  COMPLETE BUSINESS PROFILE
    ├─ Enter business details
    ├─ Select business type
    ├─ Upload KYC documents
    ├─ Enter bank details
    └─ ⏳ Wait for approval (1-24 hours)

3️⃣  GET API KEYS
    ├─ Settings → API Keys
    ├─ Copy Secret Key (pk_test_xxx)
    ├─ Copy Public Key (pk_test_xxx)
    └─ Save safely ⚠️

4️⃣  ENABLE GCASH
    ├─ Settings → Payment Methods
    ├─ Find GCash
    ├─ Click Enable
    └─ Status: ✅ Enabled

5️⃣  CONFIGURE WEBHOOKS
    ├─ Setup ngrok: ngrok http 8083
    ├─ Copy ngrok URL: https://abc123.ngrok.io
    ├─ Developers → Webhooks
    ├─ Add Endpoint:
    │  └─ https://abc123.ngrok.io/api/payments/gcash/callback
    ├─ Select events: payment.paid, payment.failed
    └─ Verify webhook ✅

6️⃣  CONFIGURE BACKEND
    ├─ Set environment variables:
    │  ├─ $env:PAYMONGO_ENABLED='true'
    │  ├─ $env:PAYMONGO_SECRET_KEY='pk_test_xxx'
    │  └─ $env:PAYMONGO_PUBLIC_KEY='pk_test_xxx'
    ├─ Restart backend
    └─ Test: curl http://localhost:8083/api/payments/gcash/status

7️⃣  TEST PAYMENT
    ├─ Create test booking
    ├─ Click "Pay with GCash"
    ├─ Complete test transaction
    ├─ Check webhook in dashboard
    └─ Status: ✅ Success

END: Ready to accept GCash payments!
```

---

## 🗂️ File Structure After Setup

```
IT342-Chavez-EbikeRental/
├── PAYMONGO_GCASH_SETUP.md          ← Full setup guide
├── PAYMONGO_SETUP_STEPS.md          ← Step-by-step guide
├── PAYMONGO_QUICK_CHECKLIST.md      ← Quick reference
├── PAYMONGO_VISUAL_GUIDE.md         ← This file
│
├── backend/ebike/
│   ├── .env                         ← ⚠️ YOUR API KEYS HERE (add to .gitignore)
│   ├── pom.xml
│   ├── mvnw.cmd
│   │
│   └── src/main/
│       ├── java/com/ebike/rental/
│       │   ├── service/
│       │   │   └── GCashPaymentService.java    ← PayMongo integration
│       │   ├── controller/
│       │   │   └── PaymentController.java      ← Payment endpoints
│       │   └── entity/
│       │       └── Payment.java                ← GCASH method added
│       │
│       └── resources/
│           └── application.properties          ← PayMongo config
│
├── web/
│   └── src/
│       └── pages/
│           └── BookingPage.tsx                 ← Add GCash button here
│
└── .gitignore
    ├── backend/ebike/.env           ← NEVER commit this!
    └── .DS_Store

```

---

## 🔐 API Keys Management

```
┌──────────────────────────────────────────────┐
│       PAYMONGO API KEYS SECURITY             │
├──────────────────────────────────────────────┤
│                                              │
│  🔓 TEST KEYS (Development Only)            │
│  ├─ Prefix: pk_test_                        │
│  ├─ Safe to: Commit to private repos        │
│  ├─ Used for: Development & testing         │
│  ├─ Charges: NO real money                  │
│  └─ Create: ✅ Multiple test transactions   │
│                                              │
│  🔐 LIVE KEYS (Production Only)             │
│  ├─ Prefix: pk_live_                        │
│  ├─ Safe to: Environment variables ONLY     │
│  ├─ Used for: Production payments           │
│  ├─ Charges: YES real money                 │
│  └─ Create: ⚠️ Be careful!                   │
│                                              │
│  ⚡ NEVER:                                    │
│  ├─ Commit to git                           │
│  ├─ Share in email/chat                     │
│  ├─ Mix test & live in production           │
│  └─ Use live key in development             │
│                                              │
│  ✅ ALWAYS:                                  │
│  ├─ Use environment variables               │
│  ├─ Add .env to .gitignore                  │
│  ├─ Keep secret key SECRET                  │
│  └─ Rotate periodically                     │
│                                              │
└──────────────────────────────────────────────┘
```

---

## 💻 Command Reference

### Development Setup Commands

```powershell
# 1. SET ENVIRONMENT VARIABLES
$env:PAYMONGO_ENABLED='true'
$env:PAYMONGO_SECRET_KEY='pk_test_xxxxxxxxxxxxxxxxxxxxxxxx'
$env:PAYMONGO_PUBLIC_KEY='pk_test_xxxxxxxxxxxxxxxxxxxxxxxx'
$env:APP_CALLBACK_URL='http://localhost:5173'
$env:PAYMONGO_API_URL='https://api.paymongo.com/v1'

# 2. START NGROK (in separate terminal)
ngrok http 8083
# Copy the https:// URL shown, e.g., https://abc123.ngrok.io

# 3. ADD WEBHOOK URL TO PAYMONGO DASHBOARD
# Go to: https://dashboard.paymongo.com/developers/webhooks
# Add: https://abc123.ngrok.io/api/payments/gcash/callback

# 4. START BACKEND
Set-Location backend/ebike
& .\mvnw.cmd spring-boot:run

# 5. START FRONTEND (in another terminal)
Set-Location web
npm run dev

# 6. TEST API
curl http://localhost:8083/api/payments/gcash/status
# Should return: { "success": true, "data": "CONFIGURED" }
```

### Production Deployment Commands

```bash
# 1. UPDATE TO LIVE KEYS (after KYC approved)
export PAYMONGO_SECRET_KEY='pk_live_xxxxxxxxxxxxxxxxxxxxxxxx'
export PAYMONGO_PUBLIC_KEY='pk_live_xxxxxxxxxxxxxxxxxxxxxxxx'

# 2. UPDATE WEBHOOK URL IN PAYMONGO DASHBOARD
# https://dashboard.paymongo.com/developers/webhooks
# Update to: https://your-production-domain.com/api/payments/gcash/callback

# 3. DEPLOY BACKEND & FRONTEND
# (Use your deployment method: Docker, Heroku, Azure, etc.)

# 4. TEST WITH SMALL TRANSACTION
# Create booking, initiate real payment (PHP 10)
# Verify webhook arrives
```

---

## 🧪 Webhook Test Events

When you complete a payment, PayMongo sends webhooks to your backend:

```json
// WEBHOOK 1: Payment Initiated
{
  "data": {
    "id": "evt_123abc",
    "attributes": {
      "type": "checkout_session.completed",
      "data": {
        "id": "cs_test_abc123",
        "status": "completed"
      }
    }
  }
}

// WEBHOOK 2: Payment Paid ✅
{
  "data": {
    "id": "evt_456def",
    "attributes": {
      "type": "payment.paid",
      "data": {
        "id": "pay_abc123def456",
        "status": "paid",
        "amount": 15000,  // PHP 150.00 in cents
        "currency": "PHP",
        "payment_method_type": "gcash"
      }
    }
  }
}
```

---

## 🔍 Verification Points

After completing a test payment, verify these:

```
✅ STEP 1: Check Backend Logs
   Look for:
   - "✓ Default admin user created"
   - "INFO: GCash payment initiated"
   - "INFO: Webhook received"

✅ STEP 2: Check PayMongo Dashboard
   Go to: Developers → Webhooks
   Click your endpoint
   View: Recent Deliveries
   Should show: payment.paid with ✅ Success

✅ STEP 3: Check Database
   Query payment table:
   SELECT * FROM payments WHERE booking_id = 1;
   Should show: paymentMethod = GCASH, paymentStatus = COMPLETED

✅ STEP 4: Check Frontend
   Redirect to: /booking/success?booking=1
   Should show: "Payment Successful! Your booking ID: 1"
```

---

## 📊 Decision Tree: Troubleshooting

```
❌ PROBLEM: API returns "NOT_CONFIGURED"
├─ Check 1: Is PAYMONGO_ENABLED='true'?
│  └─ No → Set environment variable
├─ Check 2: Do you have SECRET_KEY?
│  └─ No → Copy from PayMongo dashboard
├─ Check 3: Did you restart backend?
│  └─ No → Restart Spring Boot
└─ OK ✅ → Should work now

❌ PROBLEM: "Invalid API credentials"
├─ Check 1: Are you using test key (pk_test_) not production (pk_live_)?
│  └─ Using live → Switch to test key
├─ Check 2: Did you copy key exactly (no spaces)?
│  └─ Has spaces → Re-copy carefully
├─ Check 3: Is key correct from dashboard?
│  └─ Wrong key → Copy again from PayMongo
└─ OK ✅ → Should work now

❌ PROBLEM: "GCash not enabled"
├─ Check 1: Is GCash activated in dashboard?
│  └─ Disabled → Settings → Payment Methods → Enable
├─ Check 2: Account fully approved?
│  └─ Still pending → Wait 1-24 hours
└─ OK ✅ → Should work now

❌ PROBLEM: Webhook not arriving
├─ Check 1: Is ngrok running? (for local)
│  └─ Not running → ngrok http 8083
├─ Check 2: Webhook URL correct in dashboard?
│  └─ Wrong URL → Update to https://abc123.ngrok.io/api/payments/gcash/callback
├─ Check 3: Backend returning HTTP 200?
│  └─ Returning error → Check logs, fix error
└─ OK ✅ → Should work now
```

---

## 📱 Test GCash Numbers

PayMongo provides these for testing (NO REAL CHARGE):

```
Amount:     PHP 1.00 - PHP 1,000,000.00
Phone:      +639123456789 (or 09123456789)
Status:     Automatically approved in test mode
Refund:     Fully refundable in test mode
Multiple:   Create as many test transactions as needed
```

---

## 🚀 From Test to Production Checklist

```
☐ Review backend code for hardcoded URLs
☐ Update frontend callback URLs to production domain
☐ Switch API keys from test (pk_test_) to live (pk_live_)
☐ Update webhook URL in PayMongo dashboard to production
☐ Deploy backend to production server
☐ Deploy frontend to production server
☐ Test with small real transaction (PHP 10)
☐ Monitor first few transactions for issues
☐ Enable monitoring/alerts in PayMongo dashboard
☐ Document production secrets in secure vault
☐ Set up backup payment method if GCash fails
☐ Train support team on handling payment issues
```

---

## 📞 Getting Help

| Issue | Contact |
|-------|---------|
| PayMongo account blocked | support@paymongo.com |
| GCash not available | PayMongo support + Globe GCash |
| Payment declined | Check amount, phone format |
| Webhook not working | Check ngrok + URL in dashboard |
| Technical questions | https://developers.paymongo.com |

---

## ✨ Summary

```
     ┌──────────────────────────────────────┐
     │   YOU ARE HERE: Backend Ready!       │
     │   (All code implemented)             │
     └──────────────────────────────────────┘
                    ↓
     ┌──────────────────────────────────────┐
     │   NEXT: Setup PayMongo Account       │
     │   (Follow PAYMONGO_SETUP_STEPS.md)   │
     └──────────────────────────────────────┘
                    ↓
     ┌──────────────────────────────────────┐
     │   THEN: Get API Keys                 │
     │   (Copy pk_test_ keys)               │
     └──────────────────────────────────────┘
                    ↓
     ┌──────────────────────────────────────┐
     │   THEN: Configure Environment        │
     │   (Set $env: variables)              │
     └──────────────────────────────────────┘
                    ↓
     ┌──────────────────────────────────────┐
     │   THEN: Test Payment                 │
     │   (Complete test transaction)        │
     └──────────────────────────────────────┘
                    ↓
     ┌──────────────────────────────────────┐
     │   READY: Deploy to Production! 🎉    │
     │   (Switch to pk_live_ keys)          │
     └──────────────────────────────────────┘
```

---

**Need help? Check the other guides:**
- 📋 [PAYMONGO_SETUP_STEPS.md](PAYMONGO_SETUP_STEPS.md) - Detailed step-by-step
- ✅ [PAYMONGO_QUICK_CHECKLIST.md](PAYMONGO_QUICK_CHECKLIST.md) - Quick reference
- 🔧 [PAYMONGO_GCASH_SETUP.md](PAYMONGO_GCASH_SETUP.md) - Technical integration details
