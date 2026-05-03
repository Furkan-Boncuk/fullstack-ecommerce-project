import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';
import { useAuthStore } from '../../store/authStore';
import { assertApiResponse, parseAuthResponse, postApi } from './apiClient';

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';

interface RetryableRequest extends InternalAxiosRequestConfig {
  _retry?: boolean;
}

const refreshClient = axios.create({
  baseURL: apiBaseUrl,
  withCredentials: true,
  headers: {
    Accept: 'application/json'
  }
});

export const axiosInstance = axios.create({
  baseURL: apiBaseUrl,
  withCredentials: true,
  headers: {
    Accept: 'application/json'
  }
});

axiosInstance.interceptors.request.use((config) => {
  const token = useAuthStore.getState().accessToken;
  const url = config.url ?? '';
  const isAuthRequest = url.includes('/api/v1/auth/');

  if (token && !isAuthRequest) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

axiosInstance.interceptors.response.use(
  (response) => {
    assertApiResponse(response);
    return response;
  },
  async (error: AxiosError) => {
    const originalRequest = error.config as RetryableRequest | undefined;
    const requestUrl = originalRequest?.url ?? '';
    const isRefreshRequest = requestUrl.includes('/api/v1/auth/refresh');
    const isAuthRequest = requestUrl.includes('/api/v1/auth/');

    if (error.response?.status === 401 && originalRequest && !originalRequest._retry && !isAuthRequest && !isRefreshRequest) {
      originalRequest._retry = true;
      try {
        const auth = await postApi(refreshClient, '/api/v1/auth/refresh', parseAuthResponse);

        useAuthStore.getState().setAuth(auth);
        originalRequest.headers.Authorization = `Bearer ${auth.accessToken}`;
        return axiosInstance(originalRequest);
      } catch {
        useAuthStore.getState().clearAuth();
      }
    }

    return Promise.reject(error);
  }
);
