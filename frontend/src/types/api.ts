export interface ApiError {
  title: string;
  status: number;
  detail: string;
  code?: string;
  fieldErrors?: Array<{
    field: string;
    message: string;
  }>;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  expiresInSeconds: number;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}
