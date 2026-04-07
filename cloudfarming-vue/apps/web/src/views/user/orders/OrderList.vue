<template>
  <div class="orders-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">我的订单</h2>
        <p class="page-subtitle">按淘宝式订单台账排版整理，快速查看商品、金额、状态与当前可执行操作。</p>
      </div>
      <div class="page-summary">
        <span class="summary-pill">当前 {{ tabLabelMap[activeTab] }}</span>
        <span class="summary-count">{{ displayTotal }} 笔</span>
      </div>
    </div>

    <div class="tabs-wrapper">
      <a-tabs v-model:activeKey="activeTab" @change="handleTabChange">
        <a-tab-pane key="all" tab="全部订单" />
        <a-tab-pane key="pending" tab="待支付" />
        <a-tab-pane key="pendingShip" tab="待发货" />
        <a-tab-pane key="shipped" tab="待收货" />
      </a-tabs>
    </div>

    <div v-if="currentOrders.length > 0" class="order-table-head">
      <div class="line-items-head">
        <span>商品信息</span>
        <span>单价</span>
        <span>数量</span>
      </div>
      <span>实付款</span>
      <span>订单状态</span>
      <span>操作</span>
    </div>

    <div class="orders-list">
      <a-spin :spinning="loading">
        <div v-if="currentOrders.length === 0" class="empty-state">
          <InboxOutlined class="empty-icon" />
          <h3 class="empty-title">暂无相关订单</h3>
          <p class="empty-desc">当前筛选条件下还没有订单记录，稍后再来看看。</p>
        </div>

        <div v-else class="order-items">
          <article
            v-for="order in currentOrders"
            :key="order.payOrderNo || order.orderNo || order.id"
            class="order-card"
          >
            <header class="order-meta">
              <div class="order-meta-main">
                <span class="meta-time">{{ formatDateTime(order.expireTime || order.createTime) }}</span>
                <span class="meta-divider"></span>
                <span class="meta-label">订单编号</span>
                <span class="meta-value">{{ order.orderNo || order.payOrderNo || '--' }}</span>
                <span class="meta-divider"></span>
                <span class="meta-label">店铺</span>
                <span class="meta-shop">{{ order.shopName || '--' }}</span>
              </div>
              <div class="order-meta-side">
                <span class="order-type-tag" :class="getOrderTypeClass(order)">
                  {{ getOrderTypeText(order) }}
                </span>
              </div>
            </header>

            <div class="order-content">
              <div class="line-items">
                <template v-if="resolveOrderItems(order).length">
                  <div
                    v-for="(item, index) in resolveOrderItems(order)"
                    :key="`${order.payOrderNo || order.orderNo || order.id}-${index}`"
                    class="line-item"
                  >
                    <div class="item-main">
                      <img :src="getItemImage(item.coverImage)" :alt="item.productName || '订单商品'" class="product-img" />
                      <div class="item-info">
                        <h3 class="product-title">{{ item.productName || '订单商品' }}</h3>
                        <div class="item-tags">
                          <span class="item-tag">{{ getOrderTypeText(order) }}</span>
                          <span v-if="order.shopName" class="item-shop-inline">{{ order.shopName }}</span>
                        </div>
                      </div>
                    </div>
                    <div class="item-price">
                      <span class="price-current">¥{{ formatMoney(item.price || item.totalPrice) }}</span>
                    </div>
                    <div class="item-quantity">
                      x{{ item.quantity || 1 }}
                    </div>
                  </div>
                </template>
                <div v-else class="line-item line-item--empty">
                  <div class="item-empty-text">暂无商品明细</div>
                </div>
              </div>

              <div class="summary-col">
                <p class="summary-main">¥{{ formatMoney(order.actualPayAmount || order.totalAmount || order.totalPrice) }}</p>
                <p class="summary-label">
                  {{ resolveOrderStatus(order) === ORDER_STATUS.PENDING_PAYMENT ? '待支付金额' : '实付款' }}
                </p>
                <p
                  v-if="order.totalAmount !== undefined && order.actualPayAmount !== undefined && Number(order.totalAmount) !== Number(order.actualPayAmount)"
                  class="summary-sub"
                >
                  订单金额 ¥{{ formatMoney(order.totalAmount) }}
                </p>
              </div>

              <div class="status-col">
                <span class="status-badge" :class="getOrderStatusClass(order)">
                  {{ getOrderStatusText(order) }}
                </span>
                <p class="status-desc">{{ getOrderStatusHint(order) }}</p>
              </div>

              <div class="action-col">
                <a-button
                  v-if="canConfirmReceive(order)"
                  type="primary"
                  class="action-button"
                  @click="handleConfirmReceive(order)"
                >
                  确认收货
                </a-button>
                <span v-else class="action-placeholder">{{ getActionPlaceholder(order) }}</span>
              </div>
            </div>
          </article>
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
import { computed, reactive, ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import { InboxOutlined } from '@ant-design/icons-vue'
import { useRoute, useRouter } from 'vue-router'
import {
  getOrderList,
  getPayOrderList,
  ORDER_STATUS,
  ORDER_STATUS_TEXT,
  ORDER_TYPE,
  receiveUserOrder
} from '@/api/order'
import { useUserStore } from '@/stores/useUserStore'

const PAGE_SIZE = 20
const DEFAULT_TAB = 'all'
const VALID_TAB_KEYS = ['all', 'pending', 'pendingShip', 'shipped']
const tabLabelMap = {
  all: '全部订单',
  pending: '待支付',
  pendingShip: '待发货',
  shipped: '待收货'
}

const loading = ref(false)
const activeTab = ref(DEFAULT_TAB)
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

const orderImageFallback = `data:image/svg+xml;charset=UTF-8,${encodeURIComponent(`
<svg xmlns="http://www.w3.org/2000/svg" width="120" height="120" viewBox="0 0 120 120">
  <defs>
    <linearGradient id="g" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="#eef8f0" />
      <stop offset="100%" stop-color="#d9efe0" />
    </linearGradient>
  </defs>
  <rect width="120" height="120" rx="16" fill="url(#g)" />
  <rect x="24" y="30" width="72" height="48" rx="10" fill="#ffffff" opacity="0.95" />
  <rect x="34" y="42" width="52" height="8" rx="4" fill="#2f8b49" opacity="0.7" />
  <rect x="34" y="58" width="38" height="6" rx="3" fill="#97c6a5" />
  <circle cx="40" cy="90" r="6" fill="#2f8b49" opacity="0.5" />
  <circle cx="80" cy="90" r="6" fill="#2f8b49" opacity="0.5" />
</svg>
`)}` 

const TAB_ORDER_STATUS = {
  pendingShip: ORDER_STATUS.PENDING_SHIPMENT,
  shipped: ORDER_STATUS.SHIPPED
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

const resolveOrderItems = (order) => {
  return Array.isArray(order?.items) ? order.items : []
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
  if (status === ORDER_STATUS.COMPLETED) {
    return 'status-completed'
  }
  return 'status-default'
}

const getOrderStatusHint = (order) => {
  const status = resolveOrderStatus(order)
  if (status === ORDER_STATUS.PENDING_PAYMENT) {
    return '订单已创建，等待完成支付'
  }
  if (status === ORDER_STATUS.PENDING_ASSIGNMENT) {
    return '农户正在分配认养对象'
  }
  if (status === ORDER_STATUS.PENDING_SHIPMENT) {
    return '商家正在准备发货'
  }
  if (status === ORDER_STATUS.SHIPPED) {
    return '商品已发出，请及时确认收货'
  }
  if (status === ORDER_STATUS.BREEDING) {
    return '认养对象养殖进行中'
  }
  if (status === ORDER_STATUS.COMPLETED) {
    return '订单流程已完成'
  }
  if (status === ORDER_STATUS.CANCEL) {
    return '订单已关闭'
  }
  return '请关注订单最新状态'
}

const getActionPlaceholder = (order) => {
  const status = resolveOrderStatus(order)
  if (status === ORDER_STATUS.PENDING_PAYMENT) {
    return '等待支付'
  }
  if (status === ORDER_STATUS.PENDING_ASSIGNMENT) {
    return '等待分配'
  }
  if (status === ORDER_STATUS.PENDING_SHIPMENT) {
    return '等待发货'
  }
  if (status === ORDER_STATUS.BREEDING) {
    return '养殖中'
  }
  if (status === ORDER_STATUS.COMPLETED) {
    return '已完成'
  }
  if (status === ORDER_STATUS.CANCEL) {
    return '已关闭'
  }
  return '暂无操作'
}

const getOrderTypeText = (order) => {
  if (order?.orderType === ORDER_TYPE.ADOPT) {
    return '认养订单'
  }
  if (order?.orderType === ORDER_TYPE.GOODS) {
    return '商品订单'
  }
  return '订单'
}

const getOrderTypeClass = (order) => {
  if (order?.orderType === ORDER_TYPE.ADOPT) {
    return 'order-type-tag--adopt'
  }
  if (order?.orderType === ORDER_TYPE.GOODS) {
    return 'order-type-tag--goods'
  }
  return 'order-type-tag--default'
}

const canConfirmReceive = (order) => {
  return resolveOrderStatus(order) === ORDER_STATUS.SHIPPED && Boolean(order?.orderNo)
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

const handleConfirmReceive = async (order) => {
  if (!order?.orderNo) {
    message.error('订单号不存在')
    return
  }

  try {
    const response = await receiveUserOrder({
      orderNo: order.orderNo
    })
    if (response.code !== '0') {
      message.error(response.message || '确认收货失败')
      return
    }
    message.success('确认收货成功')
    if (
      activeTab.value === 'shipped' &&
      currentOrders.value.length === 1 &&
      pagination.current > 1
    ) {
      pagination.current -= 1
    }
    fetchOrders()
  } catch (error) {
    console.error('handleConfirmReceive error:', error)
    message.error('确认收货失败，请稍后重试')
  }
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

const formatMoney = (value) => {
  const amount = Number(value)
  if (Number.isNaN(amount)) {
    return '0.00'
  }
  return amount.toFixed(2)
}

const formatDateTime = (timestamp) => {
  if (!timestamp) {
    return ''
  }
  const date = new Date(timestamp)
  if (Number.isNaN(date.getTime())) {
    return String(timestamp)
  }
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

const getItemImage = (image) => {
  return image || orderImageFallback
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
  padding: 4px 0 8px;
}

.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24px;
  padding: 4px 0 18px;
}

.page-title {
  margin: 0;
  font-size: 24px;
  line-height: 1.2;
  font-weight: 700;
  color: #1f2937;
}

.page-subtitle {
  margin: 8px 0 0;
  font-size: 14px;
  color: #6b7280;
}

.page-summary {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border-radius: 16px;
  background: linear-gradient(135deg, #f3fbf5 0%, #eef8f0 100%);
  border: 1px solid #dceedd;
}

.summary-pill {
  font-size: 12px;
  font-weight: 600;
  color: #2f6f42;
}

.summary-count {
  font-size: 20px;
  font-weight: 700;
  color: #1f7a3f;
}

.tabs-wrapper {
  padding: 0 0 8px;
}

.tabs-wrapper :deep(.ant-tabs-nav) {
  margin-bottom: 0;
}

.tabs-wrapper :deep(.ant-tabs-nav::before) {
  border-bottom-color: #e5efe7;
}

.tabs-wrapper :deep(.ant-tabs-tab) {
  padding: 14px 18px;
  margin-right: 8px;
  color: #5f6d64;
  font-size: 14px;
  font-weight: 600;
  transition: color 0.2s ease;
}

.tabs-wrapper :deep(.ant-tabs-tab:hover) {
  color: #2f8b49;
}

.tabs-wrapper :deep(.ant-tabs-tab-active .ant-tabs-tab-btn) {
  color: #1f7a3f;
}

.tabs-wrapper :deep(.ant-tabs-ink-bar) {
  height: 3px;
  border-radius: 999px;
  background: linear-gradient(90deg, #1f7a3f 0%, #49a166 100%);
}

.order-table-head {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 156px 150px 132px;
  gap: 0;
  margin-top: 12px;
  padding: 0 18px;
  height: 48px;
  align-items: center;
  border-radius: 14px;
  background: #f8fbf8;
  border: 1px solid #e3eee5;
  color: #5f6d64;
  font-size: 13px;
  font-weight: 600;
}

.line-items-head {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 120px 88px;
  gap: 0;
}

.line-items-head span:nth-child(2),
.line-items-head span:nth-child(3),
.order-table-head > span {
  text-align: center;
}

.orders-list {
  margin-top: 16px;
}

.empty-state {
  padding: 90px 24px;
  text-align: center;
  border: 1px dashed #d9e7dc;
  border-radius: 20px;
  background: linear-gradient(180deg, #fcfefd 0%, #f7fbf8 100%);
}

.empty-icon {
  font-size: 56px;
  color: #a7b6ac;
}

.empty-title {
  margin: 18px 0 8px;
  font-size: 20px;
  color: #1f2937;
}

.empty-desc {
  margin: 0;
  color: #8b949e;
  font-size: 14px;
}

.order-items {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.order-card {
  border: 1px solid #deebdf;
  border-radius: 20px;
  overflow: hidden;
  background: #ffffff;
  box-shadow: 0 10px 30px rgba(31, 122, 63, 0.06);
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.order-card:hover {
  transform: translateY(-2px);
  border-color: #cfe4d3;
  box-shadow: 0 16px 36px rgba(31, 122, 63, 0.1);
}

.order-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 18px;
  background: linear-gradient(180deg, #f9fcfa 0%, #f3f8f4 100%);
  border-bottom: 1px solid #e4eee6;
}

.order-meta-main {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  min-width: 0;
  color: #52625a;
  font-size: 13px;
}

.meta-time,
.meta-value,
.meta-shop {
  color: #1f2937;
}

.meta-time {
  font-weight: 600;
}

.meta-label {
  color: #6b7280;
}

.meta-divider {
  width: 1px;
  height: 12px;
  background: #d6e2d8;
}

.order-meta-side {
  display: flex;
  align-items: center;
  gap: 8px;
}

.order-type-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 76px;
  height: 28px;
  padding: 0 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.order-type-tag--goods {
  background: #edf9f0;
  color: #1f7a3f;
}

.order-type-tag--adopt {
  background: #f6f3ff;
  color: #6d4fe0;
}

.order-type-tag--default {
  background: #f3f4f6;
  color: #6b7280;
}

.order-content {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 156px 150px 132px;
  gap: 0;
}

.line-items {
  min-width: 0;
}

.line-item {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 120px 88px;
  align-items: center;
  gap: 0;
  min-height: 112px;
  padding: 0 18px;
  border-bottom: 1px solid #edf3ee;
}

.line-item:last-child {
  border-bottom: none;
}

.line-item--empty {
  display: flex;
  align-items: center;
  min-height: 100px;
}

.item-empty-text {
  color: #9ca3af;
  font-size: 14px;
}

.item-main {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
  padding: 18px 0;
}

.product-img {
  width: 84px;
  height: 84px;
  object-fit: cover;
  border-radius: 14px;
  border: 1px solid #e7efe8;
  background: #f4f8f5;
  flex-shrink: 0;
}

.item-info {
  min-width: 0;
}

.product-title {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  line-height: 1.5;
  color: #1f2937;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.item-tags {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
}

.item-tag,
.item-shop-inline {
  display: inline-flex;
  align-items: center;
  height: 24px;
  padding: 0 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.item-tag {
  background: #eef8f0;
  color: #1f7a3f;
}

.item-shop-inline {
  background: #f7f7f8;
  color: #6b7280;
}

.item-price,
.item-quantity {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100%;
  color: #1f2937;
}

.price-current {
  font-size: 16px;
  font-weight: 700;
}

.item-quantity {
  font-size: 15px;
  color: #52625a;
  font-weight: 600;
}

.summary-col,
.status-col,
.action-col {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 20px 16px;
  border-left: 1px solid #edf3ee;
  text-align: center;
}

.summary-main {
  margin: 0;
  font-size: 22px;
  line-height: 1.1;
  font-weight: 700;
  color: #156b33;
}

.summary-label {
  margin: 0;
  font-size: 12px;
  color: #6b7280;
}

.summary-sub {
  margin: 0;
  font-size: 12px;
  color: #9ca3af;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 86px;
  min-height: 32px;
  padding: 0 12px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 700;
}

.status-desc {
  margin: 0;
  max-width: 120px;
  font-size: 12px;
  line-height: 1.5;
  color: #7b8794;
}

.status-pending {
  background: #fff4e8;
  color: #ea580c;
}

.status-shipping {
  background: #eefaf1;
  color: #1f7a3f;
}

.status-assignment {
  background: #fffbeb;
  color: #c98512;
}

.status-shipped {
  background: #ecfeff;
  color: #0891b2;
}

.status-breeding {
  background: #effaf1;
  color: #4d7c0f;
}

.status-closed {
  background: #f3f4f6;
  color: #6b7280;
}

.status-completed {
  background: #e8f8ee;
  color: #0f9f57;
}

.status-default {
  background: #f4f4f5;
  color: #52525b;
}

.action-button {
  min-width: 96px;
  height: 36px;
  border-radius: 999px;
  font-weight: 700;
  background: linear-gradient(135deg, #1f7a3f 0%, #3da85f 100%);
  border: none;
  box-shadow: 0 8px 18px rgba(31, 122, 63, 0.18);
}

.action-button:hover,
.action-button:focus {
  background: linear-gradient(135deg, #176633 0%, #338d52 100%);
}

.action-placeholder {
  font-size: 13px;
  color: #98a2b3;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 28px;
}

@media (max-width: 1280px) {
  .order-table-head,
  .order-content {
    grid-template-columns: minmax(0, 1fr) 140px 132px 120px;
  }

  .line-items-head,
  .line-item {
    grid-template-columns: minmax(0, 1fr) 104px 72px;
  }
}

@media (max-width: 960px) {
  .page-header {
    flex-direction: column;
    align-items: stretch;
  }

  .page-summary {
    justify-content: space-between;
  }

  .order-table-head {
    display: none;
  }

  .order-content {
    grid-template-columns: 1fr;
  }

  .summary-col,
  .status-col,
  .action-col {
    border-left: none;
    border-top: 1px solid #edf3ee;
  }
}

@media (max-width: 640px) {
  .page-title {
    font-size: 22px;
  }

  .page-subtitle {
    font-size: 13px;
  }

  .tabs-wrapper :deep(.ant-tabs-tab) {
    padding: 12px 10px;
    margin-right: 4px;
    font-size: 13px;
  }

  .order-meta {
    flex-direction: column;
    align-items: flex-start;
  }

  .line-item {
    grid-template-columns: 1fr;
    gap: 12px;
    padding-top: 16px;
    padding-bottom: 16px;
  }

  .item-price,
  .item-quantity {
    justify-content: flex-start;
    padding-left: 98px;
  }

  .summary-col,
  .status-col,
  .action-col {
    padding: 16px;
  }

  .pagination-wrapper {
    justify-content: center;
  }
}
</style>
