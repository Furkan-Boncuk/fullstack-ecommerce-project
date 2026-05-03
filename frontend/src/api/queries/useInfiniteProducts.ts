import { useInfiniteQuery } from '@tanstack/react-query';
import { productService } from '../../services/api/productService';
import { ProductFilterParams } from '../../types/product';

export const useInfiniteProducts = (params: ProductFilterParams) =>
  useInfiniteQuery({
    queryKey: ['products', 'infinite', params],
    queryFn: ({ pageParam }) => productService.getProducts({ ...params, page: pageParam }),
    initialPageParam: 0,
    getNextPageParam: (lastPage) => (lastPage.last ? undefined : lastPage.page + 1)
  });
