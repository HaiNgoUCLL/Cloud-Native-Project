import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';

const mockPush = jest.fn();
jest.mock('next/navigation', () => ({
  useRouter: () => ({ push: mockPush }),
}));

jest.mock('@/context/AuthContext', () => ({
  useAuth: () => ({
    user: { id: 'owner1', role: 'RESTAURANT_OWNER', name: 'Mario' },
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

import OwnerDashboard from '@/app/dashboard/owner/page';

describe('Owner Dashboard', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockGet.mockImplementation((url: string) => {
      if (url === '/api/restaurants') {
        return Promise.resolve({
          data: { data: [{ id: 'rest1', name: "Mario's", ownerId: 'owner1' }] },
        });
      }
      if (url.includes('/menu')) {
        return Promise.resolve({ data: { data: [] } });
      }
      if (url === '/api/orders') {
        return Promise.resolve({
          data: {
            data: [
              {
                id: 'order1',
                restaurantId: 'rest1',
                status: 'PENDING',
                totalAmount: 29.98,
                items: [{ menuItemId: 'm1', name: 'Pizza', price: 14.99, quantity: 2 }],
                deliveryAddress: '123 St',
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

  it('renders orders', async () => {
    render(<OwnerDashboard />);
    await waitFor(() => {
      expect(screen.getByText(/order1/i)).toBeInTheDocument();
    });
  });

  it('shows confirm button for pending orders', async () => {
    render(<OwnerDashboard />);
    await waitFor(() => {
      expect(screen.getByText('Confirm')).toBeInTheDocument();
    });
  });

  it('calls API when confirm button clicked', async () => {
    mockPut.mockResolvedValue({ data: { data: {} } });
    render(<OwnerDashboard />);

    await waitFor(() => {
      expect(screen.getByText('Confirm')).toBeInTheDocument();
    });

    await userEvent.click(screen.getByText('Confirm'));

    expect(mockPut).toHaveBeenCalledWith(
      '/api/orders/order1/status',
      expect.objectContaining({ status: 'CONFIRMED' })
    );
  });
});
