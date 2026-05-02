import { Badge, Box, Button, Divider, Heading, HStack, Skeleton, Stack, Text } from '@chakra-ui/react';
import { Link as RouterLink } from 'react-router-dom';
import { ErrorMessage } from '../../components/feedback/ErrorMessage';
import { PaymentStatusResponse } from '../../types/payment';

interface PaymentResultViewProps {
  orderId?: number;
  callbackStatus?: string;
  paymentStatus?: PaymentStatusResponse;
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
  if (status === 'success') return 'Siparişiniz ödeme onayı aldı. Durumu Siparişlerim ekranından takip edebilirsiniz.';
  if (status === 'failed') return 'Ödeme tamamlanamadı. Sipariş süresi dolmadan aynı sipariş için tekrar deneyebilirsiniz.';
  if (status === 'review') return 'Ödeme otomatik tamamlanamadı, kontrol gerekiyor. Lütfen sipariş durumunu takip edin.';
  return 'Ödeme sonucunu backend üzerinden doğruluyoruz.';
}

function color(status?: string) {
  if (status === 'success') return 'green';
  if (status === 'failed') return 'red';
  if (status === 'review') return 'orange';
  return 'purple';
}

export function PaymentResultView({ orderId, callbackStatus, paymentStatus, isLoading, isRetrying, errorMessage, onRetry }: PaymentResultViewProps) {
  if (isLoading) {
    return <Skeleton height="320px" borderRadius="lg" startColor="purple.50" endColor="purple.100" />;
  }

  return (
    <Box bg="white" border="1px solid" borderColor="purple.100" borderRadius="lg" p={{ base: 6, md: 8 }} boxShadow="sm">
      <Stack spacing={5}>
        <HStack justify="space-between" align="start">
          <Box>
            <Heading color="gray.900" letterSpacing="-0.04em">{title(callbackStatus)}</Heading>
            <Text color="gray.600" mt={2}>{description(callbackStatus)}</Text>
          </Box>
          <Badge colorScheme={color(callbackStatus)} borderRadius="full" px={3} py={1} textTransform="none">
            {callbackStatus ?? 'pending'}
          </Badge>
        </HStack>

        {errorMessage ? <ErrorMessage message={errorMessage} /> : null}

        <Divider borderColor="purple.100" />

        <Stack spacing={2}>
          <Text color="gray.700"><b>Sipariş:</b> #{orderId ?? '-'}</Text>
          <Text color="gray.700"><b>Payment status:</b> {paymentStatus?.paymentStatus ?? '-'}</Text>
          <Text color="gray.700"><b>Attempt status:</b> {paymentStatus?.latestAttemptStatus ?? '-'}</Text>
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
