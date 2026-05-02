import { axiosInstance } from './axiosInstance';
import { PaymentProfile, UpdatePaymentProfileRequest } from '../../types/paymentProfile';

export const paymentProfileService = {
  async getPaymentProfile(): Promise<PaymentProfile> {
    const { data } = await axiosInstance.get<PaymentProfile>('/api/v1/auth/profile/payment');
    return data;
  },

  async updatePaymentProfile(payload: UpdatePaymentProfileRequest): Promise<PaymentProfile> {
    const { data } = await axiosInstance.patch<PaymentProfile>('/api/v1/auth/profile/payment', payload);
    return data;
  }
};
