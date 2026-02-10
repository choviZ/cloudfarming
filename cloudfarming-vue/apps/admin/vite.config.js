import { fileURLToPath, URL } from 'node:url'
import { dirname, resolve } from 'node:path'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

const __dirname = dirname(fileURLToPath(import.meta.url))

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
      }
    ],
    extensions: ['.js', '.json', '.vue']
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
