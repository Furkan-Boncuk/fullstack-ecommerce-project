import { useQuery } from '@tanstack/react-query';
import { productService } from '../../services/api/productService';
import { ProductFilterParams } from '../../types/product';

export const useProducts = (params: ProductFilterParams) =>
  useQuery({
    queryKey: ['products', params],
    queryFn: () => productService.getProducts(params),
    placeholderData: (previousData) => previousData
  });
