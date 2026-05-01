import { useQuery } from '@tanstack/react-query';
import { cartService } from '../../services/api/cartService';

export const cartQueryKey = ['cart'] as const;

export const useCart = () =>
  useQuery({
    queryKey: cartQueryKey,
    queryFn: cartService.getCart
  });
