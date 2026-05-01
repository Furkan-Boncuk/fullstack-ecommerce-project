import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';
import { useAuthStore } from '../../store/authStore';

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';

interface RetryableRequest extends InternalAxiosRequestConfig {
  _retry?: boolean;
}

const refreshClient = axios.create({
  baseURL: apiBaseUrl,
  withCredentials: true
});

export const axiosInstance = axios.create({
  baseURL: apiBaseUrl,
  withCredentials: true
});

axiosInstance.interceptors.request.use((config) => {
  const token = useAuthStore.getState().accessToken;
  const url = config.url ?? '';
  const isRefreshRequest = url.includes('/api/v1/auth/refresh');

  if (token && !isRefreshRequest) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

axiosInstance.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as RetryableRequest | undefined;
    const requestUrl = originalRequest?.url ?? '';
    const isRefreshRequest = requestUrl.includes('/api/v1/auth/refresh');

    if (error.response?.status === 401 && originalRequest && !originalRequest._retry && !isRefreshRequest) {
      originalRequest._retry = true;
      try {
        const refreshResponse = await refreshClient.post<{ accessToken: string }>('/api/v1/auth/refresh');
        const newAccessToken = refreshResponse.data.accessToken;

        useAuthStore.getState().setToken(newAccessToken);
        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
        return axiosInstance(originalRequest);
      } catch {
        useAuthStore.getState().clearAuth();
      }
    }

    return Promise.reject(error);
  }
);
