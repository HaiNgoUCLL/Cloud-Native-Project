import React from 'react';
import { render, screen } from '@testing-library/react';
import { OrderStatusStepper } from '@/components/OrderStatusStepper';

describe('OrderStatusStepper', () => {
  const statusLabels = ['Pending', 'Confirmed', 'Preparing', 'On the way', 'Delivered'];

  it('renders all status step labels', () => {
    render(<OrderStatusStepper currentStatus="PENDING" />);
    statusLabels.forEach((label) => {
      expect(screen.getByText(label)).toBeInTheDocument();
    });
  });

  it('highlights correct step for CONFIRMED status', () => {
    render(<OrderStatusStepper currentStatus="CONFIRMED" />);
    // Both Pending and Confirmed should be highlighted (done or active)
    const pendingLabel = screen.getByText('Pending');
    const confirmedLabel = screen.getByText('Confirmed');
    // Completed/active steps get the yellow color
    expect(pendingLabel).toHaveStyle({ color: 'var(--y)' });
    expect(confirmedLabel).toHaveStyle({ color: 'var(--y)' });
    // Future steps should not be yellow
    const preparingLabel = screen.getByText('Preparing');
    expect(preparingLabel).toHaveStyle({ color: 'var(--text3)' });
  });

  it('shows CANCELLED state with red text', () => {
    render(<OrderStatusStepper currentStatus="CANCELLED" />);
    const cancelledText = screen.getByText('ORDER CANCELLED');
    expect(cancelledText).toBeInTheDocument();
    expect(cancelledText).toHaveStyle({ color: '#ef4444' });
    // Regular steps should not be visible
    statusLabels.forEach((label) => {
      expect(screen.queryByText(label)).not.toBeInTheDocument();
    });
  });

  it('marks DELIVERED status with all steps completed', () => {
    render(<OrderStatusStepper currentStatus="DELIVERED" />);
    statusLabels.forEach((label) => {
      expect(screen.getByText(label)).toHaveStyle({ color: 'var(--y)' });
    });
  });
});
