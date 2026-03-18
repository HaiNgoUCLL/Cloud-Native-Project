'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { RefreshCw } from 'lucide-react';
import toast from 'react-hot-toast';
import { ProtectedRoute } from '@/components/ProtectedRoute';
import { LoadingSpinner } from '@/components/LoadingSpinner';
import api from '@/lib/axios';
import { Order } from '@/types';

export default function OrdersPage() {
  return (
    <ProtectedRoute role="CUSTOMER">
      <OrdersContent />
    </ProtectedRoute>
  );
}

function OrdersContent() {
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [reordering, setReordering] = useState<string | null>(null);
  const router = useRouter();

  useEffect(() => {
    fetchOrders();
  }, []);

  const fetchOrders = async () => {
    try {
      const res = await api.get('/api/orders');
      setOrders(res.data.data);
    } catch {
      setOrders([]);
    } finally {
      setLoading(false);
    }
  };

  const handleReorder = async (e: React.MouseEvent, orderId: string) => {
    e.preventDefault();
    e.stopPropagation();
    setReordering(orderId);
    try {
      await api.post(`/api/orders/${orderId}/reorder`);
      toast.success('Items added to cart!');
      router.push('/cart');
    } catch (err: unknown) {
      const error = err as { response?: { data?: { message?: string } } };
      toast.error(error.response?.data?.message || 'Failed to reorder');
    } finally {
      setReordering(null);
    }
  };

  const getStatusBadgeStyle = (status: string) => {
    switch (status) {
      case 'PENDING':
        return { background: 'var(--glow)', color: 'var(--y)', border: '1.5px solid var(--border)' };
      case 'CONFIRMED':
      case 'PREPARING':
        return { background: 'rgba(255,149,0,0.1)', color: 'var(--ya)', border: '1.5px solid rgba(255,149,0,0.3)' };
      case 'DELIVERED':
        return { background: 'rgba(34,197,94,0.1)', color: '#22c55e', border: '1.5px solid rgba(34,197,94,0.3)' };
      case 'CANCELLED':
        return { background: 'rgba(239,68,68,0.1)', color: '#ef4444', border: '1.5px solid rgba(239,68,68,0.3)' };
      default:
        return { background: 'var(--bg3)', color: 'var(--text2)', border: '1.5px solid var(--border2)' };
    }
  };

  if (loading) return <div className="py-8"><LoadingSpinner /></div>;

  return (
    <div className="max-w-3xl mx-auto px-4 py-8" style={{ background: 'var(--bg)', minHeight: '100vh' }}>
      <h1 style={{ fontSize: '22px', fontWeight: 700, color: 'var(--text)', marginBottom: '24px' }}>Your Orders</h1>

      {orders.length === 0 ? (
        <div className="text-center py-16">
          <div style={{ fontSize: '48px', marginBottom: '12px' }}>📦</div>
          <p style={{ fontSize: '16px', fontWeight: 700, color: 'var(--text)' }}>No orders yet</p>
          <p style={{ fontSize: '13px', fontWeight: 500, color: 'var(--text3)', marginTop: '4px' }}>Start ordering from your favorite restaurants</p>
        </div>
      ) : (
        <div className="space-y-4">
          {orders.map((order) => (
            <Link
              key={order.id}
              href={`/orders/${order.id}`}
              className="block p-6 rounded-2xl"
              style={{
                background: 'var(--bg2)',
                border: '1.5px solid var(--border2)',
                borderRadius: '16px',
                transition: 'all 0.2s',
              }}
              onMouseEnter={(e) => (e.currentTarget.style.borderColor = 'var(--y)')}
              onMouseLeave={(e) => (e.currentTarget.style.borderColor = 'var(--border2)')}
            >
              <div className="flex items-start justify-between mb-3">
                <div>
                  <p style={{ fontSize: '16px', fontWeight: 700, color: 'var(--text)' }}>
                    Order #{order.id.slice(-6)}
                  </p>
                  <p style={{ fontSize: '13px', fontWeight: 500, color: 'var(--text3)', marginTop: '2px' }}>
                    {new Date(order.createdAt).toLocaleDateString()}
                  </p>
                </div>
                <div className="flex items-center gap-2">
                  <span
                    style={{
                      padding: '4px 12px',
                      borderRadius: '12px',
                      fontSize: '11px',
                      fontWeight: 700,
                      textTransform: 'uppercase',
                      ...getStatusBadgeStyle(order.status),
                    }}
                  >
                    {order.status.replace('_', ' ')}
                  </span>
                </div>
              </div>
              <div className="flex justify-between items-center">
                <span style={{ fontSize: '13px', fontWeight: 500, color: 'var(--text2)' }}>
                  {order.items.length} item{order.items.length > 1 ? 's' : ''}
                </span>
                <div className="flex items-center gap-3">
                  {order.status === 'DELIVERED' && (
                    <button
                      onClick={(e) => handleReorder(e, order.id)}
                      disabled={reordering === order.id}
                      style={{
                        padding: '6px 14px',
                        borderRadius: '20px',
                        background: 'var(--bg3)',
                        color: 'var(--y)',
                        fontWeight: 700,
                        fontSize: '12px',
                        border: '1.5px solid var(--border2)',
                        cursor: reordering === order.id ? 'not-allowed' : 'pointer',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '4px',
                        opacity: reordering === order.id ? 0.6 : 1,
                      }}
                    >
                      <RefreshCw size={12} /> {reordering === order.id ? 'Adding...' : 'Reorder'}
                    </button>
                  )}
                  <span className="gradient-text" style={{ fontSize: '16px', fontWeight: 700 }}>
                    ${order.totalAmount.toFixed(2)}
                  </span>
                </div>
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}
