import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    // Em dev o front chama /api no próprio :5173 e o Vite repassa pro backend -
    // mesmo domínio, então o cookie de sessão funciona sem depender de CORS.
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
