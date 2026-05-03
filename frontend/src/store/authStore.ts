import { create } from 'zustand';
import { AuthResponse, AuthUser } from '../types/api';

export type AuthSession = Pick<AuthResponse, 'accessToken' | 'user'>;

interface AuthState {
  accessToken: string | null;
  user: AuthUser | null;
  isAuthenticated: boolean;
  setAuth: (auth: AuthSession) => void;
  clearAuth: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  accessToken: null,
  user: null,
  isAuthenticated: false,
  setAuth: (auth) => set({ accessToken: auth.accessToken, user: auth.user, isAuthenticated: true }),
  clearAuth: () => set({ accessToken: null, user: null, isAuthenticated: false })
}));
