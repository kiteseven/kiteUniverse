import type { AuthResultVO, AuthUserVO, UserDetailVO } from './api';

/**
 * Shared local-storage helpers used by the auth modal and personal center.
 */

export interface SessionSnapshot {
  token: string;
  user: AuthUserVO | null;
}

const TOKEN_STORAGE_KEY = 'kite-universe-auth-token';
const USER_STORAGE_KEY = 'kite-universe-auth-user';
const SESSION_CHANGE_EVENT = 'kite-universe-session-change';

/**
 * Reads the saved token from localStorage.
 */
export function loadStoredToken() {
  return readStorage(TOKEN_STORAGE_KEY);
}

/**
 * Reads the saved lightweight user summary from localStorage.
 */
export function loadStoredUser(): AuthUserVO | null {
  const rawValue = readStorage(USER_STORAGE_KEY);
  if (!rawValue) {
    return null;
  }

  try {
    return JSON.parse(rawValue) as AuthUserVO;
  } catch {
    removeStorage(USER_STORAGE_KEY);
    return null;
  }
}

/**
 * Returns whether a token is currently stored.
 */
export function hasStoredToken() {
  return Boolean(loadStoredToken());
}

/**
 * Persists the authenticated session returned by the backend.
 */
export function saveAuthSession(session: AuthResultVO) {
  writeStorage(TOKEN_STORAGE_KEY, session.token);
  writeStorage(USER_STORAGE_KEY, JSON.stringify(session.user));
  dispatchSessionChange();
}

/**
 * Updates the stored lightweight user summary.
 */
export function updateStoredUser(user: AuthUserVO | null) {
  if (!user) {
    removeStorage(USER_STORAGE_KEY);
  } else {
    writeStorage(USER_STORAGE_KEY, JSON.stringify(user));
  }
  dispatchSessionChange();
}

/**
 * Clears the stored token and user summary.
 */
export function clearStoredSession() {
  removeStorage(TOKEN_STORAGE_KEY);
  removeStorage(USER_STORAGE_KEY);
  dispatchSessionChange();
}

/**
 * Converts a full profile record into the lightweight navigation summary.
 */
export function toAuthUser(detail: UserDetailVO): AuthUserVO {
  return {
    id: detail.id,
    username: detail.username,
    nickname: detail.nickname || detail.username,
    phone: detail.phone || '',
    avatar: detail.avatar,
    status: detail.status ?? 1
  };
}

/**
 * Updates the stored lightweight user summary from a full profile record.
 */
export function syncStoredUserFromDetail(detail: UserDetailVO) {
  updateStoredUser(toAuthUser(detail));
}

/**
 * Subscribes to session changes triggered by login, logout, or profile updates.
 */
export function subscribeSessionChange(listener: (snapshot: SessionSnapshot) => void) {
  if (typeof window === 'undefined') {
    return () => undefined;
  }

  const handler = (event: Event) => {
    const customEvent = event as CustomEvent<SessionSnapshot>;
    listener(customEvent.detail);
  };

  window.addEventListener(SESSION_CHANGE_EVENT, handler as EventListener);
  return () => {
    window.removeEventListener(SESSION_CHANGE_EVENT, handler as EventListener);
  };
}

/**
 * Returns the current in-memory session snapshot.
 */
export function getSessionSnapshot(): SessionSnapshot {
  return {
    token: loadStoredToken(),
    user: loadStoredUser()
  };
}

/**
 * Reads a value from localStorage when running in the browser.
 */
function readStorage(key: string) {
  if (typeof window === 'undefined') {
    return '';
  }
  return window.localStorage.getItem(key) || '';
}

/**
 * Writes a value to localStorage when running in the browser.
 */
function writeStorage(key: string, value: string) {
  if (typeof window === 'undefined') {
    return;
  }
  window.localStorage.setItem(key, value);
}

/**
 * Removes a value from localStorage when running in the browser.
 */
function removeStorage(key: string) {
  if (typeof window === 'undefined') {
    return;
  }
  window.localStorage.removeItem(key);
}

/**
 * Broadcasts the latest session snapshot to other parts of the frontend.
 */
function dispatchSessionChange() {
  if (typeof window === 'undefined') {
    return;
  }

  window.dispatchEvent(new CustomEvent<SessionSnapshot>(SESSION_CHANGE_EVENT, {
    detail: getSessionSnapshot()
  }));
}
