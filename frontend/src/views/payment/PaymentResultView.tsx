import { Box, Button, Divider, Heading, HStack, Image, Skeleton, Stack, Text } from '@chakra-ui/react';
import { Link as RouterLink } from 'react-router-dom';
import { ErrorMessage } from '../../components/feedback/ErrorMessage';
import { formatPrice } from '../../helpers/formatPrice';
import { Order } from '../../types/order';
import { PaymentStatusResponse } from '../../types/payment';

interface PaymentResultViewProps {
  orderId?: number;
  callbackStatus?: string;
  paymentStatus?: PaymentStatusResponse;
  order?: Order;
  isLoading: boolean;
  isRetrying: boolean;
  errorMessage?: string;
  onRetry: () => void;
}

function title(status?: string) {
  if (status === 'success') return 'Ödeme başarılı.';
  if (status === 'failed') return 'Ödeme başarısız.';
  if (status === 'review') return 'Ödeme kontrol ediliyor.';
  return 'Ödeme sonucu';
}

function description(status?: string) {
  if (status === 'success') return 'Siparişiniz ödeme onayı aldı. Satın aldığınız ürünleri aşağıda görebilirsiniz.';
  if (status === 'failed') return 'Ödeme tamamlanamadı. Sipariş süresi dolmadan aynı sipariş için tekrar deneyebilirsiniz.';
  if (status === 'review') return 'Ödeme otomatik tamamlanamadı, kontrol gerekiyor. Lütfen sipariş durumunu takip edin.';
  return 'Ödeme sonucunu backend üzerinden doğruluyoruz.';
}

export function PaymentResultView({ orderId, callbackStatus, paymentStatus, order, isLoading, isRetrying, errorMessage, onRetry }: PaymentResultViewProps) {
  if (isLoading) {
    return <Skeleton height="320px" borderRadius="lg" startColor="purple.50" endColor="purple.100" />;
  }

  const showPurchasedItems = callbackStatus === 'success' && order;

  return (
    <Box bg="white" border="1px solid" borderColor="purple.100" borderRadius="lg" p={{ base: 6, md: 8 }} boxShadow="sm">
      <Stack spacing={6}>
        <Box>
          <Heading color="gray.900" letterSpacing="-0.04em">{title(callbackStatus)}</Heading>
          <Text color="gray.600" mt={2}>{description(callbackStatus)}</Text>
        </Box>

        {errorMessage ? <ErrorMessage message={errorMessage} /> : null}

        {showPurchasedItems ? (
          <Box bg="purple.50" border="1px solid" borderColor="purple.100" borderRadius="xl" p={{ base: 4, md: 5 }}>
            <Stack spacing={4}>
              <HStack justify="space-between" align="end">
                <Box>
                  <Text color="gray.900" fontSize="lg" fontWeight="900">Satın Aldıklarınız</Text>
                  <Text color="gray.600" fontSize="sm">Sipariş #{orderId}</Text>
                </Box>
                <Text color="brand.700" fontSize={{ base: 'xl', md: '2xl' }} fontWeight="900">
                  {formatPrice(order.totalAmount)}
                </Text>
              </HStack>

              <Divider borderColor="purple.100" />

              <Stack spacing={3} maxH="360px" overflowY="auto" pr={2}>
                {order.items.map((item) => (
                  <HStack
                    key={item.productId}
                    as={RouterLink}
                    to={`/products/${item.productId}`}
                    bg="white"
                    border="1px solid"
                    borderColor="purple.100"
                    borderRadius="xl"
                    p={3}
                    spacing={4}
                    align="center"
                    _hover={{ borderColor: 'brand.300', boxShadow: '0 10px 26px rgba(50, 25, 100, 0.1)', transform: 'translateY(-1px)' }}
                    transition="all .16s ease"
                  >
                    <Image
                      src={item.productImageUrl}
                      alt={item.productName}
                      boxSize={{ base: '64px', md: '74px' }}
                      objectFit="cover"
                      borderRadius="lg"
                      bg="purple.100"
                      flexShrink={0}
                    />
                    <Box minW={0} flex="1">
                      <Text color="gray.900" fontWeight="900" noOfLines={1}>{item.productName}</Text>
                      <Text color="gray.500" fontSize="sm">
                        {item.quantity} adet x {formatPrice(item.unitPrice)}
                      </Text>
                    </Box>
                    <Text color="gray.900" fontWeight="900" whiteSpace="nowrap">{formatPrice(item.lineTotal)}</Text>
                  </HStack>
                ))}
              </Stack>
            </Stack>
          </Box>
        ) : null}

        {callbackStatus !== 'success' ? (
          <Stack spacing={2}>
            <Text color="gray.700"><b>Sipariş:</b> #{orderId ?? '-'}</Text>
            <Text color="gray.700"><b>Durum:</b> {paymentStatus?.paymentStatus ?? '-'}</Text>
          </Stack>
        ) : null}

        <Stack spacing={2}>
          {paymentStatus?.errorCode ? <Text color="red.600"><b>Hata:</b> {paymentStatus.errorCode}</Text> : null}
        </Stack>

        <HStack spacing={3} flexWrap="wrap">
          <Button as={RouterLink} to="/orders" colorScheme="brand" borderRadius="full">Siparişlerime Git</Button>
          {callbackStatus === 'failed' ? (
            <Button variant="outline" colorScheme="brand" borderRadius="full" isLoading={isRetrying} onClick={onRetry}>
              Tekrar Dene
            </Button>
          ) : null}
          <Button as={RouterLink} to="/products" variant="ghost" colorScheme="brand" borderRadius="full">Ürünlere Dön</Button>
        </HStack>
      </Stack>
    </Box>
  );
}
