import { renderHook, act } from '@testing-library/react';
import { useNotifications } from '@/lib/useNotifications';

// Mock js-cookie
jest.mock('js-cookie', () => ({
  __esModule: true,
  default: {
    get: jest.fn().mockReturnValue('mock-token'),
  },
}));

// Mock react-hot-toast
jest.mock('react-hot-toast', () => ({
  __esModule: true,
  default: jest.fn(),
}));

// Mock EventSource
class MockEventSource {
  url: string;
  listeners: Record<string, Function> = {};
  onerror: Function | null = null;
  close = jest.fn();

  constructor(url: string) {
    this.url = url;
  }

  addEventListener(event: string, callback: Function) {
    this.listeners[event] = callback;
  }
}

(global as any).EventSource = MockEventSource;

describe('useNotifications', () => {
  it('initializes with empty notifications and zero unread', () => {
    const { result } = renderHook(() => useNotifications(false));

    expect(result.current.notifications).toEqual([]);
    expect(result.current.unreadCount).toBe(0);
  });

  it('markAllRead sets unreadCount to 0', () => {
    const { result } = renderHook(() => useNotifications(false));

    act(() => {
      result.current.markAllRead();
    });

    expect(result.current.unreadCount).toBe(0);
  });

  it('provides markAllRead function', () => {
    const { result } = renderHook(() => useNotifications(false));
    expect(typeof result.current.markAllRead).toBe('function');
  });
});
