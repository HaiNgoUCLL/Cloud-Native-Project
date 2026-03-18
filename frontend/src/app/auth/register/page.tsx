'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import toast from 'react-hot-toast';
import { useAuth } from '@/context/AuthContext';
import { UserRole } from '@/types';

const ROLES: { value: UserRole; label: string }[] = [
  { value: 'CUSTOMER', label: 'Customer' },
  { value: 'RESTAURANT_OWNER', label: 'Restaurant Owner' },
  { value: 'DELIVERY_DRIVER', label: 'Delivery Driver' },
];

export default function RegisterPage() {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState<UserRole>('CUSTOMER');
  const [loading, setLoading] = useState(false);
  const { register } = useAuth();
  const router = useRouter();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      await register(name, email, password, role);
      toast.success('Registration successful!');
      router.push('/');
    } catch (err: unknown) {
      const error = err as { response?: { data?: { message?: string } } };
      toast.error(error.response?.data?.message || 'Registration failed');
    } finally {
      setLoading(false);
    }
  };

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
    <div className="flex items-center justify-center min-h-screen px-4" style={{ background: 'var(--bg)' }}>
      <div
        className="w-full max-w-md p-8 rounded-2xl"
        style={{ background: 'var(--bg2)', border: '1.5px solid var(--border2)' }}
      >
        <h1 className="text-center gradient-text" style={{ fontSize: '22px', fontWeight: 700, marginBottom: '8px' }}>
          Create Account
        </h1>
        <p className="text-center" style={{ fontSize: '14px', fontWeight: 500, color: 'var(--text2)', marginBottom: '32px' }}>
          Join our food delivery platform
        </p>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text2)', display: 'block', marginBottom: '6px' }}>
              Full Name
            </label>
            <input type="text" value={name} onChange={(e) => setName(e.target.value)} required style={inputStyle} />
          </div>
          <div>
            <label style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text2)', display: 'block', marginBottom: '6px' }}>
              Email
            </label>
            <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required style={inputStyle} />
          </div>
          <div>
            <label style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text2)', display: 'block', marginBottom: '6px' }}>
              Password
            </label>
            <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required style={inputStyle} />
          </div>
          <div>
            <label style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text2)', display: 'block', marginBottom: '6px' }}>
              Role
            </label>
            <div className="grid grid-cols-3 gap-2">
              {ROLES.map((r) => (
                <button
                  type="button"
                  key={r.value}
                  onClick={() => setRole(r.value)}
                  style={{
                    padding: '10px 8px',
                    borderRadius: '12px',
                    fontSize: '12px',
                    fontWeight: role === r.value ? 700 : 500,
                    color: role === r.value ? '#000000' : 'var(--text2)',
                    background: role === r.value ? 'var(--y)' : 'var(--bg3)',
                    border: `1.5px solid ${role === r.value ? 'var(--y)' : 'var(--border2)'}`,
                    cursor: 'pointer',
                    transition: 'all 0.2s',
                  }}
                >
                  {r.label}
                </button>
              ))}
            </div>
          </div>
          <button
            type="submit"
            disabled={loading}
            className="w-full gradient-bg"
            style={{
              padding: '14px',
              borderRadius: '30px',
              color: '#000000',
              fontSize: '15px',
              fontWeight: 700,
              border: 'none',
              cursor: loading ? 'not-allowed' : 'pointer',
              opacity: loading ? 0.7 : 1,
            }}
          >
            {loading ? 'Creating Account...' : 'Create Account'}
          </button>
        </form>
        <p className="text-center mt-6" style={{ fontSize: '13px', fontWeight: 500, color: 'var(--text2)' }}>
          Already have an account?{' '}
          <Link href="/auth/login" className="gradient-text" style={{ fontWeight: 700 }}>
            Sign In
          </Link>
        </p>
      </div>
    </div>
  );
}
