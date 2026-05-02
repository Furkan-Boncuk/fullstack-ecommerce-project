import { AxiosError } from 'axios';
import { useMemo } from 'react';
import toast from 'react-hot-toast';
import { useSearchParams } from 'react-router-dom';
import { useInitPayment } from '../../api/mutations/useInitPayment';
import { useOrders } from '../../api/queries/useOrders';
import { usePaymentStatus } from '../../api/queries/usePaymentStatus';
import { PaymentResultView } from '../../views/payment/PaymentResultView';

function getErrorMessage(error: unknown, fallback: string) {
  return (error as AxiosError<{ detail?: string }>).response?.data?.detail ?? fallback;
}

export function PaymentResultContainer() {
  const [searchParams] = useSearchParams();
  const initPaymentMutation = useInitPayment();
  const orderId = useMemo(() => {
    const raw = searchParams.get('orderId');
    if (!raw) return undefined;
    const parsed = Number(raw);
    return Number.isFinite(parsed) ? parsed : undefined;
  }, [searchParams]);
  const status = searchParams.get('status') ?? undefined;
  const paymentStatusQuery = usePaymentStatus(orderId);
  const ordersQuery = useOrders();
  const order = useMemo(
    () => ordersQuery.data?.find((item) => item.id === orderId),
    [orderId, ordersQuery.data]
  );

  const retry = () => {
    if (!orderId) {
      toast.error('Sipariş bilgisi bulunamadı.');
      return;
    }
    initPaymentMutation.mutate(
      { orderId },
      {
        onSuccess: (payment) => {
          if (payment.checkoutUrl) {
            window.location.assign(payment.checkoutUrl);
            return;
          }
          toast.error(payment.errorCode ?? 'Ödeme başlatılamadı.');
        },
        onError: (error) => toast.error(getErrorMessage(error, 'Ödeme başlatılamadı.'))
      }
    );
  };

  return (
    <PaymentResultView
      orderId={orderId}
      callbackStatus={status}
      paymentStatus={paymentStatusQuery.data}
      order={order}
      isLoading={paymentStatusQuery.isLoading || ordersQuery.isLoading}
      isRetrying={initPaymentMutation.isPending}
      errorMessage={paymentStatusQuery.isError || ordersQuery.isError ? 'Ödeme sonucu yüklenirken bir hata oluştu.' : undefined}
      onRetry={retry}
    />
  );
}
