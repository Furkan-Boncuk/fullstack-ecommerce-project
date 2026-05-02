import { useQuery } from '@tanstack/react-query';
import { orderService } from '../../services/api/orderService';

export const ordersQueryKey = ['orders'];

export const useOrders = () =>
  useQuery({
    queryKey: ordersQueryKey,
    queryFn: orderService.getOrders
  });
