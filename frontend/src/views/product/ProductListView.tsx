import {
  Box,
  HStack,
  Heading,
  SimpleGrid,
  Skeleton,
  Stack,
  Text
} from '@chakra-ui/react';
import { ErrorMessage } from '../../components/feedback/ErrorMessage';
import { ProductCard } from '../../business-components/product/ProductCard';
import { ProductFilters, ProductFilterValues } from '../../business-components/product/ProductFilters';
import { ProductPagination } from '../../business-components/product/ProductPagination';
import { Category } from '../../types/category';
import { Product } from '../../types/product';

interface ProductListViewProps {
  filters: ProductFilterValues;
  categories: Category[];
  isCategoryLoading: boolean;
  products: Product[];
  page: number;
  totalPages: number;
  totalElements: number;
  isLast: boolean;
  isLoading: boolean;
  isFetching: boolean;
  addingProductId?: number;
  cartProductIds: Set<number>;
  errorMessage?: string;
  onFiltersChange: (values: ProductFilterValues) => void;
  onFiltersClear: () => void;
  onPageChange: (page: number) => void;
  onAddToCart: (productId: number) => void;
}

export function ProductListView({
  filters,
  categories,
  isCategoryLoading,
  products,
  page,
  totalPages,
  totalElements,
  isLast,
  isLoading,
  isFetching,
  addingProductId,
  cartProductIds,
  errorMessage,
  onFiltersChange,
  onFiltersClear,
  onPageChange,
  onAddToCart
}: ProductListViewProps) {
  const hasProducts = products.length > 0;

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
        <Stack spacing={6} opacity={isFetching ? 0.72 : 1} transition="opacity .18s ease">
          <SimpleGrid columns={{ base: 1, md: 2, xl: 4 }} spacing={5}>
            {products.map((product) => (
              <ProductCard
                key={product.id}
                product={product}
                isAddingToCart={addingProductId === product.id}
                isInCart={cartProductIds.has(product.id)}
                onAddToCart={onAddToCart}
              />
            ))}
          </SimpleGrid>
          <ProductPagination page={page} totalPages={totalPages} isLast={isLast} onPageChange={onPageChange} />
        </Stack>
      ) : null}
    </Stack>
  );
}
