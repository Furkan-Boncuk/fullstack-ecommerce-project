import { Navigate, createBrowserRouter, useLocation } from 'react-router-dom';
import { AuthLayout } from '../layouts/AuthLayout';
import { MainLayout } from '../layouts/MainLayout';
import { LoginView } from '../views/auth/LoginView';
import { RegisterView } from '../views/auth/RegisterView';
import { CartContainer } from '../containers/cart/CartContainer';
import { ProductDetailContainer } from '../containers/product/ProductDetailContainer';
import { ProductListContainer } from '../containers/product/ProductListContainer';
import { CheckoutContainer } from '../containers/checkout/CheckoutContainer';
import { OrdersContainer } from '../containers/order/OrdersContainer';
import { PaymentResultContainer } from '../containers/payment/PaymentResultContainer';
import { AdminProductsContainer } from '../containers/admin/AdminProductsContainer';
import { AdminOrdersContainer } from '../containers/admin/AdminOrdersContainer';
import { AdminHomeView } from '../views/admin/AdminHomeView';
import { useAuthStore } from '../store/authStore';

function ProtectedRoute({ children }: { children: JSX.Element }) {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const location = useLocation();
  return isAuthenticated ? children : <Navigate to="/auth/login" replace state={{ from: location }} />;
}

function AdminRoute({ children }: { children: JSX.Element }) {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const user = useAuthStore((state) => state.user);
  const location = useLocation();
  if (!isAuthenticated) {
    return <Navigate to="/auth/login" replace state={{ from: location }} />;
  }
  return user?.roles?.includes('ADMIN') ? children : <Navigate to="/products" replace />;
}

export const router = createBrowserRouter([
  {
    path: '/auth',
    element: <AuthLayout />,
    children: [
      { path: 'login', element: <LoginView /> },
      { path: 'register', element: <RegisterView /> }
    ]
  },
  {
    path: '/',
    element: (
      <ProtectedRoute>
        <MainLayout />
      </ProtectedRoute>
    ),
    children: [
      { index: true, element: <Navigate to="/products" replace /> },
      { path: 'products', element: <ProductListContainer /> },
      { path: 'products/:id', element: <ProductDetailContainer /> },
      { path: 'cart', element: <CartContainer /> },
      { path: 'checkout', element: <CheckoutContainer /> },
      { path: 'payment/result', element: <PaymentResultContainer /> },
      { path: 'orders', element: <OrdersContainer /> },
      {
        path: 'admin',
        element: (
          <AdminRoute>
            <AdminHomeView />
          </AdminRoute>
        )
      },
      {
        path: 'admin/products',
        element: (
          <AdminRoute>
            <AdminProductsContainer />
          </AdminRoute>
        )
      },
      {
        path: 'admin/orders',
        element: (
          <AdminRoute>
            <AdminOrdersContainer />
          </AdminRoute>
        )
      }
    ]
  },
  { path: '*', element: <Navigate to="/auth/login" replace /> }
]);
