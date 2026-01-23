<template>
  <div id="global-header" class="global-header">
    <div class="top-nav">
      <div class="header-container flex-between">
        <div class="left-links nav-group">
          <span class="nav-item">中国大陆</span>
          <span v-if="!userStore.loginUser" class="flex gap-small">
            <a @click="handleLoginClick" class="nav-item highlight">亲，请登录</a>
            <a class="nav-item">免费注册</a>
          </span>
          <span v-else class="flex gap-small">
            <span>Hi, {{ userStore.loginUser.username }}</span>
            <a @click="handleLogout" class="nav-item">退出</a>
          </span>
        </div>
        <div class="right-links nav-group">
          <a class="nav-item flex-center" @click="goToCart">
            <ShoppingCartOutlined class="icon"/>
            购物车
          </a>
          <a class="nav-item" @click="handleSellerCenterClick">卖家中心</a>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {useRouter} from 'vue-router'
import {
  ShoppingCartOutlined
} from '@ant-design/icons-vue'
import { useUserStore } from '@/stores/useUserStore'
import {message} from 'ant-design-vue'
import { userLogout } from '@/api/user'

const router = useRouter()
const userStore = useUserStore()

const handleLoginClick = () => {
  router.push('/user/login')
}

const goToCart = () => {
  router.push('/cart')
}

const handleSellerCenterClick = () => {
  if (!userStore.loginUser) {
    message.warning('请先登录')
    router.push('/user/login')
    return
  }
  
  // userType: 0-普通用户 1-农户 2-系统管理员
  if (userStore.loginUser.userType === 1) {
    router.push('/farmer')
  } else {
    // 普通用户或其他角色跳转到入驻页面
    router.push('/farmer/join')
  }
}

// 退出登录处理函数
const handleLogout = async () => {
  try {
    await userLogout()
    userStore.clearUser()
    message.success('退出登录成功')
    await router.push('/user/login')
  } catch (error) {
    console.log(error)
    message.error('退出登录失败')
  }
}
</script>

<style scoped>
.global-header {
  display: flex;
  flex-direction: column;
  width: 100%;
  background-color: #FFFFFF;
}

.header-container {
  width: 100%;
  max-width: 1550px;
  margin: 0 auto;
  padding: 0 20px;
}

.flex-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.flex-center {
  display: flex;
  align-items: center;
}

.gap-small {
  display: flex;
  gap: 8px;
}

/* Top Nav */
.top-nav {
  background-color: #F5F5F5;
  color: #718096;
  font-size: 12px;
  padding: 0;
  height: 32px;
  line-height: 32px;
  border-bottom: 1px solid #E2E8F0;
}

.nav-group {
  display: flex;
  gap: 16px;
  align-items: center;
}

.nav-item {
  cursor: pointer;
  transition: color 0.2s;
  display: flex;
  align-items: center;
}

.nav-item:hover {
  color: #388E3C;
}

.nav-item.highlight {
  color: #388E3C;
}

.nav-item.highlight:hover {
  color: #1B5E20;
}

.icon {
  margin-right: 4px;
}
</style>
