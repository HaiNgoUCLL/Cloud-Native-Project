'use client';

import { useEffect, useState } from 'react';
import { useSearchParams } from 'next/navigation';
import { Suspense } from 'react';
import Link from 'next/link';
import { ArrowLeft, TrendingUp, DollarSign, ShoppingBag, Star } from 'lucide-react';
import {
  LineChart, Line, BarChart, Bar, PieChart, Pie, Cell,
  XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend,
} from 'recharts';
import toast from 'react-hot-toast';
import { ProtectedRoute } from '@/components/ProtectedRoute';
import { LoadingSpinner } from '@/components/LoadingSpinner';
import api from '@/lib/axios';

interface Analytics {
  totalOrders: number;
  totalRevenue: number;
  averageOrderValue: number;
  ordersByStatus: Record<string, number>;
  revenueByDay: { date: string; revenue: number }[];
  topItems: { name: string; quantity: number; revenue: number }[];
  ordersByHour: Record<string, number>;
  reviewStats: {
    averageRating: number;
    totalReviews: number;
    ratingDistribution: Record<string, number>;
  };
}

const CHART_COLORS = ['#FFD700', '#FFA500', '#FF6347', '#4CAF50', '#2196F3', '#9C27B0'];

export default function AnalyticsPage() {
  return (
    <ProtectedRoute role="RESTAURANT_OWNER">
      <Suspense fallback={<div className="py-8"><LoadingSpinner /></div>}>
        <AnalyticsContent />
      </Suspense>
    </ProtectedRoute>
  );
}

function AnalyticsContent() {
  const searchParams = useSearchParams();
  const restaurantId = searchParams.get('id');
  const [analytics, setAnalytics] = useState<Analytics | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (restaurantId) fetchAnalytics();
  }, [restaurantId]);

  const fetchAnalytics = async () => {
    try {
      const res = await api.get(`/api/analytics/restaurant/${restaurantId}`);
      setAnalytics(res.data.data);
    } catch {
      toast.error('Failed to load analytics');
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div className="py-8"><LoadingSpinner /></div>;
  if (!analytics) return <div className="text-center py-16"><p style={{ fontSize: '16px', fontWeight: 700 }}>No analytics data</p></div>;

  const statusData = Object.entries(analytics.ordersByStatus).map(([name, value]) => ({ name: name.replace('_', ' '), value }));
  const hourData = Object.entries(analytics.ordersByHour).map(([hour, count]) => ({ hour: `${hour}:00`, count }));
  const ratingData = Object.entries(analytics.reviewStats.ratingDistribution).map(([stars, count]) => ({ stars: `${stars} star`, count }));

  const cardStyle = {
    background: 'var(--bg2)',
    border: '1.5px solid var(--border2)',
    borderRadius: '16px',
    padding: '20px',
  };

  return (
    <div className="max-w-7xl mx-auto px-4 py-8" style={{ background: 'var(--bg)', minHeight: '100vh' }}>
      <div className="flex items-center gap-4 mb-8">
        <Link
          href="/dashboard/owner"
          style={{
            width: '36px', height: '36px', borderRadius: '50%',
            background: 'var(--bg2)', border: '1.5px solid var(--border2)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
          }}
        >
          <ArrowLeft size={18} color="var(--text2)" />
        </Link>
        <h1 style={{ fontSize: '22px', fontWeight: 700, color: 'var(--text)' }}>Restaurant Analytics</h1>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-8">
        <div className="relative overflow-hidden" style={cardStyle}>
          <ShoppingBag size={20} style={{ color: 'var(--y)', marginBottom: '8px' }} />
          <p style={{ fontSize: '12px', fontWeight: 500, color: 'var(--text3)' }}>Total Orders</p>
          <p style={{ fontSize: '28px', fontWeight: 700, color: 'var(--text)' }}>{analytics.totalOrders}</p>
        </div>
        <div className="relative overflow-hidden" style={cardStyle}>
          <DollarSign size={20} style={{ color: 'var(--y)', marginBottom: '8px' }} />
          <p style={{ fontSize: '12px', fontWeight: 500, color: 'var(--text3)' }}>Total Revenue</p>
          <p className="gradient-text" style={{ fontSize: '28px', fontWeight: 700 }}>${analytics.totalRevenue.toFixed(2)}</p>
        </div>
        <div className="relative overflow-hidden" style={cardStyle}>
          <TrendingUp size={20} style={{ color: 'var(--y)', marginBottom: '8px' }} />
          <p style={{ fontSize: '12px', fontWeight: 500, color: 'var(--text3)' }}>Avg Order Value</p>
          <p style={{ fontSize: '28px', fontWeight: 700, color: 'var(--text)' }}>${analytics.averageOrderValue.toFixed(2)}</p>
        </div>
        <div className="relative overflow-hidden" style={cardStyle}>
          <Star size={20} style={{ color: 'var(--y)', marginBottom: '8px' }} />
          <p style={{ fontSize: '12px', fontWeight: 500, color: 'var(--text3)' }}>Rating</p>
          <p style={{ fontSize: '28px', fontWeight: 700, color: 'var(--text)' }}>
            {analytics.reviewStats.averageRating || 'N/A'}
            <span style={{ fontSize: '14px', fontWeight: 500, color: 'var(--text3)', marginLeft: '4px' }}>
              ({analytics.reviewStats.totalReviews} reviews)
            </span>
          </p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
        <div style={cardStyle}>
          <h3 style={{ fontSize: '15px', fontWeight: 700, color: 'var(--text)', marginBottom: '16px' }}>Revenue Trend (30 Days)</h3>
          <ResponsiveContainer width="100%" height={250}>
            <LineChart data={analytics.revenueByDay}>
              <CartesianGrid strokeDasharray="3 3" stroke="var(--border2)" />
              <XAxis dataKey="date" tick={{ fontSize: 11, fill: 'var(--text3)' }} interval={4} />
              <YAxis tick={{ fontSize: 11, fill: 'var(--text3)' }} />
              <Tooltip
                contentStyle={{ background: 'var(--bg2)', border: '1px solid var(--border2)', borderRadius: '8px', fontSize: '12px' }}
                labelStyle={{ color: 'var(--text)' }}
              />
              <Line type="monotone" dataKey="revenue" stroke="#FFD700" strokeWidth={2} dot={false} />
            </LineChart>
          </ResponsiveContainer>
        </div>

        <div style={cardStyle}>
          <h3 style={{ fontSize: '15px', fontWeight: 700, color: 'var(--text)', marginBottom: '16px' }}>Top Items</h3>
          <ResponsiveContainer width="100%" height={250}>
            <BarChart data={analytics.topItems} layout="vertical">
              <CartesianGrid strokeDasharray="3 3" stroke="var(--border2)" />
              <XAxis type="number" tick={{ fontSize: 11, fill: 'var(--text3)' }} />
              <YAxis dataKey="name" type="category" tick={{ fontSize: 11, fill: 'var(--text3)' }} width={120} />
              <Tooltip
                contentStyle={{ background: 'var(--bg2)', border: '1px solid var(--border2)', borderRadius: '8px', fontSize: '12px' }}
              />
              <Bar dataKey="quantity" fill="#FFD700" radius={[0, 4, 4, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div style={cardStyle}>
          <h3 style={{ fontSize: '15px', fontWeight: 700, color: 'var(--text)', marginBottom: '16px' }}>Orders by Status</h3>
          <ResponsiveContainer width="100%" height={220}>
            <PieChart>
              <Pie data={statusData} cx="50%" cy="50%" outerRadius={80} dataKey="value" label={({ name, value }) => `${name}: ${value}`}>
                {statusData.map((_, i) => (
                  <Cell key={i} fill={CHART_COLORS[i % CHART_COLORS.length]} />
                ))}
              </Pie>
              <Tooltip />
            </PieChart>
          </ResponsiveContainer>
        </div>

        <div style={cardStyle}>
          <h3 style={{ fontSize: '15px', fontWeight: 700, color: 'var(--text)', marginBottom: '16px' }}>Orders by Hour</h3>
          <ResponsiveContainer width="100%" height={220}>
            <BarChart data={hourData}>
              <CartesianGrid strokeDasharray="3 3" stroke="var(--border2)" />
              <XAxis dataKey="hour" tick={{ fontSize: 10, fill: 'var(--text3)' }} interval={3} />
              <YAxis tick={{ fontSize: 11, fill: 'var(--text3)' }} />
              <Tooltip
                contentStyle={{ background: 'var(--bg2)', border: '1px solid var(--border2)', borderRadius: '8px', fontSize: '12px' }}
              />
              <Bar dataKey="count" fill="#FFA500" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>

        <div style={cardStyle}>
          <h3 style={{ fontSize: '15px', fontWeight: 700, color: 'var(--text)', marginBottom: '16px' }}>Rating Distribution</h3>
          <ResponsiveContainer width="100%" height={220}>
            <BarChart data={ratingData}>
              <CartesianGrid strokeDasharray="3 3" stroke="var(--border2)" />
              <XAxis dataKey="stars" tick={{ fontSize: 11, fill: 'var(--text3)' }} />
              <YAxis tick={{ fontSize: 11, fill: 'var(--text3)' }} allowDecimals={false} />
              <Tooltip
                contentStyle={{ background: 'var(--bg2)', border: '1px solid var(--border2)', borderRadius: '8px', fontSize: '12px' }}
              />
              <Bar dataKey="count" fill="#4CAF50" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>
  );
}
