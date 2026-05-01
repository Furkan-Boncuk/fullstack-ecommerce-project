export interface CartLine {
  productId: number;
  name: string;
  imageUrl: string;
  unitPrice: number;
  quantity: number;
  lineTotal: number;
  availableStock: number;
}

export interface CartSummary {
  itemCount: number;
  subtotal: number;
}

export interface Cart {
  userId: number;
  items: CartLine[];
  summary: CartSummary;
}

export interface CartItemRequest {
  productId: number;
  quantity: number;
}
