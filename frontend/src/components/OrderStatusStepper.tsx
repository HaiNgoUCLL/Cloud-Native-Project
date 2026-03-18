'use client';

import { Check } from 'lucide-react';
import { OrderStatus } from '@/types';

const STEPS: { status: OrderStatus; label: string }[] = [
  { status: 'PENDING', label: 'Pending' },
  { status: 'CONFIRMED', label: 'Confirmed' },
  { status: 'PREPARING', label: 'Preparing' },
  { status: 'OUT_FOR_DELIVERY', label: 'On the way' },
  { status: 'DELIVERED', label: 'Delivered' },
];

interface Props {
  currentStatus: OrderStatus;
}

export function OrderStatusStepper({ currentStatus }: Props) {
  if (currentStatus === 'CANCELLED') {
    return (
      <div className="flex items-center justify-center py-4">
        <span
          style={{
            fontSize: '14px',
            fontWeight: 700,
            color: '#ef4444',
            background: 'rgba(239,68,68,0.1)',
            padding: '8px 20px',
            borderRadius: '30px',
            border: '1.5px solid rgba(239,68,68,0.3)',
          }}
        >
          ORDER CANCELLED
        </span>
      </div>
    );
  }

  const currentIndex = STEPS.findIndex((s) => s.status === currentStatus);

  return (
    <div className="flex items-center w-full py-4">
      {STEPS.map((step, i) => {
        const isDone = i <= currentIndex;
        const isActive = i === currentIndex;

        return (
          <div key={step.status} className="flex items-center flex-1 last:flex-none">
            <div className="flex flex-col items-center">
              <div
                className="flex items-center justify-center"
                style={{
                  width: '22px',
                  height: '22px',
                  borderRadius: '50%',
                  background: isDone
                    ? 'var(--gradient)'
                    : isActive
                    ? 'var(--glow)'
                    : 'var(--bg3)',
                  border: isActive
                    ? '2px solid var(--y)'
                    : isDone
                    ? 'none'
                    : '1.5px solid var(--border2)',
                  transition: 'all 0.2s',
                }}
              >
                {isDone && !isActive ? (
                  <Check size={12} color="#000000" strokeWidth={3} />
                ) : isActive ? (
                  <div
                    style={{
                      width: '6px',
                      height: '6px',
                      borderRadius: '50%',
                      background: 'var(--y)',
                    }}
                  />
                ) : null}
              </div>
              <span
                style={{
                  fontSize: '10px',
                  fontWeight: 600,
                  color: isDone ? 'var(--y)' : 'var(--text3)',
                  marginTop: '4px',
                  whiteSpace: 'nowrap',
                }}
              >
                {step.label}
              </span>
            </div>
            {i < STEPS.length - 1 && (
              <div
                className="flex-1 mx-1"
                style={{
                  height: '2px',
                  background: i < currentIndex ? 'var(--gradient)' : 'var(--border2)',
                  marginBottom: '18px',
                  transition: 'all 0.2s',
                }}
              />
            )}
          </div>
        );
      })}
    </div>
  );
}
