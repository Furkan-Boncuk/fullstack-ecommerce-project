import { Box, Button, Grid, HStack, Heading, Image, Input, NumberInput, NumberInputField, Select, Stack, Table, Tbody, Td, Text, Textarea, Th, Thead, Tr } from '@chakra-ui/react';
import { formatPrice } from '../../helpers/formatPrice';
import { AdminProduct, AdminProductPayload } from '../../types/admin';
import { Category } from '../../types/category';

interface AdminProductsViewProps {
  products: AdminProduct[];
  categories: Category[];
  page: number;
  totalPages: number;
  isLoading: boolean;
  isSaving: boolean;
  deletingProductId?: number;
  editingProductId?: number;
  form: AdminProductPayload;
  canSubmit: boolean;
  onFormChange: (form: AdminProductPayload) => void;
  onSubmit: () => void;
  onCancel: () => void;
  onEdit: (product: AdminProduct) => void;
  onDelete: (productId: number) => void;
  onPageChange: (page: number) => void;
}

export function AdminProductsView({
  products,
  categories,
  page,
  totalPages,
  isLoading,
  isSaving,
  deletingProductId,
  editingProductId,
  form,
  canSubmit,
  onFormChange,
  onSubmit,
  onCancel,
  onEdit,
  onDelete,
  onPageChange
}: AdminProductsViewProps) {
  const update = (patch: Partial<AdminProductPayload>) => onFormChange({ ...form, ...patch });

  return (
    <Stack spacing={6}>
      <Box>
        <Heading size="lg" color="gray.900">Ürün Yönetimi</Heading>
        <Text color="gray.600" mt={2}>Ürünleri ekleyin, stok ve katalog bilgilerini güncelleyin.</Text>
      </Box>

      <Box bg="white" border="1px solid" borderColor="purple.100" borderRadius="lg" p={5} boxShadow="sm">
        <Stack spacing={4}>
          <Heading size="sm">{editingProductId ? `Ürün #${editingProductId} düzenleniyor` : 'Yeni Ürün'}</Heading>
          <Grid templateColumns={{ base: '1fr', md: '1.3fr 1fr 1fr' }} gap={3}>
            <Input value={form.name} placeholder="Ürün adı" onChange={(event) => update({ name: event.target.value })} />
            <NumberInput value={form.price} min={0} precision={2} onChange={(_, value) => update({ price: Number.isFinite(value) ? value : 0 })}>
              <NumberInputField placeholder="Fiyat" />
            </NumberInput>
            <NumberInput value={form.stock} min={0} onChange={(_, value) => update({ stock: Number.isFinite(value) ? value : 0 })}>
              <NumberInputField placeholder="Stok" />
            </NumberInput>
          </Grid>
          <Grid templateColumns={{ base: '1fr', md: '1fr 1fr' }} gap={3}>
            <Select value={form.categoryId || ''} placeholder="Kategori seçin" onChange={(event) => update({ categoryId: Number(event.target.value) })}>
              {categories.map((category) => <option key={category.id} value={category.id}>{category.name}</option>)}
            </Select>
            <Input value={form.imageUrl} placeholder="Görsel URL" onChange={(event) => update({ imageUrl: event.target.value })} />
          </Grid>
          <Textarea value={form.description} placeholder="Açıklama" onChange={(event) => update({ description: event.target.value })} />
          <HStack>
            <Button colorScheme="brand" borderRadius="full" isDisabled={!canSubmit} isLoading={isSaving} onClick={onSubmit}>
              {editingProductId ? 'Güncelle' : 'Ürün Ekle'}
            </Button>
            {editingProductId ? <Button variant="ghost" borderRadius="full" onClick={onCancel}>Vazgeç</Button> : null}
          </HStack>
        </Stack>
      </Box>

      <Box bg="white" border="1px solid" borderColor="purple.100" borderRadius="lg" overflowX="auto" boxShadow="sm">
        <Table size="sm">
          <Thead>
            <Tr>
              <Th>Ürün</Th>
              <Th>Kategori</Th>
              <Th isNumeric>Fiyat</Th>
              <Th isNumeric>Stok</Th>
              <Th>Aksiyon</Th>
            </Tr>
          </Thead>
          <Tbody>
            {products.map((product) => (
              <Tr key={product.id}>
                <Td>
                  <HStack>
                    <Image src={product.imageUrl} alt={product.name} boxSize="44px" objectFit="cover" borderRadius="md" fallbackSrc="https://placehold.co/90x90/f6f1ff/6536ab?text=E" />
                    <Box>
                      <Text fontWeight="800">{product.name}</Text>
                      <Text color="gray.500" fontSize="xs">#{product.id}</Text>
                    </Box>
                  </HStack>
                </Td>
                <Td>{product.category.name}</Td>
                <Td isNumeric>{formatPrice(product.price)}</Td>
                <Td isNumeric>{product.stock}</Td>
                <Td>
                  <HStack>
                    <Button size="xs" variant="outline" colorScheme="brand" onClick={() => onEdit(product)}>Düzenle</Button>
                    <Button size="xs" colorScheme="red" variant="ghost" isLoading={deletingProductId === product.id} onClick={() => onDelete(product.id)}>Yayından Kaldır</Button>
                  </HStack>
                </Td>
              </Tr>
            ))}
          </Tbody>
        </Table>
        {!isLoading && products.length === 0 ? <Text p={5} color="gray.600">Ürün bulunamadı.</Text> : null}
      </Box>

      <HStack justify="flex-end">
        <Button size="sm" variant="outline" isDisabled={page === 0} onClick={() => onPageChange(page - 1)}>Önceki</Button>
        <Text color="gray.600" fontSize="sm">Sayfa {page + 1} / {Math.max(totalPages, 1)}</Text>
        <Button size="sm" variant="outline" isDisabled={page + 1 >= totalPages} onClick={() => onPageChange(page + 1)}>Sonraki</Button>
      </HStack>
    </Stack>
  );
}
