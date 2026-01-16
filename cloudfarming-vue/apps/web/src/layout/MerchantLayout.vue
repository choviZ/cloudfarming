<template>
  <a-layout style="min-height: 100vh">
    <!-- 侧边栏菜单 -->
    <a-layout-sider v-model:collapsed="collapsed" collapsible theme="light">
      <div class="logo">
        <span>农户后台</span>
      </div>
      <a-menu v-model:selectedKeys="selectedKeys" theme="light" mode="inline">
        <a-menu-item key="0">
          <dashboard-outlined />
          <span>工作台</span>
        </a-menu-item>
        <a-sub-menu key="1">
          <template #title>
            <appstore-outlined />
            <span>管理商品</span>
          </template>
          <a-menu-item key="1-1" @click="router.push('/farmer/spu/create')">
            <plus-outlined />
            <span>创建商品</span>
          </a-menu-item>
        </a-sub-menu>
        <a-menu-item key="2">
          <shop-outlined />
          <span>管理店铺</span>
        </a-menu-item>
        <a-menu-item key="3">
          <transaction-outlined />
          <span>查看交易订单</span>
        </a-menu-item>
        <a-sub-menu key="4">
          <template #title>
            <project-outlined />
            <span>认养项目</span>
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
        <a-menu-item key="5">
          <bar-chart-outlined />
          <span>数据统计分析</span>
        </a-menu-item>
      </a-menu>
    </a-layout-sider>
    <a-layout>
      <!-- 顶部头部区域 -->
      <a-layout-header style="background: #fff; padding: 0; border-bottom: 4px solid #f0f0f0">
        <a-flex gap="middle" align="center" justify="flex-end" style="height: 100%;margin-right: 24px">
          <a-button type="link" :icon="h(RollbackOutlined)" @click="router.push('/')">
            返回商城
          </a-button>
        </a-flex>
      </a-layout-header>
      <!-- 中间内容区域 -->
      <a-layout-content style="margin: 0 16px; display: flex; flex-direction: column;">
        <a-breadcrumb style="margin: 16px 0">
          <a-breadcrumb-item>农户端</a-breadcrumb-item>
          <a-breadcrumb-item>后台管理</a-breadcrumb-item>
        </a-breadcrumb>
        <div :style="{ flex: 1, minHeight: '360px' }">
          <router-view />
        </div>
      </a-layout-content>
      <!-- 底部区域 -->
      <a-layout-footer style="text-align: center">
        Cloud Farming ©2026 Created by vv9
      </a-layout-footer>
    </a-layout>
  </a-layout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import {
  DashboardOutlined,
  ShopOutlined,
  AppstoreOutlined,
  TransactionOutlined,
  BarChartOutlined,
  RollbackOutlined,
  PlusOutlined,
  ProjectOutlined,
  OrderedListOutlined
} from '@ant-design/icons-vue'
import { h } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
// 控制侧边栏折叠
const collapsed = ref<boolean>(false)
// 当前选中的菜单项
const selectedKeys = ref<string[]>(['0'])

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
:deep(.ant-layout-sider-light) {
  border-right: 1px solid #f0f0f0;
}
:deep(.ant-menu-light.ant-menu-root.ant-menu-inline) {
  border-inline-end: none;
}
</style>
