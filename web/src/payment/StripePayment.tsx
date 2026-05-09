import React, { useState, useEffect } from 'react';
import { Elements, CardElement, useStripe, useElements } from '@stripe/react-stripe-js';
import { toast } from '@/hooks/use-toast';

// Get Stripe from window (loaded from CDN)
const getStripePromise = async () => {
  if ((window as any).Stripe) {
    return (window as any).Stripe(import.meta.env.VITE_STRIPE_PUBLISHABLE_KEY || '');
  }
  throw new Error('Stripe.js failed to load');
};

interface StripePaymentProps {
  bookingId: number;
  amount: number;
  onSuccess?: (paymentIntentId: string) => void;
  onError?: (error: string) => void;
}

// Inner form component (requires Elements provider)
function StripePaymentForm({ bookingId, amount, onSuccess, onError }: StripePaymentProps) {
  const stripe = useStripe();
  const elements = useElements();
  const [loading, setLoading] = useState(false);
  const [clientSecret, setClientSecret] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [postalCode, setPostalCode] = useState('1000');

  // Create payment intent on component mount
  useEffect(() => {
    const createPaymentIntent = async () => {
      try {
        const apiUrl = import.meta.env.VITE_API_URL || 'http://192.168.254.105:8083';
        const token = localStorage.getItem('ebike_auth');
        let authToken = '';
        
        if (token) {
          try {
            const parsed = JSON.parse(token);
            authToken = parsed.token;
          } catch {
            authToken = token;
          }
        }

        const response = await fetch(`${apiUrl}/api/payments/stripe/create-payment-intent?bookingId=${bookingId}&customerId=user-${bookingId}`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            ...(authToken && { 'Authorization': `Bearer ${authToken}` })
          }
        });

        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        if (data.data?.clientSecret) {
          setClientSecret(data.data.clientSecret);
          setError(null);
        } else {
          throw new Error(data.message || 'Failed to create payment intent');
        }
      } catch (err) {
        const errorMsg = err instanceof Error ? err.message : 'Payment setup failed. Make sure backend is running.';
        console.error('Error creating payment intent:', err);
        setError(errorMsg);
        onError?.(errorMsg);
      }
    };

    createPaymentIntent();
  }, [bookingId, onError]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!stripe || !elements || !clientSecret) {
      const msg = "Payment not ready. Please wait for form to load or check backend connection.";
      setError(msg);
      toast({ title: "Payment not ready", description: msg, variant: "destructive" });
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const cardElement = elements.getElement(CardElement);
      if (!cardElement) throw new Error('Card element not found');

      const { error: stripeError, paymentIntent } = await stripe.confirmCardPayment(clientSecret, {
        payment_method: {
          card: cardElement,
          billing_details: {
            email: localStorage.getItem('email') || 'user@example.com',
            address: {
              postal_code: postalCode || '1000'
            }
          }
        }
      });

      if (stripeError) {
        setError(stripeError.message);
        toast({ title: "❌ Payment Failed", description: stripeError.message, variant: "destructive" });
        onError?.(stripeError.message);
      } else if (paymentIntent?.status === 'succeeded') {
        setError(null);
        toast({ title: "✅ Payment Successful!", description: `Payment of ₱${amount} completed`, variant: "success" });
        onSuccess?.(paymentIntent.id);
      } else {
        const msg = `Payment status: ${paymentIntent?.status}`;
        setError(msg);
        toast({ title: "Payment Status", description: msg, variant: "default" });
      }
    } catch (err) {
      const errorMsg = err instanceof Error ? err.message : 'Payment processing failed';
      setError(errorMsg);
      toast({ title: "❌ Payment Error", description: errorMsg, variant: "destructive" });
      onError?.(errorMsg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      {error && (
        <div className="bg-red-50 border border-red-200 text-red-800 px-4 py-3 rounded-lg text-sm">
          ⚠️ {error}
        </div>
      )}

      <div className="border border-gray-300 rounded-lg p-4">
        <CardElement
          options={{
            style: {
              base: {
                fontSize: '16px',
                color: '#424770',
                '::placeholder': {
                  color: '#aab7c4',
                }
              },
              invalid: {
                color: '#fa755a',
              }
            }
          }}
        />
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Postal Code
        </label>
        <input
          type="text"
          placeholder="1000"
          value={postalCode}
          onChange={(e) => setPostalCode(e.target.value)}
          className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-green-500 focus:border-green-500"
        />
      </div>

      <div className="space-y-2">
        <p className="text-sm text-gray-600">
          Total Amount: <span className="font-bold text-lg text-green-600">₱{amount.toFixed(2)}</span>
        </p>
      </div>

      <button
        type="submit"
        disabled={loading || !stripe || !clientSecret}
        className="w-full bg-green-600 hover:bg-green-700 disabled:bg-gray-400 text-white font-bold py-3 px-4 rounded-lg transition"
      >
        {loading ? '⏳ Processing...' : `💳 Pay ₱${amount.toFixed(2)}`}
      </button>

      <p className="text-xs text-gray-500 text-center">
        🔒 Safe and secure payment powered by Stripe
      </p>
    </form>
  );
}

// Wrapper component with Elements provider
export function StripePaymentComponent(props: StripePaymentProps) {
  const [stripePromise, setStripePromise] = React.useState<any>(null);

  React.useEffect(() => {
    getStripePromise()
      .then(stripe => setStripePromise(stripe))
      .catch(err => console.error('Failed to load Stripe:', err));
  }, []);

  if (!stripePromise) {
    return <div className="text-center py-4 text-gray-500">Loading payment form...</div>;
  }

  return (
    <Elements stripe={stripePromise}>
      <StripePaymentForm {...props} />
    </Elements>
  );
}

export default StripePaymentComponent;
