import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';

jest.mock('next/navigation', () => ({
  useRouter: () => ({ push: jest.fn(), back: jest.fn() }),
  useParams: () => ({ id: 'order1' }),
}));

jest.mock('@/context/AuthContext', () => ({
  useAuth: () => ({
    user: { id: 'cust1', role: 'CUSTOMER', name: 'John' },
    loading: false,
  }),
}));

const mockGet = jest.fn();
jest.mock('@/lib/axios', () => ({
  __esModule: true,
  default: {
    get: (...args: unknown[]) => mockGet(...args),
    put: jest.fn(),
    post: jest.fn(),
  },
}));

jest.mock('react-hot-toast', () => ({
  __esModule: true,
  default: { success: jest.fn(), error: jest.fn() },
}));

import OrderDetailPage from '@/app/orders/[id]/page';

describe('Order Detail Page', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    jest.useFakeTimers();
  });

  afterEach(() => {
    jest.useRealTimers();
  });

  it('displays order status stepper', async () => {
    mockGet.mockImplementation((url: string) => {
      if (url === '/api/orders/order1') {
        return Promise.resolve({
          data: {
            data: {
              id: 'order1',
              customerId: 'cust1',
              restaurantId: 'rest1',
              status: 'CONFIRMED',
              totalAmount: 29.98,
              deliveryAddress: '456 Oak Ave',
              items: [{ menuItemId: 'm1', name: 'Pizza', price: 14.99, quantity: 2 }],
              paymentStatus: 'SIMULATED_PAID',
              paymentMethod: 'CREDIT_CARD',
              createdAt: '2024-02-01T12:00:00',
              updatedAt: '2024-02-01T12:00:00',
            },
          },
        });
      }
      return Promise.resolve({ data: { data: [] } });
    });

    render(<OrderDetailPage />);

    await waitFor(() => {
      expect(screen.getByText('Confirmed')).toBeInTheDocument();
      expect(screen.getByText('Preparing')).toBeInTheDocument();
      expect(screen.getByText('Ready')).toBeInTheDocument();
    });
  });

  it('shows review section for delivered orders', async () => {
    mockGet.mockImplementation((url: string) => {
      if (url === '/api/orders/order1') {
        return Promise.resolve({
          data: {
            data: {
              id: 'order1',
              customerId: 'cust1',
              restaurantId: 'rest1',
              status: 'DELIVERED',
              totalAmount: 29.98,
              deliveryAddress: '456 Oak Ave',
              items: [{ menuItemId: 'm1', name: 'Pizza', price: 14.99, quantity: 2 }],
              paymentStatus: 'SIMULATED_PAID',
              paymentMethod: 'CREDIT_CARD',
              createdAt: '2024-02-01T12:00:00',
              updatedAt: '2024-02-01T13:00:00',
            },
          },
        });
      }
      if (url === '/api/reviews/my') {
        return Promise.resolve({ data: { data: [] } });
      }
      return Promise.resolve({ data: { data: [] } });
    });

    render(<OrderDetailPage />);

    await waitFor(() => {
      expect(screen.getByText('Delivered')).toBeInTheDocument();
    });
  });
});
