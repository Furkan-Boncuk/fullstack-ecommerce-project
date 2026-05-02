import { useQuery } from '@tanstack/react-query';
import { paymentProfileService } from '../../services/api/paymentProfileService';

export const paymentProfileQueryKey = ['paymentProfile'];

export const usePaymentProfile = () =>
  useQuery({
    queryKey: paymentProfileQueryKey,
    queryFn: paymentProfileService.getPaymentProfile
  });
