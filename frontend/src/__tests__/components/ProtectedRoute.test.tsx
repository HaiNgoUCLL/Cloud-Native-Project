import React from 'react';
import { render, screen } from '@testing-library/react';

const mockPush = jest.fn();
jest.mock('next/navigation', () => ({
  useRouter: () => ({ push: mockPush }),
}));

let mockUser: { role: string } | null = null;
let mockLoading = false;
jest.mock('@/context/AuthContext', () => ({
  useAuth: () => ({ user: mockUser, loading: mockLoading }),
}));

import { ProtectedRoute } from '@/components/ProtectedRoute';

describe('ProtectedRoute', () => {
  beforeEach(() => {
    mockPush.mockClear();
    mockUser = null;
    mockLoading = false;
  });

  it('renders children when authenticated', () => {
    mockUser = { role: 'CUSTOMER' } as any;
    render(
      <ProtectedRoute>
        <div>Protected Content</div>
      </ProtectedRoute>
    );
    expect(screen.getByText('Protected Content')).toBeInTheDocument();
  });

  it('redirects when not authenticated', () => {
    render(
      <ProtectedRoute>
        <div>Protected Content</div>
      </ProtectedRoute>
    );
    expect(mockPush).toHaveBeenCalledWith('/auth/login');
    expect(screen.queryByText('Protected Content')).not.toBeInTheDocument();
  });

  it('redirects when role does not match', () => {
    mockUser = { role: 'CUSTOMER' } as any;
    render(
      <ProtectedRoute role="ADMIN">
        <div>Admin Content</div>
      </ProtectedRoute>
    );
    expect(mockPush).toHaveBeenCalledWith('/');
    expect(screen.queryByText('Admin Content')).not.toBeInTheDocument();
  });
});
