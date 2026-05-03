import { useQuery } from '@tanstack/react-query';
import { adminService } from '../../services/api/adminService';

export const useAdminOrders = (params: { page: number; status: string; userId: string; email: string }) =>
  useQuery({
    queryKey: ['admin', 'orders', params],
    queryFn: () => adminService.getOrders(params)
  });
