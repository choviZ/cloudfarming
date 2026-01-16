import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/LoginView.vue'
import BasicLayout from '../layout/BasicLayout.vue'
import MerchantLayout from '@/layout/MerchantLayout.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: BasicLayout,
      redirect: '/index',
      children: [
        {
          path: 'index',
          name: 'index',
          component: () => import('../views/Index.vue')
        },
        {
          path: '/product/:id',
          name: 'productDetail',
          component: () => import('../views/ProductDetailView.vue')
        },
        {
          path: 'cart',
          name: 'cart',
          component: () => import('../views/Cart.vue')
        },
        {
          path: 'farmer/join',
          name: 'joinFarmer',
          component: () => import('../views/farmer/JoinFarmer.vue')
        },
      ]
    },
    // 用户相关路由组
    {
      path: '/user',
      name: 'user',
      children: [
        {
          path: 'login',
          name: 'login',
          component: Login
        },
        {
          path: 'register',
          name: 'register',
          component: Login
        }
      ]
    },
    // 农户后台路由
    {
      path: '/farmer',
      name: 'farmer',
      component: MerchantLayout,
      redirect: '/farmer/index',
      children: [
        {
          path: 'index',
          name: 'farmerWorkbench',
          component: () => import('../views/farmer/Workbench.vue')
        },
        {
          path: 'spu/create',
          name: 'createSpu',
          component: () => import('../views/farmer/admin/Index.vue')
        },
        {
          path: 'adopt/create',
          name: 'createAdopt',
          component: () => import('../views/farmer/adopt/CreateAdopt.vue')
        }
      ]
    }
  ]
})

export default router
