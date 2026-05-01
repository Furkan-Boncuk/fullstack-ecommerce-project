import { ChakraProvider } from '@chakra-ui/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { RouterProvider } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { useEffect, useMemo, useState } from 'react';
import { theme } from './theme';
import { router } from './router';
import { authService } from '../services/api/authService';
import { useAuthStore } from '../store/authStore';

export function AppProviders() {
  const queryClient = useMemo(() => new QueryClient(), []);
  const setToken = useAuthStore((state) => state.setToken);
  const clearAuth = useAuthStore((state) => state.clearAuth);
  const [ready, setReady] = useState(false);

  useEffect(() => {
    void (async () => {
      try {
        const refreshed = await authService.refresh();
        setToken(refreshed.accessToken);
      } catch {
        clearAuth();
      } finally {
        setReady(true);
      }
    })();
  }, [setToken, clearAuth]);

  if (!ready) {
    return null;
  }

  return (
    <ChakraProvider theme={theme}>
      <QueryClientProvider client={queryClient}>
        <RouterProvider router={router} />
        <Toaster position="top-right" />
      </QueryClientProvider>
    </ChakraProvider>
  );
}
