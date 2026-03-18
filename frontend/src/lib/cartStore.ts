import { create } from 'zustand';
import { Cart, CartItem } from '@/types';
import api from '@/lib/axios';

interface CartStore {
  cart: Cart | null;
  loading: boolean;
  fetchCart: () => Promise<void>;
  addToCart: (restaurantId: string, menuItemId: string, quantity: number) => Promise<void>;
  updateItem: (menuItemId: string, quantity: number) => Promise<void>;
  clearCart: () => Promise<void>;
  itemCount: () => number;
}

export const useCartStore = create<CartStore>((set, get) => ({
  cart: null,
  loading: false,

  fetchCart: async () => {
    set({ loading: true });
    try {
      const res = await api.get('/api/cart');
      set({ cart: res.data.data });
    } catch {
      set({ cart: null });
    } finally {
      set({ loading: false });
    }
  },

  addToCart: async (restaurantId: string, menuItemId: string, quantity: number) => {
    const res = await api.post('/api/cart/add', { restaurantId, menuItemId, quantity });
    set({ cart: res.data.data });
  },

  updateItem: async (menuItemId: string, quantity: number) => {
    const res = await api.put('/api/cart/update', { menuItemId, quantity });
    set({ cart: res.data.data });
  },

  clearCart: async () => {
    await api.delete('/api/cart/clear');
    set({ cart: null });
  },

  itemCount: () => {
    const cart = get().cart;
    if (!cart || !cart.items) return 0;
    return cart.items.reduce((sum: number, item: CartItem) => sum + item.quantity, 0);
  },
}));
