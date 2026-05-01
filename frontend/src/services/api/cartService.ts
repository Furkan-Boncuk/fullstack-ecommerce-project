import { axiosInstance } from './axiosInstance';
import { Cart, CartItemRequest, CartLine, CartSummary } from '../../types/cart';

interface RawCartLine {
  productId: number;
  name?: string;
  imageUrl?: string;
  unitPrice?: number;
  quantity: number;
  lineTotal?: number;
  availableStock?: number;
}

interface RawCart {
  userId: number;
  items: RawCartLine[];
  summary?: CartSummary;
}

function normalizeLine(line: RawCartLine): CartLine {
  const unitPrice = line.unitPrice ?? 0;
  const lineTotal = line.lineTotal ?? unitPrice * line.quantity;

  return {
    productId: line.productId,
    name: line.name ?? 'Ürün bilgisi yüklenemedi',
    imageUrl: line.imageUrl ?? '',
    unitPrice,
    quantity: line.quantity,
    lineTotal,
    availableStock: line.availableStock ?? 0
  };
}

function normalizeCart(rawCart: RawCart): Cart {
  const items = rawCart.items.map(normalizeLine);
  const summary = rawCart.summary ?? {
    itemCount: items.reduce((total, item) => total + item.quantity, 0),
    subtotal: items.reduce((total, item) => total + item.lineTotal, 0)
  };

  return {
    userId: rawCart.userId,
    items,
    summary
  };
}

export const cartService = {
  async getCart() {
    const response = await axiosInstance.get<RawCart>('/api/v1/cart');
    return normalizeCart(response.data);
  },

  async addItem(payload: CartItemRequest) {
    const response = await axiosInstance.post<RawCart>('/api/v1/cart/items', payload);
    return normalizeCart(response.data);
  },

  async removeItem(productId: number) {
    const response = await axiosInstance.delete<RawCart>(`/api/v1/cart/items/${productId}`);
    return normalizeCart(response.data);
  }
};
