import { Box, Button, Grid, Heading, Stack, Text } from '@chakra-ui/react';
import { Link as RouterLink } from 'react-router-dom';

export function AdminHomeView() {
  return (
    <Stack spacing={6}>
      <Box>
        <Heading size="lg" color="gray.900">Admin Paneli</Heading>
        <Text color="gray.600" mt={2}>Katalog ve sipariş operasyonlarını buradan yönetin.</Text>
      </Box>
      <Grid templateColumns={{ base: '1fr', md: '1fr 1fr' }} gap={4}>
        <Box bg="white" border="1px solid" borderColor="purple.100" borderRadius="lg" p={6} boxShadow="sm">
          <Heading size="sm">Ürün Yönetimi</Heading>
          <Text color="gray.600" mt={2}>Ürün ekleyin, stok ve katalog bilgilerini güncelleyin.</Text>
          <Button as={RouterLink} to="/admin/products" colorScheme="brand" borderRadius="full" mt={5}>Ürünlere Git</Button>
        </Box>
        <Box bg="white" border="1px solid" borderColor="purple.100" borderRadius="lg" p={6} boxShadow="sm">
          <Heading size="sm">Sipariş Yönetimi</Heading>
          <Text color="gray.600" mt={2}>Müşteri ve teslimat bilgileriyle siparişleri takip edin.</Text>
          <Button as={RouterLink} to="/admin/orders" colorScheme="brand" borderRadius="full" mt={5}>Siparişlere Git</Button>
        </Box>
      </Grid>
    </Stack>
  );
}
