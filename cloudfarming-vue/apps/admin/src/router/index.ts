import { createRouter, createWebHistory } from 'vue-router'
import useUserStore from '@/store/modules/user'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/admin/user',
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/login/index.vue'),
      meta: {
        requiresAuth: false,
      },
    },
    {
      path: '/admin',
      name: 'admin',
      component: () => import('@/layout/default-layout.vue'),
      meta: {
        requiresAuth: true,
      },
      children: [
        {
          path: '/admin/user',
          name: 'admin-user',
          component: () => import('@/views/admin/user/index.vue'),
        },
      ]
    }
  ],
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()

  // 如果不是登录页面，检查登录态
  if (to.path !== '/login') {
    // 如果用户信息不存在，尝试获取
    if (!userStore.loginUser) {
      try {
        await userStore.fetchUser()
      } catch (error) {
        // 获取用户信息失败，清除用户信息
        userStore.clearUser()
      }
    }

    // 检查是否需要认证
    if (to.meta.requiresAuth && !userStore.loginUser) {
      // 需要认证但未登录，重定向到登录页
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      })
    } else {
      // 已登录或不需要认证，继续导航
      next()
    }
  } else {
    // 是登录页面，直接放行
    next()
  }
})

export default router
