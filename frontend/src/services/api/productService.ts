import { axiosInstance } from './axiosInstance';
import { PageResponse } from '../../types/api';
import { Product, ProductFilterParams } from '../../types/product';

function compactParams(params: ProductFilterParams) {
  return Object.fromEntries(
    Object.entries(params).filter(([, value]) => value !== undefined && value !== null && value !== '')
  );
}

export const productService = {
  async getProducts(params: ProductFilterParams) {
    const response = await axiosInstance.get<PageResponse<Product>>('/api/v1/products', {
      params: compactParams(params)
    });

    return response.data;
  },

  async getProductById(id: number) {
    const response = await axiosInstance.get<Product>(`/api/v1/products/${id}`);
    return response.data;
  }
};
