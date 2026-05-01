import { Box, Heading, Text } from '@chakra-ui/react';

export function HomeView() {
  return (
    <Box bg="white" p={8} borderRadius="xl" border="1px solid" borderColor="purple.100" boxShadow="sm">
      <Heading size="md" color="gray.800">Ecommerce</Heading>
      <Text mt={2} color="gray.600">
        Giriş başarılı.
      </Text>
    </Box>
  );
}
