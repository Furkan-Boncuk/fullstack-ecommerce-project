import { Box, Button, Container, Flex, Heading, Spacer } from '@chakra-ui/react';
import { Link as RouterLink, Outlet, useNavigate } from 'react-router-dom';
import { useLogout } from '../api/mutations/useLogout';
import { useAuthStore } from '../store/authStore';

export function MainLayout() {
  const navigate = useNavigate();
  const logoutMutation = useLogout();
  const clearAuth = useAuthStore((state) => state.clearAuth);

  const handleLogout = () => {
    logoutMutation.mutate(undefined, {
      onSettled: () => {
        clearAuth();
        navigate('/auth/login');
      }
    });
  };

  return (
    <Box minH="100vh" bg="#f7f5fc">
      <Box bg="white" borderBottom="1px solid" borderColor="purple.100">
        <Container maxW="container.xl" py={4}>
          <Flex align="center" gap={4}>
            <Heading size="md" color="brand.700" letterSpacing="-0.02em">
              Ecommerce
            </Heading>
            <Button as={RouterLink} to="/products" size="sm" variant="ghost" colorScheme="brand" borderRadius="full">
              Ürünler
            </Button>
            <Spacer />
            <Button size="sm" variant="outline" colorScheme="brand" borderRadius="full" onClick={handleLogout}>
              Çıkış Yap
            </Button>
          </Flex>
        </Container>
      </Box>
      <Container maxW="container.xl" py={8}>
        <Outlet />
      </Container>
    </Box>
  );
}
