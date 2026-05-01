import { useMutation, useQueryClient } from '@tanstack/react-query';
import { cartQueryKey } from '../queries/useCart';
import { cartService } from '../../services/api/cartService';
import { Cart } from '../../types/cart';

export const useRemoveFromCart = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (productId: number) => cartService.removeItem(productId),
    onSuccess: (cart: Cart) => {
      queryClient.setQueryData(cartQueryKey, cart);
      void queryClient.invalidateQueries({ queryKey: cartQueryKey });
    }
  });
};
