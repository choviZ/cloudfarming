import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
      '@cloudfarming/core': fileURLToPath(new URL('../../packages/core/src', import.meta.url))
    },
  },
  server: {
    proxy: {
      // 配置API代理
      '/api': {
        target: 'http://localhost:8080', // 后端服务地址
        changeOrigin: true, // 允许跨域
        // 重写路径，去掉/api前缀
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
})
