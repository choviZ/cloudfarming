<template>
  <section class="weight-card">
    <div class="card-head">
      <div>
        <p class="card-caption">成长曲线</p>
        <h3 class="card-title">体重变化</h3>
      </div>
      <div class="weight-metrics">
        <div class="metric-item">
          <span class="metric-label">最新体重</span>
          <strong class="metric-value">{{ latestWeightText }}</strong>
        </div>
        <div class="metric-item">
          <span class="metric-label">变化幅度</span>
          <strong class="metric-value">{{ weightChangeText }}</strong>
        </div>
      </div>
    </div>

    <a-spin :spinning="loading">
      <div v-if="normalizedPoints.length" class="chart-wrap">
        <div ref="chartRef" class="chart-host" />
      </div>
      <a-empty v-else description="暂无体重记录" />
    </a-spin>
  </section>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import dayjs from 'dayjs'
import * as echarts from 'echarts/core'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent } from 'echarts/components'
import { SVGRenderer } from 'echarts/renderers'

echarts.use([GridComponent, TooltipComponent, LineChart, SVGRenderer])

const props = defineProps({
  points: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const chartRef = ref(null)

let chartInstance = null
let resizeObserver = null

const normalizedPoints = computed(() => {
  return (props.points || [])
    .filter((item) => item && item.weight !== undefined && item.weight !== null)
    .map((item) => ({
      recordTime: item.recordTime,
      weight: Number(item.weight)
    }))
    .sort((left, right) => new Date(left.recordTime).getTime() - new Date(right.recordTime).getTime())
})

const xAxisLabelMode = computed(() => {
  const uniqueDays = new Set(
    normalizedPoints.value.map((item) => dayjs(item.recordTime).format('YYYY-MM-DD'))
  )
  return uniqueDays.size <= 1 ? 'datetime' : 'date'
})

const xAxisLabels = computed(() => {
  return normalizedPoints.value.map((item) => {
    return xAxisLabelMode.value === 'datetime'
      ? dayjs(item.recordTime).format('MM-DD HH:mm')
      : dayjs(item.recordTime).format('MM-DD')
  })
})

const chartValues = computed(() => {
  return normalizedPoints.value.map((item) => Number(item.weight.toFixed(1)))
})

const latestWeightText = computed(() => {
  if (!normalizedPoints.value.length) {
    return '--'
  }
  return `${normalizedPoints.value[normalizedPoints.value.length - 1].weight.toFixed(1)}kg`
})

const weightChangeValue = computed(() => {
  if (normalizedPoints.value.length < 2) {
    return null
  }
  const first = normalizedPoints.value[0].weight
  const last = normalizedPoints.value[normalizedPoints.value.length - 1].weight
  return last - first
})

const weightChangeText = computed(() => {
  if (weightChangeValue.value === null) {
    return '--'
  }
  return `${weightChangeValue.value >= 0 ? '+' : ''}${weightChangeValue.value.toFixed(1)}kg`
})

const axisInterval = computed(() => {
  const total = xAxisLabels.value.length
  if (total <= 5) {
    return 0
  }
  return Math.max(0, Math.ceil(total / 5) - 1)
})

const buildChartOption = () => {
  return {
    animationDuration: 450,
    animationEasing: 'cubicOut',
    grid: {
      top: 22,
      right: 12,
      bottom: 18,
      left: 54,
      containLabel: true
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
        type: 'line',
        lineStyle: {
          color: 'rgba(47, 133, 90, 0.28)',
          width: 1.5
        }
      },
      formatter(params) {
        const point = params?.[0]
        if (!point) {
          return ''
        }
        const rawData = normalizedPoints.value[point.dataIndex]
        return [
          `<div class="weight-tooltip-title">${dayjs(rawData.recordTime).format('YYYY-MM-DD HH:mm')}</div>`,
          `<div class="weight-tooltip-row">体重 <strong>${Number(rawData.weight).toFixed(1)}kg</strong></div>`
        ].join('')
      }
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: xAxisLabels.value,
      axisTick: {
        show: false
      },
      axisLine: {
        lineStyle: {
          color: '#d8e4dc'
        }
      },
      axisLabel: {
        color: '#7b8b7f',
        fontSize: 12,
        margin: 14,
        interval: axisInterval.value,
        showMinLabel: true,
        showMaxLabel: true
      }
    },
    yAxis: {
      type: 'value',
      min: (value) => {
        if (value.min === value.max) {
          return Math.max(0, value.min - 1)
        }
        const padding = (value.max - value.min) * 0.15
        return Math.max(0, Number((value.min - padding).toFixed(1)))
      },
      max: (value) => {
        if (value.min === value.max) {
          return value.max + 1
        }
        const padding = (value.max - value.min) * 0.15
        return Number((value.max + padding).toFixed(1))
      },
      splitNumber: 4,
      axisLine: {
        show: false
      },
      axisTick: {
        show: false
      },
      splitLine: {
        lineStyle: {
          color: '#e8efe9'
        }
      },
      axisLabel: {
        color: '#7b8b7f',
        fontSize: 12,
        formatter: (value) => `${Number(value).toFixed(1)}kg`
      }
    },
    series: [
      {
        type: 'line',
        smooth: true,
        data: chartValues.value,
        showSymbol: true,
        symbol: 'circle',
        symbolSize: normalizedPoints.value.length === 1 ? 14 : 10,
        lineStyle: {
          width: 4,
          color: '#2f855a',
          cap: 'round',
          join: 'round'
        },
        itemStyle: {
          color: '#ffffff',
          borderColor: '#2f855a',
          borderWidth: 4,
          shadowBlur: 10,
          shadowColor: 'rgba(47, 133, 90, 0.18)'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(47, 133, 90, 0.22)' },
            { offset: 1, color: 'rgba(47, 133, 90, 0.03)' }
          ])
        },
        emphasis: {
          focus: 'series'
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
  if (!chartRef.value || !normalizedPoints.value.length) {
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
  if (typeof ResizeObserver !== 'undefined' && chartRef.value) {
    resizeObserver = new ResizeObserver(() => {
      handleResize()
    })
    resizeObserver.observe(chartRef.value)
  }
  window.addEventListener('resize', handleResize)
  renderChart()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  resizeObserver?.disconnect()
  resizeObserver = null
  destroyChart()
})

watch(
  () => props.loading,
  async (loading) => {
    if (!loading) {
      await renderChart()
    }
  }
)

watch(
  normalizedPoints,
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
.weight-card {
  padding: 22px 24px;
  background: #fff;
  border: 1px solid #e6efe8;
  border-radius: 22px;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.05);
}

.card-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
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
}

.weight-metrics {
  display: flex;
  gap: 12px;
}

.metric-item {
  min-width: 120px;
  padding: 12px 16px;
  background: linear-gradient(180deg, #fbfcfb 0%, #f5f9f6 100%);
  border: 1px solid #e7efe9;
  border-radius: 16px;
}

.metric-label {
  display: block;
  margin-bottom: 6px;
  color: #7b8b7f;
  font-size: 12px;
}

.metric-value {
  color: #17311f;
  font-size: 18px;
  font-weight: 700;
}

.chart-wrap {
  position: relative;
  border-radius: 18px;
  background: linear-gradient(180deg, #ffffff 0%, #fcfdfc 100%);
}

.chart-host {
  width: 100%;
  height: 288px;
}

:deep(.weight-tooltip-title) {
  margin-bottom: 8px;
  color: #e2f7ea;
  font-size: 12px;
}

:deep(.weight-tooltip-row) {
  color: #f8fafc;
  font-size: 13px;
  line-height: 1.6;
}

@media (max-width: 768px) {
  .weight-card {
    padding: 18px;
  }

  .card-head {
    flex-direction: column;
  }

  .weight-metrics {
    flex-wrap: wrap;
  }

  .chart-host {
    height: 260px;
  }
}
</style>
