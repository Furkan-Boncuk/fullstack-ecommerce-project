import { ArrowBackIcon } from '@chakra-ui/icons';
import {
  AspectRatio,
  Badge,
  Box,
  Button,
  Grid,
  Heading,
  HStack,
  Image,
  Skeleton,
  Stack,
  Text
} from '@chakra-ui/react';
import { Link as RouterLink } from 'react-router-dom';
import { ErrorMessage } from '../../components/feedback/ErrorMessage';
import { formatPrice } from '../../helpers/formatPrice';
import { Product } from '../../types/product';

interface ProductDetailViewProps {
  product?: Product;
  isLoading: boolean;
  errorMessage?: string;
}

export function ProductDetailView({ product, isLoading, errorMessage }: ProductDetailViewProps) {
  if (isLoading) {
    return (
      <Stack spacing={5}>
        <Skeleton height="40px" width="140px" borderRadius="full" startColor="purple.50" endColor="purple.100" />
        <Grid templateColumns={{ base: '1fr', lg: '1fr 1fr' }} gap={6}>
          <Skeleton height="520px" borderRadius="lg" startColor="purple.50" endColor="purple.100" />
          <Skeleton height="520px" borderRadius="lg" startColor="purple.50" endColor="purple.100" />
        </Grid>
      </Stack>
    );
  }

  if (errorMessage || !product) {
    return (
      <Stack spacing={5}>
        <Button as={RouterLink} to="/products" leftIcon={<ArrowBackIcon />} variant="ghost" colorScheme="brand" alignSelf="start">
          Ürünlere dön
        </Button>
        <ErrorMessage message={errorMessage ?? 'Ürün detayı bulunamadı.'} />
      </Stack>
    );
  }

  const hasStock = product.stock > 0;

  return (
    <Stack spacing={5}>
      <Button as={RouterLink} to="/products" leftIcon={<ArrowBackIcon />} variant="ghost" colorScheme="brand" alignSelf="start">
        Ürünlere dön
      </Button>

      <Grid templateColumns={{ base: '1fr', lg: '1.05fr .95fr' }} gap={6} alignItems="stretch">
        <Box bg="white" border="1px solid" borderColor="purple.100" borderRadius="lg" overflow="hidden" boxShadow="sm">
          <AspectRatio ratio={4 / 3} bg="brand.50" h="100%">
            <Image src={product.imageUrl} alt={product.name} objectFit="cover" fallbackSrc="https://placehold.co/900x680/f6f1ff/6536ab?text=Ecommerce" />
          </AspectRatio>
        </Box>

        <Stack bg="white" border="1px solid" borderColor="purple.100" borderRadius="lg" p={{ base: 5, md: 8 }} spacing={6} boxShadow="sm">
          <HStack spacing={3}>
            <Badge colorScheme="purple" borderRadius="full" px={3} py={1} textTransform="none">
              {product.category.name}
            </Badge>
            <Badge colorScheme={hasStock ? 'green' : 'red'} borderRadius="full" px={3} py={1} textTransform="none">
              {hasStock ? 'Stokta' : 'Stokta yok'}
            </Badge>
          </HStack>

          <Box>
            <Heading color="gray.900" letterSpacing="-0.04em" lineHeight="1.15">
              {product.name}
            </Heading>
            <Text color="gray.600" mt={4} lineHeight="1.8">
              {product.description}
            </Text>
          </Box>

          <Box borderTop="1px solid" borderColor="purple.100" pt={6}>
            <Text fontSize="sm" color="gray.500">
              Fiyat
            </Text>
            <Text color="brand.700" fontSize={{ base: '3xl', md: '4xl' }} fontWeight="900" letterSpacing="-0.04em">
              {formatPrice(product.price)}
            </Text>
          </Box>

          <Grid templateColumns="repeat(2, 1fr)" gap={4}>
            <Box bg="brand.50" borderRadius="lg" p={4}>
              <Text color="gray.500" fontSize="sm">
                Stok
              </Text>
              <Text color="gray.900" fontWeight="800" fontSize="xl">
                {product.stock}
              </Text>
            </Box>
            <Box bg="brand.50" borderRadius="lg" p={4}>
              <Text color="gray.500" fontSize="sm">
                Ürün no
              </Text>
              <Text color="gray.900" fontWeight="800" fontSize="xl">
                #{product.id}
              </Text>
            </Box>
          </Grid>
        </Stack>
      </Grid>
    </Stack>
  );
}
