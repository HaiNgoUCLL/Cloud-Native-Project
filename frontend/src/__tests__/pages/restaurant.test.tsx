import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import api from '@/lib/axios';
import { mockRestaurants, mockMenuItems, mockReviews } from '../__mocks__/mockData';

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
  useParams: () => ({ id: 'rest1' }),
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

jest.mock('@/lib/cartStore', () => ({
  useCartStore: (selector?: any) => {
    const state = {
      cart: null,
      loading: false,
      fetchCart: jest.fn(),
      addToCart: jest.fn(),
      updateItem: jest.fn(),
      clearCart: jest.fn(),
      itemCount: () => 0,
    };
    return selector ? selector(state) : state;
  },
}));

jest.mock('@/components/LoadingSpinner', () => ({
  LoadingSpinner: () => <div data-testid="loading-spinner">Loading...</div>,
  SkeletonMenuItem: () => <div data-testid="skeleton">Skeleton</div>,
}));

import RestaurantPage from '@/app/restaurants/[id]/page';

const mockedApi = api as jest.Mocked<typeof api>;

describe('RestaurantPage', () => {
  const marioRestaurant = mockRestaurants[0];
  const marioMenuItems = mockMenuItems.filter((m) => m.restaurantId === 'rest1');
  const marioReviews = mockReviews.filter((r) => r.restaurantId === 'rest1');

  beforeEach(() => {
    jest.clearAllMocks();
    (mockedApi.get as jest.Mock).mockImplementation((url: string) => {
      if (url.includes('/api/restaurants/rest1/menu')) {
        return Promise.resolve({ data: { data: marioMenuItems } });
      }
      if (url.includes('/api/restaurants/rest1')) {
        return Promise.resolve({ data: { data: marioRestaurant } });
      }
      if (url.includes('/api/reviews/restaurant/rest1')) {
        return Promise.resolve({ data: { data: marioReviews } });
      }
      if (url === '/api/cart') {
        return Promise.resolve({ data: { data: null } });
      }
      return Promise.resolve({ data: { data: null } });
    });
  });

  it('renders restaurant name and description after loading', async () => {
    render(<RestaurantPage />);

    await waitFor(() => {
      expect(screen.getByText("Mario's Trattoria")).toBeInTheDocument();
    });

    expect(screen.getByText(/Authentic Italian cuisine/)).toBeInTheDocument();
  });

  it('renders menu items for the default active category', async () => {
    render(<RestaurantPage />);

    await waitFor(() => {
      // First item of the first category (Pizza) should be visible
      expect(screen.getByText('Margherita Pizza')).toBeInTheDocument();
    });

    // Category tabs should be rendered
    expect(screen.getByText('Pizza')).toBeInTheDocument();
  });
});
