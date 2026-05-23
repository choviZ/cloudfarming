<template>
  <a-layout class="admin-layout">
    <a-layout-sider
      v-model:collapsed="collapsed"
      class="admin-sider"
      :width="224"
      :collapsed-width="72"
      collapsible
      theme="light"
      :trigger="null"
    >
      <div class="admin-brand">
        <AppstoreOutlined class="admin-brand__icon" />
        <div v-if="!collapsed" class="admin-brand__text">
          <strong>云农场后台</strong>
          <span>Admin Console</span>
        </div>
      </div>

      <a-menu
        v-model:selectedKeys="selectedKeys"
        mode="inline"
        theme="light"
        :items="items"
        class="admin-menu"
        @click="handleClick"
      />
    </a-layout-sider>

    <a-layout class="admin-main-layout">
      <a-layout-header class="admin-header">
        <div class="admin-header__left">
          <a-button type="text" class="collapse-button" @click="collapsed = !collapsed">
            <template #icon>
              <MenuUnfoldOutlined v-if="collapsed" />
              <MenuFoldOutlined v-else />
            </template>
          </a-button>

          <a-breadcrumb>
            <a-breadcrumb-item>管理后台</a-breadcrumb-item>
            <a-breadcrumb-item>{{ pageTitle }}</a-breadcrumb-item>
          </a-breadcrumb>
        </div>

        <a-dropdown placement="bottomRight">
          <a-button type="text" class="user-button">
            <a-space>
              <a-avatar :src="userStore.loginUser?.avatar" :size="32">
                <template #icon>
                  <UserOutlined />
                </template>
              </a-avatar>
              <span class="user-button__name">{{ displayName }}</span>
            </a-space>
          </a-button>
          <template #overlay>
            <a-menu @click="handleUserMenuClick">
              <a-menu-item key="logout">
                <template #icon>
                  <LogoutOutlined />
                </template>
                退出登录
              </a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
      </a-layout-header>

      <a-layout-content class="admin-content">
        <a-page-header
          class="admin-page-header"
          :title="pageTitle"
          :sub-title="pageDescription"
        />
        <router-view />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup>
import { computed, h, ref, watch } from 'vue'
import {
  AppstoreOutlined,
  DashboardOutlined,
  FileTextOutlined,
  LogoutOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  MessageOutlined,
  NotificationOutlined,
  ShopOutlined,
  TagsOutlined,
  TeamOutlined,
  UserOutlined,
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'
import useUserStore from '@/store/modules/user'
import { userLogout } from '@/api/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const collapsed = ref(false)
const selectedKeys = ref([])
const displayName = computed(() => userStore.loginUser?.username || '管理员')

const menuList = [
  {
    label: '数据看板',
    key: 'admin/dashboard',
    icon: DashboardOutlined,
    description: '平台经营、待办和趋势概览',
  },
  {
    label: '用户管理',
    key: 'admin/user',
    icon: UserOutlined,
    description: '维护用户、农户和管理员账号',
  },
  {
    label: '分类管理',
    key: 'admin/category',
    icon: AppstoreOutlined,
    description: '配置商城分类层级和展示顺序',
  },
  {
    label: '分类属性',
    key: 'admin/attribute',
    icon: TagsOutlined,
    description: '维护分类下的基础属性与销售属性',
  },
  {
    label: '商品管理',
    key: 'admin/product',
    icon: ShopOutlined,
    description: '处理商品审核、上下架和详情查看',
  },
  {
    label: '农户管理',
    key: 'admin/farmer',
    icon: TeamOutlined,
    description: '查看农户资料并维护精选状态',
  },
  {
    label: '文章资讯',
    key: 'admin/article',
    icon: NotificationOutlined,
    description: '发布公告、政策和养殖知识内容',
  },
  {
    label: '订单管理',
    key: 'admin/order',
    icon: FileTextOutlined,
    description: '查询订单、物流和售后状态',
  },
  {
    label: '反馈处理',
    key: 'admin/feedback',
    icon: MessageOutlined,
    description: '跟进用户与农户提交的问题反馈',
  },
]

const menuMap = new Map(menuList.map((item) => [item.key, item]))

const items = computed(() =>
  menuList.map((item) => ({
    label: item.label,
    title: item.label,
    key: item.key,
    icon: () => h(item.icon),
  }))
)

const currentMenu = computed(() => menuMap.get(selectedKeys.value[0]) || menuList[0])

const pageTitle = computed(() => route.meta?.title || currentMenu.value.label)
const pageDescription = computed(() => route.meta?.description || currentMenu.value.description)

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

const handleClick = ({ key }) => {
  router.push(`/${key}`)
}

const handleUserMenuClick = async ({ key }) => {
  if (key !== 'logout') {
    return
  }
  try {
    await userLogout()
  } catch (error) {
    console.error(error)
  } finally {
    userStore.clearUser()
    message.success('已退出登录')
    router.push('/login')
  }
}
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
}

.admin-sider {
  height: 100vh;
  overflow: hidden;
  border-right: 1px solid #f0f0f0;
}

.admin-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 64px;
  padding: 0 16px;
  border-bottom: 1px solid #f0f0f0;
}

.admin-brand__icon {
  font-size: 24px;
  flex-shrink: 0;
}

.admin-brand__text {
  display: flex;
  flex-direction: column;
  min-width: 0;
  line-height: 1.2;
}

.admin-brand__text strong {
  font-size: 15px;
}

.admin-brand__text span {
  font-size: 12px;
}

.admin-menu {
  height: calc(100vh - 64px);
  padding: 8px;
  overflow-y: auto;
  border-inline-end: none;
}

.admin-main-layout {
  min-width: 0;
}

.admin-header {
  position: sticky;
  top: 0;
  z-index: 20;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  padding: 0 20px;
  line-height: normal;
  background: #ffffff;
  border-bottom: 1px solid #f0f0f0;
}

.admin-header__left {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.collapse-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.user-button {
  height: 40px;
  padding: 4px 8px;
}

.user-button__name {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.admin-content {
  height: calc(100vh - 64px);
  min-width: 0;
  padding: 20px;
  overflow: auto;
  background: #f0f2f5;
}

.admin-page-header {
  margin-bottom: 16px;
  padding: 0;
}

.admin-page-header :deep(.ant-page-header-heading-title) {
  font-size: 20px;
  line-height: 1.3;
}

@media (max-width: 768px) {
  .admin-header {
    padding: 0 12px;
  }

  .user-button__name {
    display: none;
  }

  .admin-content {
    padding: 16px;
  }
}
</style>
