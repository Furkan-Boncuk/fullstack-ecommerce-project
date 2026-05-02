export interface PaymentInitRequest {
  orderId: number;
}

export interface PaymentInitResponse {
  success: boolean;
  status: string;
  transactionId?: string | null;
  errorCode?: string | null;
  checkoutUrl?: string | null;
  checkoutToken?: string | null;
  providerReference?: string | null;
  expiresAt?: string | null;
}

export interface PaymentStatusResponse {
  orderId: number;
  paymentStatus: string;
  latestAttemptStatus?: string | null;
  transactionId?: string | null;
  errorCode?: string | null;
  checkoutUrl?: string | null;
  expiresAt?: string | null;
}
