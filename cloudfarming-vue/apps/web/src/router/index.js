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
          redirect: (to) => ({
            name: 'productList',
            query: {
              ...to.query,
              mode: 'adopt'
            }
          })
        },
        {
          path: 'product/list',
          name: 'productList',
          component: () => import('../views/product/ProductList.vue')
        },
        {
          path: 'search',
          redirect: (to) => {
            const rawKeyword = Array.isArray(to.query.q) ? to.query.q[0] : to.query.q
            const rawType = Array.isArray(to.query.type) ? to.query.type[0] : to.query.type
            return {
              name: 'productList',
              query: {
                ...(rawKeyword ? { keyword: rawKeyword } : {}),
                mode: rawType === '0' ? 'adopt' : 'product'
              }
            }
          }
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
          path: 'feedback',
          name: 'feedbackCenter',
          component: () => import('../views/help/FeedbackCenter.vue')
        },
        {
          path: 'farmer/join',
          name: 'joinFarmer',
          component: () => import('../views/farmer/JoinFarmer.vue')
        },
        {
          path: 'farmers',
          name: 'farmerList',
          component: () => import('../views/farmer/public/FarmerList.vue')
        },
        {
          path: 'farmers/:id',
          name: 'farmerDetail',
          component: () => import('../views/farmer/public/FarmerDetail.vue')
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
        },
        {
          path: 'orders/review/:orderNo',
          name: 'userOrderReview',
          component: () => import('../views/user/orders/OrderReview.vue')
        },
        {
          path: 'adopts',
          name: 'userAdopts',
          component: () => import('../views/user/adopt/MyAdopts.vue')
        },
        {
          path: 'adopts/:instanceId',
          name: 'userAdoptDetail',
          component: () => import('../views/user/adopt/MyAdoptDetail.vue')
        },
        {
          path: 'profile',
          name: 'userProfile',
          component: () => import('../views/user/profile/Profile.vue')
        },
        {
          path: 'address',
          name: 'userCenterAddress',
          component: () => import('../views/user/address/AddressList.vue')
        }
      ]
    },
    {
      path: '/farmer',
      name: 'farmer',
      component: MerchantLayout,
      redirect: '/farmer/spu/list',
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
          path: 'orders',
          name: 'farmerOrders',
          component: () => import('../views/farmer/order/OrderManage.vue')
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
        },
        {
          path: 'adopt/instances',
          name: 'farmerAdoptInstances',
          component: () => import('../views/farmer/adopt/InstanceManage.vue')
        },
        {
          path: 'adopt/instances/:instanceId',
          name: 'farmerAdoptInstanceDetail',
          component: () => import('../views/farmer/adopt/InstanceDetail.vue')
        },
        {
          path: 'statistics',
          name: 'farmerStatistics',
          component: () => import('../views/farmer/statistics/StatisticsOverview.vue')
        },
        {
          path: 'showcase',
          name: 'farmerShowcase',
          component: () => import('../views/farmer/showcase/FarmerShowcaseManage.vue')
        }
      ]
    }
  ]
})

export default router
