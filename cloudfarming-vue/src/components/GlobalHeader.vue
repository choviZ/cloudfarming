<template>
  <div id="global-header">
    <div class="header-container">
      <!-- Logo区域 -->
      <div class="header-logo">
        <a href="/" class="logo-text">云农场</a>
      </div>
      <!-- 用户功能区域 -->
      <div class="header-user">
        <a-space>
          <a-button type="text" @click="handleLoginClick">
            <template #icon>
              <UserOutlined />
            </template>
            登录
          </a-button>
          <a-button type="primary" size="small">免费注册</a-button>
          <a-badge count="3">
            <a-button type="text">
              <template #icon>
                <MessageOutlined />
              </template>
            </a-button>
          </a-badge>
          <a-button type="text">
            <template #icon>
              <ShoppingCartOutlined />
            </template>
            购物车
          </a-button>
        </a-space>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import {
  UserOutlined,
  MessageOutlined,
  ShoppingCartOutlined
} from '@ant-design/icons-vue'

const router = useRouter()

const handleLoginClick = () => {
  router.push('/user/login')
}
</script>

<style scoped>
#global-header {
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.09);
}

.header-container {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 240px;
  box-sizing: border-box;
}

.header-logo {
  flex: 0 0 auto;
}

.logo-text {
  font-size: 28px;
  font-weight: bold;
  color: #52c41a;
  text-decoration: none;
  font-family: 'Arial', sans-serif;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .header-container {
    padding: 0 40px;
  }
}

@media (max-width: 768px) {
  .header-container {
    padding: 0 20px;
    /* 禁止换行，保持水平排列 */
    flex-wrap: nowrap;
    /* 确保固定高度 */
    height: 60px;
    /* 调整元素间距 */
    gap: 10px;
  }
  
  .logo-text {
    font-size: 24px;
  }
  
  .header-user {
    /* 缩小用户功能区 */
    transform: scale(0.9);
    transform-origin: right center;
  }
}
</style>