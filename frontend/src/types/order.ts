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
  items: OrderItem[];
}
