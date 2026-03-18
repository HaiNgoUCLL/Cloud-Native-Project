'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { Plus, Minus, Trash2 } from 'lucide-react';
import toast from 'react-hot-toast';
import { ProtectedRoute } from '@/components/ProtectedRoute';
import { useCartStore } from '@/lib/cartStore';
import { LoadingSpinner } from '@/components/LoadingSpinner';
import api from '@/lib/axios';
import { Restaurant } from '@/types';

export default function CartPage() {
  return (
    <ProtectedRoute role="CUSTOMER">
      <CartContent />
    </ProtectedRoute>
  );
}

function CartContent() {
  const { cart, fetchCart, updateItem, clearCart, loading } = useCartStore();
  const [restaurant, setRestaurant] = useState<Restaurant | null>(null);
  const router = useRouter();

  useEffect(() => {
    fetchCart();
  }, []);

  useEffect(() => {
    if (cart?.restaurantId) {
      api.get(`/api/restaurants/${cart.restaurantId}`)
        .then((res) => setRestaurant(res.data.data))
        .catch(() => {});
    }
  }, [cart?.restaurantId]);

  const total = cart?.items?.reduce((sum, item) => sum + item.price * item.quantity, 0) || 0;

  if (loading) return <LoadingSpinner />;

  if (!cart || !cart.items || cart.items.length === 0) {
    return (
      <div className="text-center py-16" style={{ background: 'var(--bg)', minHeight: '100vh' }}>
        <div style={{ fontSize: '48px', marginBottom: '12px' }}>🛒</div>
        <p style={{ fontSize: '16px', fontWeight: 700, color: 'var(--text)' }}>Your cart is empty</p>
        <p style={{ fontSize: '13px', fontWeight: 500, color: 'var(--text3)', marginTop: '4px' }}>Add some items to get started</p>
        <button
          onClick={() => router.push('/')}
          className="gradient-bg mt-6"
          style={{ padding: '12px 30px', borderRadius: '30px', color: '#000', fontWeight: 700, fontSize: '14px', border: 'none', cursor: 'pointer' }}
        >
          Browse Restaurants
        </button>
      </div>
    );
  }

  return (
    <div className="max-w-3xl mx-auto px-4 py-8" style={{ background: 'var(--bg)', minHeight: '100vh' }}>
      <h1 style={{ fontSize: '22px', fontWeight: 700, color: 'var(--text)', marginBottom: '8px' }}>Your Cart</h1>
      {restaurant && (
        <p style={{ fontSize: '14px', fontWeight: 500, color: 'var(--text2)', marginBottom: '24px' }}>
          From {restaurant.name}
        </p>
      )}

      <div className="space-y-4">
        {cart.items.map((item) => (
          <div
            key={item.menuItemId}
            className="flex items-center justify-between p-4"
            style={{ background: 'var(--bg2)', borderRadius: '16px', border: '1.5px solid var(--border2)' }}
          >
            <div className="flex-1">
              <h3 style={{ fontSize: '15px', fontWeight: 700, color: 'var(--text)' }}>{item.name}</h3>
              <p style={{ fontSize: '14px', fontWeight: 500, color: 'var(--text2)', marginTop: '4px' }}>
                ${item.price.toFixed(2)} each
              </p>
            </div>
            <div className="flex items-center gap-3">
              <button
                onClick={() => updateItem(item.menuItemId, Math.max(0, item.quantity - 1))}
                className="flex items-center justify-center"
                style={{
                  width: '30px',
                  height: '30px',
                  borderRadius: '50%',
                  border: '1.5px solid var(--border)',
                  background: 'transparent',
                  cursor: 'pointer',
                  color: 'var(--text)',
                  transition: 'all 0.2s',
                }}
                onMouseEnter={(e) => { e.currentTarget.style.background = 'var(--y)'; e.currentTarget.style.color = '#000'; }}
                onMouseLeave={(e) => { e.currentTarget.style.background = 'transparent'; e.currentTarget.style.color = 'var(--text)'; }}
              >
                <Minus size={14} />
              </button>
              <span style={{ fontSize: '15px', fontWeight: 700, color: 'var(--text)', minWidth: '20px', textAlign: 'center' }}>
                {item.quantity}
              </span>
              <button
                onClick={() => updateItem(item.menuItemId, item.quantity + 1)}
                className="flex items-center justify-center"
                style={{
                  width: '30px',
                  height: '30px',
                  borderRadius: '50%',
                  border: '1.5px solid var(--border)',
                  background: 'transparent',
                  cursor: 'pointer',
                  color: 'var(--text)',
                  transition: 'all 0.2s',
                }}
                onMouseEnter={(e) => { e.currentTarget.style.background = 'var(--y)'; e.currentTarget.style.color = '#000'; }}
                onMouseLeave={(e) => { e.currentTarget.style.background = 'transparent'; e.currentTarget.style.color = 'var(--text)'; }}
              >
                <Plus size={14} />
              </button>
              <span style={{ fontSize: '15px', fontWeight: 700, color: 'var(--y)', minWidth: '60px', textAlign: 'right' }}>
                ${(item.price * item.quantity).toFixed(2)}
              </span>
            </div>
          </div>
        ))}
      </div>

      <div
        className="mt-6 p-6"
        style={{ background: 'var(--bg2)', borderRadius: '16px', border: '1.5px solid var(--border)' }}
      >
        <div className="flex justify-between items-center mb-4">
          <span style={{ fontSize: '14px', fontWeight: 500, color: 'var(--text2)' }}>Subtotal</span>
          <span style={{ fontSize: '14px', fontWeight: 700, color: 'var(--text)' }}>${total.toFixed(2)}</span>
        </div>
        <div className="flex justify-between items-center mb-4">
          <span style={{ fontSize: '14px', fontWeight: 500, color: 'var(--text2)' }}>Delivery Fee</span>
          <span style={{ fontSize: '14px', fontWeight: 700, color: 'var(--text)' }}>$0.00</span>
        </div>
        <div className="flex justify-between items-center pt-4" style={{ borderTop: '1.5px solid var(--border2)' }}>
          <span style={{ fontSize: '18px', fontWeight: 700, color: 'var(--text)' }}>Total</span>
          <span className="gradient-text" style={{ fontSize: '18px', fontWeight: 700 }}>${total.toFixed(2)}</span>
        </div>
      </div>

      <div className="flex gap-3 mt-6">
        <button
          onClick={async () => { await clearCart(); toast.success('Cart cleared'); }}
          style={{
            padding: '14px 24px',
            borderRadius: '30px',
            background: 'var(--bg3)',
            color: 'var(--text)',
            fontWeight: 700,
            fontSize: '14px',
            border: '1.5px solid var(--border2)',
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            gap: '8px',
          }}
        >
          <Trash2 size={16} /> Clear
        </button>
        <button
          onClick={() => router.push('/checkout')}
          className="flex-1 gradient-bg"
          style={{
            padding: '14px',
            borderRadius: '30px',
            color: '#000000',
            fontWeight: 700,
            fontSize: '15px',
            border: 'none',
            cursor: 'pointer',
          }}
        >
          Proceed to Checkout
        </button>
      </div>
    </div>
  );
}
