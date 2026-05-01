import { useParams } from 'react-router-dom';
import { useProductById } from '../../api/queries/useProductById';
import { ProductDetailView } from '../../views/product/ProductDetailView';

export function ProductDetailContainer() {
  const { id } = useParams();
  const productId = id ? Number(id) : undefined;
  const hasValidProductId = Boolean(productId && Number.isInteger(productId) && productId > 0);
  const productQuery = useProductById(hasValidProductId ? productId : undefined);

  return (
    <ProductDetailView
      product={productQuery.data}
      isLoading={productQuery.isLoading && hasValidProductId}
      errorMessage={!hasValidProductId || productQuery.isError ? 'Ürün detayı bulunamadı.' : undefined}
    />
  );
}
