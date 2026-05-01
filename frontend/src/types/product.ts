export interface ProductCategorySummary {
  id: number;
  name: string;
  slug: string;
}

export interface Product {
  id: number;
  name: string;
  description: string;
  category: ProductCategorySummary;
  imageUrl: string;
  price: number;
  stock: number;
}

export interface ProductFilterParams {
  search?: string;
  minPrice?: number;
  maxPrice?: number;
  categorySlug?: string;
  inStock?: boolean;
  page?: number;
  size?: number;
}
