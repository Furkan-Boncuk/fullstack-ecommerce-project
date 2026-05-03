import { axiosInstance } from './axiosInstance';
import { AdminOrder, AdminProduct, AdminProductPayload } from '../../types/admin';
import { PageResponse } from '../../types/api';

export const adminService = {
  async getProducts(page = 0, size = 20): Promise<PageResponse<AdminProduct>> {
    const { data } = await axiosInstance.get<PageResponse<AdminProduct>>('/api/v1/admin/products', {
      params: { page, size }
    });
    return data;
  },

  async createProduct(payload: AdminProductPayload): Promise<AdminProduct> {
    const { data } = await axiosInstance.post<AdminProduct>('/api/v1/admin/products', payload);
    return data;
  },

  async updateProduct(id: number, payload: AdminProductPayload): Promise<AdminProduct> {
    const { data } = await axiosInstance.patch<AdminProduct>(`/api/v1/admin/products/${id}`, payload);
    return data;
  },

  async deleteProduct(id: number): Promise<void> {
    await axiosInstance.delete(`/api/v1/admin/products/${id}`);
  },

  async getOrders(params: { page?: number; size?: number; status?: string; userId?: string; email?: string }): Promise<PageResponse<AdminOrder>> {
    const { data } = await axiosInstance.get<PageResponse<AdminOrder>>('/api/v1/admin/orders', {
      params: {
        page: params.page ?? 0,
        size: params.size ?? 20,
        status: params.status || undefined,
        userId: params.userId || undefined,
        email: params.email || undefined
      }
    });
    return data;
  }
};
