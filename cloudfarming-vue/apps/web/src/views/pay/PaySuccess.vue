<template>
  <div class="pay-success">
    <div class="success-card">
      <div class="success-icon" :class="{ pending: !paid }">
        <svg viewBox="0 0 100 100" class="check-icon">
          <circle cx="50" cy="50" r="45" fill="#f0f9f0" stroke="#4CAF50" stroke-width="2" />
          <path
            d="M30,50 L45,65 L75,35"
            fill="none"
            stroke="#4CAF50"
            stroke-width="6"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </svg>
      </div>

      <div class="success-text">
        <h2 class="title">{{ paid ? '支付成功' : '支付结果确认中' }}</h2>
        <p class="desc">{{ statusMessage }}</p>
      </div>

      <div class="status-tip" v-if="loading">
        正在同步支付结果，请稍候...
      </div>

      <div class="order-info">
        <div class="info-item">
          <span class="label">支付单号：</span>
          <span class="value">{{ payOrderNo || '--' }}</span>
        </div>
        <div class="info-item">
          <span class="label">支付金额：</span>
          <span class="value">¥{{ displayAmount }}</span>
        </div>
        <div class="info-item" v-if="tradeStatus">
          <span class="label">交易状态：</span>
          <span class="value">{{ tradeStatus }}</span>
        </div>
      </div>

      <div class="btn-group">
        <button class="btn btn-primary" @click="goToOrder">查看订单列表</button>
        <button class="btn btn-secondary" @click="goToHome">返回首页</button>
      </div>
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
.pay-success {
  min-height: 100vh;
  background-color: #f8fbf8;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  box-sizing: border-box;
}

.success-card {
  width: 100%;
  max-width: 500px;
  background: #ffffff;
  border-radius: 12px;
  padding: 40px 30px;
  box-shadow: 0 4px 20px rgba(76, 175, 80, 0.08);
  text-align: center;
  box-sizing: border-box;
}

.success-icon {
  margin-bottom: 24px;
}

.success-icon.pending {
  opacity: 0.8;
}

.check-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto;
}

.success-text .title {
  font-size: 24px;
  color: #2e7d32;
  margin: 0 0 12px;
  font-weight: 600;
}

.success-text .desc {
  font-size: 16px;
  color: #558b2f;
  margin: 0;
  line-height: 1.5;
}

.status-tip {
  margin-top: 18px;
  padding: 10px 12px;
  border-radius: 8px;
  background-color: #f0f9f0;
  color: #2e7d32;
  font-size: 14px;
}

.order-info {
  margin: 30px 0;
  padding: 20px;
  background-color: #f0f9f0;
  border-radius: 8px;
  text-align: left;
}

.info-item {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 12px;
  font-size: 14px;
}

.info-item:last-child {
  margin-bottom: 0;
}

.label {
  color: #388e3c;
  font-weight: 500;
}

.value {
  color: #212121;
  font-family: monospace;
  text-align: right;
  word-break: break-all;
}

.btn-group {
  display: flex;
  gap: 16px;
  margin-top: 10px;
}

.btn {
  flex: 1;
  height: 48px;
  border-radius: 8px;
  border: none;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-primary {
  background-color: #4CAF50;
  color: #ffffff;
}

.btn-primary:hover {
  background-color: #388e3c;
}

.btn-secondary {
  background-color: #ffffff;
  color: #4CAF50;
  border: 1px solid #4CAF50;
}

.btn-secondary:hover {
  background-color: #f0f9f0;
}

@media (max-width: 480px) {
  .success-card {
    padding: 30px 20px;
  }

  .btn-group {
    flex-direction: column;
    gap: 12px;
  }

  .success-text .title {
    font-size: 20px;
  }
}
</style>
