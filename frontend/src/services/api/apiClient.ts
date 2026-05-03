import { AxiosInstance, AxiosResponse } from 'axios';
import { AuthResponse, AuthUser } from '../../types/api';

export class ApiContractError extends Error {
  readonly code = 'API_CONTRACT_ERROR';

  constructor(message = 'Beklenmeyen sunucu yanıtı alındı.') {
    super(message);
    this.name = 'ApiContractError';
  }
}

export function assertApiResponse(response: AxiosResponse<unknown>) {
  const requestUrl = response.config.url ?? '';
  const contentType = String(response.headers['content-type'] ?? '');

  if (!requestUrl.includes('/api/')) {
    return;
  }

  if (contentType.includes('text/html') || looksLikeHtml(response.data)) {
    throw new ApiContractError();
  }
}

type ResponseParser<T> = (value: unknown) => T;

export function postApi<TResponse>(
  client: AxiosInstance,
  url: string,
  parser: ResponseParser<TResponse>
): Promise<TResponse>;

export function postApi<TPayload, TResponse>(
  client: AxiosInstance,
  url: string,
  payload: TPayload,
  parser: ResponseParser<TResponse>
): Promise<TResponse>;

export async function postApi<TPayload, TResponse>(
  client: AxiosInstance,
  url: string,
  payloadOrParser: TPayload | ResponseParser<TResponse>,
  parser?: ResponseParser<TResponse>
): Promise<TResponse> {
  const hasPayload = parser !== undefined;
  const responseParser = hasPayload ? parser : payloadOrParser as ResponseParser<TResponse>;
  const response = hasPayload
    ? await client.post<unknown>(url, payloadOrParser as TPayload)
    : await client.post<unknown>(url);

  assertApiResponse(response);
  return responseParser(response.data);
}

export function parseAuthResponse(value: unknown): AuthResponse {
  if (!value || typeof value !== 'object' || looksLikeHtml(value)) {
    throw new ApiContractError();
  }

  const candidate = value as Partial<AuthResponse>;
  if (
    typeof candidate.accessToken !== 'string'
    || candidate.accessToken.length === 0
    || candidate.tokenType !== 'Bearer'
    || typeof candidate.expiresInSeconds !== 'number'
    || !isAuthUser(candidate.user)
  ) {
    throw new ApiContractError();
  }

  return candidate as AuthResponse;
}

function isAuthUser(value: unknown): value is AuthUser {
  if (!value || typeof value !== 'object') {
    return false;
  }

  const candidate = value as Partial<AuthUser>;
  return typeof candidate.id === 'number'
    && typeof candidate.email === 'string'
    && Array.isArray(candidate.roles)
    && candidate.roles.every((role) => typeof role === 'string');
}

function looksLikeHtml(value: unknown) {
  return typeof value === 'string' && /^\s*<!doctype html/i.test(value);
}
