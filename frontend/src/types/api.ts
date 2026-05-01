export interface ApiError {
  title: string;
  status: number;
  detail: string;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  expiresInSeconds: number;
}
