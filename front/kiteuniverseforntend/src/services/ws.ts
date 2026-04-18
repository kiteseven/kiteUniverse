/**
 * WebSocket service — STOMP over native WebSocket.
 * Loaded via CDN as window.StompJs (bundles/stomp.umd.min.js).
 */

import { API_BASE_URL } from './api';

declare const StompJs: any;

type NotificationCallback = (unreadCount: number) => void;
type MessageCallback = (msg: Record<string, unknown>) => void;
type SystemCallback = (payload: Record<string, unknown>) => void;

class WebSocketService {
  private client: any = null;
  private notifListeners: NotificationCallback[] = [];
  private msgListeners: MessageCallback[] = [];
  private systemListeners: SystemCallback[] = [];

  /**
   * Opens a STOMP connection authenticating with the provided JWT token.
   * Safe to call multiple times — disconnects the previous client first.
   */
  connect(token: string): void {
    if (typeof StompJs === 'undefined') {
      console.warn('[WS] StompJs not loaded — real-time push unavailable.');
      return;
    }
    if (this.client) {
      this.client.deactivate();
      this.client = null;
    }

    const wsUrl = API_BASE_URL.replace(/^http/, 'ws') + '/ws';

    this.client = new StompJs.Client({
      brokerURL: wsUrl,
      connectHeaders: {
        Authorization: 'Bearer ' + token
      },
      reconnectDelay: 5000,
      onConnect: () => {
        this.client.subscribe('/user/queue/notifications', (frame: any) => {
          try {
            const data = JSON.parse(frame.body);
            const count = typeof data.unreadCount === 'number' ? data.unreadCount : 0;
            this.notifListeners.forEach(cb => cb(count));
          } catch { /* ignore malformed frame */ }
        });

        this.client.subscribe('/user/queue/messages', (frame: any) => {
          try {
            const data = JSON.parse(frame.body);
            this.msgListeners.forEach(cb => cb(data));
          } catch { /* ignore malformed frame */ }
        });

        this.client.subscribe('/topic/system', (frame: any) => {
          try {
            const data = JSON.parse(frame.body);
            this.systemListeners.forEach(cb => cb(data));
          } catch { /* ignore malformed frame */ }
        });
      },
      onStompError: (frame: any) => {
        console.warn('[WS] STOMP error:', frame.headers?.message);
      }
    });

    this.client.activate();
  }

  /**
   * Closes the STOMP connection and clears all listeners.
   */
  disconnect(): void {
    if (this.client) {
      this.client.deactivate();
      this.client = null;
    }
    this.notifListeners = [];
    this.msgListeners = [];
    this.systemListeners = [];
  }

  /**
   * Registers a callback for notification unread-count updates.
   * Returns an unsubscribe function.
   */
  onNotificationUpdate(cb: NotificationCallback): () => void {
    this.notifListeners.push(cb);
    return () => {
      this.notifListeners = this.notifListeners.filter(f => f !== cb);
    };
  }

  /**
   * Registers a callback for incoming private messages.
   * Returns an unsubscribe function.
   */
  onNewMessage(cb: MessageCallback): () => void {
    this.msgListeners.push(cb);
    return () => {
      this.msgListeners = this.msgListeners.filter(f => f !== cb);
    };
  }

  /**
   * Registers a callback for system-wide events (e.g. announcements).
   * Returns an unsubscribe function.
   */
  onSystemEvent(cb: SystemCallback): () => void {
    this.systemListeners.push(cb);
    return () => {
      this.systemListeners = this.systemListeners.filter(f => f !== cb);
    };
  }

  /** Returns true when the STOMP client is connected. */
  isConnected(): boolean {
    return this.client?.connected === true;
  }
}

/** Singleton WebSocket service shared across all pages. */
export const wsService = new WebSocketService();
