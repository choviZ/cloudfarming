import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/LoginView.vue'
import BasicLayout from '../layout/BasicLayout.vue'
import MerchantLayout from '@/layout/MerchantLayout.vue'
import UserCenterLayout from '@/layout/UserCenterLayout.vue'

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
          path: 'adopt/list',
          name: 'adoptList',
          component: () => import('../views/adopt/AdoptList.vue')
        },
        {
          path: 'product/list',
          name: 'productList',
          component: () => import('../views/product/ProductList.vue')
        },
        {
          path: 'search',
          name: 'search',
          component: () => import('../views/SearchResult.vue')
        },
        {
          path: 'article/list',
          name: 'articleList',
          component: () => import('../views/article/ArticleList.vue')
        },
        {
          path: 'article/:id',
          name: 'articleDetail',
          component: () => import('../views/article/ArticleDetail.vue')
        },
        {
          path: 'adopt/detail/:id',
          name: 'adoptDetail',
          component: () => import('../views/adopt/AdoptDetail.vue')
        },
        {
          path: 'adopt/order/create/:id',
          name: 'adoptOrderCreate',
          component: () => import('../views/order/OrderCreate.vue')
        },
        {
          path: 'order/create',
          name: 'orderCreate',
          component: () => import('../views/order/OrderCreate.vue')
        },
        {
          path: 'pay',
          name: 'pay',
          component: () => import('../views/pay/Pay.vue'),
          meta: { grayLayout: true }
        },
        {
          path: 'paySuccess',
          name: 'paySuccess',
          component: () => import('../views/pay/PaySuccess.vue')
        },
        {
          path: 'product/:id',
          name: 'productDetail',
          component: () => import('../views/product/ProductDetailView.vue')
        },
        {
          path: 'shop/:shopId',
          name: 'shopHome',
          component: () => import('../views/shop/ShopHome.vue')
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
        }
      ]
    },
    {
      path: '/user',
      name: 'userState',
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
    {
      path: '/user/info',
      name: 'userInfo',
      component: BasicLayout,
      children: [
        {
          path: 'address',
          name: 'userAddress',
          component: () => import('../views/user/address/AddressList.vue')
        }
      ]
    },
    {
      path: '/usercenter',
      name: 'userCenter',
      component: UserCenterLayout,
      redirect: '/usercenter/orders',
      children: [
        {
          path: 'orders',
          name: 'userOrders',
          component: () => import('../views/user/orders/OrderList.vue')
        }
      ]
    },
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
          path: 'spu/list',
          name: 'mySpuList',
          component: () => import('../views/farmer/admin/ProductList.vue')
        },
        {
          path: 'spu/create',
          name: 'createSpu',
          component: () => import('../views/farmer/admin/Index.vue')
        },
        {
          path: 'shop',
          name: 'farmerShop',
          component: () => import('../views/farmer/shop/ShopManage.vue')
        },
        {
          path: 'adopt/create',
          name: 'createAdopt',
          component: () => import('../views/farmer/adopt/CreateAdopt.vue')
        },
        {
          path: 'adopt/my',
          name: 'myAdopts',
          component: () => import('../views/farmer/adopt/MyAdopts.vue')
        }
      ]
    }
  ]
})

export default router
