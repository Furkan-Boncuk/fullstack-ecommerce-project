import { useQuery } from '@tanstack/react-query';
import { productService } from '../../services/api/productService';

export const useProductById = (id?: number) =>
  useQuery({
    queryKey: ['products', id],
    queryFn: () => productService.getProductById(id as number),
    enabled: Boolean(id)
  });
