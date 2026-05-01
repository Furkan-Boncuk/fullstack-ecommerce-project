import { useMutation } from '@tanstack/react-query';
import { authService } from '../../services/api/authService';
import { RegisterRequest } from '../../types/user';

export const useRegister = () =>
  useMutation({
    mutationFn: (payload: RegisterRequest) => authService.register(payload)
  });
