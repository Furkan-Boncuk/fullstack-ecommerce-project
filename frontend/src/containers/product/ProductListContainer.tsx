import { AxiosError } from 'axios';
import { useMemo } from 'react';
import toast from 'react-hot-toast';
import { useSearchParams } from 'react-router-dom';
import { useAddToCart } from '../../api/mutations/useAddToCart';
import { useCart } from '../../api/queries/useCart';
import { useCategories } from '../../api/queries/useCategories';
import { useProducts } from '../../api/queries/useProducts';
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

function getPageParam(value: string | null) {
  const parsed = getNumberParam(value);
  return parsed && parsed > 0 ? parsed : 0;
}

function getErrorMessage(error: unknown, fallback: string) {
  return (error as AxiosError<{ detail?: string }>).response?.data?.detail ?? fallback;
}

export function ProductListContainer() {
  const [searchParams, setSearchParams] = useSearchParams();
  const addToCartMutation = useAddToCart();
  const cartQuery = useCart();

  const filterValues: ProductFilterValues = useMemo(
    () => ({
      search: searchParams.get('search') ?? '',
      categorySlug: searchParams.get('categorySlug') ?? '',
      minPrice: searchParams.get('minPrice') ?? '',
      maxPrice: searchParams.get('maxPrice') ?? '',
      inStock: searchParams.get('inStock') === 'true'
    }),
    [searchParams]
  );

  const page = getPageParam(searchParams.get('page'));
  const size = getNumberParam(searchParams.get('size')) ?? defaultPageSize;

  const productParams: ProductFilterParams = useMemo(
    () => ({
      search: filterValues.search.trim() || undefined,
      categorySlug: filterValues.categorySlug || undefined,
      minPrice: getNumberParam(filterValues.minPrice),
      maxPrice: getNumberParam(filterValues.maxPrice),
      inStock: filterValues.inStock ? true : undefined,
      page,
      size
    }),
    [filterValues, page, size]
  );

  const productsQuery = useProducts(productParams);
  const categoriesQuery = useCategories();
  const cartProductIds = useMemo(
    () => new Set(cartQuery.data?.items.map((item) => item.productId) ?? []),
    [cartQuery.data?.items]
  );

  const updateFilters = (nextValues: ProductFilterValues) => {
    const nextParams = new URLSearchParams(searchParams);

    const entries: Array<[keyof ProductFilterValues, string | boolean]> = [
      ['search', nextValues.search],
      ['categorySlug', nextValues.categorySlug],
      ['minPrice', nextValues.minPrice],
      ['maxPrice', nextValues.maxPrice],
      ['inStock', nextValues.inStock]
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

  const changePage = (nextPage: number) => {
    const nextParams = new URLSearchParams(searchParams);
    if (nextPage <= 0) {
      nextParams.delete('page');
    } else {
      nextParams.set('page', String(nextPage));
    }
    setSearchParams(nextParams);
  };

  const addToCart = (productId: number) => {
    addToCartMutation.mutate(
      { productId, quantity: 1 },
      {
        onSuccess: () => toast.success('Ürün sepete eklendi.'),
        onError: (error) => toast.error(getErrorMessage(error, 'Ürün sepete eklenemedi.'))
      }
    );
  };

  return (
    <ProductListView
      filters={filterValues}
      categories={categoriesQuery.data ?? []}
      isCategoryLoading={categoriesQuery.isLoading}
      products={productsQuery.data?.content ?? []}
      page={productsQuery.data?.page ?? page}
      totalPages={productsQuery.data?.totalPages ?? 0}
      totalElements={productsQuery.data?.totalElements ?? 0}
      isLast={productsQuery.data?.last ?? true}
      isLoading={productsQuery.isLoading}
      isFetching={productsQuery.isFetching}
      addingProductId={addToCartMutation.isPending ? addToCartMutation.variables?.productId : undefined}
      cartProductIds={cartProductIds}
      errorMessage={productsQuery.isError ? 'Ürünler yüklenirken bir hata oluştu.' : undefined}
      onFiltersChange={updateFilters}
      onFiltersClear={clearFilters}
      onPageChange={changePage}
      onAddToCart={addToCart}
    />
  );
}
