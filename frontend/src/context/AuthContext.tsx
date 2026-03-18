'use client';

import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import Cookies from 'js-cookie';
import { User, UserRole } from '@/types';
import api from '@/lib/axios';

interface AuthContextType {
  user: User | null;
  token: string | null;
  login: (email: string, password: string) => Promise<void>;
  register: (name: string, email: string, password: string, role: UserRole) => Promise<void>;
  logout: () => void;
  loading: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  const fetchUser = useCallback(async () => {
    try {
      const savedToken = Cookies.get('token');
      if (savedToken) {
        setToken(savedToken);
        const res = await api.get('/api/auth/me');
        setUser(res.data.data);
      }
    } catch {
      Cookies.remove('token');
      Cookies.remove('user');
      setToken(null);
      setUser(null);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchUser();
  }, [fetchUser]);

  const login = async (email: string, password: string) => {
    const res = await api.post('/api/auth/login', { email, password });
    const data = res.data.data;
    Cookies.set('token', data.token, { expires: 1 });
    setToken(data.token);
    const meRes = await api.get('/api/auth/me', {
      headers: { Authorization: `Bearer ${data.token}` },
    });
    setUser(meRes.data.data);
  };

  const register = async (name: string, email: string, password: string, role: UserRole) => {
    const res = await api.post('/api/auth/register', { name, email, password, role });
    const data = res.data.data;
    Cookies.set('token', data.token, { expires: 1 });
    setToken(data.token);
    const meRes = await api.get('/api/auth/me', {
      headers: { Authorization: `Bearer ${data.token}` },
    });
    setUser(meRes.data.data);
  };

  const logout = () => {
    Cookies.remove('token');
    Cookies.remove('user');
    setToken(null);
    setUser(null);
    window.location.href = '/';
  };

  return (
    <AuthContext.Provider value={{ user, token, login, register, logout, loading }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
