import { axiosInstance } from './axiosInstance';
import { parseAuthResponse, postApi } from './apiClient';
import { AuthResponse } from '../../types/api';
import { LoginRequest, RegisterRequest } from '../../types/user';

export const authService = {
  async register(payload: RegisterRequest): Promise<AuthResponse> {
    return postApi(axiosInstance, '/api/v1/auth/register', payload, parseAuthResponse);
  },
  async login(payload: LoginRequest): Promise<AuthResponse> {
    return postApi(axiosInstance, '/api/v1/auth/login', payload, parseAuthResponse);
  },
  async refresh(): Promise<AuthResponse> {
    return postApi(axiosInstance, '/api/v1/auth/refresh', parseAuthResponse);
  },
  async logout(): Promise<void> {
    await axiosInstance.post('/api/v1/auth/logout');
  }
};
