import { axiosInstance } from './axiosInstance';
import { Category, CategoryTreeNode } from '../../types/category';

export const categoryService = {
  async getCategories() {
    const response = await axiosInstance.get<Category[]>('/api/v1/categories');
    return response.data;
  },

  async getCategoryTree() {
    const response = await axiosInstance.get<CategoryTreeNode[]>('/api/v1/categories/tree');
    return response.data;
  }
};
