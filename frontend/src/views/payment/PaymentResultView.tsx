import { Badge, Box, Button, Divider, Heading, HStack, Image, Skeleton, Stack, Text } from '@chakra-ui/react';
import { Link as RouterLink } from 'react-router-dom';
import { ErrorMessage } from '../../components/feedback/ErrorMessage';
import { formatPrice } from '../../helpers/formatPrice';
import { orderStatusColor, orderStatusLabel } from '../../helpers/orderStatus';
import { Order } from '../../types/order';
import { PaymentStatusResponse } from '../../types/payment';

interface PaymentResultViewProps {
  orderId?: number;
  paymentStatus?: PaymentStatusResponse;
  order?: Order;
  isLoading: boolean;
  isRetrying: boolean;
  errorMessage?: string;
  onRetry: () => void;
}

type ResultState = 'success' | 'failed' | 'review' | 'pending' | 'unknown';

function resultState(order?: Order, paymentStatus?: PaymentStatusResponse): ResultState {
  if (!order) return 'unknown';
  if (order?.status === 'PAID' || paymentStatus?.paymentStatus === 'SUCCEEDED') return 'success';
  if (order?.status === 'PAYMENT_FAILED' || paymentStatus?.paymentStatus === 'FAILED') return 'failed';
  if (order?.status === 'REQUIRES_REVIEW' || paymentStatus?.paymentStatus === 'REQUIRES_REVIEW') return 'review';
  if (
    order?.status === 'PENDING' ||
    paymentStatus?.paymentStatus === 'INITIATED' ||
    paymentStatus?.paymentStatus === 'ACTION_REQUIRED' ||
    paymentStatus?.paymentStatus === 'NOT_STARTED'
  ) {
    return 'pending';
  }
  return 'unknown';
}

function title(state: ResultState) {
  if (state === 'success') return 'Ödeme başarılı.';
  if (state === 'failed') return 'Ödeme başarısız.';
  if (state === 'review') return 'Ödeme kontrol ediliyor.';
  if (state === 'pending') return 'Ödeme sonucu doğrulanıyor.';
  return 'Ödeme sonucu bulunamadı.';
}

function description(state: ResultState) {
  if (state === 'success') return 'Siparişiniz ödeme onayı aldı. Satın aldığınız ürünleri aşağıda görebilirsiniz.';
  if (state === 'failed') return 'Ödeme tamamlanamadı. Sipariş süresi dolmadan aynı sipariş için tekrar deneyebilirsiniz.';
  if (state === 'review') return 'Ödeme otomatik tamamlanamadı, kontrol gerekiyor. Sipariş durumunu buradan takip edebilirsiniz.';
  if (state === 'pending') return 'Ödeme sağlayıcısından gelen sonucu backend üzerinden doğruluyoruz. Birazdan tekrar kontrol edebilirsiniz.';
  return 'Bu sipariş için doğrulanmış bir ödeme sonucu gösterilemiyor.';
}

export function PaymentResultView({ orderId, paymentStatus, order, isLoading, isRetrying, errorMessage, onRetry }: PaymentResultViewProps) {
  if (isLoading) {
    return <Skeleton height="320px" borderRadius="lg" startColor="purple.50" endColor="purple.100" />;
  }

  const state = resultState(order, paymentStatus);
  const showPurchasedItems = state === 'success' && order;
  const canRetry = state === 'failed' || state === 'pending';

  return (
    <Stack spacing={6}>
      <Box>
        <Heading color="gray.900" size="lg">
          {title(state)}
        </Heading>
        <Text color="gray.600" mt={2}>
          {description(state)}
        </Text>
      </Box>

      {errorMessage ? <ErrorMessage message={errorMessage} /> : null}

      <Box bg="white" border="1px solid" borderColor="purple.100" borderRadius="lg" p={{ base: 5, md: 6 }} boxShadow="sm">
        <Stack spacing={5}>
          <HStack justify="space-between" align="start" gap={4}>
            <Stack spacing={1}>
              <Text color="gray.500" fontSize="sm">Sipariş</Text>
              <Text color="gray.900" fontSize="xl" fontWeight="900">#{orderId ?? '-'}</Text>
            </Stack>

            {order ? (
              <Badge colorScheme={orderStatusColor(order.status)} borderRadius="full" px={3} py={1} textTransform="none">
                {orderStatusLabel(order.status)}
              </Badge>
            ) : null}
          </HStack>

          <Divider borderColor="purple.100" />

          <Stack spacing={2}>
            {order?.createdAt ? <Text color="gray.700"><b>İşlem tarihi:</b> {new Date(order.createdAt).toLocaleString('tr-TR')}</Text> : null}
            {state !== 'success' && paymentStatus?.errorCode ? <Text color="red.600"><b>Hata:</b> {paymentStatus.errorCode}</Text> : null}
          </Stack>
        </Stack>
      </Box>

      {order ? (
        <Box bg="purple.50" border="1px solid" borderColor="purple.100" borderRadius="lg" p={{ base: 4, md: 5 }}>
          <Stack spacing={4}>
            <HStack justify="space-between" align="end" gap={4}>
              <Text color="gray.900" fontSize="lg" fontWeight="900">
                {showPurchasedItems ? 'Satın Aldıklarınız' : 'Sipariş Ürünleri'}
              </Text>
              <Text color="brand.700" fontSize={{ base: 'xl', md: '2xl' }} fontWeight="900">{formatPrice(order.totalAmount)}</Text>
            </HStack>

            <Stack spacing={3}>
              {order.items.map((item) => (
                <HStack key={item.productId} as={RouterLink} to={`/products/${item.productId}`} bg="white" border="1px solid" borderColor="purple.100" borderRadius="lg" p={3} spacing={4} align="center" _hover={{ borderColor: 'brand.300', transform: 'translateY(-1px)' }} transition="all .16s ease">
                  <Image src={item.productImageUrl} alt={item.productName} boxSize={{ base: '56px', md: '64px' }} objectFit="cover" borderRadius="md" bg="purple.100" flexShrink={0} fallbackSrc="https://placehold.co/120x120/f6f1ff/6536ab?text=E" />
                  <Box minW={0} flex="1">
                    <Text color="gray.900" fontWeight="900" noOfLines={1}>{item.productName}</Text>
                    <Text color="gray.500" fontSize="sm">{item.quantity} adet x {formatPrice(item.unitPrice)}</Text>
                  </Box>
                  <Text color="gray.900" fontWeight="900" whiteSpace="nowrap">{formatPrice(item.lineTotal)}</Text>
                </HStack>
              ))}
            </Stack>
          </Stack>
        </Box>
      ) : null}

      <HStack spacing={3} flexWrap="wrap">
        <Button as={RouterLink} to="/orders" colorScheme="brand" borderRadius="full">Siparişlerime Git</Button>
        {canRetry ? (
          <Button variant="outline" colorScheme="brand" borderRadius="full" isLoading={isRetrying} onClick={onRetry}>
            Tekrar Dene
          </Button>
        ) : null}
        <Button as={RouterLink} to="/products" variant="ghost" colorScheme="brand" borderRadius="full">Ürünlere Dön</Button>
      </HStack>
    </Stack>
  );
}
