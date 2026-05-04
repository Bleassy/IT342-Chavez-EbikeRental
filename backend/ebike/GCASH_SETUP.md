# GCash Payment Integration Guide

## Overview
GCash payment integration is now added to your E-Bike Rental system. This guide explains how to set it up and use it.

## What's Been Added

### Backend Changes
1. **GCashPaymentService** - Service for handling GCash API calls
2. **PaymentController Endpoints**:
   - `POST /api/payments/gcash/initiate` - Initiate a GCash payment
   - `POST /api/payments/gcash/callback` - Receive GCash webhook callbacks
   - `GET /api/payments/gcash/status` - Check GCash configuration status
3. **Payment Entity** - Added `GCASH` to PaymentMethod enum
4. **Configuration** - Added GCash properties to `application.properties`

## Prerequisites

### 1. Get GCash Merchant Account
You have several options:

#### Option A: Direct GCash Partnership (For High Volume)
- Contact: GCash Business Support in Philippines
- Requirements: Business registration, VAT permit, bank account
- Website: https://www.globallpay.com/

#### Option B: Payment Gateway Integration (Recommended for Startups)
Use popular Philippine payment gateways that support GCash:

1. **Xendit** - https://xendit.co
   - Easy integration
   - Lower transaction fees
   - Good for Filipino businesses
   - Supports direct GCash, bank transfers, e-wallets

2. **Paymaya** - https://www.paymaya.com
   - Built-in GCash support
   - Good QR code support
   - Competitive rates

3. **Paybox** - https://paybox.com.ph
   - GCash integration
   - Easy setup

4. **2Checkout/Verifone** - https://www.verifone.com
   - Global + local payment support

### 2. Which Option to Choose?

For your e-bike rental system, I recommend **Xendit** because:
- ✅ Easy REST API integration
- ✅ Supports multiple payment methods (GCash, Dana, Grabpay, Bank Transfer)
- ✅ Good documentation
- ✅ Webhook support for payment confirmation
- ✅ Competitive Philippine market support

## Setup Instructions

### For Xendit Integration (Recommended)

#### Step 1: Create Xendit Account
1. Go to https://xendit.co
2. Sign up with your business email
3. Complete KYC (Know Your Customer) verification
4. Get your API keys from dashboard

#### Step 2: Add Environment Variables

When starting your backend, add these environment variables:

```bash
# .env or system environment variables
GCASH_ENABLED=true
GCASH_MERCHANT_ID=your_xendit_business_id
GCASH_MERCHANT_KEY=your_xendit_api_key
GCASH_API_URL=https://api.xendit.co
```

Or in your startup command:
```powershell
$env:GCASH_ENABLED='true'
$env:GCASH_MERCHANT_ID='your_xendit_id'
$env:GCASH_MERCHANT_KEY='your_xendit_key'
$env:GCASH_API_URL='https://api.xendit.co'
```

#### Step 3: Complete GCashPaymentService Implementation

The `GCashPaymentService.java` has TODO comments. Replace them with Xendit API calls:

```java
// For initiatePayment method - replace TODO with:
HttpHeaders headers = new HttpHeaders();
headers.setBasicAuth(merchantId, merchantKey);
headers.setContentType(MediaType.APPLICATION_JSON);

Map<String, Object> xenditRequest = new HashMap<>();
xenditRequest.put("external_id", transactionRefId);
xenditRequest.put("amount", amount.intValue());
xenditRequest.put("email_addresses", Collections.singletonList("customer@example.com"));
xenditRequest.put("items", Collections.singletonList(new HashMap<String, Object>() {{
    put("name", description);
    put("quantity", 1);
    put("price", amount.intValue());
}}));
xenditRequest.put("success_redirect_url", "http://localhost:5173/booking/success");
xenditRequest.put("failure_redirect_url", "http://localhost:5173/booking/failed");

HttpEntity<Map<String, Object>> entity = new HttpEntity<>(xenditRequest, headers);
ResponseEntity<Map> xenditResponse = restTemplate.postForEntity(
    apiUrl + "/v2/invoices",
    entity,
    Map.class
);

String checkoutUrl = (String) xenditResponse.getBody().get("invoice_url");
response.put("checkoutUrl", checkoutUrl);
```

#### Step 4: Add Required Dependencies

Already available in your `pom.xml`:
- Spring RestTemplate (for HTTP calls)
- Jackson for JSON parsing

## Frontend Integration

### Add GCash Payment Option

In your booking/payment page, add GCash button:

```tsx
import { Button } from "@/components/ui/button";

const handleGCashPayment = async (bookingId: string) => {
  try {
    const response = await fetch(
      `${import.meta.env.VITE_API_URL}/api/payments/gcash/initiate?bookingId=${bookingId}`,
      { method: "POST" }
    );
    
    const data = await response.json();
    if (data.success && data.data.checkoutUrl) {
      window.location.href = data.data.checkoutUrl;
    }
  } catch (error) {
    console.error("GCash initiation failed:", error);
  }
};

// In your payment method selection:
<Button onClick={() => handleGCashPayment(bookingId)}>
  Pay with GCash
</Button>
```

## Testing

### Test Mode
1. Xendit provides test API keys for sandbox environment
2. Use test GCash numbers for testing
3. No real charges during testing

### Test Flow
1. Click "Pay with GCash"
2. Redirected to Xendit checkout
3. Select GCash payment method
4. Complete payment flow
5. Webhook confirms payment
6. Backend updates payment status to COMPLETED

## Webhook Configuration

In Xendit dashboard:
1. Go to Settings → Webhook Configuration
2. Add callback URL: `https://your-domain.com/api/payments/gcash/callback`
3. Enable invoice/payment webhook events

## API Endpoints Reference

### Initiate GCash Payment
```
POST /api/payments/gcash/initiate
Query Params:
  - bookingId: Long (required)

Response:
{
  "success": true,
  "message": "GCash payment initiated",
  "data": {
    "transactionRefId": "BOOKING_123_xxx",
    "amount": 150.00,
    "checkoutUrl": "https://checkout.xendit.co/..."
  }
}
```

### Check GCash Status
```
GET /api/payments/gcash/status

Response:
{
  "success": true,
  "message": "GCash status retrieved",
  "data": "CONFIGURED" // or "DISABLED", "NOT_CONFIGURED"
}
```

### Payment Completed Regular Flow
```
POST /api/payments
Query Params:
  - bookingId: Long
  - paymentMethod: GCASH

Response:
{
  "success": true,
  "data": {
    "id": 1,
    "bookingId": 123,
    "amount": 150.00,
    "paymentMethod": "GCASH",
    "paymentStatus": "PENDING"
  }
}
```

## Payment Status Flow

```
PENDING → (User completes GCash payment) → COMPLETED
       → (User cancels) → FAILED
       → (Refund requested) → REFUNDED
```

## Security Considerations

1. **API Key Security**:
   - Never commit API keys to git
   - Use environment variables only
   - Rotate keys periodically

2. **Webhook Verification**:
   - Always verify webhook signature
   - Check transaction amount matches
   - Validate webhook source

3. **HTTPS Only**:
   - Always use HTTPS for webhook URLs
   - Callback endpoints must be HTTPS

## Troubleshooting

### Issue: GCash payment fails
- Check merchant credentials in environment variables
- Verify GCash is CONFIGURED: call `/api/payments/gcash/status`
- Check Xendit dashboard for API errors

### Issue: Webhook not being called
- Verify callback URL in Xendit dashboard
- Check URL is publicly accessible (not localhost)
- Verify HTTPS is enabled

### Issue: Payment status not updating
- Check webhook verification logic
- Verify database connection
- Check application logs for errors

## Next Steps

1. Create Xendit account
2. Get API credentials
3. Update GCashPaymentService with Xendit API calls
4. Add GCash button to frontend booking page
5. Configure webhook in Xendit dashboard
6. Test with sandbox credentials
7. Deploy to production with production API keys

## Support

- Xendit Documentation: https://docs.xendit.co
- GCash: https://www.globallpay.com/
- Payment Gateway Comparison: https://ph.startupranking.com/payment-gateways

## Cost Estimate

- Xendit GCash: 2-3% transaction fee
- Minimum transaction: PHP 1.00
- Settlement: Within 1-5 business days
