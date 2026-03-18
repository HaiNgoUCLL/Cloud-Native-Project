import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';

const mockPush = jest.fn();
jest.mock('next/navigation', () => ({
  useRouter: () => ({ push: mockPush }),
}));

jest.mock('@/context/AuthContext', () => ({
  useAuth: () => ({
    user: { id: 'driver1', role: 'DELIVERY_DRIVER', name: 'Mike' },
    loading: false,
  }),
}));

const mockGet = jest.fn();
const mockPut = jest.fn();
jest.mock('@/lib/axios', () => ({
  __esModule: true,
  default: {
    get: (...args: unknown[]) => mockGet(...args),
    put: (...args: unknown[]) => mockPut(...args),
  },
}));

jest.mock('react-hot-toast', () => ({
  __esModule: true,
  default: { success: jest.fn(), error: jest.fn() },
}));

import DriverDashboard from '@/app/dashboard/driver/page';

describe('Driver Dashboard', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockGet.mockImplementation((url: string) => {
      if (url === '/api/restaurants') {
        return Promise.resolve({
          data: {
            data: [{ id: 'rest1', name: "Mario's", address: '123 Pasta Lane' }],
          },
        });
      }
      if (url === '/api/orders') {
        return Promise.resolve({
          data: {
            data: [
              {
                id: 'order1',
                restaurantId: 'rest1',
                status: 'READY',
                totalAmount: 29.98,
                deliveryAddress: '456 Oak Ave',
                items: [{ menuItemId: 'm1', name: 'Pizza', price: 14.99, quantity: 2 }],
                createdAt: '2024-02-01T12:00:00',
                updatedAt: '2024-02-01T12:00:00',
              },
            ],
          },
        });
      }
      return Promise.resolve({ data: { data: [] } });
    });
  });

  it('shows ready orders with restaurant location', async () => {
    render(<DriverDashboard />);
    await waitFor(() => {
      expect(screen.getByText("Mario's")).toBeInTheDocument();
      expect(screen.getByText('123 Pasta Lane')).toBeInTheDocument();
      expect(screen.getByText('456 Oak Ave')).toBeInTheDocument();
    });
  });

  it('pickup button sends PICKED_UP status', async () => {
    mockPut.mockResolvedValue({ data: { data: {} } });
    render(<DriverDashboard />);

    await waitFor(() => {
      expect(screen.getByText('Pick Up')).toBeInTheDocument();
    });

    await userEvent.click(screen.getByText('Pick Up'));

    expect(mockPut).toHaveBeenCalledWith(
      '/api/orders/order1/status',
      { status: 'PICKED_UP' }
    );
  });

  it('arrived button sends ARRIVED status', async () => {
    // Set up order as PICKED_UP
    mockGet.mockImplementation((url: string) => {
      if (url === '/api/restaurants') {
        return Promise.resolve({ data: { data: [] } });
      }
      if (url === '/api/orders') {
        return Promise.resolve({
          data: {
            data: [
              {
                id: 'order2',
                restaurantId: 'rest1',
                status: 'PICKED_UP',
                totalAmount: 29.98,
                deliveryAddress: '456 Oak Ave',
                items: [],
                createdAt: '2024-02-01T12:00:00',
                updatedAt: '2024-02-01T12:00:00',
              },
            ],
          },
        });
      }
      return Promise.resolve({ data: { data: [] } });
    });

    mockPut.mockResolvedValue({ data: { data: {} } });
    render(<DriverDashboard />);

    // Wait for loading to finish and the Arrived button to appear
    const arrivedButton = await screen.findByRole('button', { name: 'Arrived' });

    await userEvent.click(arrivedButton);

    expect(mockPut).toHaveBeenCalledWith(
      '/api/orders/order2/status',
      { status: 'ARRIVED' }
    );
  });
});
