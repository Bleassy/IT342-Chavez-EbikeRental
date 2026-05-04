// SAMPLE: How to integrate Stripe payment in your BookingPage.tsx
// Add this code to your existing BookingPage component

import { StripePaymentComponent } from '@/components/StripePayment';
import { useState } from 'react';

export default function BookingPage() {
  const [showPayment, setShowPayment] = useState(false);
  const [bookingId, setBookingId] = useState<number | null>(null);
  const [totalPrice, setTotalPrice] = useState(0);

  // After user confirms booking, show payment form
  const handleBookingSubmit = async (bookingData: any) => {
    try {
      const response = await fetch('http://192.168.254.105:8083/api/bookings', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        },
        body: JSON.stringify(bookingData)
      });

      const result = await response.json();
      if (result.success) {
        setBookingId(result.data.id);
        setTotalPrice(result.data.totalPrice);
        setShowPayment(true); // Show payment form
      }
    } catch (error) {
      console.error('Booking failed:', error);
    }
  };

  const handlePaymentSuccess = (paymentIntentId: string) => {
    console.log('Payment successful! Intent ID:', paymentIntentId);
    // TODO: Update booking payment status in backend
    // TODO: Redirect to confirmation page
    // TODO: Show success message
  };

  const handlePaymentError = (error: string) => {
    console.error('Payment error:', error);
    // TODO: Show error message to user
  };

  return (
    <div>
      {/* Your existing booking form */}
      {!showPayment && (
        <div>
          {/* Booking details form */}
          <button onClick={() => handleBookingSubmit({/* form data */})}>
            Confirm Booking
          </button>
        </div>
      )}

      {/* Stripe Payment Form */}
      {showPayment && bookingId && (
        <div className="payment-section">
          <h2>Complete Payment</h2>
          <StripePaymentComponent
            bookingId={bookingId}
            amount={totalPrice}
            onSuccess={handlePaymentSuccess}
            onError={handlePaymentError}
          />
        </div>
      )}
    </div>
  );
}

// ============================================
// INTEGRATION CHECKLIST:
// ============================================
// 
// 1. ✅ Backend endpoints created:
//    - GET /api/payments/stripe/config
//    - POST /api/payments/stripe/create-payment-intent
//    - GET /api/payments/stripe/payment-intent/{id}
//    - POST /api/payments/stripe/webhook
//
// 2. ✅ StripePaymentService created with methods:
//    - createPaymentIntent(bookingId, amount, customerId)
//    - getPaymentIntentStatus(paymentIntentId)
//    - isStripeEnabled()
//    - getPublishableKey()
//
// 3. ✅ Frontend component created:
//    - StripePaymentComponent.tsx with CardElement
//
// 4. ✅ Dependencies added:
//    - Backend: com.stripe:stripe-java:24.8.0 (in pom.xml)
//    - Frontend: @stripe/react-stripe-js, @stripe/js (in package.json)
//
// 5. ⚠️ STILL TODO - Add Stripe keys:
//    - Go to https://stripe.com/dashboard/apikeys
//    - Copy your test publishable and secret keys
//    - Update backend/ebike/src/main/resources/application.properties:
//      stripe.api.key=sk_test_YOUR_KEY
//      stripe.publishable.key=pk_test_YOUR_KEY
//      stripe.webhook.secret=whsec_YOUR_SECRET
//    - Update web/.env:
//      VITE_STRIPE_PUBLISHABLE_KEY=pk_test_YOUR_KEY
//
// 6. ⚠️ STILL TODO - Install frontend dependencies:
//    cd web
//    npm install
//
// 7. ⚠️ STILL TODO - Test the integration:
//    - Start backend: cd backend/ebike && .\mvnw.cmd spring-boot:run
//    - Start frontend: cd web && npm run dev
//    - Test payment with card: 4242 4242 4242 4242
//
// ============================================
