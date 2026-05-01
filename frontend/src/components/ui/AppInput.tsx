import { FormControl, FormErrorMessage, FormLabel, Input } from '@chakra-ui/react';

interface AppInputProps {
  label: string;
  name: string;
  type?: string;
  value: string;
  onChange: (value: string) => void;
  error?: string;
}

export function AppInput({ label, name, type = 'text', value, onChange, error }: AppInputProps) {
  return (
    <FormControl isInvalid={Boolean(error)}>
      <FormLabel htmlFor={name} fontSize="sm" color="gray.700">
        {label}
      </FormLabel>
      <Input
        id={name}
        name={name}
        type={type}
        value={value}
        onChange={(event) => onChange(event.target.value)}
        bg="white"
        color="gray.800"
        borderColor="purple.100"
        h="48px"
        borderRadius="xl"
        _placeholder={{ color: 'gray.400' }}
        _hover={{ borderColor: 'brand.300' }}
        _focusVisible={{ borderColor: 'brand.500', boxShadow: '0 0 0 4px rgba(154,89,255,0.18)' }}
      />
      {error ? <FormErrorMessage>{error}</FormErrorMessage> : null}
    </FormControl>
  );
}
