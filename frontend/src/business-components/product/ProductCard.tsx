import { ArrowForwardIcon } from '@chakra-ui/icons';
import {
  AspectRatio,
  Badge,
  Box,
  Button,
  HStack,
  Heading,
  Image,
  Stack,
  Text
} from '@chakra-ui/react';
import { Link as RouterLink } from 'react-router-dom';
import { formatPrice } from '../../helpers/formatPrice';
import { Product } from '../../types/product';

interface ProductCardProps {
  product: Product;
}

export function ProductCard({ product }: ProductCardProps) {
  const hasStock = product.stock > 0;

  return (
    <Box
      bg="white"
      border="1px solid"
      borderColor="purple.100"
      borderRadius="lg"
      overflow="hidden"
      boxShadow="0 16px 36px rgba(74, 37, 125, 0.08)"
      transition="all .18s ease"
      _hover={{ transform: 'translateY(-3px)', borderColor: 'brand.300', boxShadow: '0 20px 42px rgba(74, 37, 125, 0.13)' }}
    >
      <AspectRatio ratio={4 / 3} bg="brand.50">
        <Image src={product.imageUrl} alt={product.name} objectFit="cover" fallbackSrc="https://placehold.co/640x480/f6f1ff/6536ab?text=Ecommerce" />
      </AspectRatio>

      <Stack p={5} spacing={4} minH="260px">
        <HStack justify="space-between" align="start" gap={3}>
          <Badge colorScheme="purple" borderRadius="full" px={3} py={1} textTransform="none">
            {product.category.name}
          </Badge>
          <Badge colorScheme={hasStock ? 'green' : 'red'} borderRadius="full" px={3} py={1} textTransform="none">
            {hasStock ? 'Stokta' : 'Stokta yok'}
          </Badge>
        </HStack>

        <Stack spacing={2} flex="1">
          <Heading as="h3" size="sm" color="gray.900" noOfLines={2} lineHeight="1.35">
            {product.name}
          </Heading>
          <Text color="gray.600" fontSize="sm" noOfLines={3}>
            {product.description}
          </Text>
        </Stack>

        <HStack justify="space-between" align="center">
          <Box>
            <Text fontSize="xs" color="gray.500">
              Fiyat
            </Text>
            <Text fontWeight="800" color="brand.700" fontSize="lg">
              {formatPrice(product.price)}
            </Text>
          </Box>
          <Button
            as={RouterLink}
            to={`/products/${product.id}`}
            rightIcon={<ArrowForwardIcon />}
            colorScheme="brand"
            variant="outline"
            borderRadius="full"
            size="sm"
          >
            Detaya Git
          </Button>
        </HStack>
      </Stack>
    </Box>
  );
}
