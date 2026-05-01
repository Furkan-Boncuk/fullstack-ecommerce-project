import { Box, Button, Heading, Stack, Text } from '@chakra-ui/react';
import { Link as RouterLink } from 'react-router-dom';

export function CheckoutView() {
  return (
    <Box bg="white" border="1px solid" borderColor="purple.100" borderRadius="lg" p={8} boxShadow="sm">
      <Stack spacing={4}>
        <Heading size="md" color="gray.900">
          Ödeme ekranı sonraki adımda tamamlanacak.
        </Heading>
        <Text color="gray.600">
          Bu adımda sepet işlemleri hazırlandı. Order ve ödeme akışı sonraki parçada bağlanacak.
        </Text>
        <Button as={RouterLink} to="/cart" colorScheme="brand" borderRadius="full" alignSelf="start">
          Sepete Dön
        </Button>
      </Stack>
    </Box>
  );
}
