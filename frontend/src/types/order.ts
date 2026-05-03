export type OrderStatus = 'PENDING' | 'PAYMENT_FAILED' | 'PAID' | 'EXPIRED' | 'CANCELLED' | 'REQUIRES_REVIEW';

export interface OrderItem {
  productId: number;
  productName: string;
  productImageUrl?: string;
  unitPrice: number;
  quantity: number;
  lineTotal: number;
}

export interface Order {
  id: number;
  status: OrderStatus;
  totalAmount: number;
  createdAt: string;
  expiresAt: string;
  shippingAddress?: ShippingAddress | null;
  items: OrderItem[];
}

export interface ShippingAddress {
  firstName?: string | null;
  lastName?: string | null;
  phoneNumber?: string | null;
  address?: string | null;
  city?: string | null;
  country?: string | null;
  zipCode?: string | null;
}
