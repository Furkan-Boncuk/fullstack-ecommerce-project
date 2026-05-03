import {
  Button,
  Checkbox,
  FormControl,
  FormLabel,
  Grid,
  GridItem,
  HStack,
  Input,
  Select
} from '@chakra-ui/react';
import { Category } from '../../types/category';

export interface ProductFilterValues {
  search: string;
  categorySlug: string;
  minPrice: string;
  maxPrice: string;
  inStock: boolean;
}

interface ProductFiltersProps {
  values: ProductFilterValues;
  categories: Category[];
  isCategoryLoading: boolean;
  onChange: (values: ProductFilterValues) => void;
  onSubmit: () => void;
  onClear: () => void;
}

export function ProductFilters({ values, categories, isCategoryLoading, onChange, onSubmit, onClear }: ProductFiltersProps) {
  const update = (nextValues: Partial<ProductFilterValues>) => {
    onChange({ ...values, ...nextValues });
  };

  return (
    <Grid
      as="form"
      templateColumns={{ base: '1fr', md: 'repeat(2, 1fr)', xl: '2fr 1fr 1fr 1fr auto' }}
      gap={4}
      alignItems="end"
      onSubmit={(event) => {
        event.preventDefault();
        onSubmit();
      }}
    >
      <GridItem>
        <FormControl>
          <FormLabel fontSize="sm" color="gray.700">
            Arama
          </FormLabel>
          <Input
            value={values.search}
            onChange={(event) => update({ search: event.target.value })}
            placeholder="Ürün adı veya açıklama"
            bg="white"
            borderColor="purple.100"
            h="44px"
            borderRadius="lg"
            _focusVisible={{ borderColor: 'brand.500', boxShadow: '0 0 0 4px rgba(154,89,255,0.16)' }}
          />
        </FormControl>
      </GridItem>

      <GridItem>
        <FormControl>
          <FormLabel fontSize="sm" color="gray.700">
            Kategori
          </FormLabel>
          <Select
            value={values.categorySlug}
            onChange={(event) => update({ categorySlug: event.target.value })}
            isDisabled={isCategoryLoading}
            bg="white"
            borderColor="purple.100"
            h="44px"
            borderRadius="lg"
            _focusVisible={{ borderColor: 'brand.500', boxShadow: '0 0 0 4px rgba(154,89,255,0.16)' }}
          >
            <option value="">Tüm kategoriler</option>
            {categories.map((category) => (
              <option key={category.slug} value={category.slug}>
                {category.name}
              </option>
            ))}
          </Select>
        </FormControl>
      </GridItem>

      <GridItem>
        <FormControl>
          <FormLabel fontSize="sm" color="gray.700">
            Minimum fiyat
          </FormLabel>
          <Input
            value={values.minPrice}
            onChange={(event) => update({ minPrice: event.target.value })}
            type="number"
            min="0"
            placeholder="0"
            bg="white"
            borderColor="purple.100"
            h="44px"
            borderRadius="lg"
            _focusVisible={{ borderColor: 'brand.500', boxShadow: '0 0 0 4px rgba(154,89,255,0.16)' }}
          />
        </FormControl>
      </GridItem>

      <GridItem>
        <FormControl>
          <FormLabel fontSize="sm" color="gray.700">
            Maksimum fiyat
          </FormLabel>
          <Input
            value={values.maxPrice}
            onChange={(event) => update({ maxPrice: event.target.value })}
            type="number"
            min="0"
            placeholder="99999"
            bg="white"
            borderColor="purple.100"
            h="44px"
            borderRadius="lg"
            _focusVisible={{ borderColor: 'brand.500', boxShadow: '0 0 0 4px rgba(154,89,255,0.16)' }}
          />
        </FormControl>
      </GridItem>

      <GridItem>
        <HStack minH="44px" spacing={3} align="center" flexWrap="wrap">
          <Checkbox
            isChecked={values.inStock}
            onChange={(event) => update({ inStock: event.target.checked })}
            colorScheme="purple"
            whiteSpace="nowrap"
          >
            Stokta olanlar
          </Checkbox>
          <Button type="submit" colorScheme="brand" borderRadius="full">
            Filtrele
          </Button>
          <Button type="button" variant="ghost" colorScheme="brand" borderRadius="full" onClick={onClear}>
            Temizle
          </Button>
        </HStack>
      </GridItem>
    </Grid>
  );
}
