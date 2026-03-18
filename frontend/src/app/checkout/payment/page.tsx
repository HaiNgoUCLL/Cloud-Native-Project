'use client';

import { useState, Suspense } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import toast from 'react-hot-toast';
import { ProtectedRoute } from '@/components/ProtectedRoute';
import { LoadingSpinner } from '@/components/LoadingSpinner';
import api from '@/lib/axios';

export default function PaymentPage() {
  return (
    <ProtectedRoute role="CUSTOMER">
      <Suspense fallback={<LoadingSpinner />}>
        <PaymentContent />
      </Suspense>
    </ProtectedRoute>
  );
}

function PaymentContent() {
  const searchParams = useSearchParams();
  const orderId = searchParams.get('orderId');
  const [loading, setLoading] = useState(false);
  const [paid, setPaid] = useState(false);
  const [cardNumber, setCardNumber] = useState('');
  const [expiry, setExpiry] = useState('');
  const [cvv, setCvv] = useState('');
  const router = useRouter();

  const handlePayment = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!orderId) return;
    setLoading(true);
    try {
      await api.post(`/api/orders/${orderId}/pay`);
      setPaid(true);
      toast.success('Payment successful!');
      setTimeout(() => router.push(`/orders/${orderId}`), 2000);
    } catch (err: unknown) {
      const error = err as { response?: { data?: { message?: string } } };
      toast.error(error.response?.data?.message || 'Payment failed');
    } finally {
      setLoading(false);
    }
  };

  if (paid) {
    return (
      <div className="flex flex-col items-center justify-center min-h-screen px-4" style={{ background: 'var(--bg)' }}>
        <div style={{ fontSize: '64px', marginBottom: '16px' }}>✅</div>
        <h1 className="gradient-text" style={{ fontSize: '22px', fontWeight: 700 }}>Payment Successful!</h1>
        <p style={{ fontSize: '14px', fontWeight: 500, color: 'var(--text2)', marginTop: '8px' }}>
          Redirecting to your order...
        </p>
      </div>
    );
  }

  const inputStyle = {
    width: '100%',
    padding: '12px 16px',
    borderRadius: '12px',
    border: '1.5px solid var(--border2)',
    background: 'var(--bg3)',
    color: 'var(--text)',
    fontSize: '14px',
    fontWeight: 500 as const,
    outline: 'none',
  };

  return (
    <div className="max-w-md mx-auto px-4 py-8" style={{ background: 'var(--bg)', minHeight: '100vh' }}>
      <h1 style={{ fontSize: '22px', fontWeight: 700, color: 'var(--text)', marginBottom: '8px' }}>
        Simulated Payment
      </h1>
      <p style={{ fontSize: '14px', fontWeight: 500, color: 'var(--text2)', marginBottom: '24px' }}>
        This is a simulated payment. No real charges will be made.
      </p>

      <form onSubmit={handlePayment}>
        <div className="p-6 rounded-2xl space-y-4" style={{ background: 'var(--bg2)', border: '1.5px solid var(--border2)' }}>
          <div>
            <label style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text2)', display: 'block', marginBottom: '6px' }}>
              Card Number
            </label>
            <input
              type="text"
              value={cardNumber}
              onChange={(e) => setCardNumber(e.target.value)}
              placeholder="4242 4242 4242 4242"
              required
              style={inputStyle}
            />
          </div>
          <div className="flex gap-4">
            <div className="flex-1">
              <label style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text2)', display: 'block', marginBottom: '6px' }}>
                Expiry
              </label>
              <input
                type="text"
                value={expiry}
                onChange={(e) => setExpiry(e.target.value)}
                placeholder="MM/YY"
                required
                style={inputStyle}
              />
            </div>
            <div className="flex-1">
              <label style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text2)', display: 'block', marginBottom: '6px' }}>
                CVV
              </label>
              <input
                type="text"
                value={cvv}
                onChange={(e) => setCvv(e.target.value)}
                placeholder="123"
                required
                style={inputStyle}
              />
            </div>
          </div>
        </div>
        <button
          type="submit"
          disabled={loading}
          className="w-full gradient-bg mt-6"
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
          {loading ? 'Processing...' : 'Pay Now (Simulated)'}
        </button>
      </form>
    </div>
  );
}
