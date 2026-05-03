import {
  Box,
  HStack,
  Heading,
  SimpleGrid,
  Skeleton,
  Spinner,
  Stack,
  Text
} from '@chakra-ui/react';
import { ErrorMessage } from '../../components/feedback/ErrorMessage';
import { ProductCard } from '../../business-components/product/ProductCard';
import { ProductFilters, ProductFilterValues } from '../../business-components/product/ProductFilters';
import { useInfiniteScroll } from '../../hooks/useInfiniteScroll';
import { Category } from '../../types/category';
import { Product } from '../../types/product';

interface ProductListViewProps {
  filters: ProductFilterValues;
  categories: Category[];
  isCategoryLoading: boolean;
  products: Product[];
  totalElements: number;
  hasNextPage: boolean;
  isLoading: boolean;
  isFetching: boolean;
  isFetchingNextPage: boolean;
  addingProductId?: number;
  cartProductQuantities: Map<number, number>;
  errorMessage?: string;
  onFiltersChange: (values: ProductFilterValues) => void;
  onFiltersSubmit: () => void;
  onFiltersClear: () => void;
  onLoadMore: () => void;
  onAddToCart: (productId: number) => void;
}

export function ProductListView({
  filters,
  categories,
  isCategoryLoading,
  products,
  totalElements,
  hasNextPage,
  isLoading,
  isFetching,
  isFetchingNextPage,
  addingProductId,
  cartProductQuantities,
  errorMessage,
  onFiltersChange,
  onFiltersSubmit,
  onFiltersClear,
  onLoadMore,
  onAddToCart
}: ProductListViewProps) {
  const hasProducts = products.length > 0;
  const loadMoreRef = useInfiniteScroll({
    enabled: hasNextPage && !isLoading && !isFetchingNextPage,
    onLoadMore
  });

  return (
    <Stack spacing={6}>
      <HStack justify="space-between" align={{ base: 'start', md: 'end' }} flexDirection={{ base: 'column', md: 'row' }} gap={3}>
        <Box>
          <Heading color="gray.900" letterSpacing="-0.04em" size="lg">
            Ürünler
          </Heading>
          <Text color="gray.600" mt={2}>
            Listeleme, arama ve filtreleme akışı backend ürün servisi üzerinden çalışır.
          </Text>
        </Box>
        <Text color="brand.700" fontWeight="800">
          {totalElements} ürün
        </Text>
      </HStack>

      <Box bg="white" border="1px solid" borderColor="purple.100" borderRadius="lg" p={{ base: 4, md: 5 }} boxShadow="sm">
        <ProductFilters
          values={filters}
          categories={categories}
          isCategoryLoading={isCategoryLoading}
          onChange={onFiltersChange}
          onSubmit={onFiltersSubmit}
          onClear={onFiltersClear}
        />
      </Box>

      {errorMessage ? <ErrorMessage message={errorMessage} /> : null}

      {isLoading ? (
        <SimpleGrid columns={{ base: 1, md: 2, xl: 4 }} spacing={5}>
          {Array.from({ length: 8 }).map((_, index) => (
            <Skeleton key={index} height="396px" borderRadius="lg" startColor="purple.50" endColor="purple.100" />
          ))}
        </SimpleGrid>
      ) : null}

      {!isLoading && !errorMessage && !hasProducts ? (
        <Box bg="white" border="1px solid" borderColor="purple.100" borderRadius="lg" p={8} textAlign="center">
          <Heading size="sm" color="gray.800">
            Ürün bulunamadı.
          </Heading>
          <Text color="gray.600" mt={2}>
            Filtreleri değiştirerek tekrar deneyebilirsin.
          </Text>
        </Box>
      ) : null}

      {!isLoading && hasProducts ? (
        <Stack spacing={6} opacity={isFetching && !isFetchingNextPage ? 0.72 : 1} transition="opacity .18s ease">
          <SimpleGrid columns={{ base: 1, md: 2, xl: 4 }} spacing={5}>
            {products.map((product) => (
              <ProductCard
                key={product.id}
                product={product}
                isAddingToCart={addingProductId === product.id}
                cartQuantity={cartProductQuantities.get(product.id) ?? 0}
                onAddToCart={onAddToCart}
              />
            ))}
          </SimpleGrid>

          <Box ref={loadMoreRef} minH="52px" display="flex" alignItems="center" justifyContent="center">
            {isFetchingNextPage ? (
              <HStack color="brand.700" fontWeight="800">
                <Spinner size="sm" />
                <Text>Daha fazla ürün yükleniyor</Text>
              </HStack>
            ) : null}
            {!hasNextPage && hasProducts ? (
              <Text color="gray.500" fontSize="sm">
                Tüm ürünler gösterildi.
              </Text>
            ) : null}
          </Box>
        </Stack>
      ) : null}
    </Stack>
  );
}
