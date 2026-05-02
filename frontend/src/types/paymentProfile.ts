export interface PaymentProfile {
  userId: number;
  email: string;
  firstName?: string | null;
  lastName?: string | null;
  phoneNumber?: string | null;
  identityNumber?: string | null;
  address?: string | null;
  city?: string | null;
  country?: string | null;
  zipCode?: string | null;
}

export interface UpdatePaymentProfileRequest {
  firstName: string;
  lastName: string;
  phoneNumber: string;
  identityNumber: string;
  address: string;
  city: string;
  country: string;
  zipCode: string;
}
