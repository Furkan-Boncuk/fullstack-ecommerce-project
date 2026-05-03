import { create } from 'zustand';
import { AuthUser } from '../types/api';

interface AuthState {
  accessToken: string | null;
  user: AuthUser | null;
  isAuthenticated: boolean;
  setAuth: (token: string, user: AuthUser) => void;
  setToken: (token: string) => void;
  clearAuth: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  accessToken: null,
  user: null,
  isAuthenticated: false,
  setAuth: (token, user) => set({ accessToken: token, user, isAuthenticated: true }),
  setToken: (token) => set({ accessToken: token, isAuthenticated: true }),
  clearAuth: () => set({ accessToken: null, user: null, isAuthenticated: false })
}));
