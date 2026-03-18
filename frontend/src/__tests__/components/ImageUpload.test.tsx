import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { ImageUpload } from '@/components/ImageUpload';

// Mock axios
jest.mock('@/lib/axios', () => ({
  __esModule: true,
  default: {
    post: jest.fn(),
  },
}));

// Mock react-hot-toast
jest.mock('react-hot-toast', () => ({
  __esModule: true,
  default: {
    error: jest.fn(),
    success: jest.fn(),
  },
}));

describe('ImageUpload', () => {
  const mockOnUpload = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders upload prompt when no image', () => {
    render(<ImageUpload currentUrl="" onUpload={mockOnUpload} />);
    expect(screen.getByText('Click or drag image here')).toBeInTheDocument();
  });

  it('shows preview image when currentUrl provided', () => {
    render(<ImageUpload currentUrl="https://example.com/image.jpg" onUpload={mockOnUpload} />);
    const img = screen.getByAltText('Preview');
    expect(img).toBeInTheDocument();
    expect(img).toHaveAttribute('src', 'https://example.com/image.jpg');
  });

  it('has hidden file input for image upload', () => {
    const { container } = render(<ImageUpload currentUrl="" onUpload={mockOnUpload} />);
    const fileInput = container.querySelector('input[type="file"]');
    expect(fileInput).toBeInTheDocument();
    expect(fileInput).toHaveAttribute('accept', 'image/*');
    expect(fileInput).toHaveStyle({ display: 'none' });
  });
});
