import { Button, ButtonProps } from '@chakra-ui/react';

export function AppButton(props: ButtonProps) {
  return (
    <Button
      colorScheme="brand"
      borderRadius="full"
      boxShadow="0 10px 24px rgba(139, 70, 255, 0.35)"
      _hover={{ transform: 'translateY(-1px)', boxShadow: '0 14px 28px rgba(139, 70, 255, 0.45)' }}
      _active={{ transform: 'translateY(0)' }}
      transition="all .2s ease"
      {...props}
    />
  );
}
