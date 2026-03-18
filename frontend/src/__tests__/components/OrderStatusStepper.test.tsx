import React from 'react';
import { render, screen } from '@testing-library/react';
import { OrderStatusStepper } from '@/components/OrderStatusStepper';

describe('OrderStatusStepper', () => {
  const statusLabels = ['Pending', 'Confirmed', 'Preparing', 'Ready', 'Picked Up', 'Arrived', 'Delivered'];

  it('renders all status step labels', () => {
    render(<OrderStatusStepper currentStatus="PENDING" />);
    statusLabels.forEach((label) => {
      expect(screen.getByText(label)).toBeInTheDocument();
    });
  });

  it('highlights correct step for CONFIRMED status', () => {
    render(<OrderStatusStepper currentStatus="CONFIRMED" />);
    expect(screen.getByText('Pending')).toHaveStyle({ color: 'var(--y)' });
    expect(screen.getByText('Confirmed')).toHaveStyle({ color: 'var(--y)' });
    expect(screen.getByText('Preparing')).toHaveStyle({ color: 'var(--text3)' });
  });

  it('shows CANCELLED state with red text', () => {
    render(<OrderStatusStepper currentStatus="CANCELLED" />);
    const cancelledText = screen.getByText('ORDER CANCELLED');
    expect(cancelledText).toBeInTheDocument();
    expect(cancelledText).toHaveStyle({ color: '#ef4444' });
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

  it('highlights READY step correctly', () => {
    render(<OrderStatusStepper currentStatus="READY" />);
    expect(screen.getByText('Pending')).toHaveStyle({ color: 'var(--y)' });
    expect(screen.getByText('Confirmed')).toHaveStyle({ color: 'var(--y)' });
    expect(screen.getByText('Preparing')).toHaveStyle({ color: 'var(--y)' });
    expect(screen.getByText('Ready')).toHaveStyle({ color: 'var(--y)' });
    expect(screen.getByText('Picked Up')).toHaveStyle({ color: 'var(--text3)' });
  });

  it('highlights PICKED_UP step correctly', () => {
    render(<OrderStatusStepper currentStatus="PICKED_UP" />);
    expect(screen.getByText('Ready')).toHaveStyle({ color: 'var(--y)' });
    expect(screen.getByText('Picked Up')).toHaveStyle({ color: 'var(--y)' });
    expect(screen.getByText('Arrived')).toHaveStyle({ color: 'var(--text3)' });
  });

  it('highlights ARRIVED step correctly', () => {
    render(<OrderStatusStepper currentStatus="ARRIVED" />);
    expect(screen.getByText('Picked Up')).toHaveStyle({ color: 'var(--y)' });
    expect(screen.getByText('Arrived')).toHaveStyle({ color: 'var(--y)' });
    expect(screen.getByText('Delivered')).toHaveStyle({ color: 'var(--text3)' });
  });
});
