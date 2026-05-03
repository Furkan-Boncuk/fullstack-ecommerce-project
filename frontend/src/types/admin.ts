import { OrderItem, OrderStatus, ShippingAddress } from './order';
import { Product } from './product';

export type AdminProductPayload = {
  name: string;
  description: string;
  price: number;
  stock: number;
  imageUrl: string;
  categoryId: number;
};

export type AdminOrder = {
  id: number;
  userId: number;
  userEmail?: string | null;
  status: OrderStatus;
  totalAmount: number;
  createdAt: string;
  expiresAt: string;
  shippingAddress?: ShippingAddress | null;
  items: OrderItem[];
};

export type AdminProduct = Product;
