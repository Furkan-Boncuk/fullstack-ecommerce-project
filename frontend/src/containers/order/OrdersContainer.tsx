import { AxiosError } from 'axios';
import toast from 'react-hot-toast';
import { useInitPayment } from '../../api/mutations/useInitPayment';
import { useOrders } from '../../api/queries/useOrders';
import { OrdersView } from '../../views/order/OrdersView';

function getErrorMessage(error: unknown, fallback: string) {
  return (error as AxiosError<{ detail?: string }>).response?.data?.detail ?? fallback;
}

export function OrdersContainer() {
  const ordersQuery = useOrders();
  const initPaymentMutation = useInitPayment();

  const payOrder = (orderId: number) => {
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
    <OrdersView
      orders={ordersQuery.data ?? []}
      isLoading={ordersQuery.isLoading}
      payingOrderId={initPaymentMutation.isPending ? initPaymentMutation.variables?.orderId : undefined}
      errorMessage={ordersQuery.isError ? 'Siparişler yüklenirken bir hata oluştu.' : undefined}
      onPay={payOrder}
    />
  );
}
