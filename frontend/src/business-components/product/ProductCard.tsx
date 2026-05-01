import { ArrowForwardIcon, CheckIcon } from '@chakra-ui/icons';
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
  isAddingToCart: boolean;
  isInCart: boolean;
  onAddToCart: (productId: number) => void;
}

export function ProductCard({ product, isAddingToCart, isInCart, onAddToCart }: ProductCardProps) {
  const hasStock = product.stock > 0;

  return (
    <Box
      position="relative"
      bg={isInCart ? 'brand.50' : 'white'}
      border="1px solid"
      borderColor={isInCart ? 'brand.400' : 'purple.100'}
      borderRadius="lg"
      overflow="hidden"
      boxShadow={isInCart ? '0 20px 44px rgba(101, 54, 171, 0.16)' : '0 16px 36px rgba(74, 37, 125, 0.08)'}
      transition="all .18s ease"
      _hover={{ transform: 'translateY(-3px)', borderColor: 'brand.300', boxShadow: '0 20px 42px rgba(74, 37, 125, 0.13)' }}
    >
      {isInCart ? (
        <Badge
          position="absolute"
          top={3}
          right={3}
          zIndex={1}
          colorScheme="purple"
          borderRadius="full"
          px={3}
          py={1}
          textTransform="none"
          display="inline-flex"
          alignItems="center"
          gap={1}
          boxShadow="0 10px 22px rgba(56, 29, 92, 0.18)"
        >
          <CheckIcon boxSize="10px" /> Sepette
        </Badge>
      ) : null}

      <AspectRatio ratio={4 / 3} bg="brand.50">
        <Image src={product.imageUrl} alt={product.name} objectFit="cover" fallbackSrc="https://placehold.co/640x480/f6f1ff/6536ab?text=Ecommerce" />
      </AspectRatio>

      <Stack p={5} spacing={4} minH="300px">
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

        <Stack spacing={3}>
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

          <Button
            colorScheme="brand"
            variant={isInCart ? 'outline' : 'solid'}
            borderRadius="full"
            isDisabled={!hasStock}
            isLoading={isAddingToCart}
            onClick={() => onAddToCart(product.id)}
          >
            {isInCart ? 'Sepette' : 'Sepete Ekle'}
          </Button>
        </Stack>
      </Stack>
    </Box>
  );
}
