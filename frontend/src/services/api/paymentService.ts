import { axiosInstance } from './axiosInstance';
import { PaymentInitRequest, PaymentInitResponse, PaymentStatusResponse } from '../../types/payment';

export const paymentService = {
  async initPayment(payload: PaymentInitRequest): Promise<PaymentInitResponse> {
    const { data } = await axiosInstance.post<PaymentInitResponse>('/api/v1/payments/init', payload);
    return data;
  },

  async getPaymentStatus(orderId: number): Promise<PaymentStatusResponse> {
    const { data } = await axiosInstance.get<PaymentStatusResponse>(`/api/v1/payments/orders/${orderId}`);
    return data;
  }
};
