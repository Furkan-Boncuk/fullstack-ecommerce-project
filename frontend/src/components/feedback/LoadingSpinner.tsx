import { Center, Spinner } from '@chakra-ui/react';

export function LoadingSpinner() {
  return (
    <Center py={8}>
      <Spinner color="brand.500" size="lg" />
    </Center>
  );
}
