<template>
  <a-modal
    v-model:open="visible"
    title="订单详情"
    width="860px"
    :footer="null"
    :destroy-on-close="true"
    @cancel="handleClose"
  >
    <a-spin :spinning="loading" tip="加载中...">
      <template v-if="orderType === 0 && orderData.length">
        <div v-for="(item, index) in orderData" :key="`${item.orderNo || 'adopt'}-${index}`">
          <a-divider v-if="index > 0" style="margin: 24px 0" />
          <a-descriptions :column="2" bordered :title="`认养明细 #${index + 1}`">
            <a-descriptions-item label="订单号" :span="2">
              {{ item.orderNo }}
            </a-descriptions-item>
            <a-descriptions-item label="项目名称" :span="2">
              {{ item.itemName }}
            </a-descriptions-item>
            <a-descriptions-item label="项目图片" :span="2">
              <a-image
                :src="item.itemImage"
                :alt="item.itemName"
                style="width: 120px; height: 120px; object-fit: cover"
              />
            </a-descriptions-item>
            <a-descriptions-item label="单价">
              ￥{{ formatAmount(item.price) }}
            </a-descriptions-item>
            <a-descriptions-item label="数量">
              {{ item.quantity }}
            </a-descriptions-item>
            <a-descriptions-item label="合计金额" :span="2">
              <span class="amount-highlight">￥{{ formatAmount(item.totalAmount) }}</span>
            </a-descriptions-item>
            <a-descriptions-item label="开始日期">
              {{ formatDate(item.startDate) }}
            </a-descriptions-item>
            <a-descriptions-item label="结束日期">
              {{ formatDate(item.endDate) }}
            </a-descriptions-item>
            <a-descriptions-item label="创建时间" :span="2">
              {{ formatTime(item.createTime) }}
            </a-descriptions-item>
          </a-descriptions>
        </div>
      </template>

      <template v-if="orderType === 1 && orderData.length">
        <div v-for="(item, index) in orderData" :key="`${item.orderNo || 'sku'}-${index}`">
          <a-divider v-if="index > 0" style="margin: 24px 0" />
          <a-descriptions :column="2" bordered :title="`商品明细 #${index + 1}`">
            <a-descriptions-item label="订单号" :span="2">
              {{ item.orderNo }}
            </a-descriptions-item>
            <a-descriptions-item label="商品名称" :span="2">
              {{ item.skuName }}
            </a-descriptions-item>
            <a-descriptions-item label="商品图片" :span="2">
              <a-image
                :src="item.skuImage"
                :alt="item.skuName"
                style="width: 120px; height: 120px; object-fit: cover"
              />
            </a-descriptions-item>
            <a-descriptions-item label="商品规格" :span="2">
              {{ item.skuSpecs || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="单价">
              ￥{{ formatAmount(item.price) }}
            </a-descriptions-item>
            <a-descriptions-item label="数量">
              {{ item.quantity }}
            </a-descriptions-item>
            <a-descriptions-item label="合计金额" :span="2">
              <span class="amount-highlight">￥{{ formatAmount(item.totalAmount) }}</span>
            </a-descriptions-item>
            <a-descriptions-item label="创建时间" :span="2">
              {{ formatTime(item.createTime) }}
            </a-descriptions-item>
          </a-descriptions>
        </div>
      </template>

      <section v-if="showLogisticsSection" class="logistics-section">
        <a-divider orientation="left">物流轨迹</a-divider>
        <a-spin :spinning="logisticsLoading">
          <template v-if="logisticsData">
            <div class="summary-card">
              <div class="summary-main">
                <div class="summary-row">
                  <span class="status-badge" :class="statusClass">
                    {{ logisticsData.deliveryStatusText || '物流查询' }}
                  </span>
                  <span v-if="String(logisticsData.queryStatus) !== '0'" class="summary-tip">
                    {{ logisticsData.queryMessage || '暂无物流更新' }}
                  </span>
                </div>
                <h3 class="summary-title">
                  {{ logisticsData.companyName || logisticsCompany || '快递公司待识别' }}
                </h3>
                <p class="summary-text">物流单号：{{ logisticsData.logisticsNo || logisticsNo || '--' }}</p>
                <p v-if="logisticsData.latestTrackTime" class="summary-text">
                  最近更新：{{ logisticsData.latestTrackTime }}
                </p>
              </div>
              <img
                v-if="logisticsData.logo"
                :src="logisticsData.logo"
                :alt="logisticsData.companyName || logisticsCompany || '物流公司'"
                class="company-logo"
              />
            </div>

            <a-alert
              v-if="showLogisticsHint"
              class="logistics-alert"
              type="warning"
              show-icon
              :message="logisticsData.queryMessage || '暂无物流轨迹'"
            />

            <div class="meta-grid">
              <div class="meta-item">
                <span class="meta-label">物流公司</span>
                <span class="meta-value">
                  {{ logisticsData.companyName || logisticsCompany || '--' }}
                </span>
              </div>
              <div class="meta-item">
                <span class="meta-label">公司电话</span>
                <span class="meta-value">{{ logisticsData.companyPhone || '--' }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-label">快递员</span>
                <span class="meta-value">{{ logisticsData.courierName || '--' }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-label">快递员电话</span>
                <span class="meta-value">{{ logisticsData.courierPhone || '--' }}</span>
              </div>
            </div>

            <a-timeline v-if="traceList.length" class="logistics-timeline">
              <a-timeline-item
                v-for="(trace, index) in traceList"
                :key="`${trace.time || '--'}-${trace.status || '--'}-${index}`"
                :color="index === 0 ? '#1677ff' : '#d0d7de'"
              >
                <p class="trace-time">{{ trace.time || '--' }}</p>
                <p class="trace-status">{{ trace.status || '--' }}</p>
              </a-timeline-item>
            </a-timeline>
            <a-empty v-else description="暂无物流轨迹" />
          </template>

          <template v-else-if="logisticsError">
            <a-alert type="warning" show-icon :message="logisticsError" />
          </template>

          <template v-else>
            <a-empty description="暂无物流信息" />
          </template>
        </a-spin>
      </section>

      <a-empty v-if="!loading && !orderData.length" description="暂无订单数据" />
    </a-spin>

    <div class="footer-actions">
      <a-button @click="handleClose">关闭</a-button>
    </div>
  </a-modal>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { getAdoptOrderDetail, getOrderLogistics, getSkuOrderDetail } from '@/api/order'

const props = defineProps({
  open: {
    type: Boolean,
    default: false
  },
  orderType: {
    type: Number,
    default: 0
  },
  orderNo: {
    type: String,
    default: ''
  },
  logisticsNo: {
    type: String,
    default: ''
  },
  logisticsCompany: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:open', 'close'])

const visible = ref(false)
const loading = ref(false)
const logisticsLoading = ref(false)
const orderData = ref([])
const logisticsData = ref(null)
const logisticsError = ref('')

const traceList = computed(() => {
  return Array.isArray(logisticsData.value?.traces) ? logisticsData.value.traces : []
})

const showLogisticsSection = computed(() => {
  return Boolean(props.logisticsNo || logisticsLoading.value || logisticsData.value || logisticsError.value)
})

const showLogisticsHint = computed(() => {
  return Boolean(logisticsData.value?.queryMessage) && String(logisticsData.value?.queryStatus) !== '0'
})

const statusClass = computed(() => {
  const status = Number(logisticsData.value?.deliveryStatus)
  if (status === 1) {
    return 'status-shipping'
  }
  if (status === 2) {
    return 'status-delivering'
  }
  if (status === 3) {
    return 'status-signed'
  }
  if (status === 4) {
    return 'status-failed'
  }
  if (status === 5) {
    return 'status-exception'
  }
  if (status === 6) {
    return 'status-returned'
  }
  return 'status-default'
})

const resetState = () => {
  orderData.value = []
  logisticsData.value = null
  logisticsError.value = ''
  loading.value = false
  logisticsLoading.value = false
}

const fetchOrderDetail = async () => {
  if (!props.orderNo) {
    message.warning('订单号不能为空')
    return
  }

  loading.value = true
  orderData.value = []

  try {
    const params = { orderNo: props.orderNo }
    const response = props.orderType === 0
      ? await getAdoptOrderDetail(params)
      : await getSkuOrderDetail(params)

    if (String(response.code) === '0') {
      orderData.value = Array.isArray(response.data) ? response.data : (response.data ? [response.data] : [])
      return
    }
    message.error(response.message || '获取订单详情失败')
  } catch (error) {
    console.error('fetch order detail failed:', error)
    message.error('获取订单详情失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const fetchLogistics = async () => {
  if (!props.orderNo || !props.logisticsNo) {
    logisticsData.value = null
    logisticsError.value = ''
    return
  }

  logisticsLoading.value = true
  logisticsData.value = null
  logisticsError.value = ''

  try {
    const response = await getOrderLogistics({ orderNo: props.orderNo })
    if (String(response.code) === '0') {
      logisticsData.value = response.data || null
      return
    }
    logisticsError.value = response.message || '物流查询失败'
  } catch (error) {
    console.error('fetch logistics failed:', error)
    logisticsError.value = '物流查询失败，请稍后重试'
  } finally {
    logisticsLoading.value = false
  }
}

watch(
  () => [props.open, props.orderNo, props.orderType, props.logisticsNo],
  ([open, orderNo]) => {
    visible.value = open
    if (!open) {
      resetState()
      return
    }
    if (orderNo) {
      fetchOrderDetail()
      fetchLogistics()
    }
  },
  { immediate: true }
)

watch(
  () => visible.value,
  (value) => {
    emit('update:open', value)
    if (!value) {
      resetState()
    }
  }
)

const handleClose = () => {
  visible.value = false
  emit('close')
}

const formatAmount = (amount) => {
  if (amount === null || amount === undefined) {
    return '0.00'
  }
  return Number(amount).toFixed(2)
}

const formatDate = (date) => {
  if (!date) {
    return '-'
  }
  return dayjs(date).format('YYYY-MM-DD')
}

const formatTime = (time) => {
  if (!time) {
    return '-'
  }
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}
</script>

<style scoped>
.amount-highlight {
  color: #f5222d;
  font-weight: 700;
}

.logistics-section {
  margin-top: 24px;
}

.summary-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 18px;
  border-radius: 16px;
  background: linear-gradient(135deg, #f6fbff 0%, #edf5ff 100%);
  border: 1px solid #d7e6ff;
}

.summary-main {
  min-width: 0;
}

.summary-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 92px;
  height: 30px;
  padding: 0 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.status-default {
  background: #eef2f7;
  color: #516074;
}

.status-shipping {
  background: #edf7ff;
  color: #1677ff;
}

.status-delivering {
  background: #eef6ff;
  color: #0f5bd8;
}

.status-signed {
  background: #edf9f0;
  color: #14834a;
}

.status-failed,
.status-exception,
.status-returned {
  background: #fff2f0;
  color: #cf1322;
}

.summary-tip {
  color: #7b8896;
  font-size: 13px;
}

.summary-title {
  margin: 12px 0 0;
  color: #1f2937;
  font-size: 18px;
  font-weight: 700;
}

.summary-text {
  margin: 8px 0 0;
  color: #5b6675;
  font-size: 13px;
  word-break: break-all;
}

.company-logo {
  width: 72px;
  height: 72px;
  object-fit: contain;
  border-radius: 16px;
  padding: 10px;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid #e2ebf7;
}

.logistics-alert {
  margin-top: 16px;
}

.meta-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-top: 16px;
}

.meta-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 14px 16px;
  border-radius: 14px;
  border: 1px solid #e8edf3;
  background: #fafcff;
}

.meta-label {
  color: #7b8896;
  font-size: 12px;
}

.meta-value {
  color: #1f2937;
  font-size: 14px;
  font-weight: 600;
  word-break: break-all;
}

.logistics-timeline {
  margin-top: 20px;
}

.trace-time {
  margin: 0;
  color: #6b7280;
  font-size: 12px;
}

.trace-status {
  margin: 6px 0 0;
  color: #1f2937;
  font-size: 14px;
  line-height: 1.7;
  word-break: break-word;
}

.footer-actions {
  margin-top: 24px;
  text-align: right;
}

@media (max-width: 768px) {
  :deep(.ant-modal) {
    width: 95% !important;
    max-width: 95%;
  }

  .summary-card {
    flex-direction: column;
    align-items: flex-start;
  }

  .company-logo {
    width: 60px;
    height: 60px;
  }

  .meta-grid {
    grid-template-columns: 1fr;
  }
}
</style>
