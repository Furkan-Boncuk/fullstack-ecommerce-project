import { useMutation, useQueryClient } from '@tanstack/react-query';
import { paymentProfileQueryKey } from '../queries/usePaymentProfile';
import { paymentProfileService } from '../../services/api/paymentProfileService';
import { PaymentProfile, UpdatePaymentProfileRequest } from '../../types/paymentProfile';

export const useUpdatePaymentProfile = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: UpdatePaymentProfileRequest) => paymentProfileService.updatePaymentProfile(payload),
    onSuccess: (profile: PaymentProfile) => {
      queryClient.setQueryData(paymentProfileQueryKey, profile);
      void queryClient.invalidateQueries({ queryKey: paymentProfileQueryKey });
    }
  });
};
