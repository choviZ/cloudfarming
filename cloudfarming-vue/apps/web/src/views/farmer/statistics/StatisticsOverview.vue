<template>
  <div class="statistics-page">
    <section class="hero-panel">
      <div class="hero-copy">
        <p class="hero-tag">农户经营数据</p>
        <h1 class="hero-title">{{ statistics.shopName || '我的店铺' }}</h1>
        <p class="hero-desc">
          展示当前店铺已支付且未关闭订单的经营数据，支持查看今日、近 7 天、近 30 天的订单与销售表现。
        </p>
      </div>

      <div class="hero-actions">
        <a-button type="primary" :loading="loading" @click="fetchStatistics">刷新数据</a-button>
      </div>
    </section>

    <section class="period-panel">
      <div class="panel-head">
        <div>
          <p class="panel-caption">周期统计</p>
          <h2 class="panel-title">经营表现概览</h2>
        </div>
        <span class="panel-hint">统计口径：已支付且未关闭订单</span>
      </div>

      <div class="period-grid">
        <article
          v-for="metric in periodMetrics"
          :key="metric.key"
          class="period-card"
        >
          <div class="period-card__head">
            <span class="period-card__label">{{ metric.label }}</span>
            <span class="period-card__badge">{{ metric.badge }}</span>
          </div>
          <p class="period-card__value">{{ metric.value }}</p>
          <p class="period-card__hint">{{ metric.hint }}</p>
        </article>
      </div>
    </section>

    <section class="trend-panel">
      <div class="panel-head panel-head--trend">
        <div>
          <p class="panel-caption">趋势图表</p>
          <h2 class="panel-title">{{ trendPanelTitle }}</h2>
        </div>
        <div class="trend-switch" role="tablist" aria-label="趋势周期切换">
          <button
            type="button"
            class="trend-switch__item"
            :class="{ 'trend-switch__item--active': activeTrendRange === '7d' }"
            @click="activeTrendRange = '7d'"
          >
            近 7 天
          </button>
          <button
            type="button"
            class="trend-switch__item"
            :class="{ 'trend-switch__item--active': activeTrendRange === '30d' }"
            @click="activeTrendRange = '30d'"
          >
            近 30 天
          </button>
        </div>
      </div>

      <a-spin :spinning="loading">
        <div v-if="filteredTrendPoints.length" class="trend-chart-wrap">
          <div ref="chartRef" class="trend-chart" />
        </div>
        <a-empty v-else description="暂无统计数据" />
      </a-spin>
    </section>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import * as echarts from 'echarts/core'
import { BarChart, LineChart } from 'echarts/charts'
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components'
import { SVGRenderer } from 'echarts/renderers'
import { getFarmerOrderStatistics } from '@/api/order'

echarts.use([GridComponent, LegendComponent, TooltipComponent, BarChart, LineChart, SVGRenderer])

const loading = ref(false)
const chartRef = ref(null)
const activeTrendRange = ref('30d')

let chartInstance = null

const statistics = reactive({
  shopId: '',
  shopName: '',
  salesAmount: 0,
  orderCount: 0,
  todaySalesAmount: 0,
  todayOrderCount: 0,
  last7DaysSalesAmount: 0,
  last7DaysOrderCount: 0,
  last30DaysSalesAmount: 0,
  last30DaysOrderCount: 0,
  trendPoints: []
})

const periodMetrics = computed(() => [
  {
    key: 'totalSales',
    label: '累计销售额',
    value: `¥${formatMoney(statistics.salesAmount)}`,
    badge: '累计',
    hint: '当前店铺已支付订单的实付金额汇总'
  },
  {
    key: 'totalOrders',
    label: '累计订单量',
    value: `${formatCount(statistics.orderCount)} 单`,
    badge: '累计',
    hint: '当前店铺已进入履约流程的订单总量'
  },
  {
    key: 'todaySales',
    label: '今日销售额',
    value: `¥${formatMoney(statistics.todaySalesAmount)}`,
    badge: '今日',
    hint: '按今日创建且已支付订单统计'
  },
  {
    key: 'todayOrders',
    label: '今日订单量',
    value: `${formatCount(statistics.todayOrderCount)} 单`,
    badge: '今日',
    hint: '按今日创建且已支付订单统计'
  },
  {
    key: 'last7Sales',
    label: '近 7 天销售额',
    value: `¥${formatMoney(statistics.last7DaysSalesAmount)}`,
    badge: '近 7 天',
    hint: '包含今日在内最近 7 天'
  },
  {
    key: 'last7Orders',
    label: '近 7 天订单量',
    value: `${formatCount(statistics.last7DaysOrderCount)} 单`,
    badge: '近 7 天',
    hint: '包含今日在内最近 7 天'
  },
  {
    key: 'last30Sales',
    label: '近 30 天销售额',
    value: `¥${formatMoney(statistics.last30DaysSalesAmount)}`,
    badge: '近 30 天',
    hint: '包含今日在内最近 30 天'
  },
  {
    key: 'last30Orders',
    label: '近 30 天订单量',
    value: `${formatCount(statistics.last30DaysOrderCount)} 单`,
    badge: '近 30 天',
    hint: '包含今日在内最近 30 天'
  }
])

const normalizedTrendPoints = computed(() => {
  return (statistics.trendPoints || []).map((item) => ({
    statDate: item?.statDate || '',
    salesAmount: Number(item?.salesAmount || 0),
    orderCount: Number(item?.orderCount || 0)
  }))
})

const filteredTrendPoints = computed(() => {
  if (activeTrendRange.value === '7d') {
    return normalizedTrendPoints.value.slice(-7)
  }
  return normalizedTrendPoints.value
})

const trendPanelTitle = computed(() => {
  return activeTrendRange.value === '7d'
    ? '近 7 天订单与销售趋势'
    : '近 30 天订单与销售趋势'
})

const chartLabels = computed(() => {
  return filteredTrendPoints.value.map((item) => dayjs(item.statDate).format('MM-DD'))
})

const chartSales = computed(() => {
  return filteredTrendPoints.value.map((item) => Number(item.salesAmount.toFixed(2)))
})

const chartOrders = computed(() => {
  return filteredTrendPoints.value.map((item) => item.orderCount)
})

const axisInterval = computed(() => {
  const total = chartLabels.value.length
  if (total <= 7) {
    return 0
  }
  return Math.max(0, Math.ceil(total / 7) - 1)
})

const applyStatistics = (data = {}) => {
  statistics.shopId = data.shopId || ''
  statistics.shopName = data.shopName || ''
  statistics.salesAmount = Number(data.salesAmount || 0)
  statistics.orderCount = Number(data.orderCount || 0)
  statistics.todaySalesAmount = Number(data.todaySalesAmount || 0)
  statistics.todayOrderCount = Number(data.todayOrderCount || 0)
  statistics.last7DaysSalesAmount = Number(data.last7DaysSalesAmount || 0)
  statistics.last7DaysOrderCount = Number(data.last7DaysOrderCount || 0)
  statistics.last30DaysSalesAmount = Number(data.last30DaysSalesAmount || 0)
  statistics.last30DaysOrderCount = Number(data.last30DaysOrderCount || 0)
  statistics.trendPoints = Array.isArray(data.trendPoints) ? data.trendPoints : []
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

const buildChartOption = () => {
  return {
    color: ['#2f855a', '#d97706'],
    animationDuration: 450,
    animationEasing: 'cubicOut',
    legend: {
      top: 0,
      right: 0,
      itemWidth: 12,
      itemHeight: 12,
      textStyle: {
        color: '#5f6f66',
        fontSize: 13
      }
    },
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(15, 23, 42, 0.94)',
      borderWidth: 0,
      textStyle: {
        color: '#f8fafc',
        fontSize: 12
      },
      extraCssText: 'border-radius: 14px; box-shadow: 0 16px 40px rgba(15, 23, 42, 0.18);',
      axisPointer: {
        type: 'shadow',
        shadowStyle: {
          color: 'rgba(47, 133, 90, 0.08)'
        }
      },
      formatter(params) {
        if (!params?.length) {
          return ''
        }
        const dataIndex = params[0].dataIndex
        const point = filteredTrendPoints.value[dataIndex]
        return [
          `<div class="trend-tooltip-title">${dayjs(point.statDate).format('YYYY-MM-DD')}</div>`,
          `<div class="trend-tooltip-row">销售额 <strong>¥${formatMoney(point.salesAmount)}</strong></div>`,
          `<div class="trend-tooltip-row">订单量 <strong>${formatCount(point.orderCount)} 单</strong></div>`
        ].join('')
      }
    },
    grid: {
      top: 52,
      right: 20,
      bottom: 20,
      left: 20,
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: chartLabels.value,
      axisTick: {
        show: false
      },
      axisLine: {
        lineStyle: {
          color: '#d9e6dc'
        }
      },
      axisLabel: {
        color: '#758379',
        fontSize: 12,
        interval: axisInterval.value
      }
    },
    yAxis: [
      {
        type: 'value',
        name: '销售额',
        min: 0,
        splitNumber: 4,
        axisLine: {
          show: false
        },
        axisTick: {
          show: false
        },
        splitLine: {
          lineStyle: {
            color: '#ecf1ee'
          }
        },
        axisLabel: {
          color: '#758379',
          fontSize: 12,
          formatter: (value) => `¥${Number(value).toFixed(0)}`
        }
      },
      {
        type: 'value',
        name: '订单量',
        min: 0,
        splitNumber: 4,
        axisLine: {
          show: false
        },
        axisTick: {
          show: false
        },
        splitLine: {
          show: false
        },
        axisLabel: {
          color: '#758379',
          fontSize: 12
        }
      }
    ],
    series: [
      {
        name: '销售额',
        type: 'line',
        smooth: true,
        yAxisIndex: 0,
        data: chartSales.value,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: {
          width: 4,
          color: '#2f855a'
        },
        itemStyle: {
          color: '#ffffff',
          borderColor: '#2f855a',
          borderWidth: 3
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(47, 133, 90, 0.18)' },
            { offset: 1, color: 'rgba(47, 133, 90, 0.02)' }
          ])
        }
      },
      {
        name: '订单量',
        type: 'bar',
        yAxisIndex: 1,
        barWidth: 14,
        data: chartOrders.value,
        itemStyle: {
          borderRadius: [10, 10, 0, 0],
          color: '#d89b3b'
        }
      }
    ]
  }
}

const initChart = () => {
  if (!chartRef.value) {
    return
  }
  chartInstance = echarts.init(chartRef.value, null, {
    renderer: 'svg'
  })
}

const renderChart = async () => {
  await nextTick()
  if (!chartRef.value || !filteredTrendPoints.value.length) {
    return
  }
  if (!chartInstance) {
    initChart()
  }
  chartInstance?.setOption(buildChartOption(), true)
  chartInstance?.resize()
}

const destroyChart = () => {
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
}

const handleResize = () => {
  chartInstance?.resize()
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
  fetchStatistics()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  destroyChart()
})

watch(
  () => loading.value,
  async (value) => {
    if (!value) {
      await renderChart()
    }
  }
)

watch(
  filteredTrendPoints,
  async (points) => {
    if (!points.length) {
      destroyChart()
      return
    }
    await renderChart()
  },
  { deep: true, immediate: true }
)
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
  align-items: stretch;
  gap: 18px;
  padding: 28px 30px;
  border-radius: 24px;
  background: linear-gradient(135deg, #fbfdfb 0%, #f4faf5 100%);
  border: 1px solid #e4ede6;
  box-shadow: 0 18px 40px rgba(18, 52, 36, 0.06);
}

.hero-copy {
  min-width: 0;
  flex: 1;
}

.hero-tag {
  margin: 0 0 10px;
  color: #2f855a;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.hero-title {
  margin: 0;
  color: #17311f;
  font-size: 30px;
  font-weight: 800;
  line-height: 1.2;
}

.hero-desc {
  margin: 12px 0 0;
  max-width: 680px;
  color: #5f6f66;
  font-size: 14px;
  line-height: 1.75;
}

.hero-actions {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.period-panel,
.trend-panel {
  padding: 22px 24px;
  background: #ffffff;
  border: 1px solid #e5eee7;
  border-radius: 22px;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.05);
}

.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 16px;
  margin-bottom: 18px;
}

.panel-head--trend {
  margin-bottom: 20px;
}

.panel-caption {
  margin: 0 0 8px;
  color: #7b8b7f;
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.panel-title {
  margin: 0;
  color: #17311f;
  font-size: 24px;
  font-weight: 700;
  line-height: 1.25;
}

.panel-hint {
  color: #809086;
  font-size: 13px;
  white-space: nowrap;
}

.period-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.period-card {
  min-height: 148px;
  padding: 18px 18px 16px;
  border-radius: 18px;
  background: linear-gradient(180deg, #fcfdfc 0%, #f6faf7 100%);
  border: 1px solid #ebf2ed;
}

.period-card__head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.period-card__label {
  color: #617067;
  font-size: 14px;
  font-weight: 600;
}

.period-card__badge {
  display: inline-flex;
  align-items: center;
  min-height: 26px;
  padding: 0 10px;
  border-radius: 999px;
  background: #edf7ef;
  color: #237548;
  font-size: 12px;
  font-weight: 700;
}

.period-card__value {
  margin: 18px 0 12px;
  color: #17311f;
  font-size: 30px;
  font-weight: 800;
  line-height: 1.15;
}

.period-card__hint {
  margin: 0;
  color: #819086;
  font-size: 13px;
  line-height: 1.6;
}

.trend-switch {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px;
  border-radius: 999px;
  background: #f4f8f5;
  border: 1px solid #e3ece5;
}

.trend-switch__item {
  min-height: 34px;
  padding: 0 16px;
  border-radius: 999px;
  border: none;
  background: transparent;
  color: #698074;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
}

.trend-switch__item--active {
  background: linear-gradient(135deg, #2f855a 0%, #3ea66e 100%);
  color: #ffffff;
  box-shadow: 0 10px 20px rgba(47, 133, 90, 0.18);
}

.trend-switch__item:not(.trend-switch__item--active):hover {
  color: #2f855a;
  background: rgba(47, 133, 90, 0.08);
}

.trend-chart-wrap {
  border-radius: 18px;
  background: linear-gradient(180deg, #ffffff 0%, #fbfcfb 100%);
}

.trend-chart {
  width: 100%;
  height: 360px;
}

:deep(.trend-tooltip-title) {
  margin-bottom: 8px;
  color: #dcfce7;
  font-size: 12px;
}

:deep(.trend-tooltip-row) {
  color: #f8fafc;
  font-size: 13px;
  line-height: 1.6;
}

@media (max-width: 1200px) {
  .period-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 960px) {
  .hero-panel,
  .panel-head {
    flex-direction: column;
    align-items: stretch;
  }

  .hero-actions {
    justify-content: flex-start;
  }

  .trend-switch {
    align-self: flex-start;
  }
}

@media (max-width: 768px) {
  .hero-panel,
  .period-panel,
  .trend-panel {
    padding: 18px;
  }

  .hero-title {
    font-size: 24px;
  }

  .period-card__value {
    font-size: 32px;
  }

  .period-grid {
    grid-template-columns: 1fr;
  }

  .trend-chart {
    height: 320px;
  }
}
</style>
