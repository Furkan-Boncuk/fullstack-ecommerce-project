import { AxiosError } from 'axios';
import { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { useNavigate } from 'react-router-dom';
import { useCreateOrder } from '../../api/mutations/useCreateOrder';
import { useInitPayment } from '../../api/mutations/useInitPayment';
import { useUpdatePaymentProfile } from '../../api/mutations/useUpdatePaymentProfile';
import { useCart } from '../../api/queries/useCart';
import { usePaymentProfile } from '../../api/queries/usePaymentProfile';
import { useTurkeyCities } from '../../api/queries/useTurkeyCities';
import { TURKEY_COUNTRY_VALUE } from '../../constants/location';
import { Cart } from '../../types/cart';
import { City } from '../../types/location';
import { UpdatePaymentProfileRequest } from '../../types/paymentProfile';
import { CheckoutView } from '../../views/checkout/CheckoutView';

const emptyProfile: UpdatePaymentProfileRequest = {
  firstName: '',
  lastName: '',
  phoneNumber: '',
  identityNumber: '',
  address: '',
  city: '',
  country: TURKEY_COUNTRY_VALUE,
  zipCode: ''
};

function getErrorMessage(error: unknown, fallback: string) {
  return (error as AxiosError<{ detail?: string }>).response?.data?.detail ?? fallback;
}

function normalizePaymentProfile(profile: UpdatePaymentProfileRequest, cities: City[]): UpdatePaymentProfileRequest {
  const cityNames = new Set(cities.map((city) => city.name));

  return {
    ...profile,
    city: cityNames.has(profile.city) ? profile.city : '',
    country: TURKEY_COUNTRY_VALUE
  };
}

export function CheckoutContainer() {
  const navigate = useNavigate();
  const cartQuery = useCart();
  const profileQuery = usePaymentProfile();
  const turkeyCitiesQuery = useTurkeyCities();
  const updateProfileMutation = useUpdatePaymentProfile();
  const createOrderMutation = useCreateOrder();
  const initPaymentMutation = useInitPayment();
  const [profileValues, setProfileValues] = useState<UpdatePaymentProfileRequest>(emptyProfile);
  const [checkoutOrderId, setCheckoutOrderId] = useState<number>();
  const [checkoutCart, setCheckoutCart] = useState<Cart>();

  useEffect(() => {
    const cities = turkeyCitiesQuery.data ?? [];

    if (profileQuery.data && cities.length > 0) {
      setProfileValues(normalizePaymentProfile({
        firstName: profileQuery.data.firstName ?? '',
        lastName: profileQuery.data.lastName ?? '',
        phoneNumber: profileQuery.data.phoneNumber ?? '',
        identityNumber: profileQuery.data.identityNumber ?? '',
        address: profileQuery.data.address ?? '',
        city: profileQuery.data.city ?? '',
        country: TURKEY_COUNTRY_VALUE,
        zipCode: profileQuery.data.zipCode ?? ''
      }, cities));
    }
  }, [profileQuery.data, turkeyCitiesQuery.data]);

  useEffect(() => {
    if (!checkoutOrderId && cartQuery.data && cartQuery.data.items.length > 0) {
      setCheckoutCart(cartQuery.data);
    }
  }, [cartQuery.data, checkoutOrderId]);

  const updateField = (field: keyof UpdatePaymentProfileRequest, value: string) => {
    if (field === 'country') {
      return;
    }

    setProfileValues((current) => normalizePaymentProfile({ ...current, [field]: value }, turkeyCitiesQuery.data ?? []));
  };

  const saveProfile = () => {
    updateProfileMutation.mutate(normalizePaymentProfile(profileValues, turkeyCitiesQuery.data ?? []), {
      onSuccess: () => toast.success('Ödeme bilgileri kaydedildi.'),
      onError: (error) => toast.error(getErrorMessage(error, 'Ödeme bilgileri kaydedilemedi.'))
    });
  };

  const pay = async () => {
    try {
      await updateProfileMutation.mutateAsync(normalizePaymentProfile(profileValues, turkeyCitiesQuery.data ?? []));
      const orderId = checkoutOrderId ?? (await createOrderMutation.mutateAsync()).id;
      setCheckoutOrderId(orderId);
      const payment = await initPaymentMutation.mutateAsync({ orderId });

      if (payment.checkoutUrl) {
        window.location.assign(payment.checkoutUrl);
        return;
      }

      if (payment.success) {
        navigate(`/payment/result?orderId=${orderId}`);
        return;
      }

      toast.error(payment.errorCode ?? 'Ödeme başlatılamadı.');
    } catch (error) {
      toast.error(getErrorMessage(error, 'Ödeme akışı başlatılamadı.'));
    }
  };

  return (
    <CheckoutView
      cart={checkoutCart ?? cartQuery.data}
      profileValues={profileValues}
      cities={turkeyCitiesQuery.data ?? []}
      isLoading={cartQuery.isLoading || profileQuery.isLoading || turkeyCitiesQuery.isLoading}
      isCityLoading={turkeyCitiesQuery.isLoading}
      isProfileSaving={updateProfileMutation.isPending}
      isPaying={createOrderMutation.isPending || initPaymentMutation.isPending}
      errorMessage={cartQuery.isError || profileQuery.isError || turkeyCitiesQuery.isError ? 'Checkout bilgileri yüklenirken bir hata oluştu.' : undefined}
      onProfileChange={updateField}
      onProfileSubmit={saveProfile}
      onPay={pay}
    />
  );
}
