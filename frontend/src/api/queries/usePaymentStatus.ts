import { useQuery } from '@tanstack/react-query';
import { paymentService } from '../../services/api/paymentService';

export const paymentStatusQueryKey = (orderId?: number) => ['paymentStatus', orderId];

export const usePaymentStatus = (orderId?: number) =>
  useQuery({
    queryKey: paymentStatusQueryKey(orderId),
    queryFn: () => paymentService.getPaymentStatus(orderId as number),
    enabled: Boolean(orderId)
  });
