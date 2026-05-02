import {
  Accordion,
  AccordionButton,
  AccordionIcon,
  AccordionItem,
  AccordionPanel,
  Box,
  Button,
  Divider,
  Grid,
  Heading,
  HStack,
  Image,
  Skeleton,
  Stack,
  Text
} from '@chakra-ui/react';
import { Link as RouterLink } from 'react-router-dom';
import { PaymentProfileForm } from '../../business-components/checkout/PaymentProfileForm';
import { ErrorMessage } from '../../components/feedback/ErrorMessage';
import { formatPrice } from '../../helpers/formatPrice';
import { Cart } from '../../types/cart';
import { City } from '../../types/location';
import { UpdatePaymentProfileRequest } from '../../types/paymentProfile';

interface CheckoutViewProps {
  cart?: Cart;
  profileValues: UpdatePaymentProfileRequest;
  cities: City[];
  isLoading: boolean;
  isCityLoading: boolean;
  isProfileSaving: boolean;
  isPaying: boolean;
  errorMessage?: string;
  onProfileChange: (field: keyof UpdatePaymentProfileRequest, value: string) => void;
  onProfileSubmit: () => void;
  onPay: () => void;
}

export function CheckoutView({
  cart,
  profileValues,
  cities,
  isLoading,
  isCityLoading,
  isProfileSaving,
  isPaying,
  errorMessage,
  onProfileChange,
  onProfileSubmit,
  onPay
}: CheckoutViewProps) {
  const isEmpty = !cart || cart.items.length === 0;
  const isProfileComplete = Object.values(profileValues).every((value) => value.trim().length > 0);

  if (isLoading) {
    return (
      <Grid templateColumns={{ base: '1fr', lg: '1fr 360px' }} gap={6}>
        <Skeleton height="560px" borderRadius="lg" startColor="purple.50" endColor="purple.100" />
        <Skeleton height="320px" borderRadius="lg" startColor="purple.50" endColor="purple.100" />
      </Grid>
    );
  }

  if (isEmpty) {
    return (
      <Box bg="white" border="1px solid" borderColor="purple.100" borderRadius="lg" p={8} textAlign="center" boxShadow="sm">
        <Heading size="sm" color="gray.900">Sepetiniz boş.</Heading>
        <Text color="gray.600" mt={2}>Ödeme yapabilmek için önce sepete ürün eklemelisiniz.</Text>
        <Button as={RouterLink} to="/products" colorScheme="brand" borderRadius="full" mt={5}>Ürünlere Git</Button>
      </Box>
    );
  }

  return (
    <Stack spacing={6}>
      <Box>
        <Heading color="gray.900" letterSpacing="-0.04em" size="lg">Ödeme</Heading>
        <Text color="gray.600" mt={2}>Sipariş oluşturulacak ve Iyzico güvenli ödeme sayfasına yönlendirileceksiniz.</Text>
      </Box>

      <Accordion allowToggle defaultIndex={[0]} border="0">
        <AccordionItem border="1px solid" borderColor="purple.100" borderRadius="xl" overflow="hidden" bg="purple.50">
          <AccordionButton px={4} py={3} _hover={{ bg: 'purple.100' }}>
            <HStack flex="1" justify="space-between" textAlign="left" spacing={4}>
              <Box>
                <Text color="gray.900" fontWeight="900">Sepet İçeriğim</Text>
              </Box>
              <Text color="brand.700" fontWeight="900" whiteSpace="nowrap">
                {cart.items.length} ürün
              </Text>
            </HStack>
            <AccordionIcon color="brand.700" ml={3} />
          </AccordionButton>
          <AccordionPanel bg="white" px={4} py={3}>
            <Stack maxH="188px" overflowY="auto" spacing={3} pr={2}>
              {cart.items.map((item) => (
                <HStack
                  key={item.productId}
                  justify="space-between"
                  align="center"
                  border="1px solid"
                  borderColor="purple.50"
                  borderRadius="lg"
                  px={3}
                  py={2}
                  minH="82px"
                >
                  <HStack minW={0} spacing={3}>
                    <Image
                      src={item.imageUrl}
                      alt={item.name}
                      boxSize="58px"
                      objectFit="cover"
                      borderRadius="lg"
                      bg="purple.50"
                      flexShrink={0}
                    />
                    <Box minW={0}>
                      <Text color="gray.900" fontWeight="800" noOfLines={1}>{item.name}</Text>
                      <Text color="gray.500" fontSize="sm">Adet: {item.quantity}</Text>
                    </Box>
                  </HStack>
                  <Text color="gray.900" fontWeight="900" whiteSpace="nowrap">{formatPrice(item.lineTotal)}</Text>
                </HStack>
              ))}
            </Stack>
          </AccordionPanel>
        </AccordionItem>
      </Accordion>

      {errorMessage ? <ErrorMessage message={errorMessage} /> : null}

      <Grid templateColumns={{ base: '1fr', lg: '1fr 360px' }} gap={6} alignItems="start">
        <Box bg="white" border="1px solid" borderColor="purple.100" borderRadius="lg" p={{ base: 5, md: 6 }} boxShadow="sm">
          <Stack spacing={5}>
            <Box>
              <Heading size="md" color="gray.900">Ödeme Bilgileri</Heading>
              <Text color="gray.600" mt={1}>Iyzico ödeme başlatmak için bu bilgiler eksiksiz olmalı.</Text>
            </Box>
            <PaymentProfileForm
              values={profileValues}
              cities={cities}
              isCityLoading={isCityLoading}
              isSaving={isProfileSaving}
              onChange={onProfileChange}
              onSubmit={onProfileSubmit}
            />
          </Stack>
        </Box>

        <Box bg="white" border="1px solid" borderColor="purple.100" borderRadius="lg" p={5} boxShadow="sm">
          <Stack spacing={4}>
            <Heading size="sm" color="gray.900">Sipariş Özeti</Heading>
            <Stack spacing={3}>
              {cart.items.map((item) => (
                <HStack key={item.productId} justify="space-between" align="start">
                  <Box>
                    <Text color="gray.900" fontWeight="800">{item.name}</Text>
                    <Text color="gray.500" fontSize="sm">{item.quantity} adet</Text>
                  </Box>
                  <Text color="gray.900" fontWeight="800">{formatPrice(item.lineTotal)}</Text>
                </HStack>
              ))}
            </Stack>
            <Divider borderColor="purple.100" />
            <HStack justify="space-between" align="end">
              <Text color="gray.600">Toplam</Text>
              <Text color="brand.700" fontSize="2xl" fontWeight="900">{formatPrice(cart.summary.subtotal)}</Text>
            </HStack>
            <Button colorScheme="brand" borderRadius="full" h="48px" isLoading={isPaying} isDisabled={!isProfileComplete} onClick={onPay}>
              Iyzico ile Öde
            </Button>
            {!isProfileComplete ? <Text color="red.500" fontSize="sm">Ödemeye geçmek için ödeme bilgilerini doldurun.</Text> : null}
          </Stack>
        </Box>
      </Grid>
    </Stack>
  );
}
