import { OrderStatus } from '../types/order';

export function orderStatusLabel(status: OrderStatus | string) {
  const labels: Record<string, string> = {
    PENDING: 'Ödeme bekleniyor',
    PAYMENT_FAILED: 'Ödeme başarısız',
    PAID: 'Ödendi',
    EXPIRED: 'Süresi doldu',
    CANCELLED: 'İptal edildi',
    REQUIRES_REVIEW: 'Kontrol gerekiyor'
  };

  return labels[status] ?? status;
}

export function orderStatusColor(status: OrderStatus | string) {
  const colors: Record<string, string> = {
    PENDING: 'purple',
    PAYMENT_FAILED: 'red',
    PAID: 'green',
    EXPIRED: 'gray',
    CANCELLED: 'gray',
    REQUIRES_REVIEW: 'orange'
  };

  return colors[status] ?? 'gray';
}
