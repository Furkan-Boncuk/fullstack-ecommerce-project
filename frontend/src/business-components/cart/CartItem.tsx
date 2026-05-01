import { AddIcon, DeleteIcon, MinusIcon } from '@chakra-ui/icons';
import { AspectRatio, Badge, Box, Button, Grid, HStack, IconButton, Image, Stack, Text } from '@chakra-ui/react';
import { formatPrice } from '../../helpers/formatPrice';
import { CartLine } from '../../types/cart';

interface CartItemProps {
  item: CartLine;
  isRemoving: boolean;
  isUpdating: boolean;
  onRemove: (productId: number) => void;
  onQuantityChange: (productId: number, quantity: number) => void;
}

export function CartItem({ item, isRemoving, isUpdating, onRemove, onQuantityChange }: CartItemProps) {
  const canIncrease = item.availableStock === 0 || item.quantity < item.availableStock;
  const isBusy = isRemoving || isUpdating;

  return (
    <Grid
      templateColumns={{ base: '96px 1fr', md: '120px 1fr auto' }}
      gap={4}
      bg="white"
      border="1px solid"
      borderColor="purple.100"
      borderRadius="lg"
      p={4}
      boxShadow="sm"
      alignItems="center"
    >
      <AspectRatio ratio={1} bg="brand.50" borderRadius="lg" overflow="hidden">
        <Image src={item.imageUrl} alt={item.name} objectFit="cover" fallbackSrc="https://placehold.co/320x320/f6f1ff/6536ab?text=Ecommerce" />
      </AspectRatio>

      <Stack spacing={3} minW={0}>
        <Box>
          <Text color="gray.900" fontWeight="800" noOfLines={2}>
            {item.name}
          </Text>
          <HStack mt={2} spacing={2} flexWrap="wrap">
            <Badge colorScheme="purple" borderRadius="full" px={3} py={1} textTransform="none">
              Adet: {item.quantity}
            </Badge>
            <Badge colorScheme={item.availableStock > 0 ? 'green' : 'red'} borderRadius="full" px={3} py={1} textTransform="none">
              Stok: {item.availableStock}
            </Badge>
          </HStack>
        </Box>

        <HStack spacing={2}>
          <IconButton
            aria-label="Adet azalt"
            icon={<MinusIcon />}
            size="sm"
            borderRadius="full"
            variant="outline"
            colorScheme="brand"
            isDisabled={isBusy}
            onClick={() => onQuantityChange(item.productId, item.quantity - 1)}
          />
          <Box minW="44px" textAlign="center" border="1px solid" borderColor="purple.100" borderRadius="full" px={3} py={1} fontWeight="900" color="gray.900">
            {item.quantity}
          </Box>
          <IconButton
            aria-label="Adet artır"
            icon={<AddIcon />}
            size="sm"
            borderRadius="full"
            variant="outline"
            colorScheme="brand"
            isDisabled={isBusy || !canIncrease}
            onClick={() => onQuantityChange(item.productId, item.quantity + 1)}
          />
        </HStack>

        <Grid templateColumns={{ base: '1fr', sm: 'repeat(2, 1fr)' }} gap={3}>
          <Box bg="brand.50" borderRadius="lg" p={3}>
            <Text color="gray.500" fontSize="xs">
              Birim fiyat
            </Text>
            <Text color="gray.900" fontWeight="800">
              {formatPrice(item.unitPrice)}
            </Text>
          </Box>
          <Box bg="brand.50" borderRadius="lg" p={3}>
            <Text color="gray.500" fontSize="xs">
              Toplam
            </Text>
            <Text color="brand.700" fontWeight="900">
              {formatPrice(item.lineTotal)}
            </Text>
          </Box>
        </Grid>
      </Stack>

      <Button
        leftIcon={<DeleteIcon />}
        variant="outline"
        colorScheme="red"
        borderRadius="full"
        size="sm"
        isLoading={isRemoving}
        isDisabled={isUpdating}
        onClick={() => onRemove(item.productId)}
        gridColumn={{ base: '1 / -1', md: 'auto' }}
      >
        Kaldır
      </Button>
    </Grid>
  );
}
