import { Box, Button, Heading, Skeleton, Stack, Text } from '@chakra-ui/react';
import { Link as RouterLink } from 'react-router-dom';
import { OrderCard } from '../../business-components/order/OrderCard';
import { ErrorMessage } from '../../components/feedback/ErrorMessage';
import { Order } from '../../types/order';

interface OrdersViewProps {
  orders: Order[];
  isLoading: boolean;
  payingOrderId?: number;
  errorMessage?: string;
  onPay: (orderId: number) => void;
}

export function OrdersView({ orders, isLoading, payingOrderId, errorMessage, onPay }: OrdersViewProps) {
  return (
    <Stack spacing={6}>
      <Box>
        <Heading color="gray.900" letterSpacing="-0.04em" size="lg">Siparişlerim</Heading>
        <Text color="gray.600" mt={2}>Sipariş durumlarınızı ve ürün snapshotlarını buradan takip edebilirsiniz.</Text>
      </Box>

      {errorMessage ? <ErrorMessage message={errorMessage} /> : null}

      {isLoading ? (
        <Stack spacing={4}>
          {Array.from({ length: 3 }).map((_, index) => (
            <Skeleton key={index} height="220px" borderRadius="lg" startColor="purple.50" endColor="purple.100" />
          ))}
        </Stack>
      ) : null}

      {!isLoading && !errorMessage && orders.length === 0 ? (
        <Box bg="white" border="1px solid" borderColor="purple.100" borderRadius="lg" p={8} textAlign="center" boxShadow="sm">
          <Heading size="sm" color="gray.900">Henüz siparişiniz yok.</Heading>
          <Text color="gray.600" mt={2}>Ürünleri inceleyip ilk siparişinizi oluşturabilirsiniz.</Text>
          <Button as={RouterLink} to="/products" colorScheme="brand" borderRadius="full" mt={5}>Ürünlere Git</Button>
        </Box>
      ) : null}

      {!isLoading && orders.length > 0 ? (
        <Stack spacing={4}>
          {orders.map((order) => <OrderCard key={order.id} order={order} isPaying={payingOrderId === order.id} onPay={onPay} />)}
        </Stack>
      ) : null}
    </Stack>
  );
}
