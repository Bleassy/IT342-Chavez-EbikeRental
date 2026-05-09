import React from 'react';
import { CreditCard, Banknote } from 'lucide-react';
import { Button } from '@/components/ui/button';

interface PaymentMethodSelectorProps {
  amount: number;
  bikeName: string;
  onSelectOnline: () => void;
  onSelectCash: () => void;
  disabled?: boolean;
}

export function PaymentMethodSelector({
  amount,
  bikeName,
  onSelectOnline,
  onSelectCash,
  disabled = false
}: PaymentMethodSelectorProps) {
  return (
    <div className="space-y-4">
      <div className="mb-6">
        <h2 className="font-display text-2xl font-bold text-foreground">Choose Payment Method</h2>
        <p className="text-muted-foreground mt-1">For {bikeName} - ₱{amount.toFixed(2)}</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {/* Online Payment (Stripe) */}
        <button
          onClick={onSelectOnline}
          disabled={disabled}
          className="group relative overflow-hidden rounded-xl border-2 border-green-500 bg-green-50 p-6 transition-all hover:bg-green-100 hover:shadow-lg disabled:opacity-50 disabled:cursor-not-allowed"
        >
          <div className="flex flex-col items-center text-center space-y-3">
            <div className="flex h-16 w-16 items-center justify-center rounded-full bg-green-600 text-white group-hover:scale-110 transition-transform">
              <CreditCard className="h-8 w-8" />
            </div>
            <div>
              <h3 className="font-display text-lg font-bold text-green-900">Online Payment</h3>
              <p className="text-sm text-green-700 mt-1">Secure Stripe Payment</p>
            </div>
            <div className="pt-2">
              <p className="text-xs text-green-600">Pay securely with credit/debit card</p>
            </div>
          </div>
        </button>

        {/* Cash Payment */}
        <button
          onClick={onSelectCash}
          disabled={disabled}
          className="group relative overflow-hidden rounded-xl border-2 border-blue-500 bg-blue-50 p-6 transition-all hover:bg-blue-100 hover:shadow-lg disabled:opacity-50 disabled:cursor-not-allowed"
        >
          <div className="flex flex-col items-center text-center space-y-3">
            <div className="flex h-16 w-16 items-center justify-center rounded-full bg-blue-600 text-white group-hover:scale-110 transition-transform">
              <Banknote className="h-8 w-8" />
            </div>
            <div>
              <h3 className="font-display text-lg font-bold text-blue-900">Cash Payment</h3>
              <p className="text-sm text-blue-700 mt-1">Pay at Pickup</p>
            </div>
            <div className="pt-2">
              <p className="text-xs text-blue-600">Pay cash when you pick up the bike</p>
            </div>
          </div>
        </button>
      </div>

      <div className="rounded-lg bg-yellow-50 border border-yellow-200 p-4 text-sm text-yellow-800">
        <p className="font-semibold">💡 Note:</p>
        <p className="mt-1">
          Choose "Online Payment" for immediate booking confirmation with Stripe. Choose "Cash Payment" to complete payment when you pick up the bike.
        </p>
      </div>
    </div>
  );
}

export default PaymentMethodSelector;
