import { Box } from '@chakra-ui/react';
import { ReactNode } from 'react';

interface AuthPanelProps {
  children: ReactNode;
}

export function AuthPanel({ children }: AuthPanelProps) {
  return (
    <Box
      bg="white"
      p={{ base: 7, md: 9 }}
      borderRadius="3xl"
      boxShadow="0 20px 50px rgba(101, 54, 171, 0.14)"
      border="1px solid"
      borderColor="purple.100"
      minW={{ base: '100%', md: '430px' }}
    >
      {children}
    </Box>
  );
}
