export interface User {
  id: string;
  name: string;
  email: string;
  role: UserRole;
  address: string;
  phone: string;
  createdAt: string;
}

export type UserRole = 'CUSTOMER' | 'RESTAURANT_OWNER' | 'DELIVERY_DRIVER' | 'ADMIN';

export interface AuthResponse {
  token: string;
  id: string;
  name: string;
  email: string;
  role: UserRole;
}

export interface Restaurant {
  id: string;
  ownerId: string;
  name: string;
  description: string;
  cuisineType: string;
  address: string;
  imageUrl: string;
  isOpen: boolean;
  open: boolean;
  rating: number;
  createdAt: string;
}

export interface MenuItem {
  id: string;
  restaurantId: string;
  name: string;
  description: string;
  price: number;
  category: string;
  imageUrl: string;
  isAvailable: boolean;
  available: boolean;
}

export interface CartItem {
  menuItemId: string;
  name: string;
  price: number;
  quantity: number;
}

export interface Cart {
  id: string;
  customerId: string;
  restaurantId: string;
  items: CartItem[];
}

export interface OrderItem {
  menuItemId: string;
  name: string;
  price: number;
  quantity: number;
}

export type OrderStatus = 'PENDING' | 'CONFIRMED' | 'PREPARING' | 'OUT_FOR_DELIVERY' | 'DELIVERED' | 'CANCELLED';
export type PaymentStatus = 'PENDING' | 'SIMULATED_PAID' | 'FAILED';
export type PaymentMethod = 'CREDIT_CARD' | 'PAYPAL' | 'CASH';

export interface Order {
  id: string;
  customerId: string;
  restaurantId: string;
  items: OrderItem[];
  status: OrderStatus;
  totalAmount: number;
  deliveryAddress: string;
  paymentStatus: PaymentStatus;
  paymentMethod: PaymentMethod;
  driverId: string;
  promoCode: string | null;
  discountAmount: number;
  createdAt: string;
  updatedAt: string;
}

export interface PromoCode {
  id: string;
  code: string;
  discountType: 'PERCENTAGE' | 'FIXED_AMOUNT';
  discountValue: number;
  minimumOrderAmount: number;
  maxUsageCount: number;
  currentUsageCount: number;
  expiresAt: string | null;
  active: boolean;
  restaurantId: string | null;
  createdBy: string;
  createdAt: string;
}

export interface PromoValidationResponse {
  valid: boolean;
  discountAmount: number;
  message: string;
}

export interface Review {
  id: string;
  customerId: string;
  customerName: string;
  restaurantId: string;
  orderId: string;
  rating: number;
  comment: string;
  sentimentScore: number | null;
  createdAt: string;
}

export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message: string;
}
