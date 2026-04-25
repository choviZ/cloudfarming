<template>
  <a-modal
    v-model:open="visible"
    title="查看物流"
    :footer="null"
    :destroy-on-close="true"
    :mask-closable="true"
    width="760px"
    @cancel="handleClose"
  >
    <a-spin :spinning="loading">
      <template v-if="logistics">
        <section class="summary-card">
          <div class="summary-main">
            <div class="status-row">
              <span class="status-badge" :class="statusClass">
                {{ logistics.deliveryStatusText || '物流查询' }}
              </span>
              <span v-if="logistics.queryStatus !== '0'" class="query-status">
                {{ logistics.queryMessage || '物流信息同步中' }}
              </span>
            </div>
            <h3 class="company-name">
              {{ logistics.companyName || logistics.logisticsCompany || '快递公司待识别' }}
            </h3>
            <p class="summary-text">物流单号：{{ logistics.logisticsNo || '--' }}</p>
            <p v-if="logistics.latestTrackTime" class="summary-text">
              最近更新：{{ logistics.latestTrackTime }}
            </p>
          </div>

          <img
            v-if="logistics.logo"
            :src="logistics.logo"
            :alt="logistics.companyName || logistics.logisticsCompany || '物流公司'"
            class="company-logo"
          />
        </section>

        <a-alert
          v-if="showQueryHint"
          class="query-alert"
          type="warning"
          show-icon
          :message="logistics.queryMessage || '暂无物流轨迹'"
        />

        <section class="meta-grid">
          <div v-for="item in metaItems" :key="item.label" class="meta-item">
            <span class="meta-label">{{ item.label }}</span>
            <span class="meta-value">{{ item.value }}</span>
          </div>
        </section>

        <section class="timeline-section">
          <div class="section-head">
            <h4 class="section-title">物流轨迹</h4>
            <span class="section-subtitle">{{ traceList.length }} 条更新</span>
          </div>

          <a-timeline v-if="traceList.length" class="timeline">
            <a-timeline-item
              v-for="(trace, index) in traceList"
              :key="`${trace.time || '--'}-${trace.status || '--'}-${index}`"
              :color="index === 0 ? '#1f7a3f' : '#b8c4bd'"
            >
              <p class="trace-time">{{ trace.time || '--' }}</p>
              <p class="trace-status">{{ trace.status || '--' }}</p>
            </a-timeline-item>
          </a-timeline>

          <a-empty v-else description="暂无物流轨迹" />
        </section>
      </template>

      <template v-else>
        <a-alert v-if="errorText" class="query-alert" type="warning" show-icon :message="errorText" />
        <a-empty description="暂无物流信息" />
      </template>
    </a-spin>
  </a-modal>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { getOrderLogistics } from '@/api/order'

const props = defineProps({
  open: {
    type: Boolean,
    default: false
  },
  orderNo: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:open', 'close'])

const visible = ref(false)
const loading = ref(false)
const logistics = ref(null)
const errorText = ref('')

const traceList = computed(() => {
  return Array.isArray(logistics.value?.traces) ? logistics.value.traces : []
})

const metaItems = computed(() => {
  if (!logistics.value) {
    return []
  }
  return [
    {
      label: '物流公司',
      value: logistics.value.companyName || logistics.value.logisticsCompany || '--'
    },
    {
      label: '公司电话',
      value: logistics.value.companyPhone || '--'
    },
    {
      label: '快递员',
      value: logistics.value.courierName || '--'
    },
    {
      label: '快递员电话',
      value: logistics.value.courierPhone || '--'
    },
    {
      label: '官网地址',
      value: logistics.value.companyWebsite || '--'
    },
    {
      label: '流转耗时',
      value: logistics.value.takeTime || '--'
    }
  ]
})

const statusClass = computed(() => {
  const status = Number(logistics.value?.deliveryStatus)
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

const showQueryHint = computed(() => {
  return Boolean(logistics.value?.queryMessage) && String(logistics.value?.queryStatus) !== '0'
})

const resetState = () => {
  logistics.value = null
  errorText.value = ''
  loading.value = false
}

const fetchLogistics = async () => {
  if (!props.orderNo) {
    errorText.value = '订单号不能为空'
    logistics.value = null
    return
  }
  loading.value = true
  errorText.value = ''
  logistics.value = null
  try {
    const response = await getOrderLogistics(props.orderNo)
    if (String(response.code) === '0') {
      logistics.value = response.data || null
      return
    }
    errorText.value = response.message || '物流查询失败'
  } catch (error) {
    console.error('fetch logistics failed:', error)
    errorText.value = '物流查询失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

watch(
  () => [props.open, props.orderNo],
  ([open, orderNo]) => {
    visible.value = open
    if (!open) {
      resetState()
      return
    }
    if (orderNo) {
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
</script>

<style scoped>
.summary-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 20px;
  border-radius: 18px;
  background: linear-gradient(135deg, #f6fbf7 0%, #eef8f0 100%);
  border: 1px solid #dceedd;
}

.summary-main {
  min-width: 0;
}

.status-row {
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
  background: #eef4ef;
  color: #5f6d64;
}

.status-shipping {
  background: #eef8f1;
  color: #1f7a3f;
}

.status-delivering {
  background: #eef6ff;
  color: #1860d6;
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

.query-status {
  color: #8a9b90;
  font-size: 13px;
}

.company-name {
  margin: 14px 0 0;
  color: #1f2937;
  font-size: 20px;
  font-weight: 700;
}

.summary-text {
  margin: 8px 0 0;
  color: #5f6d64;
  font-size: 13px;
  word-break: break-all;
}

.company-logo {
  width: 76px;
  height: 76px;
  object-fit: contain;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid #e3eee5;
  padding: 12px;
}

.query-alert {
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
  border: 1px solid #e6efe8;
  background: #fafcfb;
}

.meta-label {
  color: #7b887f;
  font-size: 12px;
}

.meta-value {
  color: #22352a;
  font-size: 14px;
  font-weight: 600;
  word-break: break-all;
}

.timeline-section {
  margin-top: 20px;
  padding: 18px 20px;
  border-radius: 18px;
  border: 1px solid #e6efe8;
  background: #fff;
}

.section-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.section-title {
  margin: 0;
  color: #22352a;
  font-size: 16px;
  font-weight: 700;
}

.section-subtitle {
  color: #8a9b90;
  font-size: 12px;
}

.timeline {
  margin-top: 8px;
}

.trace-time {
  margin: 0;
  color: #5f6d64;
  font-size: 12px;
}

.trace-status {
  margin: 6px 0 0;
  color: #1f2937;
  font-size: 14px;
  line-height: 1.7;
  word-break: break-word;
}

@media (max-width: 768px) {
  .summary-card {
    flex-direction: column;
    align-items: flex-start;
  }

  .company-logo {
    width: 64px;
    height: 64px;
  }

  .meta-grid {
    grid-template-columns: 1fr;
  }
}
</style>
