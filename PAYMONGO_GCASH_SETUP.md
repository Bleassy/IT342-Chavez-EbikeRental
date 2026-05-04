# PayMongo GCash Integration Guide

## Overview
Your E-Bike Rental system now has full PayMongo GCash payment integration. This is ready to use with PayMongo API keys.

## What's Been Implemented

### Backend
- ✅ **GCashPaymentService** - Full PayMongo API integration using RestTemplate  
- ✅ **PaymentController Endpoints**:
  - `POST /api/payments/gcash/initiate` - Create GCash checkout session
  - `POST /api/payments/gcash/callback` - Handle PayMongo webhooks
  - `GET /api/payments/gcash/status` - Check PayMongo configuration
  - `GET /api/payments/gcash/payment/{paymentId}` - Get payment status
- ✅ **Payment Entity** - `GCASH` added to PaymentMethod enum
- ✅ **Configuration** - PayMongo properties configured

## PayMongo Setup (Step by Step)

### Step 1: Create PayMongo Account
1. Go to https://dashboard.paymongo.com/register
2. Sign up with your email
3. Verify your email
4. Complete business information
5. KYC verification (ID, business details)

### Step 2: Get API Keys
1. Login to PayMongo Dashboard
2. Go to **Settings → API Keys**
3. Copy your **Secret Key** (pk_test_... for testing, pk_live_... for production)
4. Copy your **Public Key** (for frontend, if needed)

### Step 3: Configure Environment Variables

**For Local Development:**

Add to your backend startup command or .env file:

```bash
# PowerShell command:
$env:PAYMONGO_ENABLED='true'
$env:PAYMONGO_SECRET_KEY='pk_test_your_secret_key_here'
$env:PAYMONGO_PUBLIC_KEY='pk_test_your_public_key_here'
$env:APP_CALLBACK_URL='http://localhost:5173'
$env:PAYMONGO_API_URL='https://api.paymongo.com/v1'
```

**Or update your vscode tasks.json:**

```json
{
  "label": "Run Backend with PayMongo",
  "type": "shell",
  "command": "Set-Location backend/ebike; $env:SPRING_PROFILES_ACTIVE='prod'; $env:PAYMONGO_ENABLED='true'; $env:PAYMONGO_SECRET_KEY='pk_test_xxx'; $env:PAYMONGO_PUBLIC_KEY='pk_test_xxx'; & .\\mvnw.cmd spring-boot:run"
}
```

### Step 4: Test the Integration

#### Check PayMongo Status
```bash
curl http://localhost:8083/api/payments/gcash/status
```

Response (if configured):
```json
{
  "success": true,
  "data": "CONFIGURED"
}
```

#### Initiate a Test Payment
```bash
curl -X POST http://localhost:8083/api/payments/gcash/initiate?bookingId=1
```

Response:
```json
{
  "success": true,
  "message": "GCash payment initiated",
  "data": {
    "sessionId": "cs_test_xxx",
    "checkoutUrl": "https://checkout.paymongo.com/cs_test_xxx",
    "amount": 150.00,
    "currency": "PHP",
    "bookingId": 1
  }
}
```

## Frontend Integration

### Add GCash Payment Button

In your booking/payment confirmation page, add:

```tsx
import { Button } from "@/components/ui/button";
import { AlertCircle, Loader2 } from "lucide-react";

const BookingPage = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const bookingId = /* get from route or state */;

  const handleGCashPayment = async () => {
    setLoading(true);
    setError("");

    try {
      const apiUrl = import.meta.env.VITE_API_URL || "http://localhost:8083";
      const response = await fetch(
        `${apiUrl}/api/payments/gcash/initiate?bookingId=${bookingId}`,
        { method: "POST" }
      );

      if (!response.ok) {
        throw new Error("Failed to initiate payment");
      }

      const result = await response.json();
      
      if (result.success && result.data.checkoutUrl) {
        // Redirect to PayMongo checkout
        window.location.href = result.data.checkoutUrl;
      } else {
        setError(result.message || "Failed to create checkout");
      }
    } catch (err) {
      setError(err.message || "Payment initialization failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-4">
      {error && (
        <div className="flex gap-2 rounded-lg bg-destructive/10 p-3 text-destructive">
          <AlertCircle className="h-5 w-5" />
          <p>{error}</p>
        </div>
      )}

      <Button
        onClick={handleGCashPayment}
        disabled={loading}
        size="lg"
        className="w-full bg-blue-600 hover:bg-blue-700"
      >
        {loading ? (
          <>
            <Loader2 className="mr-2 h-4 w-4 animate-spin" />
            Processing...
          </>
        ) : (
          "Pay with GCash"
        )}
      </Button>
    </div>
  );
};
```

### Query Parameter Handling for Callbacks

After PayMongo checkout, user is redirected to:
- Success: `http://localhost:5173/booking/success?booking=1`
- Cancel: `http://localhost:5173/booking/cancel?booking=1`

Handle these in your routing:

```tsx
// In your routing setup
<Route path="/booking/success" element={<BookingSuccess />} />
<Route path="/booking/cancel" element={<BookingCanceled />} />

// BookingSuccess component
const BookingSuccess = () => {
  const searchParams = new URLSearchParams(window.location.search);
  const bookingId = searchParams.get('booking');
  
  useEffect(() => {
    // Verify payment with backend
    checkPaymentStatus(bookingId);
  }, [bookingId]);

  return <div>Payment Successful! Your booking ID: {bookingId}</div>;
};
```

## API Reference

### 1. Initiate GCash Payment
```
POST /api/payments/gcash/initiate?bookingId={bookingId}

Response 200:
{
  "success": true,
  "message": "GCash payment initiated",
  "data": {
    "sessionId": "cs_test_xxx",
    "checkoutUrl": "https://checkout.paymongo.com/cs_test_xxx",
    "amount": 150.00,
    "currency": "PHP",
    "bookingId": 1
  }
}

Response 400/500:
{
  "success": false,
  "message": "Error message"
}
```

### 2. Get PayMongo Status
```
GET /api/payments/gcash/status

Response:
{
  "success": true,
  "message": "PayMongo status retrieved",
  "data": "CONFIGURED" // or "DISABLED", "NOT_CONFIGURED"
}
```

### 3. Get Payment Status
```
GET /api/payments/gcash/payment/{paymentId}

Response 200:
{
  "success": true,
  "message": "Payment status retrieved",
  "data": {
    "paymentId": "pay_xxx",
    "status": "paid",
    "amount": 15000,
    "currency": "PHP",
    "paymentMethod": "gcash"
  }
}
```

### 4. Handle PayMongo Webhook
```
POST /api/payments/gcash/callback

PayMongo sends (example):
{
  "data": {
    "attributes": {
      "type": "payment.paid",
      "data": {
        "id": "pay_xxx",
        "status": "paid",
        "amount": 15000,
        "currency": "PHP"
      }
    }
  }
}

Response:
{
  "success": true,
  "message": "Webhook verified successfully",
  "data": {
    "paymentId": "pay_xxx",
    "status": "paid"
  }
}
```

## Webhook Configuration

### Setup PayMongo Webhooks

1. Login to PayMongo Dashboard
2. Go to **Developers → Webhooks**
3. Click **Add Endpoint**
4. Enter your webhook URL:
   ```
   https://your-domain.com/api/payments/gcash/callback
   ```
5. Select events to receive:
   - ✅ `payment.paid`
   - ✅ `payment.failed`
   - ✅ `payment.abandoned`
6. Click **Add Endpoint**

### Test Webhook Locally

For local testing, use ngrok to expose your local server:

```bash
# Install ngrok or use online
ngrok http 8083

# You'll get a URL like:
# https://abc123.ngrok.io

# Configure your local webhook in PayMongo dashboard:
# https://abc123.ngrok.io/api/payments/gcash/callback
```

## Payment Flow

```
1. User clicks "Pay with GCash"
   ↓
2. Frontend calls POST /api/payments/gcash/initiate
   ↓
3. Backend creates PayMongo checkout session
   ↓
4. Backend returns checkoutUrl
   ↓
5. Frontend redirects to PayMongo checkout
   ↓
6. User selects GCash payment method
   ↓
7. User completes GCash transaction
   ↓
8. PayMongo sends webhook: payment.paid
   ↓
9. Backend receives webhook at /api/payments/gcash/callback
   ↓
10. Backend updates payment status to COMPLETED
   ↓
11. User redirected to /booking/success?booking=1
   ↓
12. Frontend shows confirmation
```

## Testing

### PayMongo Test Credentials

PayMongo provides test modes for development:

**Test GCash Amount:** PHP 1.00 (minimum)
**Test Phone Number:** Any valid format (e.g., +639123456789)
**Success Instructions:** PayMongo sandbox shows success on completion

### Test Scenario

1. Create a booking in your app (booking ID: 1)
2. Click "Pay with GCash"
3. You'll be redirected to PayMongo test checkout
4. Select GCash payment method
5. Complete the test transaction
6. Webhook will be sent (verify in PayMongo dashboard)
7. Payment status should update to COMPLETED
8. You'll be redirected to success page

## Troubleshooting

### Issue: PayMongo Status shows "NOT_CONFIGURED"
**Solution:**
- Check environment variables are set correctly
- Verify `PAYMONGO_SECRET_KEY` is not empty
- Restart backend to apply environment changes

### Issue: Webhook not being received
**Solution:**
- Verify callback URL in PayMongo dashboard is correct
- For local testing, ensure URL is accessible (use ngrok)
- Check webhook event types are enabled
- Look at PayMongo dashboard → Webhooks → Logs for errors

### Issue: Payment fails with "Invalid amount"
**Solution:**
- PayMongo requires amount in PHP cents (multiply by 100)
- Minimum amount: PHP 1.00
- Check booking total_price is positive

### Issue: "Unknown payment method"
**Solution:**
- Ensure GCash is enabled for your merchant account
- Contact PayMongo support to enable GCash

## Production Deployment

### Step 1: Switch to Live Keys
```bash
$env:PAYMONGO_SECRET_KEY='pk_live_xxxxxxxxxxxx'  # Not pk_test_!
$env:PAYMONGO_PUBLIC_KEY='pk_live_xxxxxxxxxxxx'
```

### Step 2: Update Callback URL
```bash
$env:APP_CALLBACK_URL='https://your-production-domain.com'
```

### Step 3: Configure Production Webhook
In PayMongo Dashboard, add production webhook URL:
```
https://your-production-domain.com/api/payments/gcash/callback
```

### Step 4: Enable Alerts
In PayMongo Dashboard:
- Go to Settings → Notifications
- Enable email alerts for transaction events
- Set failed payment threshold alerts

## Security Best Practices

1. **API Keys:**
   - Never commit to git
   - Use environment variables only
   - Rotate keys periodically
   - Use different keys for test/prod

2. **Webhook Verification:**
   - Always verify webhook sender
   - Check webhook signature (implement in production)
   - Validate transaction amounts match booking

3. **HTTPS Only:**
   - All production URLs must be HTTPS
   - Webhooks only from paymongo.com domains

4. **Rate Limiting:**
   - Implement rate limiting on payment endpoints
   - Prevent duplicate payment attempts

## Support & Resources

- PayMongo API Docs: https://developers.paymongo.com/
- GCash Info: https://www.globe.com.ph/gcash
- PayMongo Discord Community: https://discord.gg/paymongo
- Email Support: support@paymongo.com

## Cost Structure

- **GCash Payment Fee:** 1.5% - 2.9% (varies by partner)
- **Settlement Time:** 1-3 business days
- **Minimum Transaction:** PHP 1.00
- **Maximum Transaction:** PHP 1,000,000.00 per day initially

## Next Steps

1. ✅ Create PayMongo account
2. ✅ Get API keys
3. ✅ Set environment variables
4. ✅ Add frontend GCash button
5. ✅ Test with PayMongo test credentials
6. ✅ Configure webhook
7. ✅ Deploy to production
