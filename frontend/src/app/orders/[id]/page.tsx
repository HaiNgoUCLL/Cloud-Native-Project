'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import toast from 'react-hot-toast';
import { Star, RefreshCw } from 'lucide-react';
import { ProtectedRoute } from '@/components/ProtectedRoute';
import { OrderStatusStepper } from '@/components/OrderStatusStepper';
import { LoadingSpinner } from '@/components/LoadingSpinner';
import api from '@/lib/axios';
import { Order, Review } from '@/types';

export default function OrderDetailPage() {
  return (
    <ProtectedRoute role="CUSTOMER">
      <OrderDetailContent />
    </ProtectedRoute>
  );
}

function OrderDetailContent() {
  const { id } = useParams<{ id: string }>();
  const [order, setOrder] = useState<Order | null>(null);
  const [loading, setLoading] = useState(true);
  const [review, setReview] = useState<Review | null>(null);
  const [showReviewForm, setShowReviewForm] = useState(false);
  const [reviewRating, setReviewRating] = useState(5);
  const [reviewComment, setReviewComment] = useState('');
  const [submittingReview, setSubmittingReview] = useState(false);
  const router = useRouter();

  useEffect(() => {
    fetchOrder();
    const interval = setInterval(fetchOrder, 5000);
    return () => clearInterval(interval);
  }, [id]);

  const fetchOrder = async () => {
    try {
      const res = await api.get(`/api/orders/${id}`);
      setOrder(res.data.data);
      if (res.data.data.status === 'DELIVERED') {
        try {
          const revRes = await api.get('/api/reviews/my');
          const existing = revRes.data.data.find((r: Review) => r.orderId === id);
          if (existing) setReview(existing);
        } catch { /* ignore */ }
      }
    } catch {
      toast.error('Failed to load order');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmitReview = async () => {
    setSubmittingReview(true);
    try {
      const res = await api.post('/api/reviews', {
        orderId: id,
        rating: reviewRating,
        comment: reviewComment,
      });
      setReview(res.data.data);
      setShowReviewForm(false);
      toast.success('Review submitted!');
    } catch (err: unknown) {
      const error = err as { response?: { data?: { message?: string } } };
      toast.error(error.response?.data?.message || 'Failed to submit review');
    } finally {
      setSubmittingReview(false);
    }
  };

  const handleCancel = async () => {
    try {
      await api.put(`/api/orders/${id}/cancel`);
      toast.success('Order cancelled');
      fetchOrder();
    } catch (err: unknown) {
      const error = err as { response?: { data?: { message?: string } } };
      toast.error(error.response?.data?.message || 'Cannot cancel order');
    }
  };

  if (loading) return <div className="py-8"><LoadingSpinner /></div>;
  if (!order) return <div className="text-center py-16"><p style={{ fontSize: '16px', fontWeight: 700 }}>Order not found</p></div>;

  return (
    <div className="max-w-3xl mx-auto px-4 py-8" style={{ background: 'var(--bg)', minHeight: '100vh' }}>
      <h1 style={{ fontSize: '22px', fontWeight: 700, color: 'var(--text)', marginBottom: '4px' }}>
        Order #{order.id.slice(-6)}
      </h1>
      <p style={{ fontSize: '13px', fontWeight: 500, color: 'var(--text3)', marginBottom: '24px' }}>
        Placed on {new Date(order.createdAt).toLocaleString()}
      </p>

      <div className="p-6 rounded-2xl mb-6" style={{ background: 'var(--bg2)', border: '1.5px solid var(--border2)' }}>
        <h2 style={{ fontSize: '16px', fontWeight: 700, color: 'var(--text)', marginBottom: '12px' }}>Order Status</h2>
        <OrderStatusStepper currentStatus={order.status} />
      </div>

      <div className="p-6 rounded-2xl mb-6" style={{ background: 'var(--bg2)', border: '1.5px solid var(--border2)' }}>
        <h2 style={{ fontSize: '16px', fontWeight: 700, color: 'var(--text)', marginBottom: '12px' }}>Items</h2>
        {order.items.map((item, i) => (
          <div key={i} className="flex justify-between py-2" style={{ borderBottom: i < order.items.length - 1 ? '1px solid var(--border2)' : 'none' }}>
            <div>
              <span style={{ fontSize: '14px', fontWeight: 700, color: 'var(--text)' }}>{item.name}</span>
              <span style={{ fontSize: '13px', fontWeight: 500, color: 'var(--text3)', marginLeft: '8px' }}>x{item.quantity}</span>
            </div>
            <span style={{ fontSize: '14px', fontWeight: 700, color: 'var(--text)' }}>${(item.price * item.quantity).toFixed(2)}</span>
          </div>
        ))}
        <div className="flex justify-between pt-4 mt-4" style={{ borderTop: '1.5px solid var(--border2)' }}>
          <span style={{ fontSize: '18px', fontWeight: 700, color: 'var(--text)' }}>Total</span>
          <span className="gradient-text" style={{ fontSize: '18px', fontWeight: 700 }}>${order.totalAmount.toFixed(2)}</span>
        </div>
      </div>

      <div className="p-6 rounded-2xl mb-6" style={{ background: 'var(--bg2)', border: '1.5px solid var(--border2)' }}>
        <h2 style={{ fontSize: '16px', fontWeight: 700, color: 'var(--text)', marginBottom: '8px' }}>Details</h2>
        <div className="space-y-2">
          <div className="flex justify-between">
            <span style={{ fontSize: '13px', fontWeight: 500, color: 'var(--text2)' }}>Delivery Address</span>
            <span style={{ fontSize: '13px', fontWeight: 700, color: 'var(--text)' }}>{order.deliveryAddress}</span>
          </div>
          <div className="flex justify-between">
            <span style={{ fontSize: '13px', fontWeight: 500, color: 'var(--text2)' }}>Payment Method</span>
            <span style={{ fontSize: '13px', fontWeight: 700, color: 'var(--text)' }}>{order.paymentMethod.replace('_', ' ')}</span>
          </div>
          <div className="flex justify-between">
            <span style={{ fontSize: '13px', fontWeight: 500, color: 'var(--text2)' }}>Payment Status</span>
            <span style={{
              fontSize: '13px',
              fontWeight: 700,
              color: order.paymentStatus === 'SIMULATED_PAID' ? '#22c55e' : 'var(--y)',
            }}>
              {order.paymentStatus.replace('_', ' ')}
            </span>
          </div>
        </div>
      </div>

      <div className="flex gap-3">
        {order.status === 'PENDING' && (
          <button
            onClick={handleCancel}
            style={{
              padding: '12px 24px',
              borderRadius: '30px',
              background: 'rgba(239,68,68,0.1)',
              color: '#ef4444',
              fontWeight: 700,
              fontSize: '14px',
              border: '1.5px solid rgba(239,68,68,0.3)',
              cursor: 'pointer',
            }}
          >
            Cancel Order
          </button>
        )}
        {order.paymentStatus === 'PENDING' && order.status !== 'CANCELLED' && (
          <button
            onClick={() => router.push(`/checkout/payment?orderId=${order.id}`)}
            className="gradient-bg"
            style={{
              padding: '12px 24px',
              borderRadius: '30px',
              color: '#000',
              fontWeight: 700,
              fontSize: '14px',
              border: 'none',
              cursor: 'pointer',
            }}
          >
            Pay Now
          </button>
        )}
        {order.status === 'DELIVERED' && (
          <button
            onClick={async () => {
              try {
                await api.post(`/api/orders/${id}/reorder`);
                toast.success('Items added to cart!');
                router.push('/cart');
              } catch (err: unknown) {
                const error = err as { response?: { data?: { message?: string } } };
                toast.error(error.response?.data?.message || 'Failed to reorder');
              }
            }}
            style={{
              padding: '12px 24px',
              borderRadius: '30px',
              background: 'var(--bg3)',
              color: 'var(--y)',
              fontWeight: 700,
              fontSize: '14px',
              border: '1.5px solid var(--border2)',
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
            }}
          >
            <RefreshCw size={16} /> Reorder
          </button>
        )}
      </div>

      {order.status === 'DELIVERED' && (
        <div className="p-6 rounded-2xl mt-6" style={{ background: 'var(--bg2)', border: '1.5px solid var(--border2)' }}>
          <h2 style={{ fontSize: '16px', fontWeight: 700, color: 'var(--text)', marginBottom: '12px' }}>
            {review ? 'Your Review' : 'Rate Your Order'}
          </h2>
          {review ? (
            <div>
              <div className="flex gap-1 mb-2">
                {[1, 2, 3, 4, 5].map((s) => (
                  <Star key={s} size={18} fill={s <= review.rating ? 'var(--y)' : 'transparent'} color="var(--y)" />
                ))}
              </div>
              {review.comment && (
                <p style={{ fontSize: '13px', fontWeight: 500, color: 'var(--text2)' }}>{review.comment}</p>
              )}
              <p style={{ fontSize: '12px', fontWeight: 500, color: 'var(--text3)', marginTop: '8px' }}>
                Reviewed on {new Date(review.createdAt).toLocaleDateString()}
              </p>
            </div>
          ) : showReviewForm ? (
            <div>
              <div className="flex gap-2 mb-4">
                {[1, 2, 3, 4, 5].map((s) => (
                  <button
                    key={s}
                    onClick={() => setReviewRating(s)}
                    style={{ background: 'transparent', border: 'none', cursor: 'pointer', padding: '2px' }}
                  >
                    <Star size={28} fill={s <= reviewRating ? 'var(--y)' : 'transparent'} color="var(--y)" />
                  </button>
                ))}
              </div>
              <textarea
                value={reviewComment}
                onChange={(e) => setReviewComment(e.target.value)}
                placeholder="Tell us about your experience..."
                rows={3}
                style={{
                  width: '100%',
                  padding: '12px',
                  borderRadius: '12px',
                  background: 'var(--bg3)',
                  color: 'var(--text)',
                  border: '1.5px solid var(--border2)',
                  fontSize: '13px',
                  fontWeight: 500,
                  resize: 'vertical',
                  marginBottom: '12px',
                  fontFamily: 'inherit',
                }}
              />
              <div className="flex gap-3">
                <button
                  onClick={() => setShowReviewForm(false)}
                  style={{
                    padding: '10px 20px',
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
                  onClick={handleSubmitReview}
                  disabled={submittingReview}
                  className="gradient-bg"
                  style={{
                    padding: '10px 20px',
                    borderRadius: '12px',
                    color: '#000',
                    fontWeight: 700,
                    fontSize: '13px',
                    border: 'none',
                    cursor: submittingReview ? 'not-allowed' : 'pointer',
                    opacity: submittingReview ? 0.6 : 1,
                  }}
                >
                  {submittingReview ? 'Submitting...' : 'Submit Review'}
                </button>
              </div>
            </div>
          ) : (
            <button
              onClick={() => setShowReviewForm(true)}
              className="gradient-bg"
              style={{
                padding: '12px 24px',
                borderRadius: '30px',
                color: '#000',
                fontWeight: 700,
                fontSize: '14px',
                border: 'none',
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
                gap: '8px',
              }}
            >
              <Star size={16} /> Leave a Review
            </button>
          )}
        </div>
      )}
    </div>
  );
}
