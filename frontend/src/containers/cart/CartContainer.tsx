import { AxiosError } from 'axios';
import toast from 'react-hot-toast';
import { useRemoveFromCart } from '../../api/mutations/useRemoveFromCart';
import { useUpdateCartItem } from '../../api/mutations/useUpdateCartItem';
import { useCart } from '../../api/queries/useCart';
import { CartView } from '../../views/cart/CartView';

function getErrorMessage(error: unknown, fallback: string) {
  return (error as AxiosError<{ detail?: string }>).response?.data?.detail ?? fallback;
}

export function CartContainer() {
  const cartQuery = useCart();
  const removeMutation = useRemoveFromCart();
  const updateMutation = useUpdateCartItem();

  const handleRemove = (productId: number) => {
    removeMutation.mutate(productId, {
      onSuccess: () => toast.success('Ürün sepetten çıkarıldı.'),
      onError: (error) => toast.error(getErrorMessage(error, 'Ürün sepetten çıkarılamadı.'))
    });
  };

  const handleQuantityChange = (productId: number, quantity: number) => {
    if (quantity <= 0) {
      handleRemove(productId);
      return;
    }

    updateMutation.mutate(
      { productId, quantity },
      {
        onError: (error) => toast.error(getErrorMessage(error, 'Ürün adedi güncellenemedi.'))
      }
    );
  };

  return (
    <CartView
      cart={cartQuery.data}
      isLoading={cartQuery.isLoading}
      errorMessage={cartQuery.isError ? 'Sepet yüklenirken bir hata oluştu.' : undefined}
      removingProductId={removeMutation.isPending ? removeMutation.variables : undefined}
      updatingProductId={updateMutation.isPending ? updateMutation.variables?.productId : undefined}
      onRemove={handleRemove}
      onQuantityChange={handleQuantityChange}
    />
  );
}
