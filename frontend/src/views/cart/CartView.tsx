import { Box, Button, Grid, Heading, Skeleton, Stack, Text } from '@chakra-ui/react';
import { Link as RouterLink } from 'react-router-dom';
import { CartItem } from '../../business-components/cart/CartItem';
import { CartSummary } from '../../business-components/cart/CartSummary';
import { ErrorMessage } from '../../components/feedback/ErrorMessage';
import { Cart } from '../../types/cart';

interface CartViewProps {
  cart?: Cart;
  isLoading: boolean;
  errorMessage?: string;
  removingProductId?: number;
  updatingProductId?: number;
  onRemove: (productId: number) => void;
  onQuantityChange: (productId: number, quantity: number) => void;
}

export function CartView({ cart, isLoading, errorMessage, removingProductId, updatingProductId, onRemove, onQuantityChange }: CartViewProps) {
  const isEmpty = !cart || cart.items.length === 0;

  return (
    <Stack spacing={6}>
      <Box>
        <Heading color="gray.900" letterSpacing="-0.04em" size="lg">
          Sepetim
        </Heading>
        <Text color="gray.600" mt={2}>
          Sepet ürünleri backend cart servisi üzerinden güncellenir.
        </Text>
      </Box>

      {errorMessage ? <ErrorMessage message={errorMessage} /> : null}

      {isLoading ? (
        <Grid templateColumns={{ base: '1fr', lg: '1fr 340px' }} gap={6} alignItems="start">
          <Stack spacing={4}>
            {Array.from({ length: 3 }).map((_, index) => (
              <Skeleton key={index} height="156px" borderRadius="lg" startColor="purple.50" endColor="purple.100" />
            ))}
          </Stack>
          <Skeleton height="260px" borderRadius="lg" startColor="purple.50" endColor="purple.100" />
        </Grid>
      ) : null}

      {!isLoading && !errorMessage && isEmpty ? (
        <Box bg="white" border="1px solid" borderColor="purple.100" borderRadius="lg" p={8} textAlign="center" boxShadow="sm">
          <Heading size="sm" color="gray.900">
            Sepetiniz boş.
          </Heading>
          <Text color="gray.600" mt={2}>
            Ürünleri inceleyip sepete ekleyebilirsiniz.
          </Text>
          <Button as={RouterLink} to="/products" colorScheme="brand" borderRadius="full" mt={5}>
            Ürünlere Git
          </Button>
        </Box>
      ) : null}

      {!isLoading && !errorMessage && cart && !isEmpty ? (
        <Grid templateColumns={{ base: '1fr', lg: '1fr 340px' }} gap={6} alignItems="start">
          <Stack spacing={4}>
            {cart.items.map((item) => (
              <CartItem
                key={item.productId}
                item={item}
                isRemoving={removingProductId === item.productId}
                isUpdating={updatingProductId === item.productId}
                onRemove={onRemove}
                onQuantityChange={onQuantityChange}
              />
            ))}
          </Stack>
          <CartSummary summary={cart.summary} isCheckoutDisabled={cart.items.length === 0} />
        </Grid>
      ) : null}
    </Stack>
  );
}
