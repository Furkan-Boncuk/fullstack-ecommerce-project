import { Box, Container, Flex, Heading, HStack, Link, Text } from '@chakra-ui/react';
import { Link as RouterLink, Outlet, useLocation } from 'react-router-dom';

export function AuthLayout() {
  const location = useLocation();
  const isLogin = location.pathname.includes('/login');

  return (
    <Box minH="100vh" bg="#f7f5fc">
      <Container maxW="container.lg" py={{ base: 8, md: 12 }}>
        <Flex justify="space-between" align="center" mb={{ base: 8, md: 10 }}>
          <Heading letterSpacing="-0.02em" color="brand.700" fontSize={{ base: '2xl', md: '3xl' }}>
            Ecommerce
          </Heading>
          <HStack spacing={2}>
            <Text color="gray.600" fontWeight="medium" fontSize="sm">
              {isLogin ? 'Hesabınız yok mu?' : 'Zaten hesabınız var mı?'}
            </Text>
            <Link as={RouterLink} color="brand.600" fontWeight="semibold" to={isLogin ? '/auth/register' : '/auth/login'}>
              {isLogin ? 'Kayıt Ol' : 'Giriş Yap'}
            </Link>
          </HStack>
        </Flex>

        <Flex justify="center">
          <Box w="full" maxW="470px">
            <Outlet />
          </Box>
        </Flex>
      </Container>
    </Box>
  );
}
