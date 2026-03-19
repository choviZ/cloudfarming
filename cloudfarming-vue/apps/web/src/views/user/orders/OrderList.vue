<template>
  <div class="orders-page">
    <div class="page-header">
      <h2 class="page-title">我的订单</h2>
    </div>

    <!-- Tab 切换 -->
    <div class="tabs-wrapper">
      <a-tabs v-model:activeKey="activeTab" @change="handleTabChange">
        <a-tab-pane key="all" tab="全部订单" />
        <a-tab-pane key="pending" tab="待支付" />
        <a-tab-pane key="pendingShip" tab="待发货" />
        <a-tab-pane key="shipped" tab="待收货" />
      </a-tabs>
    </div>

    <!-- 订单列表 -->
    <div class="orders-list">
      <a-spin :spinning="loading">
        <div v-if="orderList.length === 0" class="empty-state">
          <InboxOutlined class="empty-icon" />
          <p>暂无相关订单</p>
        </div>
        <div v-else class="order-items">
          <div v-for="order in orderList.records" :key="order.id" class="order-card">
            <!-- 订单头部 -->
            <div class="order-header">
              <span class="order-time">{{ formatDate(order.createTime) }}</span>
              <span class="order-no">订单号：{{ order.orderNo }}</span>
              <span class="order-shop">{{ order.shopName }}</span>
            </div>
            <!-- 订单商品 -->
            <div class="order-body">
              <div class="order-products">
                <div v-for="item in order.items" :key="item.productName" class="order-product">
                  <img :src="item.coverImage" :alt="item.productName" class="product-img" />
                  <div class="product-info">
                    <div class="product-title">{{ item.productName }}</div>
                  </div>
                  <div class="product-price">
                    <span class="price">¥{{ item.price }}</span>
                    <span class="quantity">x{{ item.quantity }}</span>
                  </div>
                </div>
              </div>
            </div>
            <!-- 订单底部 -->
            <div class="order-footer">
              <div class="order-total">
                <span class="label">合计：</span>
                <span class="amount">¥{{ order.totalPrice }}</span>
              </div>
            </div>
          </div>
        </div>
      </a-spin>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { InboxOutlined } from '@ant-design/icons-vue'
import { getOrderList } from '@/api/order'
import { useUserStore } from '@/stores/useUserStore'

const loading = ref(false)
const activeTab = ref('all')
const orderList = ref([])
const userStore = useUserStore()

const userId = computed(() => {
  return userStore.loginUser?.id
})

const handleTabChange = (key) => {
  activeTab.value = key
  fetchOrders()
}

const fetchOrders = async () => {
  if (!userId.value) {
    console.warn('用户未登录')
    return
  }
  
  loading.value = true
  try {
    const response = await getOrderList({ userId: userId.value })
    if (response.code === '0' && response.data) {
      orderList.value = response.data
    } else {
      console.error('获取订单列表失败:', response.message)
    }
  } catch (error) {
    console.error('获取订单列表失败:', error)
  } finally {
    loading.value = false
  }
}

const formatDate = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const formatTime = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.orders-page {
  min-height: 100%;
}

.page-header {
  padding-bottom: 16px;
  border-bottom: 1px solid #f3f4f6;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.tabs-wrapper {
  margin-top: 16px;
}

.tabs-wrapper :deep(.ant-tabs-nav) {
  margin-bottom: 0;
}

.tabs-wrapper :deep(.ant-tabs-tab) {
  padding: 12px 20px;
  font-size: 14px;
}

.tabs-wrapper :deep(.ant-tabs-tab-active) {
  font-weight: 600;
}

.orders-list {
  margin-top: 20px;
}

.empty-state {
  padding: 80px 0;
  text-align: center;
  color: #9ca3af;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.order-items {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-card {
  border: 1px solid #f3f4f6;
  border-radius: 8px;
  overflow: hidden;
}

.order-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 16px;
  background-color: #f9fafb;
  border-bottom: 1px solid #f3f4f6;
  font-size: 13px;
  color: #6b7280;
}

.order-time {
  color: #374151;
}

.order-no {
  color: #6b7280;
}

.order-shop {
  color: #1f2937;
  font-weight: 500;
}

.order-body {
  padding: 16px;
}

.order-products {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.order-product {
  display: flex;
  align-items: center;
  gap: 12px;
}

.product-img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #f3f4f6;
}

.product-info {
  flex: 1;
}

.product-title {
  font-size: 14px;
  color: #374151;
  margin-bottom: 4px;
}

.product-spec {
  font-size: 12px;
  color: #9ca3af;
}

.product-price {
  text-align: right;
}

.product-price .price {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.product-price .quantity {
  display: block;
  font-size: 12px;
  color: #9ca3af;
}

.order-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 16px;
  padding: 12px 16px;
  border-top: 1px solid #f3f4f6;
}

.order-total .label {
  font-size: 14px;
  color: #6b7280;
}

.order-total .amount {
  font-size: 18px;
  font-weight: 600;
  color: #059669;
}
</style>
