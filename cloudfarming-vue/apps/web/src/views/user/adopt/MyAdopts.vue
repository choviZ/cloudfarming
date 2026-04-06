<template>
  <div class="my-adopts-page">
    <section class="page-header">
      <div>
        <h2 class="page-title">我的认养</h2>
        <p class="page-desc">从待分配到养殖完成，统一查看认养进度、实例状态和成长记录。</p>
      </div>
    </section>

    <section class="toolbar-card">
      <div class="summary-grid">
        <div class="summary-item">
          <span class="summary-label">当前视图</span>
          <strong class="summary-value">{{ currentTabMeta.label }}</strong>
          <p class="summary-hint">{{ currentTabMeta.description }}</p>
        </div>
        <div class="summary-item">
          <span class="summary-label">总记录数</span>
          <strong class="summary-value">{{ pagination.total }}</strong>
          <p class="summary-hint">展示当前视图下的认养记录总量。</p>
        </div>
        <div class="summary-item">
          <span class="summary-label">当前页条目</span>
          <strong class="summary-value">{{ currentCards.length }}</strong>
          <p class="summary-hint">已为你整理待分配订单和已分配实例。</p>
        </div>
      </div>

      <a-tabs v-model:activeKey="activeTab" class="status-tabs" @change="handleTabChange">
        <a-tab-pane v-for="option in TAB_OPTIONS" :key="option.key" :tab="option.label" />
      </a-tabs>
    </section>

    <section class="list-card">
      <a-spin :spinning="loading">
        <div v-if="currentCards.length" class="card-grid">
          <article
            v-for="card in currentCards"
            :key="resolveCardKey(card)"
            class="adopt-card"
            :class="{ 'pending-card': isPendingTab }"
          >
            <div class="card-media">
              <img :src="resolveCardImage(card)" :alt="resolveCardTitle(card)" class="card-image" />
            </div>

            <div class="card-body">
              <div class="card-head">
                <div class="card-head-main">
                  <p class="card-caption">{{ resolveCardCaption(card) }}</p>
                  <h3 class="card-title">{{ resolveCardTitle(card) }}</h3>
                </div>
                <a-tag :color="resolveCardTagColor(card)">
                  {{ resolveCardTagText(card) }}
                </a-tag>
              </div>

              <div class="meta-grid">
                <div class="meta-item">
                  <span class="meta-label">{{ isPendingTab ? '订单号' : '耳标号' }}</span>
                  <strong class="meta-value">{{ resolvePrimaryMeta(card) }}</strong>
                </div>
                <div class="meta-item">
                  <span class="meta-label">{{ isPendingTab ? '下单时间' : '最新日记' }}</span>
                  <strong class="meta-value">{{ resolveSecondaryMeta(card) }}</strong>
                </div>
                <div class="meta-item">
                  <span class="meta-label">{{ isPendingTab ? '支付金额' : '关联订单号' }}</span>
                  <strong class="meta-value">{{ resolveAmountMeta(card) }}</strong>
                </div>
                <div class="meta-item">
                  <span class="meta-label">{{ isPendingTab ? '店铺名称' : '创建时间' }}</span>
                  <strong class="meta-value">{{ resolveExtraMeta(card) }}</strong>
                </div>
              </div>

              <div class="card-footer">
                <p class="card-note">{{ resolveCardNote(card) }}</p>
                <a-button v-if="!isPendingTab" type="primary" @click="handleViewDetail(card.id)">
                  查看详情
                </a-button>
              </div>
            </div>
          </article>
        </div>

        <a-empty v-else :description="currentTabMeta.emptyText" />
      </a-spin>

      <div class="pagination-wrap" v-if="pagination.total > 0">
        <a-pagination
          v-model:current="pagination.current"
          :total="pagination.total"
          :page-size="pagination.pageSize"
          :show-size-changer="false"
          :show-total="(total) => `共 ${total} 条记录`"
          @change="handlePageChange"
        />
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import dayjs from 'dayjs'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { pageMyAdoptInstances } from '@/api/adopt'
import { getOrderList, ORDER_STATUS, ORDER_TYPE } from '@/api/order'
import { getAdoptInstanceStatusOption, ADOPT_INSTANCE_STATUS } from '@/constants/adopt'
import { useUserStore } from '@/stores/useUserStore'

const PAGE_SIZE = 8
const DEFAULT_TAB = 'pending'
const VALID_TAB_KEYS = ['pending', 'breeding', 'completed', 'abnormal']

const TAB_OPTIONS = [
  {
    key: 'pending',
    label: '待分配',
    description: '已支付但还在等待农户分配认养对象。',
    emptyText: '暂无待分配的认养订单'
  },
  {
    key: 'breeding',
    label: '养殖中',
    description: '已完成分配，可以查看最新日记、图片和体重变化。',
    emptyText: '暂无养殖中的认养实例'
  },
  {
    key: 'completed',
    label: '已完成',
    description: '已履约完成的认养实例会沉淀在这里。',
    emptyText: '暂无已完成的认养实例'
  },
  {
    key: 'abnormal',
    label: '异常结束',
    description: '认养过程中发生异常结束的实例会归档在这里。',
    emptyText: '暂无异常结束的认养实例'
  }
]

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const activeTab = ref(DEFAULT_TAB)
const pendingOrders = ref([])
const instanceRecords = ref([])
const pagination = reactive({
  current: 1,
  pageSize: PAGE_SIZE,
  total: 0
})

const fallbackCover = `data:image/svg+xml;charset=UTF-8,${encodeURIComponent(`
<svg xmlns="http://www.w3.org/2000/svg" width="400" height="280" viewBox="0 0 400 280">
  <rect width="400" height="280" rx="32" fill="#f2f6f3"/>
  <path d="M94 182c24-41 54-63 87-63 34 0 63 22 87 63" fill="none" stroke="#8aa093" stroke-width="14" stroke-linecap="round"/>
  <circle cx="155" cy="104" r="26" fill="#bfd0c6"/>
  <circle cx="245" cy="104" r="26" fill="#d6e1db"/>
</svg>
`)}`

const userId = computed(() => userStore.loginUser?.id)
const isPendingTab = computed(() => activeTab.value === 'pending')
const currentCards = computed(() => (isPendingTab.value ? pendingOrders.value : instanceRecords.value))
const currentTabMeta = computed(() => {
  return TAB_OPTIONS.find((item) => item.key === activeTab.value) || TAB_OPTIONS[0]
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
  if ((currentRouteTab || undefined) === (nextQuery.tab || undefined)) {
    return
  }

  router.replace({
    path: route.path,
    query: nextQuery
  })
}

const isSuccessCode = (code) => ['0', '200'].includes(String(code))

const resetLists = () => {
  pendingOrders.value = []
  instanceRecords.value = []
  pagination.total = 0
}

const applyPageData = (pageData, target) => {
  const normalized = {
    records: Array.isArray(pageData?.records) ? pageData.records : [],
    total: Number(pageData?.total) || 0,
    current: Number(pageData?.current) || pagination.current
  }
  target.value = normalized.records
  pagination.total = normalized.total
  pagination.current = normalized.current
}

const fetchList = async () => {
  if (!userId.value) {
    resetLists()
    return
  }

  loading.value = true
  try {
    if (isPendingTab.value) {
      const response = await getOrderList({
        userId: userId.value,
        current: pagination.current,
        size: pagination.pageSize,
        orderStatus: ORDER_STATUS.PENDING_ASSIGNMENT,
        orderType: ORDER_TYPE.ADOPT
      })
      if (!isSuccessCode(response.code)) {
        message.error(response.message || '获取待分配认养订单失败')
        return
      }
      applyPageData(response.data || {}, pendingOrders)
      instanceRecords.value = []
      return
    }

    const status = activeTab.value === 'completed'
      ? ADOPT_INSTANCE_STATUS.FULFILLED
      : activeTab.value === 'abnormal'
        ? ADOPT_INSTANCE_STATUS.DEAD
        : ADOPT_INSTANCE_STATUS.ADOPTED
    const response = await pageMyAdoptInstances({
      current: pagination.current,
      size: pagination.pageSize,
      status
    })
    if (!isSuccessCode(response.code)) {
      message.error(response.message || '获取认养实例失败')
      return
    }
    applyPageData(response.data || {}, instanceRecords)
    pendingOrders.value = []
  } catch (error) {
    console.error('获取我的认养列表失败', error)
    message.error('获取我的认养列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleTabChange = (key) => {
  activeTab.value = normalizeTabKey(key)
  pagination.current = 1
  syncTabToRoute(activeTab.value)
}

const handlePageChange = (page) => {
  pagination.current = page
  fetchList()
}

const handleViewDetail = (instanceId) => {
  if (!instanceId) {
    return
  }
  router.push({
    path: `/usercenter/adopts/${instanceId}`,
    query: activeTab.value === DEFAULT_TAB ? {} : { tab: activeTab.value }
  })
}

const formatDate = (value, fallback = '--') => {
  return value ? dayjs(value).format('YYYY-MM-DD HH:mm') : fallback
}

const formatMoney = (value) => {
  const amount = Number(value || 0)
  return Number.isFinite(amount) ? amount.toFixed(2) : '0.00'
}

const resolveOrderItem = (order) => {
  return Array.isArray(order?.items) && order.items.length ? order.items[0] : {}
}

const resolveCardKey = (card) => {
  return isPendingTab.value ? card.orderNo || card.id : card.id
}

const resolveCardImage = (card) => {
  if (isPendingTab.value) {
    return resolveOrderItem(card).coverImage || fallbackCover
  }
  return card.image || card.itemCoverImage || fallbackCover
}

const resolveCardTitle = (card) => {
  if (isPendingTab.value) {
    return resolveOrderItem(card).productName || '认养项目'
  }
  return card.itemTitle || '认养项目'
}

const resolveCardCaption = () => {
  return isPendingTab.value ? '待分配认养订单' : currentTabMeta.value.label
}

const resolveCardTagText = (card) => {
  if (isPendingTab.value) {
    return '农户分配中'
  }
  if (activeTab.value === 'breeding') {
    return '养殖中'
  }
  if (activeTab.value === 'abnormal') {
    return card.statusDesc || '异常结束'
  }
  return card.statusDesc || '已完成'
}

const resolveCardTagColor = (card) => {
  if (isPendingTab.value) {
    return 'gold'
  }
  return getAdoptInstanceStatusOption(card.status)?.color || 'default'
}

const resolvePrimaryMeta = (card) => {
  return isPendingTab.value ? (card.orderNo || '--') : (card.earTagNo || '--')
}

const resolveSecondaryMeta = (card) => {
  return isPendingTab.value
    ? formatDate(card.createTime)
    : formatDate(card.latestLogTime, '暂无日记')
}

const resolveAmountMeta = (card) => {
  if (isPendingTab.value) {
    return `￥${formatMoney(card.actualPayAmount || card.totalAmount || card.totalPrice)}`
  }
  return card.orderNo || '--'
}

const resolveExtraMeta = (card) => {
  return isPendingTab.value
    ? (card.shopName || '--')
    : formatDate(card.createTime)
}

const resolveCardNote = (card) => {
  if (isPendingTab.value) {
    const quantity = resolveOrderItem(card).quantity
    return `农户正在为你分配唯一认养对象${quantity ? `，当前订单数量 ${quantity}` : ''}，分配完成后会自动进入“养殖中”。`
  }
  return activeTab.value === 'completed'
    ? '该认养实例已完成履约，可继续回看历史日记与体重变化。'
    : activeTab.value === 'abnormal'
      ? `该认养实例已异常结束${card.deathReason ? `，原因：${card.deathReason}` : ''}${card.deathTime ? `，处理时间：${formatDate(card.deathTime)}` : ''}。`
      : `最新成长记录更新时间：${formatDate(card.latestLogTime, '暂无日记')}。`
}

watch(
  [() => route.query.tab, () => userId.value],
  (newValues, oldValues = []) => {
    const [tabKey, currentUserId] = newValues
    const previousUserId = oldValues[1]
    const normalizedTab = normalizeTabKey(tabKey)
    const tabChanged = activeTab.value !== normalizedTab
    activeTab.value = normalizedTab
    syncTabToRoute(normalizedTab)

    if (!currentUserId) {
      resetLists()
      return
    }

    if (tabChanged || currentUserId !== previousUserId) {
      pagination.current = 1
    }
    fetchList()
  },
  { immediate: true }
)
</script>

<style scoped>
.my-adopts-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 100%;
}

.page-header,
.toolbar-card,
.list-card {
  background: #fff;
  border: 1px solid #edf2ee;
  border-radius: 18px;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.04);
}

.page-header,
.toolbar-card,
.list-card {
  padding: 22px 24px;
}

.page-title {
  margin: 0 0 8px;
  color: #17311f;
  font-size: 26px;
  font-weight: 700;
}

.page-desc {
  margin: 0;
  color: #6d7c73;
  font-size: 14px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  margin-bottom: 18px;
}

.summary-item {
  padding: 18px 20px;
  background: #f8fbf9;
  border: 1px solid #e7efe9;
  border-radius: 16px;
}

.summary-label {
  display: block;
  margin-bottom: 8px;
  color: #7b8b7f;
  font-size: 13px;
}

.summary-value {
  display: block;
  margin-bottom: 10px;
  color: #17311f;
  font-size: 24px;
  font-weight: 700;
}

.summary-hint {
  margin: 0;
  color: #6d7c73;
  font-size: 13px;
  line-height: 1.6;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.adopt-card {
  display: grid;
  grid-template-columns: 188px minmax(0, 1fr);
  gap: 18px;
  padding: 18px;
  border: 1px solid #e7efe9;
  border-radius: 20px;
  background: linear-gradient(135deg, #ffffff 0%, #fbfcfb 100%);
}

.card-media {
  min-height: 180px;
}

.card-image {
  width: 100%;
  height: 100%;
  min-height: 180px;
  object-fit: cover;
  border-radius: 18px;
  background: #f3f6f4;
}

.card-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-width: 0;
}

.card-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.card-head-main {
  min-width: 0;
}

.card-caption {
  margin: 0 0 8px;
  color: #7b8b7f;
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.card-title {
  margin: 0;
  color: #17311f;
  font-size: 22px;
  font-weight: 700;
  line-height: 1.35;
  overflow-wrap: anywhere;
}

.meta-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.meta-item {
  padding: 12px 14px;
  background: #f8fbf9;
  border: 1px solid #e7efe9;
  border-radius: 14px;
}

.meta-label {
  display: block;
  margin-bottom: 6px;
  color: #7b8b7f;
  font-size: 12px;
}

.meta-value {
  display: block;
  color: #17311f;
  font-size: 15px;
  font-weight: 600;
  overflow-wrap: anywhere;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 12px;
  margin-top: auto;
}

.card-note {
  margin: 0;
  color: #6d7c73;
  font-size: 13px;
  line-height: 1.7;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 18px;
}

@media (max-width: 1200px) {
  .summary-grid,
  .card-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 900px) {
  .adopt-card {
    grid-template-columns: 1fr;
  }

  .card-media {
    min-height: 220px;
  }
}

@media (max-width: 768px) {
  .page-header,
  .toolbar-card,
  .list-card {
    padding: 18px;
  }

  .meta-grid {
    grid-template-columns: 1fr;
  }

  .card-footer {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
