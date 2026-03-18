'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { Tag } from 'lucide-react';
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
  const [promoCode, setPromoCode] = useState('');
  const [promoApplied, setPromoApplied] = useState(false);
  const [promoMessage, setPromoMessage] = useState('');
  const [discountAmount, setDiscountAmount] = useState(0);
  const [promoLoading, setPromoLoading] = useState(false);
  const { cart } = useCartStore();
  const router = useRouter();

  const subtotal = cart?.items?.reduce((sum, item) => sum + item.price * item.quantity, 0) || 0;
  const total = Math.max(0, subtotal - discountAmount);

  const handleApplyPromo = async () => {
    if (!promoCode.trim()) return;
    setPromoLoading(true);
    try {
      const res = await api.post('/api/promo-codes/validate', {
        code: promoCode,
        orderAmount: subtotal,
        restaurantId: cart?.restaurantId || '',
      });
      const result = res.data.data;
      if (result.valid) {
        setDiscountAmount(result.discountAmount);
        setPromoApplied(true);
        setPromoMessage(result.message);
        toast.success(result.message);
      } else {
        setDiscountAmount(0);
        setPromoApplied(false);
        setPromoMessage(result.message);
        toast.error(result.message);
      }
    } catch {
      toast.error('Failed to validate promo code');
    } finally {
      setPromoLoading(false);
    }
  };

  const handleRemovePromo = () => {
    setPromoCode('');
    setPromoApplied(false);
    setPromoMessage('');
    setDiscountAmount(0);
  };

  const handlePlaceOrder = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!cart || !cart.items || cart.items.length === 0) {
      toast.error('Cart is empty');
      return;
    }
    setLoading(true);
    try {
      const res = await api.post('/api/orders', {
        deliveryAddress: address,
        paymentMethod,
        promoCode: promoApplied ? promoCode : undefined,
      });
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
          style={{ background: 'var(--bg2)', border: '1.5px solid var(--border2)' }}
        >
          <h2 style={{ fontSize: '16px', fontWeight: 700, color: 'var(--text)', marginBottom: '12px' }}>
            <Tag size={16} style={{ display: 'inline', marginRight: '6px' }} />
            Promo Code
          </h2>
          {promoApplied ? (
            <div className="flex items-center justify-between p-3 rounded-xl" style={{ background: 'rgba(34,197,94,0.1)', border: '1.5px solid rgba(34,197,94,0.3)' }}>
              <div>
                <span style={{ fontSize: '14px', fontWeight: 700, color: '#22c55e' }}>{promoCode.toUpperCase()}</span>
                <span style={{ fontSize: '13px', fontWeight: 500, color: '#22c55e', marginLeft: '8px' }}>-${discountAmount.toFixed(2)}</span>
              </div>
              <button
                type="button"
                onClick={handleRemovePromo}
                style={{ fontSize: '13px', fontWeight: 700, color: '#ef4444', background: 'transparent', border: 'none', cursor: 'pointer' }}
              >
                Remove
              </button>
            </div>
          ) : (
            <div className="flex gap-2">
              <input
                type="text"
                value={promoCode}
                onChange={(e) => setPromoCode(e.target.value)}
                placeholder="Enter promo code"
                style={{
                  flex: 1,
                  padding: '10px 14px',
                  borderRadius: '10px',
                  border: '1.5px solid var(--border2)',
                  background: 'var(--bg3)',
                  color: 'var(--text)',
                  fontSize: '13px',
                  fontWeight: 500,
                  outline: 'none',
                  textTransform: 'uppercase',
                }}
              />
              <button
                type="button"
                onClick={handleApplyPromo}
                disabled={promoLoading || !promoCode.trim()}
                style={{
                  padding: '10px 20px',
                  borderRadius: '10px',
                  background: 'var(--y)',
                  color: '#000',
                  fontWeight: 700,
                  fontSize: '13px',
                  border: 'none',
                  cursor: promoLoading ? 'not-allowed' : 'pointer',
                  opacity: promoLoading || !promoCode.trim() ? 0.6 : 1,
                }}
              >
                {promoLoading ? 'Checking...' : 'Apply'}
              </button>
            </div>
          )}
          {promoMessage && !promoApplied && (
            <p style={{ fontSize: '12px', fontWeight: 500, color: '#ef4444', marginTop: '8px' }}>{promoMessage}</p>
          )}
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
          <div className="flex justify-between py-2" style={{ borderTop: '1px solid var(--border2)', marginTop: '8px', paddingTop: '8px' }}>
            <span style={{ fontSize: '14px', fontWeight: 500, color: 'var(--text2)' }}>Subtotal</span>
            <span style={{ fontSize: '14px', fontWeight: 700, color: 'var(--text)' }}>${subtotal.toFixed(2)}</span>
          </div>
          {discountAmount > 0 && (
            <div className="flex justify-between py-1">
              <span style={{ fontSize: '14px', fontWeight: 500, color: '#22c55e' }}>Discount</span>
              <span style={{ fontSize: '14px', fontWeight: 700, color: '#22c55e' }}>-${discountAmount.toFixed(2)}</span>
            </div>
          )}
          <div className="flex justify-between pt-4 mt-2" style={{ borderTop: '1.5px solid var(--border2)' }}>
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
