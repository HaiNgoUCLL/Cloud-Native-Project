import React from 'react';
import { render, screen } from '@testing-library/react';
import { Navbar } from '@/components/Navbar';
import { mockCustomer } from '../__mocks__/mockData';

// Mock Next.js router
jest.mock('next/navigation', () => ({
  useRouter: () => ({
    push: jest.fn(),
    replace: jest.fn(),
    back: jest.fn(),
  }),
}));

// Mock Next.js Link
jest.mock('next/link', () => ({
  __esModule: true,
  default: ({ children, href, ...props }: any) =>
    <a href={href} {...props}>{children}</a>,
}));

// Mock contexts and stores
const mockLogout = jest.fn();
let mockUser: any = null;

jest.mock('@/context/AuthContext', () => ({
  useAuth: () => ({
    user: mockUser,
    logout: mockLogout,
    login: jest.fn(),
    register: jest.fn(),
    loading: false,
  }),
}));

jest.mock('@/context/ThemeContext', () => ({
  useTheme: () => ({
    isDark: true,
    toggleTheme: jest.fn(),
  }),
}));

jest.mock('@/lib/cartStore', () => ({
  useCartStore: (selector: any) => selector({
    cart: null,
    itemCount: () => 0,
  }),
}));

jest.mock('@/lib/useNotifications', () => ({
  useNotifications: () => ({
    notifications: [],
    unreadCount: 0,
    markAllRead: jest.fn(),
  }),
}));

describe('Navbar', () => {
  beforeEach(() => {
    mockUser = null;
  });

  it('renders logo/brand name', () => {
    render(<Navbar />);
    expect(screen.getByText('FoodPlatform')).toBeInTheDocument();
  });

  it('shows Sign In link when unauthenticated', () => {
    render(<Navbar />);
    expect(screen.getByText('Sign In')).toBeInTheDocument();
  });

  it('shows user initial and search when authenticated', () => {
    mockUser = mockCustomer;
    render(<Navbar />);
    // Should show user's first initial
    expect(screen.getByText('J')).toBeInTheDocument();
    // Should show search
    expect(screen.getByPlaceholderText('Search restaurants...')).toBeInTheDocument();
    // Should not show Sign In
    expect(screen.queryByText('Sign In')).not.toBeInTheDocument();
  });
});
