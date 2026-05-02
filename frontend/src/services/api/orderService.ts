import { axiosInstance } from './axiosInstance';
import { Order } from '../../types/order';

export const orderService = {
  async createOrder(): Promise<Order> {
    const { data } = await axiosInstance.post<Order>('/api/v1/orders');
    return data;
  },

  async getOrders(): Promise<Order[]> {
    const { data } = await axiosInstance.get<Order[]>('/api/v1/orders');
    return data;
  }
};
