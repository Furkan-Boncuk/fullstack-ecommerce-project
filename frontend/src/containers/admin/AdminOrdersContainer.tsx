import { useState } from 'react';
import { useAdminOrders } from '../../api/queries/useAdminOrders';
import { AdminOrdersView } from '../../views/admin/AdminOrdersView';

export function AdminOrdersContainer() {
  const [page, setPage] = useState(0);
  const [filters, setFilters] = useState({ status: '', userId: '', email: '' });
  const ordersQuery = useAdminOrders({ page, ...filters });

  const applyFilters = (nextFilters: typeof filters) => {
    setPage(0);
    setFilters(nextFilters);
  };

  return (
    <AdminOrdersView
      orders={ordersQuery.data?.content ?? []}
      page={page}
      totalPages={ordersQuery.data?.totalPages ?? 0}
      isLoading={ordersQuery.isLoading}
      filters={filters}
      onFiltersChange={applyFilters}
      onPageChange={setPage}
    />
  );
}
