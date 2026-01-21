<template>
  <div id="default-layout">
    <a-layout>
      <!-- 顶部导航栏 -->
      <a-layout-header class="navbar">
        <a-flex align="center" justify="space-between" style="height: 60px;">
          <a-space>
            <img src="@/assets/logo.svg" alt="logo" style="width: 40px; height: 40px;" />
            <h4>云养殖后台管理系统</h4>
          </a-space>
          <a-avatar size="32">
            <template #icon>
              <UserOutlined />
            </template>
          </a-avatar>
        </a-flex>
      </a-layout-header>
      <a-layout>
        <!-- 侧边栏-菜单项 -->
        <a-layout-sider class="sider">
          <a-menu v-model:selectedKeys="selectedKeys1" mode="inline" :style="{ lineHeight: '64px' }" :items="items" @click="handleClick"/>
        </a-layout-sider>
        <!-- 内容区域 -->
        <a-layout-content class="layout-content">
          <router-view />
        </a-layout-content>
      </a-layout>
      <!-- 页脚 -->
      <a-layout-footer class="footer">Footer</a-layout-footer>
    </a-layout>
  </div>
</template>

<script lang="ts" setup>
import type { MenuProps } from 'ant-design-vue';
import { ref, h } from 'vue'
import { UserOutlined, AppstoreOutlined, TagsOutlined } from '@ant-design/icons-vue'
import { useRouter } from 'vue-router';

const router = useRouter();
const selectedKeys1 = ref<string[]>(['admin/user']);
const items = ref<MenuProps['items']>([
  {
    label: '用户管理',
    title: '用户管理',
    key: 'admin/user',
    icon: () => h(UserOutlined),
  },
  {
    label: '分类管理',
    title: '分类管理',
    key: 'admin/category',
    icon: () => h(AppstoreOutlined),
  },
  {
    label: '分类属性管理',
    title: '分类属性管理',
    key: 'admin/attribute',
    icon: () => h(TagsOutlined),
  }
])

const handleClick = ({key} : {key: string}) => {
  router.push(`/${key}`);
}
</script>

<style scoped>
#default-layout {
  height: 100vh;
  width: 100vw;
}

.navbar {
  position: fixed;
  top: 0;
  left: 0;
  z-index: 100;
  width: 100%;
  height: 60px;
  background-color: white;
  border-bottom: 1px solid #e5e5e5;
}

.sider {
  position: fixed;
  top: 60px;
  left: 0;
  z-index: 100;
  width: 200px;
  height: 100%;
  background-color: white;
}

.layout-content {
  height: calc(100vh - 60px - 40px);
  /* 减去 navbar 和 footer 的高度 */
  width: calc(100% - 200px);
  /* 减去 sider 的宽度 */
  margin-top: 60px;
  /* 为 navbar 腾出空间 */
  margin-left: 200px;
  /* 为 sider 腾出空间 */
  background-color: #f5f5f5;
  overflow: auto;
  padding: 16px;
}

.footer {
  position: fixed;
  height: 40px;
  bottom: 0;
  left: 0;
  right: 0;
  text-align: center;
  color: #fff;
  background-color: #7dbcea;
}
</style>
