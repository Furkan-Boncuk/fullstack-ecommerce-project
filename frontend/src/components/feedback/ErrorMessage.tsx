import { Alert, AlertIcon, AlertTitle } from '@chakra-ui/react';

interface ErrorMessageProps {
  message: string;
}

export function ErrorMessage({ message }: ErrorMessageProps) {
  return (
    <Alert status="error" borderRadius="md">
      <AlertIcon />
      <AlertTitle fontSize="sm">{message}</AlertTitle>
    </Alert>
  );
}
