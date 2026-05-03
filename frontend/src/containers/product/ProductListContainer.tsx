import { AxiosError } from 'axios';
import { useEffect, useMemo, useState } from 'react';
import toast from 'react-hot-toast';
import { useSearchParams } from 'react-router-dom';
import { useAddToCart } from '../../api/mutations/useAddToCart';
import { useCart } from '../../api/queries/useCart';
import { useCategories } from '../../api/queries/useCategories';
import { useInfiniteProducts } from '../../api/queries/useInfiniteProducts';
import { ProductFilterValues } from '../../business-components/product/ProductFilters';
import { ProductFilterParams } from '../../types/product';
import { ProductListView } from '../../views/product/ProductListView';

const defaultPageSize = 8;

function getNumberParam(value: string | null) {
  if (!value) {
    return undefined;
  }

  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : undefined;
}

function getErrorMessage(error: unknown, fallback: string) {
  return (error as AxiosError<{ detail?: string }>).response?.data?.detail ?? fallback;
}

export function ProductListContainer() {
  const [searchParams, setSearchParams] = useSearchParams();
  const addToCartMutation = useAddToCart();
  const cartQuery = useCart();

  const appliedFilters: ProductFilterValues = useMemo(
    () => ({
      search: searchParams.get('search') ?? '',
      categorySlug: searchParams.get('categorySlug') ?? '',
      minPrice: searchParams.get('minPrice') ?? '',
      maxPrice: searchParams.get('maxPrice') ?? '',
      inStock: searchParams.get('inStock') === 'true'
    }),
    [searchParams]
  );
  const [draftFilters, setDraftFilters] = useState<ProductFilterValues>(appliedFilters);

  const size = getNumberParam(searchParams.get('size')) ?? defaultPageSize;

  const productParams: ProductFilterParams = useMemo(
    () => ({
      search: appliedFilters.search.trim() || undefined,
      categorySlug: appliedFilters.categorySlug || undefined,
      minPrice: getNumberParam(appliedFilters.minPrice),
      maxPrice: getNumberParam(appliedFilters.maxPrice),
      inStock: appliedFilters.inStock ? true : undefined,
      size
    }),
    [appliedFilters, size]
  );

  const productsQuery = useInfiniteProducts(productParams);
  const categoriesQuery = useCategories();
  const products = useMemo(
    () => productsQuery.data?.pages.flatMap((pageData) => pageData.content) ?? [],
    [productsQuery.data?.pages]
  );
  const lastPage = productsQuery.data?.pages[productsQuery.data.pages.length - 1];
  const cartProductQuantities = useMemo(
    () => new Map(cartQuery.data?.items.map((item) => [item.productId, item.quantity]) ?? []),
    [cartQuery.data?.items]
  );

  useEffect(() => {
    setDraftFilters(appliedFilters);
  }, [appliedFilters]);

  const submitFilters = () => {
    const nextParams = new URLSearchParams(searchParams);

    const entries: Array<[keyof ProductFilterValues, string | boolean]> = [
      ['search', draftFilters.search],
      ['categorySlug', draftFilters.categorySlug],
      ['minPrice', draftFilters.minPrice],
      ['maxPrice', draftFilters.maxPrice],
      ['inStock', draftFilters.inStock]
    ];

    entries.forEach(([key, value]) => {
      if (value === '' || value === false) {
        nextParams.delete(key);
      } else {
        nextParams.set(key, String(value));
      }
    });

    nextParams.delete('page');
    setSearchParams(nextParams);
  };

  const clearFilters = () => {
    const nextParams = new URLSearchParams();
    if (size !== defaultPageSize) {
      nextParams.set('size', String(size));
    }
    setSearchParams(nextParams);
  };

  const addToCart = (productId: number) => {
    const currentQuantity = cartProductQuantities.get(productId) ?? 0;
    addToCartMutation.mutate(
      { productId, quantity: currentQuantity + 1 },
      {
        onSuccess: () => toast.success('Ürün sepete eklendi.'),
        onError: (error) => toast.error(getErrorMessage(error, 'Ürün sepete eklenemedi.'))
      }
    );
  };

  return (
    <ProductListView
      filters={draftFilters}
      categories={categoriesQuery.data ?? []}
      isCategoryLoading={categoriesQuery.isLoading}
      products={products}
      totalElements={lastPage?.totalElements ?? 0}
      hasNextPage={productsQuery.hasNextPage}
      isLoading={productsQuery.isLoading}
      isFetching={productsQuery.isFetching}
      isFetchingNextPage={productsQuery.isFetchingNextPage}
      addingProductId={addToCartMutation.isPending ? addToCartMutation.variables?.productId : undefined}
      cartProductQuantities={cartProductQuantities}
      errorMessage={productsQuery.isError ? 'Ürünler yüklenirken bir hata oluştu.' : undefined}
      onFiltersChange={setDraftFilters}
      onFiltersSubmit={submitFilters}
      onFiltersClear={clearFilters}
      onLoadMore={() => {
        if (productsQuery.hasNextPage && !productsQuery.isFetchingNextPage) {
          void productsQuery.fetchNextPage();
        }
      }}
      onAddToCart={addToCart}
    />
  );
}
