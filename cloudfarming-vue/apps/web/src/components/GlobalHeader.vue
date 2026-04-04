<template>
  <div id="global-header" class="global-header">
    <div class="top-nav">
      <div class="header-container flex-between">
        <div class="left-links nav-group">
          <span class="nav-item">中国大陆</span>
          <span v-if="!userStore.loginUser" class="flex gap-small">
            <a @click="handleLoginClick" class="nav-item highlight">你好，请登录</a>
            <a @click="goToRegister" class="nav-item">免费注册</a>
          </span>
          <span v-else class="flex gap-small">
            <span>Hi, {{ userStore.loginUser.username }}</span>
            <a @click="handleLogout" class="nav-item">退出</a>
          </span>
        </div>
        <div class="right-links nav-group">
          <a v-if="showHomeEntry" class="nav-item" @click="goToHome">平台首页</a>
          <a v-if="userStore.loginUser" class="nav-item flex-center" @click="goToOrders">
            <FileTextOutlined class="icon" />
            我的订单
          </a>
          <a class="nav-item flex-center" @click="goToCart">
            <ShoppingCartOutlined class="icon" />
            购物车
          </a>
          <a class="nav-item" @click="goToFarmers">优质农户</a>
          <a class="nav-item" @click="handleSellerCenterClick">卖家中心</a>
          <a-dropdown placement="bottomRight" :trigger="['hover']">
            <a class="nav-item nav-item--dropdown" @click.prevent>
              帮助中心
              <DownOutlined class="dropdown-icon" />
            </a>
            <template #overlay>
              <a-menu @click="handleHelpMenuClick">
                <a-menu-item key="feedback">意见反馈</a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  DownOutlined,
  FileTextOutlined,
  ShoppingCartOutlined
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { userLogout } from '@/api/user'
import { useUserStore } from '@/stores/useUserStore'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const showHomeEntry = computed(() => !['/', '/index'].includes(route.path))

const handleLoginClick = () => {
  router.push('/user/login')
}

const goToRegister = () => {
  router.push('/user/register')
}

const goToHome = () => {
  router.push('/index')
}

const goToCart = () => {
  router.push('/cart')
}

const goToFarmers = () => {
  router.push('/farmers')
}

const goToOrders = () => {
  if (!userStore.loginUser) {
    message.warning('请先登录')
    router.push('/user/login')
    return
  }
  window.open('/usercenter/orders', '_blank')
}

const goToFeedback = () => {
  if (!userStore.loginUser) {
    message.warning('请先登录后再提交反馈')
    router.push('/user/login')
    return
  }
  router.push('/feedback')
}

const handleHelpMenuClick = ({ key }: { key: string }) => {
  if (key === 'feedback') {
    goToFeedback()
  }
}

const handleSellerCenterClick = () => {
  if (!userStore.loginUser) {
    message.warning('请先登录')
    router.push('/user/login')
    return
  }

  if (userStore.loginUser.userType === 1) {
    router.push('/farmer')
  } else {
    router.push('/farmer/join')
  }
}

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
  background-color: #ffffff;
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

.top-nav {
  background-color: #f5f5f5;
  color: #718096;
  font-size: 12px;
  padding: 0;
  height: 32px;
  line-height: 32px;
  border-bottom: 1px solid #e2e8f0;
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
  color: inherit;
}

.nav-item:hover {
  color: #388e3c;
}

.nav-item.highlight {
  color: #388e3c;
}

.nav-item.highlight:hover {
  color: #1b5e20;
}

.nav-item--dropdown {
  gap: 4px;
}

.dropdown-icon {
  font-size: 10px;
}

.icon {
  margin-right: 4px;
}
</style>
