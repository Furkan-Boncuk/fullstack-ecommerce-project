import { axiosInstance } from './axiosInstance';
import { AuthResponse } from '../../types/api';
import { LoginRequest, RegisterRequest } from '../../types/user';

export const authService = {
  async register(payload: RegisterRequest): Promise<AuthResponse> {
    const { data } = await axiosInstance.post<AuthResponse>('/api/v1/auth/register', payload);
    return data;
  },
  async login(payload: LoginRequest): Promise<AuthResponse> {
    const { data } = await axiosInstance.post<AuthResponse>('/api/v1/auth/login', payload);
    return data;
  },
  async refresh(): Promise<AuthResponse> {
    const { data } = await axiosInstance.post<AuthResponse>('/api/v1/auth/refresh');
    return data;
  },
  async logout(): Promise<void> {
    await axiosInstance.post('/api/v1/auth/logout');
  }
};
