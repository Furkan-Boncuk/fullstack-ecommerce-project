import { useMutation } from '@tanstack/react-query';
import { authService } from '../../services/api/authService';
import { LoginRequest } from '../../types/user';

export const useLogin = () =>
  useMutation({
    mutationFn: (payload: LoginRequest) => authService.login(payload)
  });
