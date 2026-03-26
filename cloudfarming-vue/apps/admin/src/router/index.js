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
        {
          path: '/admin/category',
          name: 'admin-category',
          component: () => import('@/views/admin/category/index.vue'),
        },
        {
          path: '/admin/attribute',
          name: 'admin-attribute',
          component: () => import('@/views/admin/attribute/index.vue'),
        },
        {
          path: '/admin/product',
          name: 'admin-product',
          component: () => import('@/views/admin/product/index.vue'),
        },
        {
          path: '/admin/article',
          name: 'admin-article',
          component: () => import('@/views/admin/article/index.vue'),
        },
        {
          path: '/admin/order',
          name: 'admin-order',
          component: () => import('@/views/admin/order/index.vue'),
        },
        {
          path: '/admin/product/detail/:id',
          name: 'admin-product-detail',
          component: () => import('@/views/admin/product/components/detail.vue'),
          meta: {
            title: '商品详情',
            hideInMenu: true
          }
        },
      ]
    }
  ],
})

router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()

  if (to.path !== '/login') {
    if (!userStore.loginUser) {
      try {
        await userStore.fetchUser()
      } catch (error) {
        userStore.clearUser()
      }
    }

    if (to.meta.requiresAuth && !userStore.loginUser) {
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      })
    } else {
      next()
    }
  } else {
    next()
  }
})

export default router
