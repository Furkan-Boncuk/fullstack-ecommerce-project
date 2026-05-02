import { useMutation, useQueryClient } from '@tanstack/react-query';
import { cartQueryKey } from '../queries/useCart';
import { ordersQueryKey } from '../queries/useOrders';
import { orderService } from '../../services/api/orderService';

export const useCreateOrder = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: orderService.createOrder,
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ordersQueryKey });
      void queryClient.invalidateQueries({ queryKey: cartQueryKey });
    }
  });
};
