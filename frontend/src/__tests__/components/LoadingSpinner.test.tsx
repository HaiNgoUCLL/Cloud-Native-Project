import React from 'react';
import { render, screen } from '@testing-library/react';
import { LoadingSpinner, SkeletonCard, SkeletonMenuItem } from '@/components/LoadingSpinner';

describe('LoadingSpinner', () => {
  it('renders spinner with animation class', () => {
    const { container } = render(<LoadingSpinner />);
    const spinner = container.querySelector('.animate-spin');
    expect(spinner).toBeInTheDocument();
    expect(spinner).toHaveClass('rounded-full');
  });
});

describe('SkeletonCard', () => {
  it('renders placeholder skeleton with pulse animation', () => {
    const { container } = render(<SkeletonCard />);
    const skeleton = container.querySelector('.animate-pulse');
    expect(skeleton).toBeInTheDocument();
  });
});

describe('SkeletonMenuItem', () => {
  it('renders menu item skeleton with pulse animation', () => {
    const { container } = render(<SkeletonMenuItem />);
    const skeleton = container.querySelector('.animate-pulse');
    expect(skeleton).toBeInTheDocument();
  });
});
