import { useMutation, useQueryClient } from '@tanstack/react-query';
import { cartQueryKey } from '../queries/useCart';
import { cartService } from '../../services/api/cartService';
import { Cart, CartItemRequest } from '../../types/cart';

export const useUpdateCartItem = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: CartItemRequest) => cartService.addItem(payload),
    onSuccess: (cart: Cart) => {
      queryClient.setQueryData(cartQueryKey, cart);
      void queryClient.invalidateQueries({ queryKey: cartQueryKey });
    }
  });
};
