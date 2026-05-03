import { useMutation, useQueryClient } from '@tanstack/react-query';
import { adminService } from '../../services/api/adminService';
import { AdminProductPayload } from '../../types/admin';

export const useCreateAdminProduct = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload: AdminProductPayload) => adminService.createProduct(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'products'] });
      queryClient.invalidateQueries({ queryKey: ['products'] });
    }
  });
};

export const useUpdateAdminProduct = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, payload }: { id: number; payload: AdminProductPayload }) => adminService.updateProduct(id, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'products'] });
      queryClient.invalidateQueries({ queryKey: ['products'] });
    }
  });
};

export const useDeleteAdminProduct = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => adminService.deleteProduct(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'products'] });
      queryClient.invalidateQueries({ queryKey: ['products'] });
    }
  });
};
