<template>
  <div class="pay-success-page">
    <div class="pay-success-shell">
      <!-- 顶部品牌区 -->
      <button type="button" class="pay-brand" @click="goToHome">
        <span class="pay-brand__mark">云</span>
        <span class="pay-brand__content">
          <span class="pay-brand__title">云养殖助农平台</span>
          <span class="pay-brand__hint">返回平台首页</span>
        </span>
      </button>

      <!-- 页面标题 -->
      <div class="page-header">
        <div>
          <h1 class="page-title">{{ paid ? '支付成功' : '支付结果确认中' }}</h1>
          <p class="page-subtitle">{{ statusMessage }}</p>
        </div>
        <div v-if="loading" class="page-summary">
          <a-spin size="small" />
          <span>正在同步支付结果，请稍候...</span>
        </div>
      </div>

      <!-- 结果卡片 -->
      <a-card class="result-card">
        <a-result
          :status="resultStatus"
          :title="paid ? '支付成功' : '支付结果确认中'"
          :subTitle="statusMessage"
        >
          <template #icon>
            <div class="custom-icon" :class="{ 'is-pending': !paid }">
              <svg viewBox="0 0 100 100" class="check-icon">
                <circle cx="50" cy="50" r="45" fill="#f0f9f0" stroke="#4CAF50" stroke-width="2" />
                <path
                  v-if="paid"
                  d="M30,50 L45,65 L75,35"
                  fill="none"
                  stroke="#4CAF50"
                  stroke-width="6"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
                <circle v-else cx="50" cy="50" r="30" fill="none" stroke="#4CAF50" stroke-width="4" stroke-dasharray="5,5">
                  <animateTransform
                    attributeName="transform"
                    type="rotate"
                    from="0 50 50"
                    to="360 50 50"
                    dur="1s"
                    repeatCount="indefinite"
                  />
                </circle>
              </svg>
            </div>
          </template>

          <template #extra>
            <div class="order-detail">
              <a-descriptions :column="1" bordered size="small">
                <a-descriptions-item label="支付单号">
                  <span class="mono">{{ payOrderNo || '--' }}</span>
                </a-descriptions-item>
                <a-descriptions-item label="支付金额">
                  <span class="money">¥{{ displayAmount }}</span>
                </a-descriptions-item>
                <a-descriptions-item v-if="tradeStatus" label="交易状态">
                  <a-tag :color="tradeStatusColor">{{ tradeStatus }}</a-tag>
                </a-descriptions-item>
              </a-descriptions>
            </div>
          </template>

          <template #actions>
            <div class="action-buttons">
              <a-button size="large" @click="goToOrder">查看订单列表</a-button>
              <a-button type="primary" size="large" @click="goToHome">返回首页</a-button>
            </div>
          </template>
        </a-result>
      </a-card>

      <!-- 温馨提示 -->
      <a-alert
        v-if="!paid"
        class="warning-tip"
        type="warning"
        show-icon
        message="支付结果需要一定时间同步"
        description="如果支付已成功但页面显示仍在确认中，请稍后到订单列表刷新查看。切勿重复支付。"
      />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { confirmPayOrder } from '@/api/order'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const paid = ref(false)
const payOrderNo = ref('')
const amount = ref('')
const tradeStatus = ref('')
const statusMessage = ref('正在为您确认支付结果，请稍候...')

const displayAmount = computed(() => amount.value || '--')

const resultStatus = computed(() => paid.value ? 'success' : 'info')

const tradeStatusColor = computed(() => {
  const status = tradeStatus.value
  if (status === 'TRADE_SUCCESS' || status === '已支付') return 'green'
  if (status === 'TRADE_CLOSED' || status === '已关闭') return 'red'
  return 'orange'
})

const goToOrder = () => {
  router.push('/usercenter/orders')
}

const goToHome = () => {
  router.push('/index')
}

const initQuery = () => {
  payOrderNo.value = String(route.query.out_trade_no || route.query.payOrderNo || '')
  amount.value = String(route.query.total_amount || route.query.amount || '')
}

const loadPayResult = async () => {
  if (!payOrderNo.value) {
    statusMessage.value = '未获取到支付单号，请到订单列表查看支付结果'
    return
  }

  loading.value = true
  try {
    const res = await confirmPayOrder(payOrderNo.value)
    if (res.code === '0' && res.data) {
      paid.value = !!res.data.paid
      payOrderNo.value = res.data.payOrderNo || payOrderNo.value
      amount.value = res.data.totalAmount ?? amount.value
      tradeStatus.value = res.data.tradeStatus || ''
      statusMessage.value = res.data.message || (paid.value ? '您的订单已支付完成' : '支付结果确认中，请稍后刷新订单列表')
      if (!paid.value) {
        message.warning(statusMessage.value)
      }
      return
    }
    statusMessage.value = res.message || '支付结果确认失败，请稍后到订单列表查看'
    message.error(statusMessage.value)
  } catch (error) {
    statusMessage.value = '支付结果确认失败，请稍后到订单列表查看'
    message.error(error?.message || statusMessage.value)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  initQuery()
  loadPayResult()
})
</script>

<style scoped>
.pay-success-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 24px 16px;
}

.pay-success-shell {
  max-width: 640px;
  margin: 0 auto;
}

/* 品牌区 */
.pay-brand {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  background: #ffffff;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 20px;
}

.pay-brand:hover {
  border-color: #4CAF50;
  box-shadow: 0 2px 8px rgba(76, 175, 80, 0.1);
}

.pay-brand__mark {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #4CAF50, #81C784);
  border-radius: 8px;
  color: #ffffff;
  font-size: 18px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

.pay-brand__content {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.pay-brand__title {
  font-size: 14px;
  font-weight: 600;
  color: #262626;
  line-height: 1.3;
}

.pay-brand__hint {
  font-size: 12px;
  color: #8c8c8c;
}

/* 页面标题 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.page-title {
  font-size: 22px;
  font-weight: 600;
  color: #262626;
  margin: 0 0 6px;
}

.page-subtitle {
  font-size: 14px;
  color: #8c8c8c;
  margin: 0;
}

.page-summary {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #595959;
}

/* 结果卡片 */
.result-card {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.result-card :deep(.ant-card-body) {
  padding: 32px 24px;
}

.custom-icon {
  display: flex;
  justify-content: center;
}

.check-icon {
  width: 72px;
  height: 72px;
}

.custom-icon.is-pending .check-icon {
  opacity: 0.8;
}

.order-detail {
  max-width: 360px;
  margin: 0 auto;
}

.order-detail :deep(.ant-descriptions-item-label) {
  width: 100px;
  color: #8c8c8c;
}

.order-detail :deep(.ant-descriptions-item-content) {
  color: #262626;
}

.mono {
  font-family: monospace;
  word-break: break-all;
}

.money {
  font-weight: 600;
  color: #f5222d;
}

.action-buttons {
  display: flex;
  gap: 16px;
  justify-content: center;
}

.action-buttons .ant-btn {
  min-width: 120px;
}

/* 提示 */
.warning-tip {
  margin-top: 16px;
  border-radius: 8px;
}

@media (max-width: 480px) {
  .pay-success-page {
    padding: 16px 12px;
  }

  .page-header {
    flex-direction: column;
    gap: 12px;
  }

  .result-card :deep(.ant-card-body) {
    padding: 24px 16px;
  }

  .action-buttons {
    flex-direction: column;
    gap: 12px;
  }

  .action-buttons .ant-btn {
    width: 100%;
  }
}
</style>
