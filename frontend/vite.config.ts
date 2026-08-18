import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Dev sunucusu 5173'te, backend 8081'de calisiyor. Proxy sayesinde tarayici
// her seyi ayni origin'den gormus oluyor: CORS ayari gerekmiyor ve uretimde
// tek origin arkasinda calisacak kurulumla ayni sekilde davraniyor.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
      '/ws': {
        target: 'ws://localhost:8081',
        ws: true,
      },
    },
  },
});
