'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import toast from 'react-hot-toast';
import { useAuth } from '@/context/AuthContext';

export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const router = useRouter();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      await login(email, password);
      toast.success('Login successful!');
      router.push('/');
    } catch (err: unknown) {
      const error = err as { response?: { data?: { message?: string } } };
      toast.error(error.response?.data?.message || 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex items-center justify-center min-h-screen px-4" style={{ background: 'var(--bg)' }}>
      <div
        className="w-full max-w-md p-8 rounded-2xl"
        style={{ background: 'var(--bg2)', border: '1.5px solid var(--border2)' }}
      >
        <h1 className="text-center gradient-text" style={{ fontSize: '22px', fontWeight: 700, marginBottom: '8px' }}>
          Welcome Back
        </h1>
        <p className="text-center" style={{ fontSize: '14px', fontWeight: 500, color: 'var(--text2)', marginBottom: '32px' }}>
          Sign in to your account
        </p>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text2)', display: 'block', marginBottom: '6px' }}>
              Email
            </label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              style={{
                width: '100%',
                padding: '12px 16px',
                borderRadius: '12px',
                border: '1.5px solid var(--border2)',
                background: 'var(--bg3)',
                color: 'var(--text)',
                fontSize: '14px',
                fontWeight: 500,
                outline: 'none',
              }}
            />
          </div>
          <div>
            <label style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text2)', display: 'block', marginBottom: '6px' }}>
              Password
            </label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              style={{
                width: '100%',
                padding: '12px 16px',
                borderRadius: '12px',
                border: '1.5px solid var(--border2)',
                background: 'var(--bg3)',
                color: 'var(--text)',
                fontSize: '14px',
                fontWeight: 500,
                outline: 'none',
              }}
            />
          </div>
          <button
            type="submit"
            disabled={loading}
            className="w-full gradient-bg flex items-center justify-center"
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
            {loading ? 'Signing in...' : 'Sign In'}
          </button>
        </form>
        <p className="text-center mt-6" style={{ fontSize: '13px', fontWeight: 500, color: 'var(--text2)' }}>
          Don&apos;t have an account?{' '}
          <Link href="/auth/register" className="gradient-text" style={{ fontWeight: 700 }}>
            Sign Up
          </Link>
        </p>
      </div>
    </div>
  );
}
