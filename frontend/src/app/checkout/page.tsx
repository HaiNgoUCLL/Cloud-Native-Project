'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import toast from 'react-hot-toast';
import { ProtectedRoute } from '@/components/ProtectedRoute';
import { useCartStore } from '@/lib/cartStore';
import api from '@/lib/axios';
import { PaymentMethod } from '@/types';

const PAYMENT_METHODS: { value: PaymentMethod; label: string; icon: string }[] = [
  { value: 'CREDIT_CARD', label: 'Credit Card', icon: '💳' },
  { value: 'PAYPAL', label: 'PayPal', icon: '🅿️' },
  { value: 'CASH', label: 'Cash on Delivery', icon: '💵' },
];

export default function CheckoutPage() {
  return (
    <ProtectedRoute role="CUSTOMER">
      <CheckoutContent />
    </ProtectedRoute>
  );
}

function CheckoutContent() {
  const [address, setAddress] = useState('');
  const [paymentMethod, setPaymentMethod] = useState<PaymentMethod>('CREDIT_CARD');
  const [loading, setLoading] = useState(false);
  const { cart } = useCartStore();
  const router = useRouter();

  const total = cart?.items?.reduce((sum, item) => sum + item.price * item.quantity, 0) || 0;

  const handlePlaceOrder = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!cart || !cart.items || cart.items.length === 0) {
      toast.error('Cart is empty');
      return;
    }
    setLoading(true);
    try {
      const res = await api.post('/api/orders', { deliveryAddress: address, paymentMethod });
      const order = res.data.data;
      toast.success('Order placed!');
      router.push(`/checkout/payment?orderId=${order.id}`);
    } catch (err: unknown) {
      const error = err as { response?: { data?: { message?: string } } };
      toast.error(error.response?.data?.message || 'Failed to place order');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-2xl mx-auto px-4 py-8" style={{ background: 'var(--bg)', minHeight: '100vh' }}>
      <h1 style={{ fontSize: '22px', fontWeight: 700, color: 'var(--text)', marginBottom: '24px' }}>Checkout</h1>

      <form onSubmit={handlePlaceOrder} className="space-y-6">
        <div
          className="p-6 rounded-2xl"
          style={{ background: 'var(--bg2)', border: '1.5px solid var(--border2)' }}
        >
          <h2 style={{ fontSize: '16px', fontWeight: 700, color: 'var(--text)', marginBottom: '12px' }}>Delivery Address</h2>
          <input
            type="text"
            value={address}
            onChange={(e) => setAddress(e.target.value)}
            required
            placeholder="Enter your full delivery address"
            style={{
              width: '100%',
              padding: '12px 16px',
              borderRadius: '12px',
              border: '1.5px solid var(--border2)',
              background: 'var(--bg3)',
              color: 'var(--text)',
              fontSize: '14px',
              fontWeight: 500,
              outline: 'none',
            }}
          />
        </div>

        <div
          className="p-6 rounded-2xl"
          style={{ background: 'var(--bg2)', border: '1.5px solid var(--border2)' }}
        >
          <h2 style={{ fontSize: '16px', fontWeight: 700, color: 'var(--text)', marginBottom: '12px' }}>Payment Method</h2>
          <div className="space-y-3">
            {PAYMENT_METHODS.map((pm) => (
              <button
                type="button"
                key={pm.value}
                onClick={() => setPaymentMethod(pm.value)}
                className="w-full flex items-center gap-3 p-4"
                style={{
                  borderRadius: '12px',
                  border: `1.5px solid ${paymentMethod === pm.value ? 'var(--y)' : 'var(--border2)'}`,
                  background: paymentMethod === pm.value ? 'var(--glow)' : 'var(--bg3)',
                  cursor: 'pointer',
                  transition: 'all 0.2s',
                }}
              >
                <span style={{ fontSize: '20px' }}>{pm.icon}</span>
                <span style={{ fontSize: '14px', fontWeight: paymentMethod === pm.value ? 700 : 500, color: 'var(--text)' }}>
                  {pm.label}
                </span>
              </button>
            ))}
          </div>
        </div>

        <div
          className="p-6 rounded-2xl"
          style={{ background: 'var(--bg2)', border: '1.5px solid var(--border)' }}
        >
          <h2 style={{ fontSize: '16px', fontWeight: 700, color: 'var(--text)', marginBottom: '12px' }}>Order Summary</h2>
          {cart?.items?.map((item) => (
            <div key={item.menuItemId} className="flex justify-between py-2">
              <span style={{ fontSize: '13px', fontWeight: 500, color: 'var(--text2)' }}>
                {item.name} x{item.quantity}
              </span>
              <span style={{ fontSize: '13px', fontWeight: 700, color: 'var(--text)' }}>
                ${(item.price * item.quantity).toFixed(2)}
              </span>
            </div>
          ))}
          <div className="flex justify-between pt-4 mt-4" style={{ borderTop: '1.5px solid var(--border2)' }}>
            <span style={{ fontSize: '18px', fontWeight: 700, color: 'var(--text)' }}>Total</span>
            <span className="gradient-text" style={{ fontSize: '18px', fontWeight: 700 }}>${total.toFixed(2)}</span>
          </div>
        </div>

        <button
          type="submit"
          disabled={loading}
          className="w-full gradient-bg"
          style={{
            padding: '16px',
            borderRadius: '30px',
            color: '#000000',
            fontSize: '15px',
            fontWeight: 700,
            border: 'none',
            cursor: loading ? 'not-allowed' : 'pointer',
            opacity: loading ? 0.7 : 1,
          }}
        >
          {loading ? 'Placing Order...' : `Place Order — $${total.toFixed(2)}`}
        </button>
      </form>
    </div>
  );
}
