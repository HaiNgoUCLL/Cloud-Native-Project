'use client';

import { useEffect, useState } from 'react';
import { Plus, Edit, Trash2, Package, DollarSign, ShoppingBag } from 'lucide-react';
import toast from 'react-hot-toast';
import { ProtectedRoute } from '@/components/ProtectedRoute';
import { LoadingSpinner } from '@/components/LoadingSpinner';
import api from '@/lib/axios';
import { Restaurant, MenuItem, Order } from '@/types';
import { useAuth } from '@/context/AuthContext';

export default function OwnerDashboard() {
  return (
    <ProtectedRoute role="RESTAURANT_OWNER">
      <OwnerContent />
    </ProtectedRoute>
  );
}

function OwnerContent() {
  const { user } = useAuth();
  const [restaurants, setRestaurants] = useState<Restaurant[]>([]);
  const [selectedRestaurant, setSelectedRestaurant] = useState<Restaurant | null>(null);
  const [menuItems, setMenuItems] = useState<MenuItem[]>([]);
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [showMenuForm, setShowMenuForm] = useState(false);
  const [showRestaurantForm, setShowRestaurantForm] = useState(false);
  const [editingItem, setEditingItem] = useState<MenuItem | null>(null);
  const [menuForm, setMenuForm] = useState({ name: '', description: '', price: '', category: '', isAvailable: true });
  const [restForm, setRestForm] = useState({ name: '', description: '', cuisineType: '', address: '', imageUrl: '', isOpen: true });

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      const [resR, resO] = await Promise.all([
        api.get('/api/restaurants', { params: {} }),
        api.get('/api/orders'),
      ]);
      const myRestaurants = resR.data.data.filter((r: Restaurant) => r.ownerId === user?.id);
      setRestaurants(myRestaurants);
      setOrders(resO.data.data);
      if (myRestaurants.length > 0 && !selectedRestaurant) {
        setSelectedRestaurant(myRestaurants[0]);
        const resM = await api.get(`/api/restaurants/${myRestaurants[0].id}/menu`);
        setMenuItems(resM.data.data);
      }
    } catch {
      toast.error('Failed to load data');
    } finally {
      setLoading(false);
    }
  };

  const selectRestaurant = async (r: Restaurant) => {
    setSelectedRestaurant(r);
    const res = await api.get(`/api/restaurants/${r.id}/menu`);
    setMenuItems(res.data.data);
  };

  const handleCreateRestaurant = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await api.post('/api/restaurants', { ...restForm, open: restForm.isOpen });
      toast.success('Restaurant created!');
      setShowRestaurantForm(false);
      setRestForm({ name: '', description: '', cuisineType: '', address: '', imageUrl: '', isOpen: true });
      fetchData();
    } catch {
      toast.error('Failed to create restaurant');
    }
  };

  const handleSaveMenuItem = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedRestaurant) return;
    try {
      const data = { ...menuForm, price: parseFloat(menuForm.price), available: menuForm.isAvailable };
      if (editingItem) {
        await api.put(`/api/restaurants/${selectedRestaurant.id}/menu/${editingItem.id}`, data);
        toast.success('Menu item updated!');
      } else {
        await api.post(`/api/restaurants/${selectedRestaurant.id}/menu`, data);
        toast.success('Menu item added!');
      }
      setShowMenuForm(false);
      setEditingItem(null);
      setMenuForm({ name: '', description: '', price: '', category: '', isAvailable: true });
      const res = await api.get(`/api/restaurants/${selectedRestaurant.id}/menu`);
      setMenuItems(res.data.data);
    } catch {
      toast.error('Failed to save menu item');
    }
  };

  const handleDeleteMenuItem = async (itemId: string) => {
    if (!selectedRestaurant) return;
    try {
      await api.delete(`/api/restaurants/${selectedRestaurant.id}/menu/${itemId}`);
      toast.success('Menu item deleted');
      setMenuItems(menuItems.filter((m) => m.id !== itemId));
    } catch {
      toast.error('Failed to delete item');
    }
  };

  const handleUpdateOrderStatus = async (orderId: string, status: string) => {
    try {
      await api.put(`/api/orders/${orderId}/status`, { status });
      toast.success('Order status updated');
      const res = await api.get('/api/orders');
      setOrders(res.data.data);
    } catch (err: unknown) {
      const error = err as { response?: { data?: { message?: string } } };
      toast.error(error.response?.data?.message || 'Failed to update status');
    }
  };

  const totalRevenue = orders.filter((o) => o.paymentStatus === 'SIMULATED_PAID').reduce((sum, o) => sum + o.totalAmount, 0);

  if (loading) return <div className="py-8"><LoadingSpinner /></div>;

  const inputStyle = {
    width: '100%',
    padding: '10px 14px',
    borderRadius: '10px',
    border: '1.5px solid var(--border2)',
    background: 'var(--bg3)',
    color: 'var(--text)',
    fontSize: '13px',
    fontWeight: 500 as const,
    outline: 'none',
  };

  return (
    <div className="max-w-7xl mx-auto px-4 py-8" style={{ background: 'var(--bg)', minHeight: '100vh' }}>
      <div className="flex items-center justify-between mb-8">
        <h1 style={{ fontSize: '22px', fontWeight: 700, color: 'var(--text)' }}>Restaurant Dashboard</h1>
        <button
          onClick={() => setShowRestaurantForm(true)}
          className="gradient-bg flex items-center gap-2"
          style={{ padding: '10px 20px', borderRadius: '10px', color: '#000', fontWeight: 700, fontSize: '13px', border: 'none', cursor: 'pointer' }}
        >
          <Plus size={16} /> New Restaurant
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8">
        <div className="relative overflow-hidden p-6 rounded-2xl" style={{ background: 'var(--bg2)', border: '1.5px solid var(--border2)' }}>
          <div className="absolute -top-8 -right-8 w-24 h-24 rounded-full" style={{ background: 'radial-gradient(circle, var(--glow), transparent)' }} />
          <Package size={20} style={{ color: 'var(--y)', marginBottom: '8px' }} />
          <p style={{ fontSize: '13px', fontWeight: 500, color: 'var(--text3)' }}>Total Orders</p>
          <p style={{ fontSize: '26px', fontWeight: 700, color: 'var(--text)' }}>{orders.length}</p>
        </div>
        <div className="relative overflow-hidden p-6 rounded-2xl" style={{ background: 'var(--bg2)', border: '1.5px solid var(--border2)' }}>
          <div className="absolute -top-8 -right-8 w-24 h-24 rounded-full" style={{ background: 'radial-gradient(circle, var(--glow), transparent)' }} />
          <DollarSign size={20} style={{ color: 'var(--y)', marginBottom: '8px' }} />
          <p style={{ fontSize: '13px', fontWeight: 500, color: 'var(--text3)' }}>Revenue</p>
          <p className="gradient-text" style={{ fontSize: '26px', fontWeight: 700 }}>${totalRevenue.toFixed(2)}</p>
        </div>
        <div className="relative overflow-hidden p-6 rounded-2xl" style={{ background: 'var(--bg2)', border: '1.5px solid var(--border2)' }}>
          <div className="absolute -top-8 -right-8 w-24 h-24 rounded-full" style={{ background: 'radial-gradient(circle, var(--glow), transparent)' }} />
          <ShoppingBag size={20} style={{ color: 'var(--y)', marginBottom: '8px' }} />
          <p style={{ fontSize: '13px', fontWeight: 500, color: 'var(--text3)' }}>Menu Items</p>
          <p style={{ fontSize: '26px', fontWeight: 700, color: 'var(--text)' }}>{menuItems.length}</p>
        </div>
      </div>

      {restaurants.length > 1 && (
        <div className="flex gap-2 mb-6">
          {restaurants.map((r) => (
            <button
              key={r.id}
              onClick={() => selectRestaurant(r)}
              style={{
                padding: '8px 16px',
                borderRadius: '30px',
                fontSize: '13px',
                fontWeight: selectedRestaurant?.id === r.id ? 700 : 500,
                color: selectedRestaurant?.id === r.id ? '#000' : 'var(--text2)',
                background: selectedRestaurant?.id === r.id ? 'var(--y)' : 'var(--bg3)',
                border: 'none',
                cursor: 'pointer',
              }}
            >
              {r.name}
            </button>
          ))}
        </div>
      )}

      <div className="mb-8">
        <div className="flex items-center justify-between mb-4">
          <h2 style={{ fontSize: '16px', fontWeight: 700, color: 'var(--text)' }}>Menu Items</h2>
          <button
            onClick={() => { setShowMenuForm(true); setEditingItem(null); setMenuForm({ name: '', description: '', price: '', category: '', isAvailable: true }); }}
            style={{ padding: '8px 16px', borderRadius: '10px', background: 'var(--y)', color: '#000', fontWeight: 700, fontSize: '13px', border: 'none', cursor: 'pointer' }}
          >
            <Plus size={14} style={{ display: 'inline', marginRight: '4px' }} /> Add Item
          </button>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full" style={{ borderCollapse: 'separate', borderSpacing: '0 4px' }}>
            <thead>
              <tr>
                {['Name', 'Category', 'Price', 'Status', 'Actions'].map((h) => (
                  <th key={h} style={{ fontSize: '11px', fontWeight: 700, textTransform: 'uppercase', color: 'var(--text3)', padding: '8px 12px', textAlign: 'left' }}>
                    {h}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {menuItems.map((item) => (
                <tr key={item.id} style={{ background: 'var(--bg2)' }}>
                  <td style={{ padding: '12px', fontSize: '13px', fontWeight: 500, color: 'var(--text)', borderRadius: '12px 0 0 12px' }}>{item.name}</td>
                  <td style={{ padding: '12px', fontSize: '13px', fontWeight: 500, color: 'var(--text2)' }}>{item.category}</td>
                  <td style={{ padding: '12px', fontSize: '13px', fontWeight: 700, color: 'var(--y)' }}>${item.price.toFixed(2)}</td>
                  <td style={{ padding: '12px', fontSize: '13px', fontWeight: 700, color: (item.isAvailable || item.available) ? '#22c55e' : 'var(--text3)' }}>
                    {(item.isAvailable || item.available) ? 'Available' : 'Unavailable'}
                  </td>
                  <td style={{ padding: '12px', borderRadius: '0 12px 12px 0' }}>
                    <div className="flex gap-2">
                      <button
                        onClick={() => {
                          setEditingItem(item);
                          setMenuForm({ name: item.name, description: item.description, price: item.price.toString(), category: item.category, isAvailable: item.isAvailable || item.available });
                          setShowMenuForm(true);
                        }}
                        style={{ padding: '6px 12px', borderRadius: '8px', background: 'var(--bg3)', color: 'var(--text)', fontSize: '12px', fontWeight: 600, border: '1.5px solid var(--border2)', cursor: 'pointer' }}
                      >
                        <Edit size={12} />
                      </button>
                      <button
                        onClick={() => handleDeleteMenuItem(item.id)}
                        style={{ padding: '6px 12px', borderRadius: '8px', background: 'rgba(239,68,68,0.1)', color: '#ef4444', fontSize: '12px', fontWeight: 600, border: '1.5px solid rgba(239,68,68,0.3)', cursor: 'pointer' }}
                      >
                        <Trash2 size={12} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      <div>
        <h2 style={{ fontSize: '16px', fontWeight: 700, color: 'var(--text)', marginBottom: '12px' }}>Recent Orders</h2>
        <div className="space-y-3">
          {orders.slice(0, 10).map((order) => (
            <div key={order.id} className="flex items-center justify-between p-4 rounded-2xl" style={{ background: 'var(--bg2)', border: '1.5px solid var(--border2)' }}>
              <div>
                <p style={{ fontSize: '14px', fontWeight: 700, color: 'var(--text)' }}>Order #{order.id.slice(-6)}</p>
                <p style={{ fontSize: '12px', fontWeight: 500, color: 'var(--text3)' }}>{order.items.length} items • ${order.totalAmount.toFixed(2)}</p>
              </div>
              <div className="flex items-center gap-2">
                <span style={{ fontSize: '12px', fontWeight: 700, color: 'var(--y)' }}>{order.status.replace('_', ' ')}</span>
                {order.status === 'PENDING' && (
                  <button
                    onClick={() => handleUpdateOrderStatus(order.id, 'CONFIRMED')}
                    style={{ padding: '6px 14px', borderRadius: '10px', background: 'var(--y)', color: '#000', fontWeight: 700, fontSize: '12px', border: 'none', cursor: 'pointer' }}
                  >
                    Confirm
                  </button>
                )}
                {order.status === 'CONFIRMED' && (
                  <button
                    onClick={() => handleUpdateOrderStatus(order.id, 'PREPARING')}
                    style={{ padding: '6px 14px', borderRadius: '10px', background: 'var(--y)', color: '#000', fontWeight: 700, fontSize: '12px', border: 'none', cursor: 'pointer' }}
                  >
                    Preparing
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      </div>

      {showMenuForm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center" style={{ background: 'rgba(0,0,0,0.7)' }}>
          <div className="p-6 rounded-2xl max-w-md w-full mx-4" style={{ background: 'var(--bg2)', border: '1.5px solid var(--border2)' }}>
            <h3 style={{ fontSize: '16px', fontWeight: 700, color: 'var(--text)', marginBottom: '16px' }}>
              {editingItem ? 'Edit Menu Item' : 'Add Menu Item'}
            </h3>
            <form onSubmit={handleSaveMenuItem} className="space-y-3">
              <input value={menuForm.name} onChange={(e) => setMenuForm({ ...menuForm, name: e.target.value })} placeholder="Name" required style={inputStyle} />
              <input value={menuForm.description} onChange={(e) => setMenuForm({ ...menuForm, description: e.target.value })} placeholder="Description" required style={inputStyle} />
              <input type="number" step="0.01" value={menuForm.price} onChange={(e) => setMenuForm({ ...menuForm, price: e.target.value })} placeholder="Price" required style={inputStyle} />
              <input value={menuForm.category} onChange={(e) => setMenuForm({ ...menuForm, category: e.target.value })} placeholder="Category" required style={inputStyle} />
              <div className="flex gap-3 pt-2">
                <button type="button" onClick={() => { setShowMenuForm(false); setEditingItem(null); }} style={{ flex: 1, padding: '10px', borderRadius: '10px', background: 'var(--bg3)', color: 'var(--text)', fontWeight: 700, fontSize: '13px', border: '1.5px solid var(--border2)', cursor: 'pointer' }}>
                  Cancel
                </button>
                <button type="submit" className="gradient-bg" style={{ flex: 1, padding: '10px', borderRadius: '10px', color: '#000', fontWeight: 700, fontSize: '13px', border: 'none', cursor: 'pointer' }}>
                  {editingItem ? 'Update' : 'Add'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {showRestaurantForm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center" style={{ background: 'rgba(0,0,0,0.7)' }}>
          <div className="p-6 rounded-2xl max-w-md w-full mx-4" style={{ background: 'var(--bg2)', border: '1.5px solid var(--border2)' }}>
            <h3 style={{ fontSize: '16px', fontWeight: 700, color: 'var(--text)', marginBottom: '16px' }}>Create Restaurant</h3>
            <form onSubmit={handleCreateRestaurant} className="space-y-3">
              <input value={restForm.name} onChange={(e) => setRestForm({ ...restForm, name: e.target.value })} placeholder="Name" required style={inputStyle} />
              <input value={restForm.description} onChange={(e) => setRestForm({ ...restForm, description: e.target.value })} placeholder="Description" required style={inputStyle} />
              <input value={restForm.cuisineType} onChange={(e) => setRestForm({ ...restForm, cuisineType: e.target.value })} placeholder="Cuisine Type" required style={inputStyle} />
              <input value={restForm.address} onChange={(e) => setRestForm({ ...restForm, address: e.target.value })} placeholder="Address" required style={inputStyle} />
              <input value={restForm.imageUrl} onChange={(e) => setRestForm({ ...restForm, imageUrl: e.target.value })} placeholder="Image URL" style={inputStyle} />
              <div className="flex gap-3 pt-2">
                <button type="button" onClick={() => setShowRestaurantForm(false)} style={{ flex: 1, padding: '10px', borderRadius: '10px', background: 'var(--bg3)', color: 'var(--text)', fontWeight: 700, fontSize: '13px', border: '1.5px solid var(--border2)', cursor: 'pointer' }}>
                  Cancel
                </button>
                <button type="submit" className="gradient-bg" style={{ flex: 1, padding: '10px', borderRadius: '10px', color: '#000', fontWeight: 700, fontSize: '13px', border: 'none', cursor: 'pointer' }}>
                  Create
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
