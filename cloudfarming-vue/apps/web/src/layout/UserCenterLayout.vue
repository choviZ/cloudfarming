<template>
  <a-layout class="user-center-layout">
    <a-layout-header class="layout-header">
      <global-header />
    </a-layout-header>
    <a-layout-content class="layout-content">
      <div class="user-center-container">
        <!-- 左侧菜单 -->
        <aside class="sidebar-menu">
          <div class="menu-section">
            <div class="menu-title">
              <a href="/" class="menu-title-link">云养殖助农平台</a>
            </div>
            <a-menu mode="inline" :selected-keys="selectedKeys" :default-open-keys="['accountSetting']">
              <a-menu-item key="orders">
                <template #icon>
                  <FileTextOutlined />
                </template>
                <router-link to="/usercenter/orders">我的订单</router-link>
              </a-menu-item>
              <a-menu-item key="adopts">
                <template #icon>
                  <HeartOutlined />
                </template>
                <router-link to="/usercenter/adopts">我的认养</router-link>
              </a-menu-item>
              <a-menu-item key="cart">
                <template #icon>
                  <ShoppingCartOutlined />
                </template>
                <router-link to="/cart">购物车</router-link>
              </a-menu-item>
              <a-sub-menu key="accountSetting">
                <template #icon>
                  <SettingOutlined />
                </template>
                <template #title>账户设置</template>
                <a-menu-item key="profile">
                  <router-link to="/usercenter/profile">个人资料</router-link>
                </a-menu-item>
                <a-menu-item key="address">
                  <router-link to="/usercenter/address">收货地址</router-link>
                </a-menu-item>
              </a-sub-menu>
            </a-menu>
          </div>
        </aside>

        <!-- 右侧内容区 -->
        <main class="main-content">
          <router-view />
        </main>
      </div>
    </a-layout-content>
    <a-layout-footer class="layout-footer">
      <div class="footer-content">
        <p>网站信息和备案信息占位</p>
      </div>
    </a-layout-footer>
  </a-layout>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import GlobalHeader from '../components/GlobalHeader.vue'
import { FileTextOutlined, HeartOutlined, SettingOutlined, ShoppingCartOutlined } from '@ant-design/icons-vue'

const route = useRoute()
const selectedKeys = computed(() => {
  if (route.path.includes('/usercenter/profile')) {
    return ['profile']
  }
  if (route.path.includes('/usercenter/address')) {
    return ['address']
  }
  if (route.path.includes('/usercenter/adopts')) {
    return ['adopts']
  }
  if (route.path.includes('/cart')) {
    return ['cart']
  }
  return ['orders']
})
</script>

<style scoped>
.user-center-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f6f7f8;
}

.layout-header {
  height: 32px;
  padding: 0;
  background: #F5F5F5;
  border-bottom: 1px solid #E2E8F0;
  z-index: 10;
}

.layout-content {
  flex: 1;
  padding: 24px 0;
}

.user-center-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 16px;
  display: flex;
  gap: 20px;
}

.sidebar-menu {
  width: 200px;
  flex-shrink: 0;
}

.menu-section {
  background: #fff;
  border-radius: 8px;
  padding: 16px 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.menu-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  padding: 8px 24px 16px;
  border-bottom: 1px solid #f3f4f6;
  margin-bottom: 8px;
}

.menu-title-link {
  color: inherit;
  text-decoration: none;
}

.menu-title-link:hover {
  color: #16a34a;
}

.sidebar-menu :deep(.ant-menu) {
  border: none;
  background: transparent;
}

.sidebar-menu :deep(.ant-menu-item) {
  margin: 4px 8px;
  border-radius: 4px;
  height: 44px;
  line-height: 44px;
}

.sidebar-menu :deep(.ant-menu-submenu-title) {
  margin: 4px 8px;
  border-radius: 4px;
  height: 44px;
  line-height: 44px;
}

.sidebar-menu :deep(.ant-menu-item:hover) {
  background-color: #f6f7f8;
}

.sidebar-menu :deep(.ant-menu-submenu-title:hover) {
  background-color: #f6f7f8;
}

.sidebar-menu :deep(.ant-menu-item-selected) {
  background-color: #f0fdf4;
  color: #16a34a;
}

.sidebar-menu :deep(.ant-menu-item a) {
  color: inherit;
  text-decoration: none;
  display: block;
  width: 100%;
  height: 100%;
}

.main-content {
  flex: 1;
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  min-height: 600px;
}

.layout-footer {
  background-color: #FFFFFF;
  padding: 40px 0;
  border-top: 1px solid #E2E8F0;
}

.footer-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 16px;
  text-align: center;
  color: #059669;
  font-size: 14px;
}
</style>
