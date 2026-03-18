'use client';

import { useEffect, useState } from 'react';
import { Truck, Package, MapPin, CheckCircle } from 'lucide-react';
import toast from 'react-hot-toast';
import { ProtectedRoute } from '@/components/ProtectedRoute';
import { LoadingSpinner } from '@/components/LoadingSpinner';
import api from '@/lib/axios';
import { Order, Restaurant } from '@/types';

export default function DriverDashboard() {
  return (
    <ProtectedRoute role="DELIVERY_DRIVER">
      <DriverContent />
    </ProtectedRoute>
  );
}

function DriverContent() {
  const [orders, setOrders] = useState<Order[]>([]);
  const [restaurants, setRestaurants] = useState<Record<string, Restaurant>>({});
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchData();
    const interval = setInterval(fetchData, 10000);
    return () => clearInterval(interval);
  }, []);

  const fetchData = async () => {
    try {
      const [ordersRes, restRes] = await Promise.all([
        api.get('/api/orders'),
        api.get('/api/restaurants'),
      ]);
      setOrders(ordersRes.data.data);
      const map: Record<string, Restaurant> = {};
      for (const r of restRes.data.data) {
        map[r.id] = r;
      }
      setRestaurants(map);
    } catch {
      toast.error('Failed to load orders');
    } finally {
      setLoading(false);
    }
  };

  const handlePickup = async (orderId: string) => {
    try {
      await api.put(`/api/orders/${orderId}/status`, { status: 'PICKED_UP' });
      toast.success('Order picked up!');
      fetchData();
    } catch (err: unknown) {
      const error = err as { response?: { data?: { message?: string } } };
      toast.error(error.response?.data?.message || 'Failed to update');
    }
  };

  const handleArrived = async (orderId: string) => {
    try {
      await api.put(`/api/orders/${orderId}/status`, { status: 'ARRIVED' });
      toast.success('Marked as arrived!');
      fetchData();
    } catch (err: unknown) {
      const error = err as { response?: { data?: { message?: string } } };
      toast.error(error.response?.data?.message || 'Failed to update');
    }
  };

  const handleDeliver = async (orderId: string) => {
    try {
      await api.put(`/api/orders/${orderId}/status`, { status: 'DELIVERED' });
      toast.success('Order delivered!');
      fetchData();
    } catch (err: unknown) {
      const error = err as { response?: { data?: { message?: string } } };
      toast.error(error.response?.data?.message || 'Failed to update');
    }
  };

  if (loading) return <div className="py-8"><LoadingSpinner /></div>;

  const readyOrders = orders.filter((o) => o.status === 'READY');
  const pickedUpOrders = orders.filter((o) => o.status === 'PICKED_UP');
  const arrivedOrders = orders.filter((o) => o.status === 'ARRIVED');
  const completedOrders = orders.filter((o) => o.status === 'DELIVERED');

  return (
    <div className="max-w-5xl mx-auto px-4 py-8" style={{ background: 'var(--bg)', minHeight: '100vh' }}>
      <h1 style={{ fontSize: '22px', fontWeight: 700, color: 'var(--text)', marginBottom: '24px' }}>Driver Dashboard</h1>

      <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-8">
        <div className="relative overflow-hidden p-6 rounded-2xl" style={{ background: 'var(--bg2)', border: '1.5px solid var(--border2)' }}>
          <div className="absolute -top-8 -right-8 w-24 h-24 rounded-full" style={{ background: 'radial-gradient(circle, var(--glow), transparent)' }} />
          <Package size={20} style={{ color: '#22c55e', marginBottom: '8px' }} />
          <p style={{ fontSize: '13px', fontWeight: 500, color: 'var(--text3)' }}>Ready Orders</p>
          <p style={{ fontSize: '26px', fontWeight: 700, color: 'var(--text)' }}>{readyOrders.length}</p>
        </div>
        <div className="relative overflow-hidden p-6 rounded-2xl" style={{ background: 'var(--bg2)', border: '1.5px solid var(--border2)' }}>
          <div className="absolute -top-8 -right-8 w-24 h-24 rounded-full" style={{ background: 'radial-gradient(circle, var(--glow), transparent)' }} />
          <Truck size={20} style={{ color: 'var(--y)', marginBottom: '8px' }} />
          <p style={{ fontSize: '13px', fontWeight: 500, color: 'var(--text3)' }}>Active Deliveries</p>
          <p style={{ fontSize: '26px', fontWeight: 700, color: 'var(--text)' }}>{pickedUpOrders.length + arrivedOrders.length}</p>
        </div>
        <div className="relative overflow-hidden p-6 rounded-2xl" style={{ background: 'var(--bg2)', border: '1.5px solid var(--border2)' }}>
          <div className="absolute -top-8 -right-8 w-24 h-24 rounded-full" style={{ background: 'radial-gradient(circle, var(--glow), transparent)' }} />
          <MapPin size={20} style={{ color: '#f59e0b', marginBottom: '8px' }} />
          <p style={{ fontSize: '13px', fontWeight: 500, color: 'var(--text3)' }}>Arrived</p>
          <p style={{ fontSize: '26px', fontWeight: 700, color: 'var(--text)' }}>{arrivedOrders.length}</p>
        </div>
        <div className="relative overflow-hidden p-6 rounded-2xl" style={{ background: 'var(--bg2)', border: '1.5px solid var(--border2)' }}>
          <div className="absolute -top-8 -right-8 w-24 h-24 rounded-full" style={{ background: 'radial-gradient(circle, var(--glow), transparent)' }} />
          <CheckCircle size={20} style={{ color: 'var(--y)', marginBottom: '8px' }} />
          <p style={{ fontSize: '13px', fontWeight: 500, color: 'var(--text3)' }}>Completed</p>
          <p style={{ fontSize: '26px', fontWeight: 700, color: 'var(--text)' }}>{completedOrders.length}</p>
        </div>
      </div>

      {/* Ready for Pickup */}
      <div className="mb-8">
        <h2 style={{ fontSize: '16px', fontWeight: 700, color: 'var(--text)', marginBottom: '12px' }}>Ready for Pickup</h2>
        {readyOrders.length === 0 ? (
          <div className="text-center py-12">
            <div style={{ fontSize: '48px', marginBottom: '12px' }}>📭</div>
            <p style={{ fontSize: '16px', fontWeight: 700, color: 'var(--text)' }}>No orders ready</p>
            <p style={{ fontSize: '13px', fontWeight: 500, color: 'var(--text3)' }}>Check back soon for new delivery orders</p>
          </div>
        ) : (
          <div className="space-y-3">
            {readyOrders.map((order) => {
              const restaurant = restaurants[order.restaurantId];
              return (
                <div key={order.id} className="p-4 rounded-2xl" style={{ background: 'var(--bg2)', border: '1.5px solid #22c55e' }}>
                  <div className="flex items-center justify-between">
                    <div>
                      <p style={{ fontSize: '14px', fontWeight: 700, color: 'var(--text)' }}>Order #{order.id.slice(-6)}</p>
                      <p style={{ fontSize: '13px', fontWeight: 700, color: 'var(--y)', marginTop: '4px' }}>${order.totalAmount.toFixed(2)}</p>
                    </div>
                    <button
                      onClick={() => handlePickup(order.id)}
                      className="gradient-bg"
                      style={{ padding: '10px 20px', borderRadius: '10px', color: '#000', fontWeight: 700, fontSize: '13px', border: 'none', cursor: 'pointer' }}
                    >
                      Pick Up
                    </button>
                  </div>
                  <div className="mt-3 grid grid-cols-1 md:grid-cols-2 gap-2">
                    <div className="p-3 rounded-xl" style={{ background: 'var(--bg3)' }}>
                      <p style={{ fontSize: '11px', fontWeight: 700, textTransform: 'uppercase', color: 'var(--text3)', marginBottom: '4px' }}>Restaurant</p>
                      <p style={{ fontSize: '13px', fontWeight: 700, color: 'var(--text)' }}>{restaurant?.name || 'Unknown'}</p>
                      <p style={{ fontSize: '12px', fontWeight: 500, color: 'var(--text2)' }}>{restaurant?.address || 'N/A'}</p>
                    </div>
                    <div className="p-3 rounded-xl" style={{ background: 'var(--bg3)' }}>
                      <p style={{ fontSize: '11px', fontWeight: 700, textTransform: 'uppercase', color: 'var(--text3)', marginBottom: '4px' }}>Deliver To</p>
                      <p style={{ fontSize: '13px', fontWeight: 500, color: 'var(--text)' }}>{order.deliveryAddress}</p>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* Active Deliveries (Picked Up) */}
      {pickedUpOrders.length > 0 && (
        <div className="mb-8">
          <h2 style={{ fontSize: '16px', fontWeight: 700, color: 'var(--text)', marginBottom: '12px' }}>Active Deliveries</h2>
          <div className="space-y-3">
            {pickedUpOrders.map((order) => (
              <div key={order.id} className="flex items-center justify-between p-4 rounded-2xl" style={{ background: 'var(--bg2)', border: '1.5px solid var(--y)' }}>
                <div>
                  <p style={{ fontSize: '14px', fontWeight: 700, color: 'var(--text)' }}>Order #{order.id.slice(-6)}</p>
                  <p style={{ fontSize: '12px', fontWeight: 500, color: 'var(--text3)' }}>{order.deliveryAddress}</p>
                  <p style={{ fontSize: '13px', fontWeight: 700, color: 'var(--y)', marginTop: '4px' }}>${order.totalAmount.toFixed(2)}</p>
                </div>
                <button
                  onClick={() => handleArrived(order.id)}
                  style={{ padding: '10px 20px', borderRadius: '10px', background: '#f59e0b', color: '#000', fontWeight: 700, fontSize: '13px', border: 'none', cursor: 'pointer' }}
                >
                  Arrived
                </button>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Arrived — ready to deliver */}
      {arrivedOrders.length > 0 && (
        <div className="mb-8">
          <h2 style={{ fontSize: '16px', fontWeight: 700, color: 'var(--text)', marginBottom: '12px' }}>At Customer Location</h2>
          <div className="space-y-3">
            {arrivedOrders.map((order) => (
              <div key={order.id} className="flex items-center justify-between p-4 rounded-2xl" style={{ background: 'var(--bg2)', border: '1.5px solid #22c55e' }}>
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
    </div>
  );
}
