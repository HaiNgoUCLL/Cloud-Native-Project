'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { ShoppingCart, User, LogOut, ChevronDown, LayoutDashboard } from 'lucide-react';
import { useAuth } from '@/context/AuthContext';
import { useTheme } from '@/context/ThemeContext';
import { useCartStore } from '@/lib/cartStore';

export function Navbar() {
  const { user, logout } = useAuth();
  const { isDark, toggleTheme } = useTheme();
  const router = useRouter();
  const itemCount = useCartStore((s) => s.itemCount);
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    router.push(`/?search=${encodeURIComponent(searchQuery)}`);
  };

  const getDashboardLink = () => {
    if (!user) return '/';
    switch (user.role) {
      case 'RESTAURANT_OWNER': return '/dashboard/owner';
      case 'DELIVERY_DRIVER': return '/dashboard/driver';
      case 'ADMIN': return '/admin';
      default: return '/orders';
    }
  };

  return (
    <nav
      className="fixed top-0 left-0 right-0 z-50"
      style={{
        backgroundColor: isDark ? 'var(--bg)' : '#0A0800',
        borderBottom: '1.5px solid var(--border2)',
        height: '72px',
      }}
    >
      <div className="max-w-7xl mx-auto px-4 h-full flex items-center justify-between gap-4">
        <Link
          href="/"
          className="flex-shrink-0"
          style={{
            fontSize: '20px',
            fontWeight: 500,
            letterSpacing: '3px',
            textTransform: 'uppercase',
            background: 'var(--gradient)',
            WebkitBackgroundClip: 'text',
            WebkitTextFillColor: 'transparent',
          }}
        >
          FoodPlatform
        </Link>

        <form onSubmit={handleSearch} className="flex-1 max-w-lg mx-4">
          <div
            className="flex items-center overflow-hidden"
            style={{
              background: '#FFFFFF',
              border: isDark ? '2px solid var(--y)' : '2px solid #B8780A',
              borderRadius: '35px',
              boxShadow: isDark ? 'none' : '0 2px 12px rgba(184,120,10,0.15)',
            }}
          >
            <input
              type="text"
              placeholder="Search restaurants..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              style={{
                flex: 1,
                padding: '10px 20px',
                border: 'none',
                outline: 'none',
                fontSize: '14px',
                fontWeight: 500,
                color: '#0A0800',
                background: '#FFFFFF',
              }}
            />
            <button
              type="submit"
              className="gradient-bg"
              style={{
                padding: '10px 24px',
                color: '#FFFFFF',
                fontWeight: 700,
                fontSize: '13px',
                border: 'none',
                cursor: 'pointer',
              }}
            >
              Search
            </button>
          </div>
        </form>

        <div className="flex items-center gap-3">
          <button
            onClick={toggleTheme}
            className="flex items-center gap-1 px-3 py-2 rounded-full"
            style={{
              border: '1.5px solid var(--border)',
              fontSize: '13px',
              fontWeight: 600,
              color: isDark ? 'var(--y)' : '#FFD000',
              background: 'transparent',
            }}
          >
            {isDark ? '☀️' : '🌙'} {isDark ? 'Light' : 'Dark'}
          </button>

          {user && user.role === 'CUSTOMER' && (
            <Link
              href="/cart"
              className="relative flex items-center justify-center"
              style={{
                width: '40px',
                height: '40px',
                borderRadius: '50%',
                border: '1.5px solid var(--border)',
              }}
            >
              <ShoppingCart size={20} style={{ color: isDark ? 'var(--y)' : '#FFD000' }} />
              {itemCount() > 0 && (
                <span
                  className="absolute -top-1 -right-1 gradient-bg flex items-center justify-center"
                  style={{
                    width: '20px',
                    height: '20px',
                    borderRadius: '50%',
                    fontSize: '12px',
                    fontWeight: 700,
                    color: '#000000',
                  }}
                >
                  {itemCount()}
                </span>
              )}
            </Link>
          )}

          {user ? (
            <div className="relative">
              <button
                onClick={() => setDropdownOpen(!dropdownOpen)}
                className="flex items-center gap-2"
              >
                <div
                  className="gradient-bg flex items-center justify-center"
                  style={{
                    width: '38px',
                    height: '38px',
                    borderRadius: '50%',
                    fontSize: '15px',
                    fontWeight: 700,
                    color: '#000000',
                  }}
                >
                  {user.name.charAt(0).toUpperCase()}
                </div>
                <ChevronDown size={16} style={{ color: isDark ? 'var(--text2)' : '#FFD000' }} />
              </button>
              {dropdownOpen && (
                <div
                  className="absolute right-0 mt-2 w-52 rounded-2xl overflow-hidden"
                  style={{
                    background: 'var(--bg2)',
                    border: '1.5px solid var(--border2)',
                    zIndex: 100,
                  }}
                >
                  <div className="px-4 py-3" style={{ borderBottom: '1.5px solid var(--border2)' }}>
                    <p style={{ fontSize: '14px', fontWeight: 700, color: 'var(--text)' }}>{user.name}</p>
                    <p style={{ fontSize: '12px', fontWeight: 500, color: 'var(--text3)' }}>{user.role.replace('_', ' ')}</p>
                  </div>
                  <Link
                    href={getDashboardLink()}
                    onClick={() => setDropdownOpen(false)}
                    className="flex items-center gap-2 px-4 py-3 transition-all"
                    style={{ fontSize: '13px', fontWeight: 500, color: 'var(--text2)' }}
                    onMouseEnter={(e) => (e.currentTarget.style.background = 'var(--bg3)')}
                    onMouseLeave={(e) => (e.currentTarget.style.background = 'transparent')}
                  >
                    <LayoutDashboard size={16} /> Dashboard
                  </Link>
                  <button
                    onClick={() => { setDropdownOpen(false); logout(); }}
                    className="flex items-center gap-2 w-full px-4 py-3 transition-all"
                    style={{ fontSize: '13px', fontWeight: 500, color: 'var(--text2)' }}
                    onMouseEnter={(e) => (e.currentTarget.style.background = 'var(--bg3)')}
                    onMouseLeave={(e) => (e.currentTarget.style.background = 'transparent')}
                  >
                    <LogOut size={16} /> Logout
                  </button>
                </div>
              )}
            </div>
          ) : (
            <Link
              href="/auth/login"
              className="flex items-center gap-2"
              style={{
                padding: '10px 20px',
                borderRadius: '30px',
                background: 'var(--gradient)',
                color: '#000000',
                fontSize: '13px',
                fontWeight: 700,
              }}
            >
              <User size={16} /> Sign In
            </Link>
          )}
        </div>
      </div>
    </nav>
  );
}
