<template>
  <a-layout style="min-height: 100vh">
    <a-layout-sider v-model:collapsed="collapsed" collapsible theme="light">
      <div class="logo">
        <span>农户控制台</span>
      </div>
      <a-menu
        :selected-keys="selectedKeys"
        :open-keys="openKeys"
        theme="light"
        mode="inline"
      >
        <a-menu-item key="0" @click="router.push('/farmer/index')">
          <dashboard-outlined />
          <span>工作台</span>
        </a-menu-item>
        <a-sub-menu key="1">
          <template #title>
            <appstore-outlined />
            <span>管理商品</span>
          </template>
          <a-menu-item key="1-1" @click="router.push('/farmer/spu/list')">
            <unordered-list-outlined />
            <span>商品列表</span>
          </a-menu-item>
          <a-menu-item key="1-2" @click="router.push('/farmer/spu/create')">
            <plus-outlined />
            <span>创建商品</span>
          </a-menu-item>
        </a-sub-menu>
        <a-menu-item key="2" @click="router.push('/farmer/shop')">
          <shop-outlined />
          <span>店铺管理</span>
        </a-menu-item>
        <a-menu-item key="3">
          <transaction-outlined />
          <span>订单管理</span>
        </a-menu-item>
        <a-sub-menu key="4">
          <template #title>
            <project-outlined />
            <span>认养管理</span>
          </template>
          <a-menu-item key="4-1" @click="router.push('/farmer/adopt/create')">
            <plus-outlined />
            <span>发布认养</span>
          </a-menu-item>
          <a-menu-item key="4-2" @click="router.push('/farmer/adopt/my')">
            <ordered-list-outlined />
            <span>我的发布</span>
          </a-menu-item>
        </a-sub-menu>
        <a-menu-item key="5" @click="router.push('/farmer/statistics')">
          <bar-chart-outlined />
          <span>数据统计</span>
        </a-menu-item>
      </a-menu>
    </a-layout-sider>
    <a-layout>
      <a-layout-header class="layout-header">
        <a-flex gap="middle" align="center" justify="flex-end" class="header-actions">
          <a-button type="link" :icon="h(RollbackOutlined)" @click="router.push('/')">
            返回商城
          </a-button>
        </a-flex>
      </a-layout-header>
      <a-layout-content class="layout-content">
        <a-breadcrumb class="layout-breadcrumb">
          <a-breadcrumb-item>农户端</a-breadcrumb-item>
          <a-breadcrumb-item>后台管理</a-breadcrumb-item>
        </a-breadcrumb>
        <div class="layout-body">
          <router-view />
        </div>
      </a-layout-content>
      <a-layout-footer style="text-align: center">
        Cloud Farming 2026
      </a-layout-footer>
    </a-layout>
  </a-layout>
</template>

<script setup>
import { computed, h, ref } from 'vue'
import {
  AppstoreOutlined,
  BarChartOutlined,
  DashboardOutlined,
  OrderedListOutlined,
  PlusOutlined,
  ProjectOutlined,
  RollbackOutlined,
  ShopOutlined,
  TransactionOutlined,
  UnorderedListOutlined
} from '@ant-design/icons-vue'
import { useRoute, useRouter } from 'vue-router'

const router = useRouter()
const route = useRoute()

const collapsed = ref(false)

const selectedKeys = computed(() => {
  if (route.path === '/farmer/index') {
    return ['0']
  }
  if (route.path === '/farmer/spu/list') {
    return ['1-1']
  }
  if (route.path === '/farmer/spu/create') {
    return ['1-2']
  }
  if (route.path === '/farmer/shop') {
    return ['2']
  }
  if (route.path === '/farmer/adopt/create') {
    return ['4-1']
  }
  if (route.path === '/farmer/adopt/my') {
    return ['4-2']
  }
  if (route.path === '/farmer/statistics') {
    return ['5']
  }
  return []
})

const openKeys = computed(() => {
  if (route.path.startsWith('/farmer/spu')) {
    return ['1']
  }
  if (route.path.startsWith('/farmer/adopt')) {
    return ['4']
  }
  return []
})
</script>

<style scoped>
.logo {
  height: 32px;
  margin: 16px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}

.layout-header {
  background: #fff;
  padding: 0;
  border-bottom: 4px solid #f0f0f0;
}

.header-actions {
  height: 100%;
  margin-right: 24px;
}

.layout-content {
  margin: 0 16px;
  display: flex;
  flex-direction: column;
}

.layout-breadcrumb {
  margin: 16px 0;
}

.layout-body {
  flex: 1;
  min-height: 360px;
}

:deep(.ant-layout-sider-light) {
  border-right: 1px solid #f0f0f0;
}

:deep(.ant-menu-light.ant-menu-root.ant-menu-inline) {
  border-inline-end: none;
}
</style>
