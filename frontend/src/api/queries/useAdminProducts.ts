import { useQuery } from '@tanstack/react-query';
import { adminService } from '../../services/api/adminService';

export const useAdminProducts = (page: number) =>
  useQuery({
    queryKey: ['admin', 'products', page],
    queryFn: () => adminService.getProducts(page)
  });
