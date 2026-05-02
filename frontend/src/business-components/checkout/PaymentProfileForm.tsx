import { Button, Grid, Stack } from '@chakra-ui/react';
import { AppInput } from '../../components/ui/AppInput';
import { UpdatePaymentProfileRequest } from '../../types/paymentProfile';

interface PaymentProfileFormProps {
  values: UpdatePaymentProfileRequest;
  isSaving: boolean;
  onChange: (field: keyof UpdatePaymentProfileRequest, value: string) => void;
  onSubmit: () => void;
}

export function PaymentProfileForm({ values, isSaving, onChange, onSubmit }: PaymentProfileFormProps) {
  return (
    <Stack spacing={4}>
      <Grid templateColumns={{ base: '1fr', md: '1fr 1fr' }} gap={4}>
        <AppInput name="firstName" label="Ad" value={values.firstName} onChange={(value) => onChange('firstName', value)} />
        <AppInput name="lastName" label="Soyad" value={values.lastName} onChange={(value) => onChange('lastName', value)} />
        <AppInput name="phoneNumber" label="Telefon (+905551112233)" value={values.phoneNumber} onChange={(value) => onChange('phoneNumber', value)} />
        <AppInput name="identityNumber" label="T.C. Kimlik No" value={values.identityNumber} onChange={(value) => onChange('identityNumber', value)} />
        <AppInput name="city" label="Şehir" value={values.city} onChange={(value) => onChange('city', value)} />
        <AppInput name="country" label="Ülke" value={values.country} onChange={(value) => onChange('country', value)} />
        <AppInput name="zipCode" label="Posta Kodu" value={values.zipCode} onChange={(value) => onChange('zipCode', value)} />
      </Grid>
      <AppInput name="address" label="Adres" value={values.address} onChange={(value) => onChange('address', value)} />
      <Button colorScheme="brand" borderRadius="full" alignSelf="start" isLoading={isSaving} onClick={onSubmit}>
        Ödeme Bilgilerini Kaydet
      </Button>
    </Stack>
  );
}
