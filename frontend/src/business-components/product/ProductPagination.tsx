import { Button, HStack, Text } from '@chakra-ui/react';

interface ProductPaginationProps {
  page: number;
  totalPages: number;
  isLast: boolean;
  onPageChange: (page: number) => void;
}

export function ProductPagination({ page, totalPages, isLast, onPageChange }: ProductPaginationProps) {
  if (totalPages <= 1) {
    return null;
  }

  return (
    <HStack justify="center" spacing={4} pt={2}>
      <Button
        variant="outline"
        colorScheme="brand"
        borderRadius="full"
        isDisabled={page === 0}
        onClick={() => onPageChange(page - 1)}
      >
        Önceki
      </Button>
      <Text color="gray.700" fontWeight="700">
        Sayfa {page + 1} / {totalPages}
      </Text>
      <Button
        variant="outline"
        colorScheme="brand"
        borderRadius="full"
        isDisabled={isLast}
        onClick={() => onPageChange(page + 1)}
      >
        Sonraki
      </Button>
    </HStack>
  );
}
