import { useQuery } from '@tanstack/react-query';
import { categoryService } from '../../services/api/categoryService';

export const useCategoryTree = () =>
  useQuery({
    queryKey: ['categories', 'tree'],
    queryFn: categoryService.getCategoryTree
  });
