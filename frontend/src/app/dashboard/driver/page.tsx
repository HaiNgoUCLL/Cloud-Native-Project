'use client';

import { useEffect, useState } from 'react';
import { Truck, Package, MapPin } from 'lucide-react';
import toast from 'react-hot-toast';
import { ProtectedRoute } from '@/components/ProtectedRoute';
import { LoadingSpinner } from '@/components/LoadingSpinner';
import api from '@/lib/axios';
import { Order } from '@/types';

export default function DriverDashboard() {
  return (
    <ProtectedRoute role="DELIVERY_DRIVER">
      <DriverContent />
    </ProtectedRoute>
  );
}

function DriverContent() {
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchOrders();
    const interval = setInterval(fetchOrders, 10000);
    return () => clearInterval(interval);
  }, []);

  const fetchOrders = async () => {
    try {
      const res = await api.get('/api/orders');
      setOrders(res.data.data);
    } catch {
      toast.error('Failed to load orders');
    } finally {
      setLoading(false);
    }
  };

  const handlePickup = async (orderId: string) => {
    try {
      await api.put(`/api/orders/${orderId}/status`, { status: 'OUT_FOR_DELIVERY' });
      toast.success('Order picked up!');
      fetchOrders();
    } catch (err: unknown) {
      const error = err as { response?: { data?: { message?: string } } };
      toast.error(error.response?.data?.message || 'Failed to update');
    }
  };

  const handleDeliver = async (orderId: string) => {
    try {
      await api.put(`/api/orders/${orderId}/status`, { status: 'DELIVERED' });
      toast.success('Order delivered!');
      fetchOrders();
    } catch (err: unknown) {
      const error = err as { response?: { data?: { message?: string } } };
      toast.error(error.response?.data?.message || 'Failed to update');
    }
  };

  if (loading) return <div className="py-8"><LoadingSpinner /></div>;

  const activeOrders = orders.filter((o) => o.status === 'OUT_FOR_DELIVERY');
  const availableOrders = orders.filter((o) => o.status === 'CONFIRMED' || o.status === 'PREPARING');
  const completedOrders = orders.filter((o) => o.status === 'DELIVERED');

  return (
    <div className="max-w-5xl mx-auto px-4 py-8" style={{ background: 'var(--bg)', minHeight: '100vh' }}>
      <h1 style={{ fontSize: '22px', fontWeight: 700, color: 'var(--text)', marginBottom: '24px' }}>Driver Dashboard</h1>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8">
        <div className="relative overflow-hidden p-6 rounded-2xl" style={{ background: 'var(--bg2)', border: '1.5px solid var(--border2)' }}>
          <div className="absolute -top-8 -right-8 w-24 h-24 rounded-full" style={{ background: 'radial-gradient(circle, var(--glow), transparent)' }} />
          <Truck size={20} style={{ color: 'var(--y)', marginBottom: '8px' }} />
          <p style={{ fontSize: '13px', fontWeight: 500, color: 'var(--text3)' }}>Active Deliveries</p>
          <p style={{ fontSize: '26px', fontWeight: 700, color: 'var(--text)' }}>{activeOrders.length}</p>
        </div>
        <div className="relative overflow-hidden p-6 rounded-2xl" style={{ background: 'var(--bg2)', border: '1.5px solid var(--border2)' }}>
          <div className="absolute -top-8 -right-8 w-24 h-24 rounded-full" style={{ background: 'radial-gradient(circle, var(--glow), transparent)' }} />
          <Package size={20} style={{ color: 'var(--y)', marginBottom: '8px' }} />
          <p style={{ fontSize: '13px', fontWeight: 500, color: 'var(--text3)' }}>Available Orders</p>
          <p style={{ fontSize: '26px', fontWeight: 700, color: 'var(--text)' }}>{availableOrders.length}</p>
        </div>
        <div className="relative overflow-hidden p-6 rounded-2xl" style={{ background: 'var(--bg2)', border: '1.5px solid var(--border2)' }}>
          <div className="absolute -top-8 -right-8 w-24 h-24 rounded-full" style={{ background: 'radial-gradient(circle, var(--glow), transparent)' }} />
          <MapPin size={20} style={{ color: 'var(--y)', marginBottom: '8px' }} />
          <p style={{ fontSize: '13px', fontWeight: 500, color: 'var(--text3)' }}>Completed</p>
          <p style={{ fontSize: '26px', fontWeight: 700, color: 'var(--text)' }}>{completedOrders.length}</p>
        </div>
      </div>

      {activeOrders.length > 0 && (
        <div className="mb-8">
          <h2 style={{ fontSize: '16px', fontWeight: 700, color: 'var(--text)', marginBottom: '12px' }}>Active Deliveries</h2>
          <div className="space-y-3">
            {activeOrders.map((order) => (
              <div key={order.id} className="flex items-center justify-between p-4 rounded-2xl" style={{ background: 'var(--bg2)', border: '1.5px solid var(--y)' }}>
                <div>
                  <p style={{ fontSize: '14px', fontWeight: 700, color: 'var(--text)' }}>Order #{order.id.slice(-6)}</p>
                  <p style={{ fontSize: '12px', fontWeight: 500, color: 'var(--text3)' }}>{order.deliveryAddress}</p>
                  <p style={{ fontSize: '13px', fontWeight: 700, color: 'var(--y)', marginTop: '4px' }}>${order.totalAmount.toFixed(2)}</p>
                </div>
                <button
                  onClick={() => handleDeliver(order.id)}
                  className="gradient-bg"
                  style={{ padding: '10px 20px', borderRadius: '10px', color: '#000', fontWeight: 700, fontSize: '13px', border: 'none', cursor: 'pointer' }}
                >
                  Mark Delivered
                </button>
              </div>
            ))}
          </div>
        </div>
      )}

      <div>
        <h2 style={{ fontSize: '16px', fontWeight: 700, color: 'var(--text)', marginBottom: '12px' }}>Available for Pickup</h2>
        {availableOrders.length === 0 ? (
          <div className="text-center py-12">
            <div style={{ fontSize: '48px', marginBottom: '12px' }}>📭</div>
            <p style={{ fontSize: '16px', fontWeight: 700, color: 'var(--text)' }}>No orders available</p>
            <p style={{ fontSize: '13px', fontWeight: 500, color: 'var(--text3)' }}>Check back soon for new delivery orders</p>
          </div>
        ) : (
          <div className="space-y-3">
            {availableOrders.map((order) => (
              <div key={order.id} className="flex items-center justify-between p-4 rounded-2xl" style={{ background: 'var(--bg2)', border: '1.5px solid var(--border2)' }}>
                <div>
                  <p style={{ fontSize: '14px', fontWeight: 700, color: 'var(--text)' }}>Order #{order.id.slice(-6)}</p>
                  <p style={{ fontSize: '12px', fontWeight: 500, color: 'var(--text3)' }}>{order.deliveryAddress}</p>
                  <p style={{ fontSize: '13px', fontWeight: 700, color: 'var(--y)', marginTop: '4px' }}>${order.totalAmount.toFixed(2)}</p>
                </div>
                <button
                  onClick={() => handlePickup(order.id)}
                  style={{ padding: '10px 20px', borderRadius: '10px', background: 'var(--y)', color: '#000', fontWeight: 700, fontSize: '13px', border: 'none', cursor: 'pointer' }}
                >
                  Pick Up
                </button>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
