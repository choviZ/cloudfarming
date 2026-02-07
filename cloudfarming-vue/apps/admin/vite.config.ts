import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'
import { resolve } from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools()
  ],
  resolve: {
    alias: [
      {
        find: '@',
        replacement: fileURLToPath(new URL('./src', import.meta.url))
      },
      {
        find: 'assets',
        replacement: resolve(__dirname, '../src/assets')
      },
      {
        find: '@cloudfarming/core',
        replacement: resolve(__dirname, '../../packages/core/src')
      }
    ],
    extensions: ['.ts', '.js']
  },
  server: {
    proxy: {
      // 配置API代理
      '/api': {
          target: 'http://localhost:8000', // 后端服务地址
        changeOrigin: true, // 允许跨域
        // 重写路径，去掉/api前缀
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    },
  },
  css: {
    preprocessorOptions: {
      less: {
        modifyVars: {
          hack: `true; @import (reference) "${resolve(
            'src/assets/style/breakpoint.less'
          )}";`
        },
        javascriptEnabled: true
      }
    }
  }
})
