# Stripe Integration Setup Guide

## Step 1: Create a Stripe Account
1. Go to https://stripe.com
2. Click "Get Started" or "Sign Up"
3. Fill in your email, password, and business information
4. Verify your email address
5. Complete profile setup (business name, country, currency)

## Step 2: Get Your API Keys
1. Log in to Stripe Dashboard: https://dashboard.stripe.com
2. Click on "Developers" in the left sidebar
3. Click on "API Keys" tab
4. You'll see two key types:
   - **Publishable Key** (starts with `pk_test_` or `pk_live_`)
   - **Secret Key** (starts with `sk_test_` or `sk_live_`)

### For Testing (Development):
- Use TEST API Keys (shows "RESTRICTED" label)
- Test Card: `4242 4242 4242 4242` | Any expiry | Any CVC

## Step 3: Configure Backend

Update `backend/ebike/src/main/resources/application.properties`:

```properties
# Stripe Configuration
stripe.enabled=true
stripe.api.key=sk_test_YOUR_SECRET_KEY_HERE
stripe.publishable.key=pk_test_YOUR_PUBLISHABLE_KEY_HERE
stripe.webhook.secret=whsec_YOUR_WEBHOOK_SECRET_HERE
```

Replace with your actual keys from the Stripe Dashboard.

## Step 4: Configure Frontend

Update `web/.env`:

```env
VITE_STRIPE_PUBLISHABLE_KEY=pk_test_YOUR_PUBLISHABLE_KEY_HERE
```

Replace with your actual publishable key.

## Step 5: Set Up Webhook (Optional but Recommended)

1. Go to Stripe Dashboard → Developers → Webhooks
2. Click "Add Endpoint"
3. Enter your endpoint URL: `http://YOUR_DOMAIN/api/payments/stripe/webhook`
   - For local testing: Use ngrok to expose your local server
   - Example: `https://your-ngrok-url.ngrok.io/api/payments/stripe/webhook`
4. Select events to listen for:
   - `payment_intent.succeeded`
   - `payment_intent.payment_failed`
   - `charge.refunded`
5. Copy the Webhook Signing Secret and add to `application.properties`:
   ```properties
   stripe.webhook.secret=whsec_YOUR_WEBHOOK_SECRET_HERE
   ```

## Step 6: Set Up ngrok for Local Webhooks (Development Only)

1. Download ngrok from https://ngrok.com/download
2. Run: `ngrok http 8083`
3. Copy the forwarding URL (e.g., `https://abc123.ngrok.io`)
4. In Stripe Webhooks, use: `https://abc123.ngrok.io/api/payments/stripe/webhook`

## Step 7: Install Dependencies

### Backend:
Already added in `pom.xml`:
```xml
<dependency>
    <groupId>com.stripe</groupId>
    <artifactId>stripe-java</artifactId>
    <version>24.8.0</version>
</dependency>
```

### Frontend:
Install Stripe React packages:
```bash
cd web
npm install @stripe/react-stripe-js @stripe/js
```

Or if dependencies already added to package.json:
```bash
npm install
```

## Step 8: Available API Endpoints

### Get Stripe Configuration
```
GET /api/payments/stripe/config
Response: { publishableKey: "pk_test_...", enabled: true }
```

### Create Payment Intent
```
POST /api/payments/stripe/create-payment-intent?bookingId=1&customerId=user123
Response: { clientSecret: "pi_..._secret_...", paymentIntentId: "pi_...", status: "requires_payment_method" }
```

### Get Payment Intent Status
```
GET /api/payments/stripe/payment-intent/{paymentIntentId}
Response: { status: "succeeded|processing|requires_payment_method", amount: 1000, ... }
```

### Stripe Webhook
```
POST /api/payments/stripe/webhook
Headers: Stripe-Signature: t=...,v1=...
Body: Stripe webhook payload
```

## Step 9: Test the Integration

### 1. Start Backend:
```bash
cd backend/ebike
.\mvnw.cmd spring-boot:run
```

### 2. Start Frontend:
```bash
cd web
npm run dev
```

### 3. Test Payment Flow:
1. Log in as a user
2. Book a bike
3. Go to checkout/payment page
4. You'll see the Stripe payment form
5. Use test card: `4242 4242 4242 4242`
6. Any future expiry date
7. Any 3-digit CVC
8. Click "Pay"
9. Check Stripe Dashboard for the payment intent

## Step 10: Test Different Scenarios

### Successful Payment:
Card: `4242 4242 4242 4242` | Result: Payment succeeds

### Require Authentication:
Card: `4000 0025 0000 3155` | Result: 3D Secure required

### Declined Card:
Card: `4000 0000 0000 0002` | Result: Card declined

### Insufficient Funds:
Card: `4000 0000 0000 9995` | Result: Insufficient funds

## Step 11: Move to Production

When ready for live payments:
1. Activate your Stripe account
2. Complete identity verification
3. Get LIVE API Keys (start with `pk_live_` and `sk_live_`)
4. Update `application.properties` with live keys
5. Update `web/.env` with live publishable key
6. Test thoroughly with small amounts
7. Update webhook URL to production domain

## Backend API Methods Implemented

### PaymentController Methods:
- `GET /stripe/config` - Get Stripe configuration (publishable key)
- `POST /stripe/create-payment-intent` - Create payment intent for booking
- `GET /stripe/payment-intent/{paymentIntentId}` - Check payment status
- `POST /stripe/webhook` - Receive Stripe webhooks

### StripePaymentService Methods:
- `createStripeCustomer(email, name)` - Create customer in Stripe
- `createPaymentIntent(bookingId, amount, customerId)` - Create payment intent
- `getPaymentIntentStatus(paymentIntentId)` - Get payment status
- `refundPayment(paymentIntentId)` - Issue refund
- `isStripeEnabled()` - Check if Stripe is enabled
- `getPublishableKey()` - Get publishable key

## Frontend Component

### StripePaymentComponent.tsx
- Automatically loads Stripe for your publishable key
- Creates payment intent on mount
- Handles card element and payment processing
- Shows success/error toasts
- Validates card input

Usage:
```typescript
<StripePaymentComponent 
  bookingId={bookingId}
  amount={totalPrice}
  onSuccess={(paymentIntentId) => handleSuccess(paymentIntentId)}
  onError={(error) => handleError(error)}
/>
```

## Troubleshooting

### Issue: "Invalid API Key"
- Verify you copied the FULL secret key from Stripe Dashboard
- Make sure you're using the TEST key (not LIVE)
- Check for extra spaces before/after the key

### Issue: "Payment Declined"
- Use valid test card numbers
- Check card expiry is in future
- Try different test cards from Step 10

### Issue: "Webhook Not Received"
- Ensure webhook endpoint URL is correct
- Check ngrok is running (for local testing)
- Verify webhook signing secret is correctly set
- Check Stripe Dashboard → Webhooks → Endpoint Details for error logs

### Issue: "CORS Error"
- Add frontend domain to backend CORS configuration
- Update SecurityConfig.java if needed

### Issue: Amount Incorrect
- Amount is converted to cents automatically (multiplied by 100)
- So ₱100 becomes 10000 cents in Stripe
- This is done in StripePaymentService.createPaymentIntent()

## Next Steps

1. Complete backend setup (add Stripe keys to application.properties)
2. Run `npm install` to install Stripe packages
3. Update .env with your Stripe publishable key
4. Test payment flow end-to-end
5. Set up webhooks for production-ready integration

## Support

For Stripe documentation, visit:
- https://stripe.com/docs/payments/accept-a-payment
- https://stripe.com/docs/payments/payment-intents
- https://stripe.com/docs/webhooks

For integration questions, check:
- Stripe Dashboard → Help center
- Stripe Community → https://support.stripe.com
