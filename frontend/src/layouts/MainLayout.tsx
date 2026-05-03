import { Box, Button, Container, Flex, Heading, IconButton, Spacer, Text } from '@chakra-ui/react';
import { Link as RouterLink, Outlet, useNavigate } from 'react-router-dom';
import { useLogout } from '../api/mutations/useLogout';
import { useCart } from '../api/queries/useCart';
import { useAuthStore } from '../store/authStore';

function ShoppingCartIcon() {
  return (
    <Box
      as="svg"
      viewBox="0 0 24 24"
      boxSize="19px"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <path d="M6 6h15l-1.5 8.5H8L6 3H3" />
      <circle cx="9" cy="20" r="1" />
      <circle cx="18" cy="20" r="1" />
    </Box>
  );
}

export function MainLayout() {
  const navigate = useNavigate();
  const logoutMutation = useLogout();
  const clearAuth = useAuthStore((state) => state.clearAuth);
  const cartQuery = useCart();
  const itemCount = cartQuery.data?.summary?.itemCount ?? 0;

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
            <Heading as={RouterLink} to="/" size="md" color="brand.700" letterSpacing="-0.02em">
              Ecommerce
            </Heading>
            <Button as={RouterLink} to="/products" size="sm" variant="ghost" colorScheme="brand" borderRadius="full">
              Ürünler
            </Button>
            <Button as={RouterLink} to="/orders" size="sm" variant="ghost" colorScheme="brand" borderRadius="full">
              Siparişlerim
            </Button>
            <Spacer />
            <Box position="relative">
              <IconButton
                as={RouterLink}
                to="/cart"
                aria-label="Sepet"
                icon={<ShoppingCartIcon />}
                size="sm"
                variant="ghost"
                colorScheme="brand"
                borderRadius="full"
                fontSize="19px"
              />
              {itemCount > 0 ? (
                <Flex
                  position="absolute"
                  top="-7px"
                  right="-7px"
                  minW="20px"
                  h="20px"
                  px="6px"
                  align="center"
                  justify="center"
                  bg="brand.600"
                  color="white"
                  border="2px solid white"
                  borderRadius="full"
                  boxShadow="0 8px 18px rgba(101, 54, 171, 0.22)"
                >
                  <Text as="span" fontSize="10px" fontWeight="900" lineHeight="1">
                    {itemCount > 99 ? '99+' : itemCount}
                  </Text>
                </Flex>
              ) : null}
            </Box>
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
