import { Box, Button, Divider, HStack, Stack, Text } from '@chakra-ui/react';
import { Link as RouterLink } from 'react-router-dom';
import { formatPrice } from '../../helpers/formatPrice';
import { CartSummary as CartSummaryType } from '../../types/cart';

interface CartSummaryProps {
  summary: CartSummaryType;
  isCheckoutDisabled: boolean;
}

export function CartSummary({ summary, isCheckoutDisabled }: CartSummaryProps) {
  return (
    <Box bg="white" border="1px solid" borderColor="purple.100" borderRadius="lg" p={5} boxShadow="sm">
      <Stack spacing={4}>
        <Text color="gray.900" fontWeight="900" fontSize="lg">
          Sepet Özeti
        </Text>
        <HStack justify="space-between">
          <Text color="gray.600">Ürün adedi</Text>
          <Text color="gray.900" fontWeight="800">
            {summary.itemCount}
          </Text>
        </HStack>
        <Divider borderColor="purple.100" />
        <HStack justify="space-between" align="end">
          <Text color="gray.600">Ara toplam</Text>
          <Text color="brand.700" fontWeight="900" fontSize="2xl" letterSpacing="-0.04em">
            {formatPrice(summary.subtotal)}
          </Text>
        </HStack>
        <Button
          as={RouterLink}
          to="/checkout"
          colorScheme="brand"
          borderRadius="full"
          h="48px"
          isDisabled={isCheckoutDisabled}
        >
          Ödemeye Geç
        </Button>
      </Stack>
    </Box>
  );
}
