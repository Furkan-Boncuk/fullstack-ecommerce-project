import { ChevronDownIcon } from '@chakra-ui/icons';
import { Button, FormControl, FormLabel, Grid, Input, Menu, MenuButton, MenuItem, MenuList, Stack } from '@chakra-ui/react';
import { AppInput } from '../../components/ui/AppInput';
import { TURKEY_COUNTRY_LABEL } from '../../constants/location';
import { City } from '../../types/location';
import { UpdatePaymentProfileRequest } from '../../types/paymentProfile';

interface PaymentProfileFormProps {
  values: UpdatePaymentProfileRequest;
  cities: City[];
  isCityLoading: boolean;
  isSaving: boolean;
  onChange: (field: keyof UpdatePaymentProfileRequest, value: string) => void;
  onSubmit: () => void;
}

export function PaymentProfileForm({ values, cities, isCityLoading, isSaving, onChange, onSubmit }: PaymentProfileFormProps) {
  return (
    <Stack spacing={4}>
      <Grid templateColumns={{ base: '1fr', md: '1fr 1fr' }} gap={4}>
        <AppInput name="firstName" label="Ad" value={values.firstName} onChange={(value) => onChange('firstName', value)} />
        <AppInput name="lastName" label="Soyad" value={values.lastName} onChange={(value) => onChange('lastName', value)} />
        <AppInput name="phoneNumber" label="Telefon (+905551112233)" value={values.phoneNumber} onChange={(value) => onChange('phoneNumber', value)} />
        <AppInput name="identityNumber" label="T.C. Kimlik No" value={values.identityNumber} onChange={(value) => onChange('identityNumber', value)} />
        <FormControl>
          <FormLabel htmlFor="city" fontSize="sm" color="gray.700">
            Şehir
          </FormLabel>
          <Menu placement="bottom-start" matchWidth>
            <MenuButton
              id="city"
              as={Button}
              rightIcon={<ChevronDownIcon />}
              isDisabled={isCityLoading}
              w="full"
              h="48px"
              px={4}
              justifyContent="space-between"
              textAlign="left"
              fontWeight="400"
              bg="white"
              color={values.city ? 'gray.800' : 'gray.400'}
              border="1px solid"
              borderColor="purple.100"
              borderRadius="xl"
              _hover={{ borderColor: 'brand.300', bg: 'white' }}
              _active={{ bg: 'white' }}
              _focusVisible={{ borderColor: 'brand.500', boxShadow: '0 0 0 4px rgba(154,89,255,0.18)' }}
            >
              {values.city || (isCityLoading ? 'Şehirler yükleniyor' : 'Şehir seçiniz')}
            </MenuButton>
            <MenuList
              maxH="280px"
              overflowY="auto"
              borderColor="purple.100"
              borderRadius="xl"
              boxShadow="0 18px 45px rgba(30, 15, 75, 0.16)"
              zIndex={20}
            >
              {cities.map((city) => (
                <MenuItem key={city.id} onClick={() => onChange('city', city.name)}>
                  {city.name}
                </MenuItem>
              ))}
            </MenuList>
          </Menu>
        </FormControl>
        <FormControl>
          <FormLabel htmlFor="country" fontSize="sm" color="gray.700">
            Ülke
          </FormLabel>
          <Input
            id="country"
            name="country"
            value={TURKEY_COUNTRY_LABEL}
            isReadOnly
            bg="gray.50"
            color="gray.700"
            borderColor="purple.100"
            h="48px"
            borderRadius="xl"
            cursor="not-allowed"
            _hover={{ borderColor: 'purple.100' }}
            _focusVisible={{ borderColor: 'brand.500', boxShadow: '0 0 0 4px rgba(154,89,255,0.18)' }}
          />
        </FormControl>
        <AppInput name="zipCode" label="Posta Kodu" value={values.zipCode} onChange={(value) => onChange('zipCode', value)} />
      </Grid>
      <AppInput name="address" label="Adres" value={values.address} onChange={(value) => onChange('address', value)} />
      <Button colorScheme="brand" borderRadius="full" alignSelf="start" isLoading={isSaving} onClick={onSubmit}>
        Ödeme Bilgilerini Kaydet
      </Button>
    </Stack>
  );
}
