import { useMutation, useQueryClient } from '@tanstack/react-query';
import { paymentStatusQueryKey } from '../queries/usePaymentStatus';
import { paymentService } from '../../services/api/paymentService';
import { PaymentInitRequest } from '../../types/payment';

export const useInitPayment = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: PaymentInitRequest) => paymentService.initPayment(payload),
    onSuccess: (_response, variables) => {
      void queryClient.invalidateQueries({ queryKey: paymentStatusQueryKey(variables.orderId) });
    }
  });
};
