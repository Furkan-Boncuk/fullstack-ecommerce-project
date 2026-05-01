import { AxiosError } from 'axios';
import toast from 'react-hot-toast';
import { useParams } from 'react-router-dom';
import { useAddToCart } from '../../api/mutations/useAddToCart';
import { useProductById } from '../../api/queries/useProductById';
import { ProductDetailView } from '../../views/product/ProductDetailView';

function getErrorMessage(error: unknown, fallback: string) {
  return (error as AxiosError<{ detail?: string }>).response?.data?.detail ?? fallback;
}

export function ProductDetailContainer() {
  const { id } = useParams();
  const productId = id ? Number(id) : undefined;
  const hasValidProductId = Boolean(productId && Number.isInteger(productId) && productId > 0);
  const productQuery = useProductById(hasValidProductId ? productId : undefined);
  const addToCartMutation = useAddToCart();

  const handleAddToCart = () => {
    if (!productQuery.data) {
      return;
    }

    addToCartMutation.mutate(
      { productId: productQuery.data.id, quantity: 1 },
      {
        onSuccess: () => toast.success('Ürün sepete eklendi.'),
        onError: (error) => toast.error(getErrorMessage(error, 'Ürün sepete eklenemedi.'))
      }
    );
  };

  return (
    <ProductDetailView
      product={productQuery.data}
      isLoading={productQuery.isLoading && hasValidProductId}
      isAddingToCart={addToCartMutation.isPending}
      errorMessage={!hasValidProductId || productQuery.isError ? 'Ürün detayı bulunamadı.' : undefined}
      onAddToCart={handleAddToCart}
    />
  );
}
