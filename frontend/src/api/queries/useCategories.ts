import { useQuery } from '@tanstack/react-query';
import { categoryService } from '../../services/api/categoryService';

export const useCategories = () =>
  useQuery({
    queryKey: ['categories'],
    queryFn: categoryService.getCategories
  });
