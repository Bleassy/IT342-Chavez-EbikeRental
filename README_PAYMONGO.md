# 🎯 PayMongo GCash Integration - Start Here

## Welcome! Your backend is ready. Now let's set up PayMongo.

---

## 📖 Which Guide Should I Read?

### 🚀 "Just tell me what to do!" 
→ **Read: [PAYMONGO_QUICK_CHECKLIST.md](PAYMONGO_QUICK_CHECKLIST.md)**
- ✅ Checkbox list of all steps
- 📋 Copy-paste commands ready
- ⏱️ ~30-60 minutes to complete

### 🔍 "I want detailed step-by-step instructions"
→ **Read: [PAYMONGO_SETUP_STEPS.md](PAYMONGO_SETUP_STEPS.md)**
- 📸 Every screen explained
- 🟢 Screenshots references (with descriptions)
- 🎓 Understand each step fully

### 📊 "Show me how this all works together"
→ **Read: [PAYMONGO_VISUAL_GUIDE.md](PAYMONGO_VISUAL_GUIDE.md)**
- 🔄 Payment flow diagrams
- 🏗️ Architecture overview
- 🧩 How components interact

### ⚙️ "I want technical implementation details"
→ **Read: [PAYMONGO_GCASH_SETUP.md](PAYMONGO_GCASH_SETUP.md)**
- 💻 Code examples
- 🔌 API endpoint reference
- 🛠️ Configuration details

---

## ⏱️ Quick Start (5 minutes reading, then action)

### You Need:
- [ ] Valid email address
- [ ] Valid government ID (for KYC)
- [ ] Bank account info
- [ ] 30-60 minutes free time

### What's Already Done:
- ✅ Backend code implemented
- ✅ Payment service created
- ✅ API endpoints ready
- ✅ Database schema updated
- ✅ Environment variables configured

### What You Need to Do:
1. Create PayMongo account
2. Get API keys
3. Set environment variables
4. Test the payment flow

---

## 🎬 Let's Start: 3-Step Quick Start

### STEP 1: Create PayMongo Account (10 min)

Go here: https://dashboard.paymongo.com/register

Fill in:
- Email: your@email.com
- Password: Strong password
- Name: Your name
- Phone: Your phone number
- Company: E-Bike Rental

Then:
- Check email for verification link
- Login at: https://dashboard.paymongo.com/login

**✅ You're registered!**

---

### STEP 2: Get Your API Keys (5 min)

Once logged in:
1. Click **Settings** (⚙️ icon)
2. Click **API Keys**
3. You'll see:
   - **Test Secret Key:** `pk_test_xxxxxxxx...`
   - **Test Public Key:** `pk_test_xxxxxxxx...`

**📋 Copy these keys and save in a safe location!**

Don't close this yet - you need these next.

---

### STEP 3: Configure Your Backend (5 min)

Open PowerShell terminal (on your computer):

```powershell
# PASTE THIS and replace YOUR_KEY with actual key from PayMongo:

$env:PAYMONGO_ENABLED='true'
$env:PAYMONGO_SECRET_KEY='pk_test_xxxxxxxxxxxxxxxxxxxxxxxx'
$env:PAYMONGO_PUBLIC_KEY='pk_test_xxxxxxxxxxxxxxxxxxxxxxxx'
$env:APP_CALLBACK_URL='http://localhost:5173'
$env:PAYMONGO_API_URL='https://api.paymongo.com/v1'

# Example (this is fake):
$env:PAYMONGO_SECRET_KEY='pk_test_abc123def456ghi789jkl'

# Then start your backend:
Set-Location backend/ebike
& .\mvnw.cmd spring-boot:run
```

**✅ Backend is now PayMongo-enabled!**

---

### STEP 4: Verify It Works (2 min)

Open another terminal/PowerShell:

```powershell
# Test if PayMongo is configured
curl http://localhost:8083/api/payments/gcash/status

# Should return something like:
# {"success":true,"message":"PayMongo status retrieved","data":"CONFIGURED"}
```

If you see `"CONFIGURED"` → ✅ **You're ready!**

If you see `"NOT_CONFIGURED"` → Go back to Step 3 and check your keys

---

## 🧪 Test a Payment

### Setup Webhook Testing (for local development)

In a new terminal, download and run ngrok:

```bash
# If not installed, download from: https://ngrok.com

# Run ngrok:
ngrok http 8083

# You'll see:
# Forwarding https://abc123.ngrok.io -> http://localhost:8083

# COPY this URL (abc123.ngrok.io)
```

Now setup webhook:
1. Go to PayMongo: https://dashboard.paymongo.com/developers/webhooks
2. Click **+ Add Endpoint**
3. Paste this URL:
   ```
   https://abc123.ngrok.io/api/payments/gcash/callback
   ```
4. Check these boxes:
   - ✅ payment.paid
   - ✅ payment.failed
5. Click **Create Endpoint**

---

## 🎯 Complete Setup (After Quick Start)

For detailed setup (KYC, payments, etc.), read one of the guides:

- **Quick & Easy:** [PAYMONGO_QUICK_CHECKLIST.md](PAYMONGO_QUICK_CHECKLIST.md)
- **Step by Step:** [PAYMONGO_SETUP_STEPS.md](PAYMONGO_SETUP_STEPS.md)  
- **Visual Overview:** [PAYMONGO_VISUAL_GUIDE.md](PAYMONGO_VISUAL_GUIDE.md)
- **Technical Details:** [PAYMONGO_GCASH_SETUP.md](PAYMONGO_GCASH_SETUP.md)

---

## ✅ Checklist After Following Guides

After completing one of the guides, you should have:

- [ ] PayMongo account created and verified
- [ ] KYC documents submitted and approved ✅
- [ ] API keys copied safely
- [ ] GCash enabled in payment methods
- [ ] Webhooks configured
- [ ] Environment variables set
- [ ] Backend restarted with PayMongo config
- [ ] Tested payment flow (test transaction completed)
- [ ] Webhook confirmed received from PayMongo

---

## 🚀 Next Steps After Setup

### Add GCash Button to Frontend

In your booking/payment page component:

```tsx
import { Button } from "@/components/ui/button";

const handleGCashPayment = async () => {
  try {
    const response = await fetch(
      'http://localhost:8083/api/payments/gcash/initiate?bookingId=1',
      { method: 'POST' }
    );
    
    const result = await response.json();
    
    if (result.success && result.data.checkoutUrl) {
      // Redirect to PayMongo checkout
      window.location.href = result.data.checkoutUrl;
    }
  } catch (error) {
    console.error('Payment failed:', error);
  }
};

// In your JSX:
<Button onClick={handleGCashPayment} size="lg">
  Pay with GCash
</Button>
```

### Test Complete Flow

1. Make sure both backend and frontend are running
2. Create a test booking in your app
3. Click "Pay with GCash"
4. Complete test transaction (amount: PHP 1.00)
5. You should be redirected to success page
6. Check webhook in PayMongo dashboard ✅

---

## 🆘 Troubleshooting

### "PayMongo Status is NOT_CONFIGURED"
```powershell
# Set environment variables again (they might have reset)
$env:PAYMONGO_ENABLED='true'
$env:PAYMONGO_SECRET_KEY='pk_test_YOUR_KEY'

# Restart backend
```

### "Webhook not arriving"
```bash
# Make sure ngrok is still running in a terminal
ngrok http 8083

# Check webhook URL in PayMongo dashboard matches ngrok URL
# https://dashboard.paymongo.com/developers/webhooks
```

### "Payment says 'Invalid credentials'"
Check these:
- Are you using `pk_test_` key (not `pk_live_`)?
- Did you copy the key exactly (no spaces)?
- Did you restart backend after setting env vars?

### Need More Help?

- **PayMongo Support:** support@paymongo.com
- **PayMongo Docs:** https://developers.paymongo.com
- **Discord Community:** https://discord.gg/paymongo

---

## 📚 Guide Navigation

```
START HERE (You are here)
    ↓
Quick Start (above)
    ↓
Pick a guide below based on what you need:
    ├─ PAYMONGO_QUICK_CHECKLIST.md (Fast track)
    ├─ PAYMONGO_SETUP_STEPS.md (Detailed)
    ├─ PAYMONGO_VISUAL_GUIDE.md (Visual learner)
    └─ PAYMONGO_GCASH_SETUP.md (Technical)
    ↓
Complete setup
    ↓
Test payment flow
    ↓
Add GCash button to frontend
    ↓
Deploy to production 🎉
```

---

## 💡 Key Concepts to Remember

### Test vs Live Keys
- **Test Keys** (`pk_test_`): For development, no real charges
- **Live Keys** (`pk_live_`): For production, real money involved

### Environment Variables
- Never commit to git
- Add `backend/ebike/.env` to `.gitignore`
- Set before starting backend

### Webhooks
- Receive payment confirmations from PayMongo
- Need public URL (use ngrok for local testing)
- Must return HTTP 200 status

### Payment Status
```
PENDING → (user completes GCash) → COMPLETED ✅
       → (user cancels) → FAILED ❌
```

---

## 🎊 You're All Set!

Your E-Bike Rental system now has full GCash payment integration!

**Next steps:**
1. Read the appropriate guide (see "Which Guide" section above)
2. Follow setup instructions
3. Test payment flow
4. Deploy to production

**Questions?** Check the guides or contact PayMongo support.

---

## 📋 File Summary

| File | Purpose | Read If |
|------|---------|---------|
| [PAYMONGO_QUICK_CHECKLIST.md](PAYMONGO_QUICK_CHECKLIST.md) | Checkbox list with commands | You want quick action items |
| [PAYMONGO_SETUP_STEPS.md](PAYMONGO_SETUP_STEPS.md) | Detailed step-by-step guide | You want full explanations |
| [PAYMONGO_VISUAL_GUIDE.md](PAYMONGO_VISUAL_GUIDE.md) | Diagrams and visual flows | You're a visual learner |
| [PAYMONGO_GCASH_SETUP.md](PAYMONGO_GCASH_SETUP.md) | Technical implementation | You want technical details |

---

**Ready? 👉 Pick a guide and get started!**

🚀 Happy coding!
