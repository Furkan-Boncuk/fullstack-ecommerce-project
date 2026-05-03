import { useMemo, useState } from 'react';
import toast from 'react-hot-toast';
import { AxiosError } from 'axios';
import { useCategories } from '../../api/queries/useCategories';
import { useAdminProducts } from '../../api/queries/useAdminProducts';
import { useCreateAdminProduct, useDeleteAdminProduct, useUpdateAdminProduct } from '../../api/mutations/useAdminProductMutations';
import { AdminProduct, AdminProductPayload } from '../../types/admin';
import { ApiError } from '../../types/api';
import { AdminProductsView } from '../../views/admin/AdminProductsView';

const emptyForm: AdminProductPayload = {
  name: '',
  description: '',
  price: 0,
  stock: 0,
  imageUrl: '',
  categoryId: 0
};

export function AdminProductsContainer() {
  const [page, setPage] = useState(0);
  const [editingProduct, setEditingProduct] = useState<AdminProduct | null>(null);
  const [form, setForm] = useState<AdminProductPayload>(emptyForm);
  const categoriesQuery = useCategories();
  const productsQuery = useAdminProducts(page);
  const createMutation = useCreateAdminProduct();
  const updateMutation = useUpdateAdminProduct();
  const deleteMutation = useDeleteAdminProduct();

  const categories = categoriesQuery.data ?? [];
  const products = productsQuery.data?.content ?? [];

  const canSubmit = useMemo(
    () => form.name.trim().length > 0 && form.price > 0 && form.stock >= 0 && form.categoryId > 0,
    [form]
  );

  const apiError = (error: unknown, fallback: string) =>
    (error as AxiosError<ApiError>).response?.data?.detail ?? fallback;

  const resetForm = () => {
    setEditingProduct(null);
    setForm(emptyForm);
  };

  const startEdit = (product: AdminProduct) => {
    setEditingProduct(product);
    setForm({
      name: product.name,
      description: product.description ?? '',
      price: product.price,
      stock: product.stock,
      imageUrl: product.imageUrl ?? '',
      categoryId: product.category.id
    });
  };

  const submit = () => {
    if (!canSubmit) {
      toast.error('Lütfen ürün bilgilerini kontrol edin.');
      return;
    }
    const options = {
      onSuccess: () => {
        toast.success(editingProduct ? 'Ürün güncellendi.' : 'Ürün eklendi.');
        resetForm();
      },
      onError: (error: unknown) => toast.error(apiError(error, 'Ürün kaydedilemedi.'))
    };
    if (editingProduct) {
      updateMutation.mutate({ id: editingProduct.id, payload: form }, options);
    } else {
      createMutation.mutate(form, options);
    }
  };

  const remove = (productId: number) => {
    deleteMutation.mutate(productId, {
      onSuccess: () => toast.success('Ürün yayından kaldırıldı.'),
      onError: (error) => toast.error(apiError(error, 'Ürün yayından kaldırılamadı.'))
    });
  };

  return (
    <AdminProductsView
      products={products}
      categories={categories}
      page={page}
      totalPages={productsQuery.data?.totalPages ?? 0}
      isLoading={productsQuery.isLoading}
      isSaving={createMutation.isPending || updateMutation.isPending}
      deletingProductId={deleteMutation.isPending ? deleteMutation.variables : undefined}
      editingProductId={editingProduct?.id}
      form={form}
      canSubmit={canSubmit}
      onFormChange={setForm}
      onSubmit={submit}
      onCancel={resetForm}
      onEdit={startEdit}
      onDelete={remove}
      onPageChange={setPage}
    />
  );
}
