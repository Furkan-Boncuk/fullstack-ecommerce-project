import { Badge, Box, Button, Divider, HStack, Image, Stack, Text } from '@chakra-ui/react';
import { formatPrice } from '../../helpers/formatPrice';
import { orderStatusColor, orderStatusLabel } from '../../helpers/orderStatus';
import { Order } from '../../types/order';

interface OrderCardProps {
  order: Order;
  isPaying?: boolean;
  onPay?: (orderId: number) => void;
}

export function OrderCard({ order, isPaying, onPay }: OrderCardProps) {
  const canPay = order.status === 'PENDING' || order.status === 'PAYMENT_FAILED';

  return (
    <Box bg="white" border="1px solid" borderColor="purple.100" borderRadius="lg" p={5} boxShadow="sm">
      <Stack spacing={4}>
        <HStack justify="space-between" align="start">
          <Stack spacing={1}>
            <Text color="gray.900" fontWeight="900">Sipariş #{order.id}</Text>
            <Text color="gray.500" fontSize="sm">{new Date(order.createdAt).toLocaleString('tr-TR')}</Text>
          </Stack>
          <Badge colorScheme={orderStatusColor(order.status)} borderRadius="full" px={3} py={1} textTransform="none">
            {orderStatusLabel(order.status)}
          </Badge>
        </HStack>
        <Divider borderColor="purple.100" />
        <Stack spacing={3}>
          {order.items.map((item) => (
            <HStack key={`${order.id}-${item.productId}`} spacing={3} align="center">
              <Image src={item.productImageUrl} alt={item.productName} boxSize="54px" objectFit="cover" borderRadius="md" fallbackSrc="https://placehold.co/120x120/f6f1ff/6536ab?text=E" />
              <Stack spacing={0} flex="1">
                <Text color="gray.900" fontWeight="800">{item.productName}</Text>
                <Text color="gray.500" fontSize="sm">{item.quantity} adet x {formatPrice(item.unitPrice)}</Text>
              </Stack>
              <Text color="brand.700" fontWeight="900">{formatPrice(item.lineTotal)}</Text>
            </HStack>
          ))}
        </Stack>
        <Divider borderColor="purple.100" />
        <HStack justify="space-between" align="center">
          <Stack spacing={0}>
            <Text color="gray.600">Toplam</Text>
            <Text color="brand.700" fontSize="xl" fontWeight="900">{formatPrice(order.totalAmount)}</Text>
          </Stack>
          {canPay && onPay ? (
            <Button colorScheme="brand" borderRadius="full" isLoading={isPaying} onClick={() => onPay(order.id)}>
              Ödemeyi Tamamla
            </Button>
          ) : null}
        </HStack>
      </Stack>
    </Box>
  );
}
