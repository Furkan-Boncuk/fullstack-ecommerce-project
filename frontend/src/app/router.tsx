import { Navigate, createBrowserRouter } from 'react-router-dom';
import { AuthLayout } from '../layouts/AuthLayout';
import { MainLayout } from '../layouts/MainLayout';
import { LoginView } from '../views/auth/LoginView';
import { RegisterView } from '../views/auth/RegisterView';
import { HomeView } from '../views/home/HomeView';
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
    children: [{ index: true, element: <HomeView /> }]
  },
  { path: '*', element: <Navigate to="/auth/login" replace /> }
]);
