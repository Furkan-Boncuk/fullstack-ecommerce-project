import { extendTheme } from '@chakra-ui/react';

export const theme = extendTheme({
  fonts: {
    heading: `'Poppins', sans-serif`,
    body: `'Nunito Sans', sans-serif`
  },
  colors: {
    brand: {
      50: '#f6f1ff',
      100: '#e9dbff',
      200: '#d8bcff',
      300: '#c49bff',
      400: '#af7aff',
      500: '#9a59ff',
      600: '#7f45d6',
      700: '#6536ab',
      800: '#4d2980',
      900: '#381d5c'
    }
  },
  styles: {
    global: {
      body: {
        bg: '#f7f5fc',
        color: 'gray.700'
      }
    }
  }
});
