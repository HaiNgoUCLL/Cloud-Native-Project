'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import toast from 'react-hot-toast';
import { ProtectedRoute } from '@/components/ProtectedRoute';
import { OrderStatusStepper } from '@/components/OrderStatusStepper';
import { LoadingSpinner } from '@/components/LoadingSpinner';
import api from '@/lib/axios';
import { Order } from '@/types';

export default function OrderDetailPage() {
  return (
    <ProtectedRoute role="CUSTOMER">
      <OrderDetailContent />
    </ProtectedRoute>
  );
}

function OrderDetailContent() {
  const { id } = useParams<{ id: string }>();
  const [order, setOrder] = useState<Order | null>(null);
  const [loading, setLoading] = useState(true);
  const router = useRouter();

  useEffect(() => {
    fetchOrder();
    const interval = setInterval(fetchOrder, 5000);
    return () => clearInterval(interval);
  }, [id]);

  const fetchOrder = async () => {
    try {
      const res = await api.get(`/api/orders/${id}`);
      setOrder(res.data.data);
    } catch {
      toast.error('Failed to load order');
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = async () => {
    try {
      await api.put(`/api/orders/${id}/cancel`);
      toast.success('Order cancelled');
      fetchOrder();
    } catch (err: unknown) {
      const error = err as { response?: { data?: { message?: string } } };
      toast.error(error.response?.data?.message || 'Cannot cancel order');
    }
  };

  if (loading) return <div className="py-8"><LoadingSpinner /></div>;
  if (!order) return <div className="text-center py-16"><p style={{ fontSize: '16px', fontWeight: 700 }}>Order not found</p></div>;

  return (
    <div className="max-w-3xl mx-auto px-4 py-8" style={{ background: 'var(--bg)', minHeight: '100vh' }}>
      <h1 style={{ fontSize: '22px', fontWeight: 700, color: 'var(--text)', marginBottom: '4px' }}>
        Order #{order.id.slice(-6)}
      </h1>
      <p style={{ fontSize: '13px', fontWeight: 500, color: 'var(--text3)', marginBottom: '24px' }}>
        Placed on {new Date(order.createdAt).toLocaleString()}
      </p>

      <div className="p-6 rounded-2xl mb-6" style={{ background: 'var(--bg2)', border: '1.5px solid var(--border2)' }}>
        <h2 style={{ fontSize: '16px', fontWeight: 700, color: 'var(--text)', marginBottom: '12px' }}>Order Status</h2>
        <OrderStatusStepper currentStatus={order.status} />
      </div>

      <div className="p-6 rounded-2xl mb-6" style={{ background: 'var(--bg2)', border: '1.5px solid var(--border2)' }}>
        <h2 style={{ fontSize: '16px', fontWeight: 700, color: 'var(--text)', marginBottom: '12px' }}>Items</h2>
        {order.items.map((item, i) => (
          <div key={i} className="flex justify-between py-2" style={{ borderBottom: i < order.items.length - 1 ? '1px solid var(--border2)' : 'none' }}>
            <div>
              <span style={{ fontSize: '14px', fontWeight: 700, color: 'var(--text)' }}>{item.name}</span>
              <span style={{ fontSize: '13px', fontWeight: 500, color: 'var(--text3)', marginLeft: '8px' }}>x{item.quantity}</span>
            </div>
            <span style={{ fontSize: '14px', fontWeight: 700, color: 'var(--text)' }}>${(item.price * item.quantity).toFixed(2)}</span>
          </div>
        ))}
        <div className="flex justify-between pt-4 mt-4" style={{ borderTop: '1.5px solid var(--border2)' }}>
          <span style={{ fontSize: '18px', fontWeight: 700, color: 'var(--text)' }}>Total</span>
          <span className="gradient-text" style={{ fontSize: '18px', fontWeight: 700 }}>${order.totalAmount.toFixed(2)}</span>
        </div>
      </div>

      <div className="p-6 rounded-2xl mb-6" style={{ background: 'var(--bg2)', border: '1.5px solid var(--border2)' }}>
        <h2 style={{ fontSize: '16px', fontWeight: 700, color: 'var(--text)', marginBottom: '8px' }}>Details</h2>
        <div className="space-y-2">
          <div className="flex justify-between">
            <span style={{ fontSize: '13px', fontWeight: 500, color: 'var(--text2)' }}>Delivery Address</span>
            <span style={{ fontSize: '13px', fontWeight: 700, color: 'var(--text)' }}>{order.deliveryAddress}</span>
          </div>
          <div className="flex justify-between">
            <span style={{ fontSize: '13px', fontWeight: 500, color: 'var(--text2)' }}>Payment Method</span>
            <span style={{ fontSize: '13px', fontWeight: 700, color: 'var(--text)' }}>{order.paymentMethod.replace('_', ' ')}</span>
          </div>
          <div className="flex justify-between">
            <span style={{ fontSize: '13px', fontWeight: 500, color: 'var(--text2)' }}>Payment Status</span>
            <span style={{
              fontSize: '13px',
              fontWeight: 700,
              color: order.paymentStatus === 'SIMULATED_PAID' ? '#22c55e' : 'var(--y)',
            }}>
              {order.paymentStatus.replace('_', ' ')}
            </span>
          </div>
        </div>
      </div>

      <div className="flex gap-3">
        {order.status === 'PENDING' && (
          <button
            onClick={handleCancel}
            style={{
              padding: '12px 24px',
              borderRadius: '30px',
              background: 'rgba(239,68,68,0.1)',
              color: '#ef4444',
              fontWeight: 700,
              fontSize: '14px',
              border: '1.5px solid rgba(239,68,68,0.3)',
              cursor: 'pointer',
            }}
          >
            Cancel Order
          </button>
        )}
        {order.paymentStatus === 'PENDING' && order.status !== 'CANCELLED' && (
          <button
            onClick={() => router.push(`/checkout/payment?orderId=${order.id}`)}
            className="gradient-bg"
            style={{
              padding: '12px 24px',
              borderRadius: '30px',
              color: '#000',
              fontWeight: 700,
              fontSize: '14px',
              border: 'none',
              cursor: 'pointer',
            }}
          >
            Pay Now
          </button>
        )}
      </div>
    </div>
  );
}
