'use client';

import { Suspense, useEffect, useState } from 'react';
import { useSearchParams } from 'next/navigation';
import Link from 'next/link';
import { Star, MapPin } from 'lucide-react';
import api from '@/lib/axios';
import { Restaurant } from '@/types';
import { SkeletonCard, LoadingSpinner } from '@/components/LoadingSpinner';

const CUISINES = ['All', 'Italian', 'Japanese', 'American', 'Mexican', 'Indian', 'Thai', 'French', 'Mediterranean', 'Brazilian', 'Korean', 'Vegan'];

export default function HomePage() {
  return (
    <Suspense fallback={<LoadingSpinner />}>
      <HomeContent />
    </Suspense>
  );
}

function HomeContent() {
  const searchParams = useSearchParams();
  const searchQuery = searchParams.get('search') || '';
  const [restaurants, setRestaurants] = useState<Restaurant[]>([]);
  const [loading, setLoading] = useState(true);
  const [activeCuisine, setActiveCuisine] = useState('All');

  useEffect(() => {
    fetchRestaurants();
  }, [searchQuery, activeCuisine]);

  const fetchRestaurants = async () => {
    setLoading(true);
    try {
      const params: Record<string, string> = {};
      if (searchQuery) params.search = searchQuery;
      if (activeCuisine !== 'All') params.cuisine = activeCuisine;
      const res = await api.get('/api/restaurants', { params });
      setRestaurants(res.data.data);
    } catch {
      setRestaurants([]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ background: 'var(--bg)', minHeight: '100vh' }}>
      <section className="relative overflow-hidden" style={{ padding: '60px 0 40px' }}>
        <div
          className="absolute"
          style={{
            width: '400px',
            height: '400px',
            borderRadius: '50%',
            background: 'radial-gradient(circle, var(--glow), transparent 70%)',
            top: '-100px',
            left: '10%',
            filter: 'blur(60px)',
          }}
        />
        <div
          className="absolute"
          style={{
            width: '300px',
            height: '300px',
            borderRadius: '50%',
            background: 'radial-gradient(circle, rgba(255,149,0,0.08), transparent 70%)',
            bottom: '-50px',
            right: '15%',
            filter: 'blur(40px)',
          }}
        />
        <div className="max-w-7xl mx-auto px-4 text-center relative z-10">
          <span
            style={{
              fontSize: '12px',
              fontWeight: 600,
              letterSpacing: '4px',
              textTransform: 'uppercase',
              background: 'var(--gradient)',
              WebkitBackgroundClip: 'text',
              WebkitTextFillColor: 'transparent',
            }}
          >
            Food Delivery Platform
          </span>
          <h1 style={{ fontSize: '32px', fontWeight: 400, color: 'var(--text)', marginTop: '12px' }}>
            Discover <strong className="gradient-text" style={{ fontWeight: 700 }}>Delicious Food</strong> Near You
          </h1>
          <p style={{ fontSize: '15px', fontWeight: 400, color: 'var(--text2)', marginTop: '12px', maxWidth: '500px', marginLeft: 'auto', marginRight: 'auto' }}>
            Order from the best local restaurants with easy delivery to your doorstep
          </p>
        </div>
      </section>

      <section className="max-w-7xl mx-auto px-4 pb-8">
        <div className="flex flex-wrap gap-2 mb-8">
          {CUISINES.map((c) => (
            <button
              key={c}
              onClick={() => setActiveCuisine(c)}
              style={{
                padding: '8px 18px',
                borderRadius: '30px',
                fontSize: '13px',
                fontWeight: activeCuisine === c ? 700 : 500,
                color: activeCuisine === c ? '#000000' : 'var(--text2)',
                background: activeCuisine === c ? 'var(--y)' : 'var(--bg3)',
                border: activeCuisine === c ? 'none' : '1.5px solid var(--border2)',
                cursor: 'pointer',
                transition: 'all 0.2s',
              }}
            >
              {c}
            </button>
          ))}
        </div>

        {loading ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {[1, 2, 3, 4, 5, 6].map((i) => (
              <SkeletonCard key={i} />
            ))}
          </div>
        ) : restaurants.length === 0 ? (
          <div className="text-center py-16">
            <div style={{ fontSize: '48px', marginBottom: '12px' }}>🍽️</div>
            <p style={{ fontSize: '16px', fontWeight: 700, color: 'var(--text)' }}>No restaurants found</p>
            <p style={{ fontSize: '13px', fontWeight: 500, color: 'var(--text3)', marginTop: '4px' }}>Try a different search or cuisine filter</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {restaurants.map((r) => (
              <Link
                key={r.id}
                href={`/restaurants/${r.id}`}
                className="block card-shadow"
                style={{
                  borderRadius: '16px',
                  border: '1.5px solid var(--border2)',
                  overflow: 'hidden',
                  transition: 'all 0.2s',
                  background: 'var(--bg2)',
                }}
                onMouseEnter={(e) => {
                  e.currentTarget.style.borderColor = 'var(--y)';
                  e.currentTarget.style.transform = 'translateY(-4px)';
                }}
                onMouseLeave={(e) => {
                  e.currentTarget.style.borderColor = 'var(--border2)';
                  e.currentTarget.style.transform = 'translateY(0)';
                }}
              >
                <div style={{ height: '180px', background: 'var(--bg3)', overflow: 'hidden', position: 'relative' }}>
                  {r.imageUrl && (
                    <img
                      src={r.imageUrl}
                      alt={r.name}
                      style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                    />
                  )}
                  <span
                    style={{
                      position: 'absolute',
                      top: '12px',
                      right: '12px',
                      padding: '4px 12px',
                      borderRadius: '12px',
                      fontSize: '12px',
                      fontWeight: 700,
                      color: (r.isOpen || r.open) ? 'var(--y)' : 'var(--text3)',
                      background: (r.isOpen || r.open) ? 'var(--glow)' : 'var(--bg3)',
                      border: `1.5px solid ${(r.isOpen || r.open) ? 'var(--border)' : 'var(--border2)'}`,
                    }}
                  >
                    {(r.isOpen || r.open) ? 'Open' : 'Closed'}
                  </span>
                </div>
                <div className="p-4">
                  <h3 style={{ fontSize: '16px', fontWeight: 700, color: 'var(--text)' }}>{r.name}</h3>
                  <p style={{ fontSize: '13px', fontWeight: 500, color: 'var(--text2)', marginTop: '4px' }}>{r.cuisineType}</p>
                  <div className="flex items-center gap-2 mt-2">
                    <div className="flex items-center gap-1">
                      <Star size={13} fill="var(--y)" color="var(--y)" />
                      <span style={{ fontSize: '13px', fontWeight: 600, color: 'var(--text)' }}>{r.rating}</span>
                    </div>
                    <span style={{ color: 'var(--text3)' }}>•</span>
                    <div className="flex items-center gap-1">
                      <MapPin size={13} color="var(--text3)" />
                      <span style={{ fontSize: '13px', fontWeight: 500, color: 'var(--text3)' }}>{r.address}</span>
                    </div>
                  </div>
                </div>
              </Link>
            ))}
          </div>
        )}
      </section>
    </div>
  );
}
