import { Stack, Text } from '@chakra-ui/react';
import { AppButton } from '../../components/ui/AppButton';
import { AppInput } from '../../components/ui/AppInput';

export interface AuthFormValues {
  email: string;
  password: string;
}

interface AuthFormProps {
  title: string;
  submitLabel: string;
  values: AuthFormValues;
  errors: Partial<AuthFormValues>;
  loading: boolean;
  onEmailChange: (value: string) => void;
  onPasswordChange: (value: string) => void;
  onSubmit: () => void;
}

export function AuthForm({
  title,
  submitLabel,
  values,
  errors,
  loading,
  onEmailChange,
  onPasswordChange,
  onSubmit
}: AuthFormProps) {
  return (
    <Stack spacing={5}>
      <Stack spacing={1}>
        <Text fontSize={{ base: '2xl', md: '3xl' }} fontWeight="bold" color="gray.900" letterSpacing="-0.03em">
          {title}
        </Text>
      </Stack>

      <AppInput
        label="E-posta"
        name="email"
        type="email"
        value={values.email}
        onChange={onEmailChange}
        error={errors.email}
      />
      <AppInput
        label="Şifre"
        name="password"
        type="password"
        value={values.password}
        onChange={onPasswordChange}
        error={errors.password}
      />
      <AppButton h="50px" fontWeight="bold" isLoading={loading} onClick={onSubmit}>
        {submitLabel}
      </AppButton>
    </Stack>
  );
}
