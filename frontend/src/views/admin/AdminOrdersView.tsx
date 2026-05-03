import { Badge, Box, Button, Grid, HStack, Heading, Input, Select, Stack, Table, Tbody, Td, Text, Th, Thead, Tr } from '@chakra-ui/react';
import { formatPrice } from '../../helpers/formatPrice';
import { orderStatusColor, orderStatusLabel } from '../../helpers/orderStatus';
import { AdminOrder } from '../../types/admin';

interface AdminOrdersViewProps {
  orders: AdminOrder[];
  page: number;
  totalPages: number;
  isLoading: boolean;
  filters: { status: string; userId: string; email: string };
  onFiltersChange: (filters: { status: string; userId: string; email: string }) => void;
  onPageChange: (page: number) => void;
}

export function AdminOrdersView({ orders, page, totalPages, isLoading, filters, onFiltersChange, onPageChange }: AdminOrdersViewProps) {
  const update = (patch: Partial<typeof filters>) => onFiltersChange({ ...filters, ...patch });

  return (
    <Stack spacing={6}>
      <Box>
        <Heading size="lg" color="gray.900">Sipariş Yönetimi</Heading>
        <Text color="gray.600" mt={2}>Tüm siparişleri, müşteri bilgisini ve teslimat snapshotlarını takip edin.</Text>
      </Box>

      <Box bg="white" border="1px solid" borderColor="purple.100" borderRadius="lg" p={4} boxShadow="sm">
        <Grid templateColumns={{ base: '1fr', md: '1fr 1fr 1fr auto' }} gap={3}>
          <Select value={filters.status} placeholder="Tüm durumlar" onChange={(event) => update({ status: event.target.value })}>
            <option value="PENDING">Ödeme bekleniyor</option>
            <option value="PAYMENT_FAILED">Ödeme başarısız</option>
            <option value="PAID">Ödendi</option>
            <option value="EXPIRED">Süresi doldu</option>
            <option value="REQUIRES_REVIEW">Kontrol gerekiyor</option>
          </Select>
          <Input value={filters.email} placeholder="Müşteri e-postası" onChange={(event) => update({ email: event.target.value })} />
          <Input value={filters.userId} placeholder="User ID" onChange={(event) => update({ userId: event.target.value })} />
          <Button variant="ghost" onClick={() => onFiltersChange({ status: '', userId: '', email: '' })}>Temizle</Button>
        </Grid>
      </Box>

      <Box bg="white" border="1px solid" borderColor="purple.100" borderRadius="lg" overflowX="auto" boxShadow="sm">
        <Table size="sm">
          <Thead>
            <Tr>
              <Th>Sipariş</Th>
              <Th>Müşteri</Th>
              <Th>Durum</Th>
              <Th>Teslimat</Th>
              <Th isNumeric>Toplam</Th>
            </Tr>
          </Thead>
          <Tbody>
            {orders.map((order) => (
              <Tr key={order.id}>
                <Td>
                  <Text fontWeight="900">#{order.id}</Text>
                  <Text color="gray.500" fontSize="xs">{new Date(order.createdAt).toLocaleString('tr-TR')}</Text>
                  <Text color="gray.600" fontSize="xs">{order.items.length} ürün satırı</Text>
                </Td>
                <Td>
                  <Text fontWeight="800">{order.userEmail ?? 'E-posta bulunamadı'}</Text>
                  <Text color="gray.500" fontSize="xs">User #{order.userId}</Text>
                </Td>
                <Td>
                  <Badge colorScheme={orderStatusColor(order.status)} borderRadius="full" px={3} py={1} textTransform="none">
                    {orderStatusLabel(order.status)}
                  </Badge>
                </Td>
                <Td minW="280px">
                  {order.shippingAddress ? (
                    <Stack spacing={0}>
                      <Text fontWeight="800">{order.shippingAddress.firstName} {order.shippingAddress.lastName}</Text>
                      <Text color="gray.600" fontSize="sm">{order.shippingAddress.phoneNumber}</Text>
                      <Text color="gray.600" fontSize="sm">{order.shippingAddress.address}</Text>
                      <Text color="gray.500" fontSize="xs">{order.shippingAddress.city}, {order.shippingAddress.country} {order.shippingAddress.zipCode}</Text>
                    </Stack>
                  ) : (
                    <Text color="gray.500" fontSize="sm">Teslimat bilgisi bu siparişte kayıtlı değil.</Text>
                  )}
                </Td>
                <Td isNumeric fontWeight="900" color="brand.700">{formatPrice(order.totalAmount)}</Td>
              </Tr>
            ))}
          </Tbody>
        </Table>
        {!isLoading && orders.length === 0 ? <Text p={5} color="gray.600">Sipariş bulunamadı.</Text> : null}
      </Box>

      <HStack justify="flex-end">
        <Button size="sm" variant="outline" isDisabled={page === 0} onClick={() => onPageChange(page - 1)}>Önceki</Button>
        <Text color="gray.600" fontSize="sm">Sayfa {page + 1} / {Math.max(totalPages, 1)}</Text>
        <Button size="sm" variant="outline" isDisabled={page + 1 >= totalPages} onClick={() => onPageChange(page + 1)}>Sonraki</Button>
      </HStack>
    </Stack>
  );
}
