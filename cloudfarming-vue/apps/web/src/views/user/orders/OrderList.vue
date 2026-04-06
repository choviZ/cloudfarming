<template>
  <div class="orders-page">
    <div class="page-header">
      <h2 class="page-title">我的订单</h2>
    </div>

    <div class="tabs-wrapper">
      <a-tabs v-model:activeKey="activeTab" @change="handleTabChange">
        <a-tab-pane key="all" tab="全部订单" />
        <a-tab-pane key="pending" tab="待支付" />
        <a-tab-pane key="pendingShip" tab="待发货" />
        <a-tab-pane key="shipped" tab="待收货" />
      </a-tabs>
    </div>

    <div class="orders-list">
      <a-spin :spinning="loading">
        <div v-if="currentOrders.length === 0" class="empty-state">
          <InboxOutlined class="empty-icon" />
          <p>暂无相关订单</p>
        </div>
        <div v-else class="order-items">
          <div v-for="order in currentOrders" :key="order.payOrderNo || order.id" class="order-card">
            <div class="order-header">
              <span class="order-time">{{ formatDate(order.expireTime || order.createTime) }}</span>
              <span class="order-no">支付单号：{{ order.payOrderNo || order.orderNo }}</span>
              <span class="order-shop">{{ order.shopName || '--' }}</span>
              <span class="order-status" :class="getOrderStatusClass(order)">{{ getOrderStatusText(order) }}</span>
            </div>
            <div class="order-body">
              <div class="order-products">
                <div v-for="item in order.items || []" :key="`${order.payOrderNo || order.id}-${item.productName}`" class="order-product">
                  <img :src="item.coverImage" :alt="item.productName" class="product-img" />
                  <div class="product-info">
                    <div class="product-title">{{ item.productName }}</div>
                  </div>
                  <div class="product-price">
                    <span class="price">￥{{ item.price }}</span>
                    <span class="quantity">x{{ item.quantity }}</span>
                  </div>
                </div>
              </div>
            </div>
            <div class="order-footer">
              <div class="order-total">
                <span class="label">合计：</span>
                <span class="amount">￥{{ order.totalAmount || order.totalPrice }}</span>
              </div>
            </div>
          </div>
        </div>

        <div v-if="currentOrders.length > 0" class="pagination-wrapper">
          <a-pagination
            v-model:current="pagination.current"
            :total="displayTotal"
            :page-size="pagination.pageSize"
            :show-size-changer="false"
            :show-total="(total) => `共 ${total} 条订单`"
            @change="handlePageChange"
          />
        </div>
      </a-spin>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { message } from 'ant-design-vue'
import { InboxOutlined } from '@ant-design/icons-vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrderList, getPayOrderList, ORDER_STATUS, ORDER_STATUS_TEXT } from '@/api/order'
import { useUserStore } from '@/stores/useUserStore'

const PAGE_SIZE = 20
const DEFAULT_TAB = 'all'
const VALID_TAB_KEYS = ['all', 'pending', 'pendingShip', 'shipped']

const loading = ref(false)
const activeTab = ref('all')
const orderList = ref({
  records: [],
  total: 0,
  current: 1,
  size: PAGE_SIZE
})
const pagination = reactive({
  current: 1,
  pageSize: PAGE_SIZE,
  total: 0
})
const userStore = useUserStore()
const route = useRoute()
const router = useRouter()

const TAB_ORDER_STATUS = {
  pendingShip: ORDER_STATUS.PENDING_SHIPMENT,
  shipped: ORDER_STATUS.SHIPPED
}

const normalizeTabKey = (tabKey) => {
  const resolvedTabKey = Array.isArray(tabKey) ? tabKey[0] : tabKey
  return VALID_TAB_KEYS.includes(resolvedTabKey) ? resolvedTabKey : DEFAULT_TAB
}

const syncTabToRoute = (tabKey) => {
  const normalizedTab = normalizeTabKey(tabKey)
  const nextQuery = { ...route.query }

  if (normalizedTab === DEFAULT_TAB) {
    delete nextQuery.tab
  } else {
    nextQuery.tab = normalizedTab
  }

  const currentRouteTab = Array.isArray(route.query.tab) ? route.query.tab[0] : route.query.tab
  const nextRouteTab = nextQuery.tab
  if ((currentRouteTab || undefined) === (nextRouteTab || undefined)) {
    return
  }

  router.replace({
    path: route.path,
    query: nextQuery
  })
}

const userId = computed(() => userStore.loginUser?.id)

const currentOrders = computed(() => {
  if (Array.isArray(orderList.value)) {
    return orderList.value
  }
  if (Array.isArray(orderList.value?.records)) {
    return orderList.value.records
  }
  return []
})

const displayTotal = computed(() => {
  return Math.max(Number(pagination.total) || 0, currentOrders.value.length)
})

const normalizePageData = (pageData) => {
  if (Array.isArray(pageData)) {
    return {
      records: pageData,
      total: pageData.length,
      current: 1,
      size: PAGE_SIZE
    }
  }

  return {
    records: Array.isArray(pageData?.records) ? pageData.records : [],
    total: Number(pageData?.total) || 0,
    current: Number(pageData?.current) || pagination.current,
    size: Number(pageData?.size) || PAGE_SIZE
  }
}

const resetOrderList = () => {
  orderList.value = {
    records: [],
    total: 0,
    current: pagination.current,
    size: PAGE_SIZE
  }
  pagination.total = 0
}

const applyPageData = (pageData) => {
  const normalizedPage = normalizePageData(pageData)
  orderList.value = normalizedPage
  pagination.current = normalizedPage.current
  pagination.total = normalizedPage.total
}

const resolveOrderStatus = (order) => {
  if (order?.orderStatus !== undefined && order?.orderStatus !== null) {
    return order.orderStatus
  }
  if (activeTab.value === 'pending') {
    return ORDER_STATUS.PENDING_PAYMENT
  }
  return undefined
}

const getOrderStatusText = (order) => {
  const status = resolveOrderStatus(order)
  return status !== undefined ? (ORDER_STATUS_TEXT[status] || '未知状态') : '未知状态'
}

const getOrderStatusClass = (order) => {
  const status = resolveOrderStatus(order)
  if (status === ORDER_STATUS.PENDING_PAYMENT) {
    return 'status-pending'
  }
  if (status === ORDER_STATUS.PENDING_ASSIGNMENT) {
    return 'status-assignment'
  }
  if (status === ORDER_STATUS.PENDING_SHIPMENT) {
    return 'status-shipping'
  }
  if (status === ORDER_STATUS.SHIPPED) {
    return 'status-shipped'
  }
  if (status === ORDER_STATUS.BREEDING) {
    return 'status-breeding'
  }
  if (status === ORDER_STATUS.CANCEL) {
    return 'status-closed'
  }
  return 'status-default'
}

const handleTabChange = (key) => {
  activeTab.value = normalizeTabKey(key)
  pagination.current = 1
  syncTabToRoute(activeTab.value)
}

const handlePageChange = (page) => {
  pagination.current = page
  fetchOrders()
}

const fetchOrders = async () => {
  if (!userId.value) {
    resetOrderList()
    return
  }

  loading.value = true
  resetOrderList()

  try {
    if (activeTab.value === 'pending') {
      const response = await getPayOrderList({
        buyerId: userId.value,
        bizStatus: 0,
        payStatus: ORDER_STATUS.PENDING_PAYMENT,
        current: pagination.current,
        size: PAGE_SIZE
      })

      if (response.code === '0' && response.data) {
        applyPageData(response.data)
      } else {
        message.error(response.message || '获取待支付订单列表失败')
      }
      return
    }

    const payload = {
      userId: userId.value,
      current: pagination.current,
      size: PAGE_SIZE
    }
    const orderStatus = TAB_ORDER_STATUS[activeTab.value]
    if (orderStatus !== undefined) {
      payload.orderStatus = orderStatus
    }

    const response = await getOrderList(payload)
    if (response.code === '0' && response.data) {
      applyPageData(response.data)
    } else {
      message.error(response.message || '获取订单列表失败')
    }
  } catch (error) {
    console.error('fetchOrders error:', error)
    message.error('获取订单列表失败，请稍后重试')
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

watch(
  () => route.query.tab,
  (tabKey) => {
    const normalizedTab = normalizeTabKey(tabKey)
    const previousTab = activeTab.value
    activeTab.value = normalizedTab
    if (previousTab !== normalizedTab) {
      pagination.current = 1
    }
    syncTabToRoute(normalizedTab)
    if (userId.value) {
      fetchOrders()
    }
  },
  { immediate: true }
)

watch(
  () => userId.value,
  (currentUserId, previousUserId) => {
    if (!currentUserId) {
      resetOrderList()
      return
    }
    if (currentUserId !== previousUserId) {
      pagination.current = 1
    }
    fetchOrders()
  }
)
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

.order-status {
  margin-left: auto;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.status-pending {
  background: #fff7ed;
  color: #ea580c;
}

.status-shipping {
  background: #eff6ff;
  color: #2563eb;
}

.status-assignment {
  background: #fffbeb;
  color: #d97706;
}

.status-shipped {
  background: #ecfeff;
  color: #0891b2;
}

.status-breeding {
  background: #f2fce7;
  color: #4d7c0f;
}

.status-closed {
  background: #f3f4f6;
  color: #6b7280;
}

.status-default {
  background: #f4f4f5;
  color: #52525b;
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

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 24px;
}
</style>
