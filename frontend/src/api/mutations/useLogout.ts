import { useMutation } from '@tanstack/react-query';
import { authService } from '../../services/api/authService';

export const useLogout = () =>
  useMutation({
    mutationFn: () => authService.logout()
  });
