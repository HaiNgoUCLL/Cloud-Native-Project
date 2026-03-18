import { useCartStore } from '@/lib/cartStore';
import api from '@/lib/axios';
import { mockCart } from '../__mocks__/mockData';

// Mock axios
jest.mock('@/lib/axios', () => ({
  __esModule: true,
  default: {
    get: jest.fn(),
    post: jest.fn(),
    put: jest.fn(),
    delete: jest.fn(),
  },
}));

const mockedApi = api as jest.Mocked<typeof api>;

describe('cartStore', () => {
  beforeEach(() => {
    // Reset store state
    useCartStore.setState({ cart: null, loading: false });
    jest.clearAllMocks();
  });

  it('fetchCart updates state from API response', async () => {
    (mockedApi.get as jest.Mock).mockResolvedValue({ data: { data: mockCart } });

    await useCartStore.getState().fetchCart();

    expect(mockedApi.get).toHaveBeenCalledWith('/api/cart');
    expect(useCartStore.getState().cart).toEqual(mockCart);
    expect(useCartStore.getState().loading).toBe(false);
  });

  it('addToCart calls API and updates cart', async () => {
    (mockedApi.post as jest.Mock).mockResolvedValue({ data: { data: mockCart } });

    await useCartStore.getState().addToCart('rest1', 'm1', 2);

    expect(mockedApi.post).toHaveBeenCalledWith('/api/cart/add', {
      restaurantId: 'rest1',
      menuItemId: 'm1',
      quantity: 2,
    });
    expect(useCartStore.getState().cart).toEqual(mockCart);
  });

  it('updateItem changes quantity via API', async () => {
    const updatedCart = {
      ...mockCart,
      items: [{ ...mockCart.items[0], quantity: 5 }],
    };
    (mockedApi.put as jest.Mock).mockResolvedValue({ data: { data: updatedCart } });

    await useCartStore.getState().updateItem('m1', 5);

    expect(mockedApi.put).toHaveBeenCalledWith('/api/cart/update', {
      menuItemId: 'm1',
      quantity: 5,
    });
    expect(useCartStore.getState().cart).toEqual(updatedCart);
  });

  it('itemCount computes total from items', () => {
    useCartStore.setState({ cart: mockCart });

    // Cart has: 2 Margherita + 1 Carbonara = 3 total
    const count = useCartStore.getState().itemCount();
    expect(count).toBe(3);
  });

  it('itemCount returns 0 when cart is null', () => {
    useCartStore.setState({ cart: null });
    expect(useCartStore.getState().itemCount()).toBe(0);
  });
});
