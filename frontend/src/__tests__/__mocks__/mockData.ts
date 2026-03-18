import type {
  User,
  Restaurant,
  MenuItem,
  Order,
  Review,
  PromoCode,
  Cart,
  CartItem,
  PromoValidationResponse,
} from '@/types';

// ── Users ────────────────────────────────────────────────────

export const mockCustomer: User = {
  id: 'cust1',
  name: 'John Customer',
  email: 'customer@food.com',
  role: 'CUSTOMER',
  address: '456 Oak Ave, Apt 2B',
  phone: '+1234567890',
  createdAt: '2024-01-15T10:00:00',
};

export const mockCustomer2: User = {
  id: 'cust2',
  name: 'Sarah Miller',
  email: 'customer2@food.com',
  role: 'CUSTOMER',
  address: '789 Pine St',
  phone: '+1234567891',
  createdAt: '2024-01-16T10:00:00',
};

export const mockOwner: User = {
  id: 'owner1',
  name: 'Mario Rossi',
  email: 'owner1@food.com',
  role: 'RESTAURANT_OWNER',
  address: '123 Main St',
  phone: '+1234567892',
  createdAt: '2024-01-10T10:00:00',
};

export const mockDriver: User = {
  id: 'driver1',
  name: 'Mike Driver',
  email: 'driver@food.com',
  role: 'DELIVERY_DRIVER',
  address: '321 Elm St',
  phone: '+1234567893',
  createdAt: '2024-01-12T10:00:00',
};

export const mockAdmin: User = {
  id: 'admin1',
  name: 'Admin User',
  email: 'admin@food.com',
  role: 'ADMIN',
  address: '999 Admin Blvd',
  phone: '+1234567894',
  createdAt: '2024-01-01T10:00:00',
};

// ── Restaurants ──────────────────────────────────────────────

export const mockRestaurants: Restaurant[] = [
  {
    id: 'rest1',
    ownerId: 'owner1',
    name: "Mario's Trattoria",
    description: 'Authentic Italian cuisine with homemade pasta and wood-fired pizzas',
    cuisineType: 'Italian',
    address: '123 Pasta Lane, Little Italy',
    imageUrl: 'https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=800',
    isOpen: true,
    open: true,
    rating: 4.7,
    createdAt: '2024-01-10T10:00:00',
  },
  {
    id: 'rest2',
    ownerId: 'owner2',
    name: 'Tokyo Omakase',
    description: 'Premium Japanese dining featuring fresh sushi, ramen, and traditional dishes',
    cuisineType: 'Japanese',
    address: '456 Sakura Street, Japantown',
    imageUrl: 'https://images.unsplash.com/photo-1579871494447-9811cf80d66c?w=800',
    isOpen: true,
    open: true,
    rating: 4.8,
    createdAt: '2024-01-11T10:00:00',
  },
  {
    id: 'rest3',
    ownerId: 'owner3',
    name: 'The Prime Cut',
    description: 'Premium steakhouse serving the finest aged beef with classic sides',
    cuisineType: 'American',
    address: '789 Grill Avenue, Downtown',
    imageUrl: 'https://images.unsplash.com/photo-1544025162-d76694265947?w=800',
    isOpen: true,
    open: true,
    rating: 4.6,
    createdAt: '2024-01-12T10:00:00',
  },
];

// ── Menu Items ───────────────────────────────────────────────

export const mockMenuItems: MenuItem[] = [
  {
    id: 'm1',
    restaurantId: 'rest1',
    name: 'Margherita Pizza',
    description: 'Fresh mozzarella, tomato sauce, basil on wood-fired crust',
    price: 14.99,
    category: 'Pizza',
    imageUrl: '',
    isAvailable: true,
    available: true,
  },
  {
    id: 'm2',
    restaurantId: 'rest1',
    name: 'Spaghetti Carbonara',
    description: 'Creamy egg sauce, pancetta, parmesan, black pepper',
    price: 15.99,
    category: 'Pasta',
    imageUrl: '',
    isAvailable: true,
    available: true,
  },
  {
    id: 'm3',
    restaurantId: 'rest1',
    name: 'Tiramisu',
    description: 'Classic Italian dessert with espresso-soaked ladyfingers',
    price: 9.99,
    category: 'Desserts',
    imageUrl: '',
    isAvailable: true,
    available: true,
  },
  {
    id: 'm4',
    restaurantId: 'rest2',
    name: 'Salmon Nigiri (6pc)',
    description: 'Fresh Atlantic salmon over seasoned rice',
    price: 16.99,
    category: 'Sushi',
    imageUrl: '',
    isAvailable: true,
    available: true,
  },
  {
    id: 'm5',
    restaurantId: 'rest2',
    name: 'Tonkotsu Ramen',
    description: 'Rich pork bone broth, chashu, soft egg, nori',
    price: 16.99,
    category: 'Ramen',
    imageUrl: '',
    isAvailable: true,
    available: true,
  },
  {
    id: 'm6',
    restaurantId: 'rest3',
    name: 'Ribeye Steak 12oz',
    description: 'Prime aged ribeye, herb butter, grilled to perfection',
    price: 38.99,
    category: 'Steaks',
    imageUrl: '',
    isAvailable: true,
    available: true,
  },
  {
    id: 'm7',
    restaurantId: 'rest3',
    name: 'Caesar Salad',
    description: 'Romaine, parmesan, croutons, house-made dressing',
    price: 11.99,
    category: 'Sides & Salads',
    imageUrl: '',
    isAvailable: true,
    available: true,
  },
];

// ── Orders ───────────────────────────────────────────────────

export const mockOrders: Order[] = [
  {
    id: 'order1',
    customerId: 'cust1',
    restaurantId: 'rest1',
    items: [
      { menuItemId: 'm1', name: 'Margherita Pizza', price: 14.99, quantity: 2 },
      { menuItemId: 'm2', name: 'Spaghetti Carbonara', price: 15.99, quantity: 1 },
    ],
    status: 'DELIVERED',
    totalAmount: 45.97,
    deliveryAddress: '456 Oak Ave, Apt 2B',
    paymentStatus: 'SIMULATED_PAID',
    paymentMethod: 'CREDIT_CARD',
    driverId: 'driver1',
    promoCode: null,
    discountAmount: 0,
    createdAt: '2024-02-01T12:00:00',
    updatedAt: '2024-02-01T13:00:00',
  },
  {
    id: 'order2',
    customerId: 'cust1',
    restaurantId: 'rest2',
    items: [
      { menuItemId: 'm4', name: 'Salmon Nigiri (6pc)', price: 16.99, quantity: 1 },
      { menuItemId: 'm5', name: 'Tonkotsu Ramen', price: 16.99, quantity: 1 },
    ],
    status: 'DELIVERED',
    totalAmount: 30.58,
    deliveryAddress: '456 Oak Ave, Apt 2B',
    paymentStatus: 'SIMULATED_PAID',
    paymentMethod: 'PAYPAL',
    driverId: 'driver1',
    promoCode: 'WELCOME10',
    discountAmount: 3.40,
    createdAt: '2024-02-03T18:00:00',
    updatedAt: '2024-02-03T19:00:00',
  },
  {
    id: 'order3',
    customerId: 'cust1',
    restaurantId: 'rest3',
    items: [
      { menuItemId: 'm6', name: 'Ribeye Steak 12oz', price: 38.99, quantity: 1 },
    ],
    status: 'PENDING',
    totalAmount: 38.99,
    deliveryAddress: '456 Oak Ave, Apt 2B',
    paymentStatus: 'PENDING',
    paymentMethod: 'CREDIT_CARD',
    driverId: '',
    promoCode: null,
    discountAmount: 0,
    createdAt: '2024-02-05T20:00:00',
    updatedAt: '2024-02-05T20:00:00',
  },
  {
    id: 'order4',
    customerId: 'cust2',
    restaurantId: 'rest1',
    items: [
      { menuItemId: 'm1', name: 'Margherita Pizza', price: 14.99, quantity: 1 },
      { menuItemId: 'm3', name: 'Tiramisu', price: 9.99, quantity: 2 },
    ],
    status: 'CONFIRMED',
    totalAmount: 34.97,
    deliveryAddress: '789 Pine St',
    paymentStatus: 'SIMULATED_PAID',
    paymentMethod: 'CREDIT_CARD',
    driverId: '',
    promoCode: null,
    discountAmount: 0,
    createdAt: '2024-02-05T19:00:00',
    updatedAt: '2024-02-05T19:15:00',
  },
  {
    id: 'order5',
    customerId: 'cust1',
    restaurantId: 'rest2',
    items: [
      { menuItemId: 'm4', name: 'Salmon Nigiri (6pc)', price: 16.99, quantity: 2 },
    ],
    status: 'OUT_FOR_DELIVERY',
    totalAmount: 33.98,
    deliveryAddress: '456 Oak Ave, Apt 2B',
    paymentStatus: 'SIMULATED_PAID',
    paymentMethod: 'CASH',
    driverId: 'driver2',
    promoCode: null,
    discountAmount: 0,
    createdAt: '2024-02-05T18:30:00',
    updatedAt: '2024-02-05T19:30:00',
  },
  {
    id: 'order6',
    customerId: 'cust2',
    restaurantId: 'rest3',
    items: [
      { menuItemId: 'm6', name: 'Ribeye Steak 12oz', price: 38.99, quantity: 1 },
      { menuItemId: 'm7', name: 'Caesar Salad', price: 11.99, quantity: 1 },
    ],
    status: 'CANCELLED',
    totalAmount: 50.98,
    deliveryAddress: '789 Pine St',
    paymentStatus: 'PENDING',
    paymentMethod: 'CREDIT_CARD',
    driverId: '',
    promoCode: null,
    discountAmount: 0,
    createdAt: '2024-02-02T14:00:00',
    updatedAt: '2024-02-02T14:05:00',
  },
];

// ── Reviews ──────────────────────────────────────────────────

export const mockReviews: Review[] = [
  {
    id: 'rev1',
    customerId: 'cust1',
    customerName: 'John Customer',
    restaurantId: 'rest1',
    orderId: 'order1',
    rating: 5,
    comment: 'Absolutely incredible pasta! The Carbonara was the best I have ever had.',
    sentimentScore: 0.95,
    createdAt: '2024-02-02T10:00:00',
  },
  {
    id: 'rev2',
    customerId: 'cust2',
    customerName: 'Sarah Miller',
    restaurantId: 'rest1',
    orderId: 'order4',
    rating: 4,
    comment: 'Great Italian food, generous portions. Tiramisu was divine.',
    sentimentScore: 0.78,
    createdAt: '2024-02-03T11:00:00',
  },
  {
    id: 'rev3',
    customerId: 'cust1',
    customerName: 'John Customer',
    restaurantId: 'rest2',
    orderId: 'order2',
    rating: 5,
    comment: 'Best sushi in town! The Salmon Nigiri melts in your mouth.',
    sentimentScore: 0.92,
    createdAt: '2024-02-04T09:00:00',
  },
  {
    id: 'rev4',
    customerId: 'cust2',
    customerName: 'Sarah Miller',
    restaurantId: 'rest3',
    orderId: 'order6',
    rating: 3,
    comment: 'Good food but the steak was a bit overcooked. Salad was fresh though.',
    sentimentScore: 0.45,
    createdAt: '2024-02-03T15:00:00',
  },
  {
    id: 'rev5',
    customerId: 'cust1',
    customerName: 'John Customer',
    restaurantId: 'rest3',
    orderId: 'order3',
    rating: 2,
    comment: 'Disappointing experience. Food arrived cold.',
    sentimentScore: 0.15,
    createdAt: '2024-02-06T12:00:00',
  },
];

// ── Promo Codes ──────────────────────────────────────────────

export const mockPromoCodes: PromoCode[] = [
  {
    id: 'promo1',
    code: 'WELCOME10',
    discountType: 'PERCENTAGE',
    discountValue: 10,
    minimumOrderAmount: 15,
    maxUsageCount: 100,
    currentUsageCount: 5,
    expiresAt: '2024-06-01T00:00:00',
    active: true,
    restaurantId: null,
    createdBy: 'admin1',
    createdAt: '2024-01-01T00:00:00',
  },
  {
    id: 'promo2',
    code: 'SAVE5',
    discountType: 'FIXED_AMOUNT',
    discountValue: 5,
    minimumOrderAmount: 25,
    maxUsageCount: 50,
    currentUsageCount: 0,
    expiresAt: '2024-04-01T00:00:00',
    active: true,
    restaurantId: null,
    createdBy: 'admin1',
    createdAt: '2024-01-01T00:00:00',
  },
  {
    id: 'promo3',
    code: 'EXPIRED20',
    discountType: 'PERCENTAGE',
    discountValue: 20,
    minimumOrderAmount: 10,
    maxUsageCount: 200,
    currentUsageCount: 5,
    expiresAt: '2024-01-01T00:00:00',
    active: true,
    restaurantId: null,
    createdBy: 'admin1',
    createdAt: '2023-12-01T00:00:00',
  },
  {
    id: 'promo4',
    code: 'MAXED15',
    discountType: 'PERCENTAGE',
    discountValue: 15,
    minimumOrderAmount: 20,
    maxUsageCount: 10,
    currentUsageCount: 10,
    expiresAt: '2024-06-01T00:00:00',
    active: true,
    restaurantId: null,
    createdBy: 'admin1',
    createdAt: '2024-01-01T00:00:00',
  },
  {
    id: 'promo5',
    code: 'MARIO20',
    discountType: 'PERCENTAGE',
    discountValue: 20,
    minimumOrderAmount: 30,
    maxUsageCount: 100,
    currentUsageCount: 12,
    expiresAt: '2024-05-01T00:00:00',
    active: true,
    restaurantId: 'rest1',
    createdBy: 'admin1',
    createdAt: '2024-01-15T00:00:00',
  },
];

// ── Cart ─────────────────────────────────────────────────────

export const mockCart: Cart = {
  id: 'cart1',
  customerId: 'cust1',
  restaurantId: 'rest1',
  items: [
    { menuItemId: 'm1', name: 'Margherita Pizza', price: 14.99, quantity: 2 },
    { menuItemId: 'm2', name: 'Spaghetti Carbonara', price: 15.99, quantity: 1 },
  ],
};

export const mockEmptyCart: Cart = {
  id: 'cart2',
  customerId: 'cust1',
  restaurantId: '',
  items: [],
};

// ── Notifications ────────────────────────────────────────────

export const mockNotifications = [
  {
    type: 'ORDER_STATUS_CHANGED',
    orderId: 'order1',
    message: 'Your order is now confirmed',
    timestamp: '2024-02-05T19:15:00',
    read: false,
  },
  {
    type: 'NEW_ORDER',
    orderId: 'order4',
    message: 'New order #4 received',
    timestamp: '2024-02-05T19:00:00',
    read: true,
  },
  {
    type: 'ORDER_ASSIGNED',
    orderId: 'order5',
    message: 'Order #5 picked up by driver',
    timestamp: '2024-02-05T19:30:00',
    read: false,
  },
];

// ── Analytics ────────────────────────────────────────────────

export const mockAnalytics = {
  totalOrders: 25,
  totalRevenue: 1250.50,
  averageOrderValue: 50.02,
  ordersByStatus: {
    DELIVERED: 18,
    PENDING: 3,
    CONFIRMED: 2,
    CANCELLED: 2,
  },
  revenueByDay: [
    { date: '02/01', revenue: 150.0 },
    { date: '02/02', revenue: 200.0 },
    { date: '02/03', revenue: 175.5 },
    { date: '02/04', revenue: 225.0 },
    { date: '02/05', revenue: 500.0 },
  ],
  topItems: [
    { name: 'Margherita Pizza', quantity: 15, revenue: 224.85 },
    { name: 'Spaghetti Carbonara', quantity: 10, revenue: 159.90 },
    { name: 'Tiramisu', quantity: 8, revenue: 79.92 },
  ],
  ordersByHour: {
    10: 2,
    11: 3,
    12: 5,
    13: 4,
    17: 3,
    18: 5,
    19: 2,
    20: 1,
  },
  reviewStats: {
    totalReviews: 10,
    averageRating: 4.3,
    ratingDistribution: {
      1: 0,
      2: 1,
      3: 2,
      4: 3,
      5: 4,
    },
  },
};

// ── Promo Validation Responses ───────────────────────────────

export const mockPromoValid: PromoValidationResponse = {
  valid: true,
  discountAmount: 4.60,
  message: 'Promo code applied! You save $4.60',
};

export const mockPromoInvalid: PromoValidationResponse = {
  valid: false,
  discountAmount: 0,
  message: 'Invalid promo code',
};
