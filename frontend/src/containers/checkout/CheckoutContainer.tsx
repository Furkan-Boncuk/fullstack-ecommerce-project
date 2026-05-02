import { AxiosError } from 'axios';
import { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { useNavigate } from 'react-router-dom';
import { useCreateOrder } from '../../api/mutations/useCreateOrder';
import { useInitPayment } from '../../api/mutations/useInitPayment';
import { useUpdatePaymentProfile } from '../../api/mutations/useUpdatePaymentProfile';
import { useCart } from '../../api/queries/useCart';
import { usePaymentProfile } from '../../api/queries/usePaymentProfile';
import { UpdatePaymentProfileRequest } from '../../types/paymentProfile';
import { CheckoutView } from '../../views/checkout/CheckoutView';

const emptyProfile: UpdatePaymentProfileRequest = {
  firstName: '',
  lastName: '',
  phoneNumber: '',
  identityNumber: '',
  address: '',
  city: '',
  country: 'Turkey',
  zipCode: ''
};

function getErrorMessage(error: unknown, fallback: string) {
  return (error as AxiosError<{ detail?: string }>).response?.data?.detail ?? fallback;
}

export function CheckoutContainer() {
  const navigate = useNavigate();
  const cartQuery = useCart();
  const profileQuery = usePaymentProfile();
  const updateProfileMutation = useUpdatePaymentProfile();
  const createOrderMutation = useCreateOrder();
  const initPaymentMutation = useInitPayment();
  const [profileValues, setProfileValues] = useState<UpdatePaymentProfileRequest>(emptyProfile);

  useEffect(() => {
    if (profileQuery.data) {
      setProfileValues({
        firstName: profileQuery.data.firstName ?? '',
        lastName: profileQuery.data.lastName ?? '',
        phoneNumber: profileQuery.data.phoneNumber ?? '',
        identityNumber: profileQuery.data.identityNumber ?? '',
        address: profileQuery.data.address ?? '',
        city: profileQuery.data.city ?? '',
        country: profileQuery.data.country ?? 'Turkey',
        zipCode: profileQuery.data.zipCode ?? ''
      });
    }
  }, [profileQuery.data]);

  const updateField = (field: keyof UpdatePaymentProfileRequest, value: string) => {
    setProfileValues((current) => ({ ...current, [field]: value }));
  };

  const saveProfile = () => {
    updateProfileMutation.mutate(profileValues, {
      onSuccess: () => toast.success('Ödeme bilgileri kaydedildi.'),
      onError: (error) => toast.error(getErrorMessage(error, 'Ödeme bilgileri kaydedilemedi.'))
    });
  };

  const pay = async () => {
    try {
      await updateProfileMutation.mutateAsync(profileValues);
      const order = await createOrderMutation.mutateAsync();
      const payment = await initPaymentMutation.mutateAsync({ orderId: order.id });

      if (payment.checkoutUrl) {
        window.location.assign(payment.checkoutUrl);
        return;
      }

      if (payment.success) {
        navigate(`/payment/result?orderId=${order.id}&status=success`);
        return;
      }

      toast.error(payment.errorCode ?? 'Ödeme başlatılamadı.');
    } catch (error) {
      toast.error(getErrorMessage(error, 'Ödeme akışı başlatılamadı.'));
    }
  };

  return (
    <CheckoutView
      cart={cartQuery.data}
      profileValues={profileValues}
      isLoading={cartQuery.isLoading || profileQuery.isLoading}
      isProfileSaving={updateProfileMutation.isPending}
      isPaying={createOrderMutation.isPending || initPaymentMutation.isPending}
      errorMessage={cartQuery.isError || profileQuery.isError ? 'Checkout bilgileri yüklenirken bir hata oluştu.' : undefined}
      onProfileChange={updateField}
      onProfileSubmit={saveProfile}
      onPay={pay}
    />
  );
}
