'use client';

export function LoadingSpinner() {
  return (
    <div className="flex items-center justify-center p-8">
      <div
        className="animate-spin rounded-full"
        style={{
          width: '40px',
          height: '40px',
          border: '3px solid var(--border2)',
          borderTopColor: 'var(--y)',
        }}
      />
    </div>
  );
}

export function SkeletonCard() {
  return (
    <div
      className="animate-pulse rounded-2xl"
      style={{
        background: 'var(--bg2)',
        border: '1.5px solid var(--border2)',
        borderRadius: '16px',
        height: '320px',
      }}
    >
      <div
        style={{
          height: '180px',
          background: 'var(--bg3)',
          borderRadius: '16px 16px 0 0',
        }}
      />
      <div className="p-4 space-y-3">
        <div style={{ height: '16px', width: '70%', background: 'var(--bg3)', borderRadius: '8px' }} />
        <div style={{ height: '13px', width: '50%', background: 'var(--bg3)', borderRadius: '8px' }} />
        <div style={{ height: '13px', width: '30%', background: 'var(--bg3)', borderRadius: '8px' }} />
      </div>
    </div>
  );
}

export function SkeletonMenuItem() {
  return (
    <div
      className="animate-pulse flex items-center gap-4 p-4 rounded-2xl"
      style={{
        background: 'var(--bg2)',
        border: '1.5px solid var(--border2)',
        borderRadius: '16px',
      }}
    >
      <div style={{ width: '80px', height: '80px', background: 'var(--bg3)', borderRadius: '12px' }} />
      <div className="flex-1 space-y-2">
        <div style={{ height: '15px', width: '60%', background: 'var(--bg3)', borderRadius: '8px' }} />
        <div style={{ height: '13px', width: '80%', background: 'var(--bg3)', borderRadius: '8px' }} />
        <div style={{ height: '15px', width: '20%', background: 'var(--bg3)', borderRadius: '8px' }} />
      </div>
    </div>
  );
}
