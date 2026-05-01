export interface Category {
  id: number;
  name: string;
  slug: string;
  description?: string | null;
  imageUrl?: string | null;
  parentId?: number | null;
  sortOrder: number;
}

export interface CategoryTreeNode {
  id: number;
  name: string;
  slug: string;
  children: CategoryTreeNode[];
}
