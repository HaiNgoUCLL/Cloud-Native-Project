import React from 'react';
import { render, screen } from '@testing-library/react';

// Mock modules BEFORE importing the component
jest.mock('@/lib/axios', () => ({
  __esModule: true,
  default: {
    post: jest.fn(),
    get: jest.fn(),
    put: jest.fn(),
    delete: jest.fn(),
    interceptors: { request: { use: jest.fn() }, response: { use: jest.fn() } },
  },
}));

jest.mock('react-hot-toast', () => ({
  __esModule: true,
  default: Object.assign(jest.fn(), { success: jest.fn(), error: jest.fn() }),
}));

jest.mock('next/navigation', () => ({
  useRouter: () => ({ push: jest.fn(), replace: jest.fn(), back: jest.fn() }),
}));

jest.mock('next/link', () => ({
  __esModule: true,
  default: ({ children, href, ...props }: any) => <a href={href} {...props}>{children}</a>,
}));

jest.mock('@/context/AuthContext', () => ({
  useAuth: () => ({
    user: { id: 'cust1', name: 'John', role: 'CUSTOMER' },
    logout: jest.fn(),
    loading: false,
  }),
}));

const mockCartData = {
  id: 'cart1',
  customerId: 'cust1',
  restaurantId: 'rest1',
  items: [
    { menuItemId: 'm1', name: 'Margherita Pizza', price: 14.99, quantity: 2 },
    { menuItemId: 'm2', name: 'Spaghetti Carbonara', price: 15.99, quantity: 1 },
  ],
};

jest.mock('@/lib/cartStore', () => ({
  useCartStore: (selector?: any) => {
    const state = {
      cart: mockCartData,
      loading: false,
      fetchCart: jest.fn(),
      addToCart: jest.fn(),
      updateItem: jest.fn(),
      clearCart: jest.fn(),
      itemCount: () => 3,
    };
    return selector ? selector(state) : state;
  },
}));

jest.mock('@/components/ProtectedRoute', () => ({
  ProtectedRoute: ({ children }: any) => <>{children}</>,
}));

import CheckoutPage from '@/app/checkout/page';

describe('CheckoutPage', () => {
  it('renders checkout form with address input', () => {
    render(<CheckoutPage />);
    expect(screen.getByPlaceholderText('Enter your full delivery address')).toBeInTheDocument();
  });

  it('renders payment method options', () => {
    render(<CheckoutPage />);
    expect(screen.getByText('Credit Card')).toBeInTheDocument();
    expect(screen.getByText('PayPal')).toBeInTheDocument();
    expect(screen.getByText('Cash on Delivery')).toBeInTheDocument();
  });

  it('renders promo code input', () => {
    render(<CheckoutPage />);
    expect(screen.getByPlaceholderText('Enter promo code')).toBeInTheDocument();
    expect(screen.getByText('Apply')).toBeInTheDocument();
  });
});
