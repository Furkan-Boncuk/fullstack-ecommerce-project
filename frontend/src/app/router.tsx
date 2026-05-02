import { Navigate, createBrowserRouter } from 'react-router-dom';
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
import { useAuthStore } from '../store/authStore';

function ProtectedRoute({ children }: { children: JSX.Element }) {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  return isAuthenticated ? children : <Navigate to="/auth/login" replace />;
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
      { path: 'orders', element: <OrdersContainer /> }
    ]
  },
  { path: '*', element: <Navigate to="/auth/login" replace /> }
]);
