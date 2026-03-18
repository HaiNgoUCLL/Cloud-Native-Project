'use client';

import { useEffect, useState } from 'react';
import { Users, Package, Shield } from 'lucide-react';
import toast from 'react-hot-toast';
import { ProtectedRoute } from '@/components/ProtectedRoute';
import { LoadingSpinner } from '@/components/LoadingSpinner';
import api from '@/lib/axios';
import { User, Order, UserRole } from '@/types';

const ROLES: UserRole[] = ['CUSTOMER', 'RESTAURANT_OWNER', 'DELIVERY_DRIVER', 'ADMIN'];

export default function AdminPage() {
  return (
    <ProtectedRoute role="ADMIN">
      <AdminContent />
    </ProtectedRoute>
  );
}

function AdminContent() {
  const [users, setUsers] = useState<User[]>([]);
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState<'users' | 'orders'>('users');

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      const [resU, resO] = await Promise.all([
        api.get('/api/admin/users'),
        api.get('/api/admin/orders'),
      ]);
      setUsers(resU.data.data);
      setOrders(resO.data.data);
    } catch {
      toast.error('Failed to load admin data');
    } finally {
      setLoading(false);
    }
  };

  const handleRoleChange = async (userId: string, newRole: UserRole) => {
    try {
      await api.put(`/api/admin/users/${userId}/role`, { role: newRole });
      toast.success('User role updated');
      const res = await api.get('/api/admin/users');
      setUsers(res.data.data);
    } catch {
      toast.error('Failed to update role');
    }
  };

  if (loading) return <div className="py-8"><LoadingSpinner /></div>;

  return (
    <div className="max-w-7xl mx-auto px-4 py-8" style={{ background: 'var(--bg)', minHeight: '100vh' }}>
      <h1 style={{ fontSize: '22px', fontWeight: 700, color: 'var(--text)', marginBottom: '24px' }}>Admin Dashboard</h1>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8">
        <div className="relative overflow-hidden p-6 rounded-2xl" style={{ background: 'var(--bg2)', border: '1.5px solid var(--border2)' }}>
          <div className="absolute -top-8 -right-8 w-24 h-24 rounded-full" style={{ background: 'radial-gradient(circle, var(--glow), transparent)' }} />
          <Users size={20} style={{ color: 'var(--y)', marginBottom: '8px' }} />
          <p style={{ fontSize: '13px', fontWeight: 500, color: 'var(--text3)' }}>Total Users</p>
          <p style={{ fontSize: '26px', fontWeight: 700, color: 'var(--text)' }}>{users.length}</p>
        </div>
        <div className="relative overflow-hidden p-6 rounded-2xl" style={{ background: 'var(--bg2)', border: '1.5px solid var(--border2)' }}>
          <div className="absolute -top-8 -right-8 w-24 h-24 rounded-full" style={{ background: 'radial-gradient(circle, var(--glow), transparent)' }} />
          <Package size={20} style={{ color: 'var(--y)', marginBottom: '8px' }} />
          <p style={{ fontSize: '13px', fontWeight: 500, color: 'var(--text3)' }}>Total Orders</p>
          <p style={{ fontSize: '26px', fontWeight: 700, color: 'var(--text)' }}>{orders.length}</p>
        </div>
        <div className="relative overflow-hidden p-6 rounded-2xl" style={{ background: 'var(--bg2)', border: '1.5px solid var(--border2)' }}>
          <div className="absolute -top-8 -right-8 w-24 h-24 rounded-full" style={{ background: 'radial-gradient(circle, var(--glow), transparent)' }} />
          <Shield size={20} style={{ color: 'var(--y)', marginBottom: '8px' }} />
          <p style={{ fontSize: '13px', fontWeight: 500, color: 'var(--text3)' }}>Admins</p>
          <p style={{ fontSize: '26px', fontWeight: 700, color: 'var(--text)' }}>{users.filter((u) => u.role === 'ADMIN').length}</p>
        </div>
      </div>

      <div className="flex gap-4 mb-6" style={{ borderBottom: '1.5px solid var(--border2)' }}>
        {(['users', 'orders'] as const).map((tab) => (
          <button
            key={tab}
            onClick={() => setActiveTab(tab)}
            style={{
              padding: '8px 0',
              fontSize: '13px',
              fontWeight: 600,
              textTransform: 'uppercase',
              color: activeTab === tab ? 'var(--y)' : 'var(--text3)',
              borderBottom: `2.5px solid ${activeTab === tab ? 'var(--y)' : 'transparent'}`,
              background: 'transparent',
              border: 'none',
              borderBottomWidth: '2.5px',
              borderBottomStyle: 'solid',
              borderBottomColor: activeTab === tab ? 'var(--y)' : 'transparent',
              cursor: 'pointer',
            }}
          >
            {tab}
          </button>
        ))}
      </div>

      {activeTab === 'users' && (
        <div className="overflow-x-auto">
          <table className="w-full" style={{ borderCollapse: 'separate', borderSpacing: '0 4px' }}>
            <thead>
              <tr>
                {['Name', 'Email', 'Role', 'Created', 'Actions'].map((h) => (
                  <th key={h} style={{ fontSize: '11px', fontWeight: 700, textTransform: 'uppercase', color: 'var(--text3)', padding: '8px 12px', textAlign: 'left' }}>
                    {h}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {users.map((u) => (
                <tr key={u.id} style={{ background: 'var(--bg2)' }}>
                  <td style={{ padding: '12px', fontSize: '13px', fontWeight: 500, color: 'var(--text)', borderRadius: '12px 0 0 12px' }}>{u.name}</td>
                  <td style={{ padding: '12px', fontSize: '13px', fontWeight: 500, color: 'var(--text2)' }}>{u.email}</td>
                  <td style={{ padding: '12px', fontSize: '12px', fontWeight: 700, color: 'var(--y)' }}>{u.role.replace('_', ' ')}</td>
                  <td style={{ padding: '12px', fontSize: '13px', fontWeight: 500, color: 'var(--text3)' }}>{new Date(u.createdAt).toLocaleDateString()}</td>
                  <td style={{ padding: '12px', borderRadius: '0 12px 12px 0' }}>
                    <select
                      value={u.role}
                      onChange={(e) => handleRoleChange(u.id, e.target.value as UserRole)}
                      style={{
                        padding: '6px 12px',
                        borderRadius: '8px',
                        background: 'var(--bg3)',
                        color: 'var(--text)',
                        border: '1.5px solid var(--border2)',
                        fontSize: '12px',
                        fontWeight: 600,
                        cursor: 'pointer',
                      }}
                    >
                      {ROLES.map((r) => (
                        <option key={r} value={r}>{r.replace('_', ' ')}</option>
                      ))}
                    </select>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {activeTab === 'orders' && (
        <div className="overflow-x-auto">
          <table className="w-full" style={{ borderCollapse: 'separate', borderSpacing: '0 4px' }}>
            <thead>
              <tr>
                {['Order ID', 'Status', 'Payment', 'Total', 'Date'].map((h) => (
                  <th key={h} style={{ fontSize: '11px', fontWeight: 700, textTransform: 'uppercase', color: 'var(--text3)', padding: '8px 12px', textAlign: 'left' }}>
                    {h}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {orders.map((o) => (
                <tr key={o.id} style={{ background: 'var(--bg2)' }}>
                  <td style={{ padding: '12px', fontSize: '13px', fontWeight: 700, color: 'var(--text)', borderRadius: '12px 0 0 12px' }}>#{o.id.slice(-6)}</td>
                  <td style={{ padding: '12px', fontSize: '12px', fontWeight: 700, color: 'var(--y)' }}>{o.status.replace('_', ' ')}</td>
                  <td style={{ padding: '12px', fontSize: '12px', fontWeight: 700, color: o.paymentStatus === 'SIMULATED_PAID' ? '#22c55e' : 'var(--text3)' }}>
                    {o.paymentStatus.replace('_', ' ')}
                  </td>
                  <td style={{ padding: '12px', fontSize: '13px', fontWeight: 700, color: 'var(--text)' }}>${o.totalAmount.toFixed(2)}</td>
                  <td style={{ padding: '12px', fontSize: '13px', fontWeight: 500, color: 'var(--text3)', borderRadius: '0 12px 12px 0' }}>
                    {new Date(o.createdAt).toLocaleDateString()}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
