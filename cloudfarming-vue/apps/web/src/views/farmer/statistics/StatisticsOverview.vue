<template>
  <div class="statistics-page">
    <section class="hero-panel">
      <div>
        <p class="hero-tag">农户经营数据</p>
        <h1 class="hero-title">{{ statistics.shopName || '我的店铺' }}</h1>
        <p class="hero-desc">
          展示当前店铺已支付且进入履约流程的订单数据，未支付和已取消订单不计入统计。
        </p>
      </div>
      <a-button type="primary" :loading="loading" @click="fetchStatistics">刷新数据</a-button>
    </section>

    <a-row :gutter="[16, 16]" class="metric-grid">
      <a-col :xs="24" :md="12">
        <a-card :bordered="false" class="metric-card revenue-card">
          <p class="metric-label">累计销售额</p>
          <p class="metric-value">¥{{ formatMoney(statistics.salesAmount) }}</p>
          <p class="metric-hint">按店铺已支付订单实付金额汇总</p>
        </a-card>
      </a-col>
      <a-col :xs="24" :md="12">
        <a-card :bordered="false" class="metric-card order-card">
          <p class="metric-label">累计订单量</p>
          <p class="metric-value">{{ formatCount(statistics.orderCount) }}</p>
          <p class="metric-hint">按店铺已支付并进入履约流程的订单统计</p>
        </a-card>
      </a-col>
    </a-row>

    <a-alert
      class="rule-alert"
      type="info"
      show-icon
      message="统计说明"
      description="销售额与订单量仅统计当前店铺已支付订单，包含待分配、待发货、已发货、已完成、售后中和养殖中状态。"
    />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { getFarmerOrderStatistics } from '@/api/order'

const loading = ref(false)
const statistics = reactive({
  shopId: '',
  shopName: '',
  salesAmount: 0,
  orderCount: 0
})

const applyStatistics = (data = {}) => {
  statistics.shopId = data.shopId || ''
  statistics.shopName = data.shopName || ''
  statistics.salesAmount = Number(data.salesAmount || 0)
  statistics.orderCount = Number(data.orderCount || 0)
}

const fetchStatistics = async () => {
  loading.value = true
  try {
    const response = await getFarmerOrderStatistics()
    if (response.code !== '0') {
      message.error(response.message || '获取经营数据失败')
      return
    }
    applyStatistics(response.data)
  } catch (error) {
    console.error('获取农户经营数据失败', error)
    message.error('获取经营数据失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const formatMoney = (value) => Number(value || 0).toFixed(2)

const formatCount = (value) => Number(value || 0).toLocaleString('zh-CN')

onMounted(() => {
  fetchStatistics()
})
</script>

<style scoped>
.statistics-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.hero-panel {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  padding: 28px 32px;
  border-radius: 24px;
  background: linear-gradient(135deg, #f3fbf2 0%, #ffffff 45%, #fdf7ec 100%);
  box-shadow: 0 18px 40px rgba(18, 52, 36, 0.08);
}

.hero-tag {
  margin: 0 0 10px;
  color: #2f855a;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.08em;
}

.hero-title {
  margin: 0;
  color: #17311f;
  font-size: 30px;
  font-weight: 700;
}

.hero-desc {
  margin: 12px 0 0;
  max-width: 620px;
  color: #5f6f66;
  font-size: 14px;
  line-height: 1.7;
}

.metric-grid {
  margin: 0;
}

.metric-card {
  min-height: 188px;
  border-radius: 22px;
  overflow: hidden;
  box-shadow: 0 16px 36px rgba(27, 94, 32, 0.08);
}

.revenue-card {
  background: linear-gradient(160deg, #1f7a4d 0%, #3a9d6a 100%);
}

.order-card {
  background: linear-gradient(160deg, #a86b18 0%, #d89b3b 100%);
}

.metric-label,
.metric-value,
.metric-hint {
  color: #fff;
}

.metric-label {
  margin: 0;
  font-size: 16px;
  opacity: 0.88;
}

.metric-value {
  margin: 22px 0 16px;
  font-size: 40px;
  font-weight: 700;
  line-height: 1.1;
}

.metric-hint {
  margin: 0;
  font-size: 14px;
  opacity: 0.84;
}

.rule-alert {
  border-radius: 18px;
}

@media (max-width: 768px) {
  .hero-panel {
    padding: 24px 20px;
    flex-direction: column;
    align-items: stretch;
  }

  .hero-title {
    font-size: 24px;
  }

  .metric-value {
    font-size: 34px;
  }
}
</style>
