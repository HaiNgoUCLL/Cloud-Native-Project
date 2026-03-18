'use client';

import { useEffect, useRef, useState, useCallback } from 'react';
import Cookies from 'js-cookie';
import toast from 'react-hot-toast';

export interface NotificationItem {
  type: string;
  orderId: string;
  message: string;
  timestamp: string;
  read: boolean;
}

export function useNotifications(enabled: boolean) {
  const [notifications, setNotifications] = useState<NotificationItem[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const eventSourceRef = useRef<EventSource | null>(null);

  const connect = useCallback(() => {
    if (!enabled) return;
    const token = Cookies.get('token');
    if (!token) return;

    // Close existing connection
    if (eventSourceRef.current) {
      eventSourceRef.current.close();
    }

    const url = `http://localhost:8080/api/notifications/stream?token=${encodeURIComponent(token)}`;
    const es = new EventSource(url);
    eventSourceRef.current = es;

    es.addEventListener('notification', (event) => {
      try {
        const data = JSON.parse(event.data);
        const item: NotificationItem = {
          type: data.type,
          orderId: data.orderId,
          message: data.message,
          timestamp: data.timestamp,
          read: false,
        };
        setNotifications((prev) => [item, ...prev].slice(0, 20));
        setUnreadCount((prev) => prev + 1);
        toast(data.message, { icon: '🔔' });
      } catch { /* ignore parse errors */ }
    });

    es.addEventListener('connected', () => {
      // Connected successfully
    });

    es.onerror = () => {
      es.close();
      // Reconnect after 5 seconds
      setTimeout(connect, 5000);
    };
  }, [enabled]);

  useEffect(() => {
    connect();
    return () => {
      if (eventSourceRef.current) {
        eventSourceRef.current.close();
      }
    };
  }, [connect]);

  const markAllRead = useCallback(() => {
    setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
    setUnreadCount(0);
  }, []);

  return { notifications, unreadCount, markAllRead };
}
