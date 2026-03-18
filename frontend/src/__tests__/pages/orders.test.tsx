import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import api from '@/lib/axios';
import { mockOrders } from '../__mocks__/mockData';

jest.mock('@/lib/axios', () => ({
  __esModule: true,
  default: {
    get: jest.fn(),
    post: jest.fn(),
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

jest.mock('@/components/ProtectedRoute', () => ({
  ProtectedRoute: ({ children }: any) => <>{children}</>,
}));

jest.mock('@/components/LoadingSpinner', () => ({
  LoadingSpinner: () => <div data-testid="loading-spinner">Loading...</div>,
}));

import OrdersPage from '@/app/orders/page';

const mockedApi = api as jest.Mocked<typeof api>;

describe('OrdersPage', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders order list with status badges after loading', async () => {
    const testOrders = mockOrders.slice(0, 3);
    (mockedApi.get as jest.Mock).mockResolvedValue({
      data: { data: testOrders },
    });

    render(<OrdersPage />);

    await waitFor(() => {
      // Orders page renders "Order #<last6chars>" for each order
      expect(screen.getByText('Your Orders')).toBeInTheDocument();
    });

    // Status badges should be rendered (multiple DELIVERED orders)
    await waitFor(() => {
      const deliveredBadges = screen.getAllByText('DELIVERED');
      expect(deliveredBadges.length).toBeGreaterThanOrEqual(1);
    });
  });

  it('shows empty state when no orders', async () => {
    (mockedApi.get as jest.Mock).mockResolvedValue({
      data: { data: [] },
    });

    render(<OrdersPage />);

    await waitFor(() => {
      expect(screen.getByText('No orders yet')).toBeInTheDocument();
    });
  });
});
