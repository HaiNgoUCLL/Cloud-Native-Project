'use client';

import { useEffect, useState } from 'react';
import { useParams } from 'next/navigation';
import { Star, MapPin, Plus, MessageSquare } from 'lucide-react';
import toast from 'react-hot-toast';
import api from '@/lib/axios';
import { Restaurant, MenuItem, Review } from '@/types';
import { useAuth } from '@/context/AuthContext';
import { useCartStore } from '@/lib/cartStore';
import { LoadingSpinner, SkeletonMenuItem } from '@/components/LoadingSpinner';

export default function RestaurantPage() {
  const { id } = useParams<{ id: string }>();
  const { user } = useAuth();
  const { cart, addToCart, fetchCart } = useCartStore();
  const [restaurant, setRestaurant] = useState<Restaurant | null>(null);
  const [menuItems, setMenuItems] = useState<MenuItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [activeCategory, setActiveCategory] = useState<string>('');
  const [reviews, setReviews] = useState<Review[]>([]);
  const [activeTab, setActiveTab] = useState<'menu' | 'reviews'>('menu');
  const [showConfirm, setShowConfirm] = useState(false);
  const [pendingItem, setPendingItem] = useState<string | null>(null);

  useEffect(() => {
    fetchData();
    if (user) fetchCart();
  }, [id]);

  const fetchData = async () => {
    setLoading(true);
    try {
      const [resR, resM] = await Promise.all([
        api.get(`/api/restaurants/${id}`),
        api.get(`/api/restaurants/${id}/menu`),
      ]);
      setRestaurant(resR.data.data);
      setMenuItems(resM.data.data);
      const categories = Array.from(new Set(resM.data.data.map((item: MenuItem) => item.category))) as string[];
      if (categories.length > 0) setActiveCategory(categories[0] as string);
      try {
        const resReviews = await api.get(`/api/reviews/restaurant/${id}`);
        setReviews(resReviews.data.data);
      } catch { /* reviews are optional */ }
    } catch {
      toast.error('Failed to load restaurant');
    } finally {
      setLoading(false);
    }
  };

  const categories = Array.from(new Set(menuItems.map((item) => item.category)));
  const filteredItems = menuItems.filter((item) => item.category === activeCategory);

  const handleAddToCart = async (menuItemId: string) => {
    if (!user) {
      toast.error('Please login to add items to cart');
      return;
    }
    if (user.role !== 'CUSTOMER') {
      toast.error('Only customers can add items to cart');
      return;
    }

    if (cart && cart.restaurantId && cart.restaurantId !== id && cart.items.length > 0) {
      setPendingItem(menuItemId);
      setShowConfirm(true);
      return;
    }

    try {
      await addToCart(id, menuItemId, 1);
      toast.success('Added to cart!');
    } catch {
      toast.error('Failed to add to cart');
    }
  };

  const confirmSwitchRestaurant = async () => {
    if (pendingItem) {
      try {
        await addToCart(id, pendingItem, 1);
        toast.success('Cart updated with new restaurant!');
      } catch {
        toast.error('Failed to add to cart');
      }
    }
    setShowConfirm(false);
    setPendingItem(null);
  };

  if (loading) {
    return (
      <div className="max-w-5xl mx-auto px-4 py-8">
        <LoadingSpinner />
        <div className="space-y-4 mt-8">
          {[1, 2, 3, 4].map((i) => <SkeletonMenuItem key={i} />)}
        </div>
      </div>
    );
  }

  if (!restaurant) {
    return (
      <div className="text-center py-16">
        <div style={{ fontSize: '48px', marginBottom: '12px' }}>🏪</div>
        <p style={{ fontSize: '16px', fontWeight: 700 }}>Restaurant not found</p>
      </div>
    );
  }

  return (
    <div style={{ background: 'var(--bg)', minHeight: '100vh' }}>
      <div className="relative" style={{ height: '250px', overflow: 'hidden' }}>
        {restaurant.imageUrl && (
          <img src={restaurant.imageUrl} alt={restaurant.name} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
        )}
        <div style={{ position: 'absolute', inset: 0, background: 'linear-gradient(transparent, var(--bg))' }} />
      </div>

      <div className="max-w-5xl mx-auto px-4 -mt-16 relative z-10">
        <div className="mb-8">
          <h1 style={{ fontSize: '22px', fontWeight: 700, color: 'var(--text)' }}>{restaurant.name}</h1>
          <p style={{ fontSize: '14px', fontWeight: 500, color: 'var(--text2)', marginTop: '4px' }}>{restaurant.description}</p>
          <div className="flex items-center gap-4 mt-3">
            <div className="flex items-center gap-1">
              <Star size={14} fill="var(--y)" color="var(--y)" />
              <span style={{ fontSize: '13px', fontWeight: 600, color: 'var(--text)' }}>{restaurant.rating}</span>
              {reviews.length > 0 && (
                <span style={{ fontSize: '12px', fontWeight: 500, color: 'var(--text3)' }}>({reviews.length} reviews)</span>
              )}
            </div>
            <div className="flex items-center gap-1">
              <MapPin size={14} color="var(--text3)" />
              <span style={{ fontSize: '13px', fontWeight: 500, color: 'var(--text3)' }}>{restaurant.address}</span>
            </div>
            <span style={{ fontSize: '13px', fontWeight: 500, color: 'var(--text2)' }}>{restaurant.cuisineType}</span>
          </div>
        </div>

        <div className="flex gap-6 mb-6" style={{ borderBottom: '1.5px solid var(--border2)' }}>
          <button
            onClick={() => setActiveTab('menu')}
            style={{
              padding: '8px 0',
              fontSize: '14px',
              fontWeight: 700,
              color: activeTab === 'menu' ? 'var(--y)' : 'var(--text3)',
              background: 'transparent',
              border: 'none',
              borderBottom: `2.5px solid ${activeTab === 'menu' ? 'var(--y)' : 'transparent'}`,
              cursor: 'pointer',
            }}
          >
            Menu
          </button>
          <button
            onClick={() => setActiveTab('reviews')}
            style={{
              padding: '8px 0',
              fontSize: '14px',
              fontWeight: 700,
              color: activeTab === 'reviews' ? 'var(--y)' : 'var(--text3)',
              background: 'transparent',
              border: 'none',
              borderBottom: `2.5px solid ${activeTab === 'reviews' ? 'var(--y)' : 'transparent'}`,
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              gap: '6px',
            }}
          >
            <MessageSquare size={14} /> Reviews {reviews.length > 0 && `(${reviews.length})`}
          </button>
        </div>

        {activeTab === 'menu' && (
          <>
            <div className="flex gap-4 mb-6 overflow-x-auto pb-2" style={{ borderBottom: '1.5px solid var(--border2)' }}>
              {categories.map((cat) => (
                <button
                  key={cat}
                  onClick={() => setActiveCategory(cat)}
                  style={{
                    padding: '8px 0',
                    fontSize: '13px',
                    fontWeight: 600,
                    textTransform: 'uppercase',
                    color: activeCategory === cat ? 'var(--y)' : 'var(--text3)',
                    background: 'transparent',
                    border: 'none',
                    borderBottom: `2.5px solid ${activeCategory === cat ? 'var(--y)' : 'transparent'}`,
                    cursor: 'pointer',
                    whiteSpace: 'nowrap',
                    transition: 'all 0.2s',
                  }}
                >
                  {cat}
                </button>
              ))}
            </div>

            <div className="space-y-4 pb-8">
              {filteredItems.map((item) => (
                <div
                  key={item.id}
                  className="flex items-center justify-between p-4"
                  style={{
                    background: 'var(--bg2)',
                    borderRadius: '16px',
                    border: '1.5px solid var(--border2)',
                    transition: 'all 0.2s',
                  }}
                >
                  {item.imageUrl && (
                    <img
                      src={item.imageUrl.startsWith('http') ? item.imageUrl : `http://localhost:8080${item.imageUrl}`}
                      alt={item.name}
                      style={{ width: '80px', height: '80px', borderRadius: '12px', objectFit: 'cover', marginRight: '12px', flexShrink: 0 }}
                    />
                  )}
                  <div className="flex-1">
                    <h3 style={{ fontSize: '15px', fontWeight: 700, color: 'var(--text)' }}>{item.name}</h3>
                    <p style={{ fontSize: '13px', fontWeight: 500, color: 'var(--text2)', marginTop: '4px' }}>{item.description}</p>
                    <p style={{ fontSize: '15px', fontWeight: 700, color: 'var(--y)', marginTop: '8px' }}>${item.price.toFixed(2)}</p>
                  </div>
                  <button
                    onClick={() => handleAddToCart(item.id)}
                    style={{
                      padding: '10px 20px',
                      borderRadius: '22px',
                      background: 'var(--y)',
                      color: '#000000',
                      fontWeight: 700,
                      fontSize: '13px',
                      border: 'none',
                      cursor: 'pointer',
                      display: 'flex',
                      alignItems: 'center',
                      gap: '6px',
                      transition: 'all 0.2s',
                    }}
                  >
                    <Plus size={16} /> Add
                  </button>
                </div>
              ))}
            </div>
          </>
        )}

        {activeTab === 'reviews' && (
          <div className="space-y-4 pb-8">
            {reviews.length === 0 ? (
              <div className="text-center py-12">
                <MessageSquare size={40} color="var(--text3)" style={{ margin: '0 auto 12px' }} />
                <p style={{ fontSize: '15px', fontWeight: 700, color: 'var(--text2)' }}>No reviews yet</p>
                <p style={{ fontSize: '13px', fontWeight: 500, color: 'var(--text3)', marginTop: '4px' }}>Be the first to review this restaurant!</p>
              </div>
            ) : (
              <>
                <div className="p-4 rounded-2xl mb-2" style={{ background: 'var(--bg2)', border: '1.5px solid var(--border2)' }}>
                  <div className="flex items-center gap-3">
                    <span className="gradient-text" style={{ fontSize: '32px', fontWeight: 700 }}>{restaurant.rating}</span>
                    <div>
                      <div className="flex gap-1">
                        {[1, 2, 3, 4, 5].map((s) => (
                          <Star key={s} size={16} fill={s <= Math.round(restaurant.rating) ? 'var(--y)' : 'transparent'} color="var(--y)" />
                        ))}
                      </div>
                      <p style={{ fontSize: '12px', fontWeight: 500, color: 'var(--text3)', marginTop: '2px' }}>{reviews.length} reviews</p>
                    </div>
                  </div>
                </div>
                {reviews.map((review) => (
                  <div key={review.id} className="p-4 rounded-2xl" style={{ background: 'var(--bg2)', border: '1.5px solid var(--border2)' }}>
                    <div className="flex items-center justify-between mb-2">
                      <div className="flex items-center gap-2">
                        <div style={{
                          width: '32px', height: '32px', borderRadius: '50%',
                          background: 'var(--y)', display: 'flex', alignItems: 'center', justifyContent: 'center',
                          fontWeight: 700, fontSize: '14px', color: '#000',
                        }}>
                          {review.customerName.charAt(0).toUpperCase()}
                        </div>
                        <span style={{ fontSize: '14px', fontWeight: 700, color: 'var(--text)' }}>{review.customerName}</span>
                      </div>
                      <span style={{ fontSize: '12px', fontWeight: 500, color: 'var(--text3)' }}>
                        {new Date(review.createdAt).toLocaleDateString()}
                      </span>
                    </div>
                    <div className="flex gap-1 mb-2">
                      {[1, 2, 3, 4, 5].map((s) => (
                        <Star key={s} size={14} fill={s <= review.rating ? 'var(--y)' : 'transparent'} color="var(--y)" />
                      ))}
                    </div>
                    {review.comment && (
                      <p style={{ fontSize: '13px', fontWeight: 500, color: 'var(--text2)' }}>{review.comment}</p>
                    )}
                  </div>
                ))}
              </>
            )}
          </div>
        )}
      </div>

      {showConfirm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center" style={{ background: 'rgba(0,0,0,0.7)' }}>
          <div className="p-6 rounded-2xl max-w-sm w-full mx-4" style={{ background: 'var(--bg2)', border: '1.5px solid var(--border2)' }}>
            <h3 style={{ fontSize: '16px', fontWeight: 700, color: 'var(--text)', marginBottom: '8px' }}>Switch Restaurant?</h3>
            <p style={{ fontSize: '13px', fontWeight: 500, color: 'var(--text2)', marginBottom: '20px' }}>
              Your cart contains items from a different restaurant. Adding this item will clear your current cart.
            </p>
            <div className="flex gap-3">
              <button
                onClick={() => { setShowConfirm(false); setPendingItem(null); }}
                style={{
                  flex: 1,
                  padding: '12px',
                  borderRadius: '12px',
                  background: 'var(--bg3)',
                  color: 'var(--text)',
                  fontWeight: 700,
                  fontSize: '13px',
                  border: '1.5px solid var(--border2)',
                  cursor: 'pointer',
                }}
              >
                Cancel
              </button>
              <button
                onClick={confirmSwitchRestaurant}
                className="gradient-bg"
                style={{
                  flex: 1,
                  padding: '12px',
                  borderRadius: '12px',
                  color: '#000000',
                  fontWeight: 700,
                  fontSize: '13px',
                  border: 'none',
                  cursor: 'pointer',
                }}
              >
                Switch
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
