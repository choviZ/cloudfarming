<template>
  <div class="admin-page dashboard-page">
    <a-card :bordered="false" class="admin-card" title="平台经营数据看板">
      <template #extra>
        <a-space>
          <span class="admin-muted">最近刷新：{{ formattedUpdatedAt }}</span>
          <a-button type="primary" :loading="loading" @click="loadDashboard">
            <template #icon>
              <ReloadOutlined />
            </template>
            刷新数据
          </a-button>
        </a-space>
      </template>
      <a-alert
        type="info"
        show-icon
        message="统计口径"
        description="订单趋势按创建时间聚合近 7 天数据；待办积压按创建时间或最近更新时间分为 24h 内、24-72h、72h+。"
      />
    </a-card>

    <a-row :gutter="[16, 16]">
      <a-col
        v-for="metric in metricCards"
        :key="metric.key"
        :xs="24"
        :sm="12"
        :xl="6"
      >
        <a-card :bordered="false" class="admin-card" size="small">
          <a-statistic :title="metric.label" :value="metric.value" />
          <div class="metric-meta">{{ metric.meta }}</div>
          <div class="admin-muted metric-hint">{{ metric.hint }}</div>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="[16, 16]">
      <a-col :xs="24" :lg="10">
        <a-card :bordered="false" class="admin-card" title="用户结构">
          <template #extra>
            <a-tag color="blue">{{ formatCount(summary.totalUsers) }}</a-tag>
          </template>
          <p class="admin-muted chart-desc">区分普通用户、农户与管理员账号的当前规模</p>
        <div ref="userChartRef" class="chart-canvas"></div>
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="14">
        <a-card :bordered="false" class="admin-card" title="近 7 日订单趋势">
          <template #extra>
            <a-tag color="blue">GMV {{ formatMoney(summary.last7Gmv, true) }}</a-tag>
          </template>
          <p class="admin-muted chart-desc">同时观察下单量、支付订单量和有效 GMV 的变化</p>
        <div ref="orderTrendChartRef" class="chart-canvas chart-canvas--trend"></div>
        </a-card>
      </a-col>

      <a-col :span="24">
        <a-card :bordered="false" class="admin-card" title="审核待办 / 积压">
          <template #extra>
            <a-tag color="blue">{{ formatCount(summary.reviewBacklogTotal) }}</a-tag>
          </template>
          <p class="admin-muted chart-desc">按审核类型和处理年龄分桶，帮助判断哪里开始堆积</p>
        <div ref="backlogChartRef" class="chart-canvas chart-canvas--wide"></div>
          <a-row :gutter="[16, 16]">
            <a-col :xs="24" :md="12">
              <a-card size="small" title="商品待审">
                <a-statistic :value="formatCount(summary.pendingProductAudit)" />
                <div class="admin-muted">72h+ {{ formatCount(summary.productBacklogOverdue) }}</div>
              </a-card>
            </a-col>
            <a-col :xs="24" :md="12">
              <a-card size="small" title="农户待审">
                <a-statistic :value="formatCount(summary.pendingFarmerReview)" />
                <div class="admin-muted">72h+ {{ formatCount(summary.farmerBacklogOverdue) }}</div>
              </a-card>
            </a-col>
          </a-row>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import { ReloadOutlined } from '@ant-design/icons-vue'
import * as echarts from 'echarts/core'
import { BarChart, LineChart, PieChart } from 'echarts/charts'
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components'
import { SVGRenderer } from 'echarts/renderers'
import { getUserPage } from '@/api/user'
import { listSpuByPage } from '@/api/spu'
import { listOrders } from '@/api/order'
import { getFarmerPage } from '@/api/farmer'
import { getFeedbackPage } from '@/api/feedback'

echarts.use([GridComponent, LegendComponent, TooltipComponent, PieChart, BarChart, LineChart, SVGRenderer])

const USER_TYPE_OPTIONS = [
  { label: '普通用户', value: 0, color: '#1677ff' },
  { label: '农户', value: 1, color: '#faad14' },
  { label: '管理员', value: 2, color: '#722ed1' },
]

const BACKLOG_BUCKETS = [
  { key: 'fresh', label: '24h 内', color: '#1677ff' },
  { key: 'warning', label: '24-72h', color: '#faad14' },
  { key: 'overdue', label: '72h+', color: '#ff4d4f' },
]

const REVIEW_BACKLOG_CONFIG = [
  {
    key: 'product',
    label: '商品审核',
    filters: { auditStatus: 0 },
    apiFn: listSpuByPage,
    getTimeValue: (record) => record?.createTime,
  },
  {
    key: 'farmer',
    label: '农户审核',
    filters: { reviewStatus: 0 },
    apiFn: getFarmerPage,
    getTimeValue: (record) => record?.updateTime,
  },
]

const numberFormatter = new Intl.NumberFormat('zh-CN')
const currencyFormatter = new Intl.NumberFormat('zh-CN', {
  minimumFractionDigits: 0,
  maximumFractionDigits: 0,
})
const prefersReducedMotion =
  typeof window !== 'undefined'
  && window.matchMedia('(prefers-reduced-motion: reduce)').matches

const loading = ref(false)
const lastUpdatedAt = ref('')

const userChartRef = ref(null)
const orderTrendChartRef = ref(null)
const backlogChartRef = ref(null)

let userChartInstance
let orderTrendChartInstance
let backlogChartInstance

const summary = reactive({
  totalUsers: 0,
  userRegularCount: 0,
  userFarmerCount: 0,
  userAdminCount: 0,
  todayOrderCount: 0,
  todayPaidOrderCount: 0,
  todayGmv: 0,
  last7OrderCount: 0,
  last7PaidOrderCount: 0,
  last7Gmv: 0,
  pendingProductAudit: 0,
  pendingFarmerReview: 0,
  pendingFeedback: 0,
  productBacklogFresh: 0,
  productBacklogWarning: 0,
  productBacklogOverdue: 0,
  farmerBacklogFresh: 0,
  farmerBacklogWarning: 0,
  farmerBacklogOverdue: 0,
  feedbackBacklogFresh: 0,
  feedbackBacklogWarning: 0,
  feedbackBacklogOverdue: 0,
  reviewBacklogTotal: 0,
  reviewBacklogOverdue: 0,
})

const orderTrendPoints = ref(createEmptyTrendPoints())

const metricCards = computed(() => [
  {
    key: 'users',
    label: '平台用户',
    value: formatCount(summary.totalUsers),
    meta: `普通用户 ${formatCount(summary.userRegularCount)} · 农户 ${formatCount(summary.userFarmerCount)}`,
    hint: `管理员账号 ${formatCount(summary.userAdminCount)} 个`,
  },
  {
    key: 'orders',
    label: '近 7 日订单',
    value: formatCount(summary.last7OrderCount),
    meta: `支付订单 ${formatCount(summary.last7PaidOrderCount)} · 今日 ${formatCount(summary.todayOrderCount)}`,
    hint: `近 7 日 GMV ${formatMoney(summary.last7Gmv, true)}`,
  },
  {
    key: 'review',
    label: '审核待办',
    value: formatCount(summary.pendingProductAudit + summary.pendingFarmerReview),
    meta: `商品 ${formatCount(summary.pendingProductAudit)} · 农户 ${formatCount(summary.pendingFarmerReview)}`,
    hint: `72h+ 积压 ${formatCount(summary.productBacklogOverdue + summary.farmerBacklogOverdue)}`,
  },
  {
    key: 'feedback',
    label: '反馈积压',
    value: formatCount(summary.pendingFeedback),
    meta: `24h 内 ${formatCount(summary.feedbackBacklogFresh)} · 24-72h ${formatCount(summary.feedbackBacklogWarning)}`,
    hint: `72h+ 未处理 ${formatCount(summary.feedbackBacklogOverdue)}`,
  },
])

const formattedUpdatedAt = computed(() => {
  if (!lastUpdatedAt.value) {
    return '尚未刷新'
  }
  return dayjs(lastUpdatedAt.value).format('YYYY-MM-DD HH:mm:ss')
})

function createEmptyTrendPoints(referenceTime = dayjs()) {
  return Array.from({ length: 7 }, (_, index) => {
    const date = referenceTime.subtract(6 - index, 'day')
    return {
      dateKey: date.format('YYYY-MM-DD'),
      label: date.format('MM-DD'),
      orderCount: 0,
      paidOrderCount: 0,
      gmv: 0,
    }
  })
}

function formatCount(value) {
  return numberFormatter.format(Number(value || 0))
}

function formatMoney(value, withSymbol = false) {
  const amount = currencyFormatter.format(Number(value || 0))
  return withSymbol ? `¥${amount}` : amount
}

function isSuccessCode(code) {
  return String(code) === '0'
}

function toDayjs(value) {
  if (!value) {
    return null
  }
  const parsed = dayjs(value)
  return parsed.isValid() ? parsed : null
}

function isPaidLikeOrder(status) {
  return ![0, 4].includes(Number(status))
}

async function fetchPageTotal(apiFn, filters = {}) {
  try {
    const response = await apiFn({
      current: 1,
      size: 1,
      ...filters,
    })
    if (isSuccessCode(response?.code)) {
      return {
        total: Number(response?.data?.total || 0),
        failed: false,
      }
    }
  } catch (error) {
    console.error(error)
  }

  return {
    total: 0,
    failed: true,
  }
}

async function fetchRecentOrderRecords(referenceTime) {
  const pageSize = 200
  const cutoff = referenceTime.startOf('day').subtract(6, 'day')
  const records = []
  let total = 0
  let failed = false
  let current = 1

  while (current <= 20) {
    try {
      const response = await listOrders({
        current,
        size: pageSize,
      })
      if (!isSuccessCode(response?.code)) {
        failed = true
        break
      }

      const pageRecords = Array.isArray(response?.data?.records) ? response.data.records : []
      total = Number(response?.data?.total || 0)
      if (!pageRecords.length) {
        break
      }

      records.push(...pageRecords)
      const lastRecordTime = toDayjs(pageRecords[pageRecords.length - 1]?.createTime)
      if (!lastRecordTime || lastRecordTime.isBefore(cutoff) || current * pageSize >= total) {
        break
      }
      current += 1
    } catch (error) {
      console.error(error)
      failed = true
      break
    }
  }

  return {
    failed,
    records: records.filter((record) => {
      const createTime = toDayjs(record?.createTime)
      return createTime && !createTime.isBefore(cutoff)
    }),
  }
}

function resolveAgeBucket(value, referenceTime) {
  const recordTime = toDayjs(value)
  if (!recordTime) {
    return 'fresh'
  }
  const diffHours = referenceTime.diff(recordTime, 'hour', true)
  if (diffHours < 24) {
    return 'fresh'
  }
  if (diffHours < 72) {
    return 'warning'
  }
  return 'overdue'
}

async function fetchBacklogStats(config, referenceTime) {
  const pageSize = 200
  const overdueThreshold = referenceTime.subtract(72, 'hour')
  const stats = {
    total: 0,
    fresh: 0,
    warning: 0,
    overdue: 0,
    failed: false,
  }

  let total = 0
  let processedCount = 0
  let current = 1

  while (current <= 20) {
    try {
      const response = await config.apiFn({
        current,
        size: pageSize,
        ...config.filters,
      })
      if (!isSuccessCode(response?.code)) {
        stats.failed = true
        break
      }

      const pageRecords = Array.isArray(response?.data?.records) ? response.data.records : []
      total = Number(response?.data?.total || 0)
      if (!pageRecords.length) {
        break
      }

      pageRecords.forEach((record) => {
        const bucket = resolveAgeBucket(config.getTimeValue(record), referenceTime)
        stats.total += 1
        stats[bucket] += 1
      })

      processedCount += pageRecords.length
      const lastRecordTime = toDayjs(config.getTimeValue(pageRecords[pageRecords.length - 1]))
      if (lastRecordTime && lastRecordTime.diff(overdueThreshold) <= 0) {
        const remaining = Math.max(total - processedCount, 0)
        stats.total += remaining
        stats.overdue += remaining
        break
      }

      if (processedCount >= total) {
        break
      }
      current += 1
    } catch (error) {
      console.error(error)
      stats.failed = true
      break
    }
  }

  return stats
}

function buildOrderTrend(records, referenceTime) {
  const points = createEmptyTrendPoints(referenceTime)
  const pointMap = new Map(points.map((item) => [item.dateKey, item]))

  records.forEach((record) => {
    const createTime = toDayjs(record?.createTime)
    if (!createTime) {
      return
    }
    const key = createTime.format('YYYY-MM-DD')
    const point = pointMap.get(key)
    if (!point) {
      return
    }

    point.orderCount += 1
    if (isPaidLikeOrder(record?.orderStatus)) {
      point.paidOrderCount += 1
      point.gmv += Number(record?.actualPayAmount || 0)
    }
  })

  return points
}

function getChartInstance(element, currentInstance) {
  if (!element) {
    return currentInstance
  }
  if (currentInstance && !currentInstance.isDisposed()) {
    return currentInstance
  }
  return echarts.init(element, null, { renderer: 'svg' })
}

function normalizePieData(items) {
  const total = items.reduce((sum, item) => sum + Number(item.value || 0), 0)
  if (total > 0) {
    return items
  }
  return [
    {
      name: '暂无数据',
      value: 1,
      itemStyle: {
        color: '#E2E8F0',
      },
    },
  ]
}

function buildUserChartOption() {
  return {
    color: USER_TYPE_OPTIONS.map((item) => item.color),
    animation: !prefersReducedMotion,
    tooltip: {
      trigger: 'item',
      formatter: ({ name, value, percent }) =>
        `${name}<br/>数量：${formatCount(value)}<br/>占比：${percent}%`,
    },
    legend: {
      bottom: 0,
      itemWidth: 10,
      itemHeight: 10,
      textStyle: {
        color: '#475569',
      },
    },
    series: [
      {
        type: 'pie',
        radius: ['54%', '76%'],
        center: ['50%', '44%'],
        itemStyle: {
          borderColor: '#FFFFFF',
          borderWidth: 4,
          borderRadius: 12,
        },
        label: {
          show: true,
          formatter: '{d}%',
          color: '#0F172A',
          fontWeight: 600,
        },
        labelLine: {
          length: 10,
          length2: 8,
        },
        data: normalizePieData([
          { name: '普通用户', value: summary.userRegularCount },
          { name: '农户', value: summary.userFarmerCount },
          { name: '管理员', value: summary.userAdminCount },
        ]),
      },
    ],
  }
}

function buildOrderTrendChartOption() {
  return {
    animation: !prefersReducedMotion,
    color: ['#d9d9d9', '#1677ff', '#722ed1'],
    legend: {
      top: 0,
      right: 0,
      itemWidth: 10,
      itemHeight: 10,
      textStyle: {
        color: '#475569',
      },
    },
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(15, 23, 42, 0.94)',
      borderWidth: 0,
      textStyle: {
        color: '#F8FAFC',
      },
      extraCssText: 'border-radius: 14px; box-shadow: 0 16px 40px rgba(15, 23, 42, 0.18);',
      axisPointer: {
        type: 'shadow',
        shadowStyle: {
          color: 'rgba(29, 78, 216, 0.08)',
        },
      },
      formatter(params) {
        if (!params?.length) {
          return ''
        }
        const index = params[0].dataIndex
        const point = orderTrendPoints.value[index]
        return [
          `<div class="trend-tooltip-title">${point.dateKey}</div>`,
          `<div class="trend-tooltip-row">下单量 <strong>${formatCount(point.orderCount)} 单</strong></div>`,
          `<div class="trend-tooltip-row">支付订单 <strong>${formatCount(point.paidOrderCount)} 单</strong></div>`,
          `<div class="trend-tooltip-row">有效 GMV <strong>${formatMoney(point.gmv, true)}</strong></div>`,
        ].join('')
      },
    },
    grid: {
      top: 56,
      right: 18,
      bottom: 20,
      left: 18,
      containLabel: true,
    },
    xAxis: {
      type: 'category',
      data: orderTrendPoints.value.map((item) => item.label),
      axisTick: {
        show: false,
      },
      axisLine: {
        lineStyle: {
          color: '#CBD5E1',
        },
      },
      axisLabel: {
        color: '#64748B',
      },
    },
    yAxis: [
      {
        type: 'value',
        name: '订单量',
        min: 0,
        splitNumber: 4,
        axisLine: {
          show: false,
        },
        axisTick: {
          show: false,
        },
        splitLine: {
          lineStyle: {
            color: '#E2E8F0',
          },
        },
        axisLabel: {
          color: '#64748B',
        },
      },
      {
        type: 'value',
        name: 'GMV',
        min: 0,
        splitNumber: 4,
        axisLine: {
          show: false,
        },
        axisTick: {
          show: false,
        },
        splitLine: {
          show: false,
        },
        axisLabel: {
          color: '#64748B',
          formatter: (value) => `¥${Number(value).toFixed(0)}`,
        },
      },
    ],
    series: [
      {
        name: '下单量',
        type: 'bar',
        barWidth: 18,
        data: orderTrendPoints.value.map((item) => item.orderCount),
        itemStyle: {
          borderRadius: [10, 10, 0, 0],
          color: '#CBD5E1',
        },
      },
      {
        name: '支付订单',
        type: 'bar',
        barWidth: 18,
        data: orderTrendPoints.value.map((item) => item.paidOrderCount),
        itemStyle: {
          borderRadius: [10, 10, 0, 0],
          color: '#1677ff',
        },
      },
      {
        name: '有效 GMV',
        type: 'line',
        yAxisIndex: 1,
        smooth: true,
        data: orderTrendPoints.value.map((item) => Number(item.gmv.toFixed(2))),
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: {
          width: 4,
          color: '#722ed1',
        },
        itemStyle: {
          color: '#FFFFFF',
          borderColor: '#722ed1',
          borderWidth: 3,
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(114, 46, 209, 0.16)' },
            { offset: 1, color: 'rgba(114, 46, 209, 0.02)' },
          ]),
        },
      },
    ],
  }
}

function buildBacklogChartOption() {
  const categories = [
    {
      label: '商品审核',
      fresh: summary.productBacklogFresh,
      warning: summary.productBacklogWarning,
      overdue: summary.productBacklogOverdue,
    },
    {
      label: '农户审核',
      fresh: summary.farmerBacklogFresh,
      warning: summary.farmerBacklogWarning,
      overdue: summary.farmerBacklogOverdue,
    },
  ]

  return {
    animation: !prefersReducedMotion,
    legend: {
      top: 0,
      right: 0,
      itemWidth: 10,
      itemHeight: 10,
      textStyle: {
        color: '#475569',
      },
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow',
      },
      formatter(params) {
        if (!params?.length) {
          return ''
        }
        const category = categories[params[0].dataIndex]
        const total = category.fresh + category.warning + category.overdue
        return [
          `${category.label}`,
          `24h 内：${formatCount(category.fresh)}`,
          `24-72h：${formatCount(category.warning)}`,
          `72h+：${formatCount(category.overdue)}`,
          `总量：${formatCount(total)}`,
        ].join('<br/>')
      },
    },
    grid: {
      top: 50,
      right: 18,
      bottom: 20,
      left: 30,
      containLabel: true,
    },
    xAxis: {
      type: 'value',
      min: 0,
      splitLine: {
        lineStyle: {
          color: '#E2E8F0',
        },
      },
      axisLabel: {
        color: '#64748B',
      },
    },
    yAxis: {
      type: 'category',
      data: categories.map((item) => item.label),
      axisTick: {
        show: false,
      },
      axisLine: {
        show: false,
      },
      axisLabel: {
        color: '#475569',
      },
    },
    series: BACKLOG_BUCKETS.map((bucket, index) => ({
      name: bucket.label,
      type: 'bar',
      stack: 'total',
      barWidth: 22,
      data: categories.map((item) => item[bucket.key]),
      itemStyle: {
        color: bucket.color,
        borderRadius:
          index === 0
            ? [8, 0, 0, 8]
            : index === BACKLOG_BUCKETS.length - 1
              ? [0, 8, 8, 0]
              : 0,
      },
    })),
  }
}

function renderCharts() {
  userChartInstance = getChartInstance(userChartRef.value, userChartInstance)
  orderTrendChartInstance = getChartInstance(orderTrendChartRef.value, orderTrendChartInstance)
  backlogChartInstance = getChartInstance(backlogChartRef.value, backlogChartInstance)

  userChartInstance?.setOption(buildUserChartOption(), true)
  orderTrendChartInstance?.setOption(buildOrderTrendChartOption(), true)
  backlogChartInstance?.setOption(buildBacklogChartOption(), true)
}

function resizeCharts() {
  userChartInstance?.resize()
  orderTrendChartInstance?.resize()
  backlogChartInstance?.resize()
}

function disposeCharts() {
  userChartInstance?.dispose()
  orderTrendChartInstance?.dispose()
  backlogChartInstance?.dispose()

  userChartInstance = undefined
  orderTrendChartInstance = undefined
  backlogChartInstance = undefined
}

async function loadDashboard() {
  loading.value = true
  const referenceTime = dayjs()

  const [
    regularUsers,
    farmerUsers,
    adminUsers,
    recentOrdersResult,
    productBacklog,
    farmerBacklog,
    feedbackBacklog,
  ] = await Promise.all([
    fetchPageTotal(getUserPage, { userType: 0 }),
    fetchPageTotal(getUserPage, { userType: 1 }),
    fetchPageTotal(getUserPage, { userType: 2 }),
    fetchRecentOrderRecords(referenceTime),
    fetchBacklogStats(REVIEW_BACKLOG_CONFIG[0], referenceTime),
    fetchBacklogStats(REVIEW_BACKLOG_CONFIG[1], referenceTime),
    fetchBacklogStats(
      {
        key: 'feedback',
        label: '反馈处理',
        filters: { status: 0 },
        apiFn: getFeedbackPage,
        getTimeValue: (record) => record?.createTime,
      },
      referenceTime
    ),
  ])

  const failedCount = [
    regularUsers,
    farmerUsers,
    adminUsers,
    recentOrdersResult,
    productBacklog,
    farmerBacklog,
    feedbackBacklog,
  ].filter((item) => item.failed).length

  summary.userRegularCount = regularUsers.total
  summary.userFarmerCount = farmerUsers.total
  summary.userAdminCount = adminUsers.total
  summary.totalUsers = summary.userRegularCount + summary.userFarmerCount + summary.userAdminCount

  orderTrendPoints.value = buildOrderTrend(recentOrdersResult.records, referenceTime)
  summary.todayOrderCount = orderTrendPoints.value[orderTrendPoints.value.length - 1]?.orderCount || 0
  summary.todayPaidOrderCount = orderTrendPoints.value[orderTrendPoints.value.length - 1]?.paidOrderCount || 0
  summary.todayGmv = orderTrendPoints.value[orderTrendPoints.value.length - 1]?.gmv || 0
  summary.last7OrderCount = orderTrendPoints.value.reduce((sum, item) => sum + item.orderCount, 0)
  summary.last7PaidOrderCount = orderTrendPoints.value.reduce((sum, item) => sum + item.paidOrderCount, 0)
  summary.last7Gmv = orderTrendPoints.value.reduce((sum, item) => sum + item.gmv, 0)

  summary.pendingProductAudit = productBacklog.total
  summary.productBacklogFresh = productBacklog.fresh
  summary.productBacklogWarning = productBacklog.warning
  summary.productBacklogOverdue = productBacklog.overdue

  summary.pendingFarmerReview = farmerBacklog.total
  summary.farmerBacklogFresh = farmerBacklog.fresh
  summary.farmerBacklogWarning = farmerBacklog.warning
  summary.farmerBacklogOverdue = farmerBacklog.overdue

  summary.pendingFeedback = feedbackBacklog.total
  summary.feedbackBacklogFresh = feedbackBacklog.fresh
  summary.feedbackBacklogWarning = feedbackBacklog.warning
  summary.feedbackBacklogOverdue = feedbackBacklog.overdue

  summary.reviewBacklogTotal =
    summary.pendingProductAudit + summary.pendingFarmerReview
  summary.reviewBacklogOverdue =
    summary.productBacklogOverdue + summary.farmerBacklogOverdue

  lastUpdatedAt.value = new Date().toISOString()

  await nextTick()
  renderCharts()

  if (failedCount > 0) {
    message.warning(`有 ${failedCount} 项统计未能成功加载，页面已展示当前可获取数据`)
  }

  loading.value = false
}

onMounted(async () => {
  await loadDashboard()
  window.addEventListener('resize', resizeCharts)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  disposeCharts()
})
</script>

<style scoped lang="less">
.metric-meta {
  margin-top: 8px;
}

.metric-hint {
  margin-top: 4px;
  font-size: 12px;
}

.chart-desc {
  margin-bottom: 12px;
}

.chart-canvas {
  width: 100%;
  height: 320px;
}

.chart-canvas--trend,
.chart-canvas--wide {
  height: 340px;
}

:deep(.trend-tooltip-title) {
  margin-bottom: 8px;
  color: #dbeafe;
  font-size: 12px;
}

:deep(.trend-tooltip-row) {
  color: #f8fafc;
  font-size: 13px;
  line-height: 1.7;
}

@media (max-width: 768px) {
  .chart-canvas,
  .chart-canvas--trend,
  .chart-canvas--wide {
    height: 280px;
  }
}
</style>
