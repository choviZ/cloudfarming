<template>
  <div id="default-layout">
    <a-layout class="layout-shell">
      <a-layout-header class="navbar">
        <div class="navbar__inner">
          <div class="brand">
            <img :src="brandMark" alt="云农场管理后台" class="brand__mark" />
            <div class="brand__copy">
              <h1>云农场管理后台</h1>
              <p>经营总览、内容治理与平台运营中心</p>
            </div>
          </div>
          <div class="profile">
            <div class="profile__meta">
              <span class="profile__name">{{ displayName }}</span>
              <span class="profile__role">系统管理员</span>
            </div>
            <a-avatar :src="userStore.loginUser?.avatar" size="40">
              <template #icon>
                <UserOutlined />
              </template>
            </a-avatar>
          </div>
        </div>
      </a-layout-header>

      <a-layout class="main-shell">
        <a-layout-sider class="sider" :width="220">
          <a-menu
            v-model:selectedKeys="selectedKeys"
            mode="inline"
            :items="items"
            class="sider-menu"
            @click="handleClick"
          />
        </a-layout-sider>

        <a-layout-content class="layout-content">
          <router-view />
        </a-layout-content>
      </a-layout>

      <a-layout-footer class="footer">CloudFarming Admin Console</a-layout-footer>
    </a-layout>
  </div>
</template>

<script setup>
import { computed, h, ref, watch } from 'vue'
import {
  AppstoreOutlined,
  DashboardOutlined,
  FileTextOutlined,
  MessageOutlined,
  NotificationOutlined,
  ShopOutlined,
  TagsOutlined,
  TeamOutlined,
  UserOutlined,
} from '@ant-design/icons-vue'
import { useRoute, useRouter } from 'vue-router'
import useUserStore from '@/store/modules/user'
import brandMark from '@/assets/brand-mark.svg'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const selectedKeys = ref([])
const displayName = computed(() => userStore.loginUser?.username || '管理员')

const resolveSelectedKey = (path) => {
  if (path.startsWith('/admin/product/detail/')) {
    return 'admin/product'
  }
  return path.replace(/^\//, '')
}

watch(
  () => route.path,
  (path) => {
    selectedKeys.value = [resolveSelectedKey(path)]
  },
  { immediate: true }
)

const items = ref([
  {
    label: '数据看板',
    title: '数据看板',
    key: 'admin/dashboard',
    icon: () => h(DashboardOutlined),
  },
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
    label: '分类属性',
    title: '分类属性',
    key: 'admin/attribute',
    icon: () => h(TagsOutlined),
  },
  {
    label: '商品管理',
    title: '商品管理',
    key: 'admin/product',
    icon: () => h(ShopOutlined),
  },
  {
    label: '农户管理',
    title: '农户管理',
    key: 'admin/farmer',
    icon: () => h(TeamOutlined),
  },
  {
    label: '文章资讯',
    title: '文章资讯',
    key: 'admin/article',
    icon: () => h(NotificationOutlined),
  },
  {
    label: '订单管理',
    title: '订单管理',
    key: 'admin/order',
    icon: () => h(FileTextOutlined),
  },
  {
    label: '反馈处理',
    title: '反馈处理',
    key: 'admin/feedback',
    icon: () => h(MessageOutlined),
  },
])

const handleClick = ({ key }) => {
  router.push(`/${key}`)
}
</script>

<style scoped>
#default-layout {
  min-height: 100vh;
  background:
    radial-gradient(circle at top right, rgba(59, 130, 246, 0.16), transparent 28%),
    linear-gradient(180deg, #f8fbff 0%, #eef4fb 100%);
}

.layout-shell {
  min-height: 100vh;
}

.navbar {
  position: fixed;
  top: 0;
  left: 0;
  z-index: 100;
  width: 100%;
  height: 72px;
  padding: 0 24px;
  line-height: normal;
  background: rgba(255, 255, 255, 0.92);
  border-bottom: 1px solid rgba(15, 23, 42, 0.08);
  backdrop-filter: blur(16px);
}

.navbar__inner {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 14px;
  min-height: 100%;
}

.brand__mark {
  width: 44px;
  height: 44px;
  flex-shrink: 0;
}

.brand__copy {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 2px;
  min-height: 100%;
}

.brand__copy h1 {
  margin: 0;
  color: #0f172a;
  font-size: 18px;
  font-weight: 700;
  line-height: 1.2;
}

.brand__copy p {
  margin: 0;
  color: #64748b;
  font-size: 12px;
  line-height: 1.4;
}

.profile {
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 100%;
}

.profile__meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  justify-content: center;
  gap: 2px;
  min-height: 100%;
}

.profile__name {
  color: #0f172a;
  font-size: 14px;
  font-weight: 600;
  line-height: 1.2;
}

.profile__role {
  color: #64748b;
  font-size: 12px;
  line-height: 1.4;
  white-space: nowrap;
}

.sider {
  position: fixed;
  top: 72px;
  left: 0;
  z-index: 100;
  height: calc(100vh - 72px);
  background: rgba(255, 255, 255, 0.9);
  border-right: 1px solid rgba(15, 23, 42, 0.08);
  backdrop-filter: blur(12px);
}

.sider-menu {
  height: 100%;
  padding: 18px 12px 24px;
  background: transparent;
  border-inline-end: none;
}

.sider-menu :deep(.ant-menu-item) {
  height: 46px;
  margin-bottom: 6px;
  border-radius: 14px;
  line-height: 46px;
}

.sider-menu :deep(.ant-menu-item-selected) {
  background: linear-gradient(135deg, rgba(30, 64, 175, 0.16), rgba(59, 130, 246, 0.08));
  color: #1d4ed8;
  font-weight: 600;
}

.sider-menu :deep(.ant-menu-item .ant-menu-item-icon) {
  font-size: 16px;
}

.layout-content {
  min-height: calc(100vh - 72px - 48px);
  width: calc(100% - 220px);
  margin-top: 72px;
  margin-left: 220px;
  padding: 24px 24px 64px;
  overflow: auto;
}

.footer {
  position: fixed;
  height: 48px;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  color: rgba(255, 255, 255, 0.72);
  font-size: 12px;
  letter-spacing: 0.08em;
  background: #0f172a;
}

@media (max-width: 992px) {
  .navbar {
    padding: 0 16px;
  }

  .brand__copy p,
  .profile__meta {
    display: none;
  }

  .sider {
    width: 200px !important;
  }

  .layout-content {
    width: calc(100% - 200px);
    margin-left: 200px;
    padding: 20px 16px 64px;
  }
}
</style>
