<template>
  <div class="adopt-detail-page">
    <section class="page-header">
      <div>
        <a-button type="link" class="back-button" @click="backToList">
          返回我的认养
        </a-button>
        <h1 class="page-title">认养详情</h1>
        <p class="page-desc">查看农户持续更新的成长日记、图片视频和体重变化，追踪专属认养对象的养殖进度。</p>
      </div>
      <a-button @click="fetchPageData">刷新数据</a-button>
    </section>

    <a-spin :spinning="pageLoading">
      <adopt-instance-summary-card :instance="instanceDetail" />

      <section class="detail-grid">
        <weight-trend-card :points="weightPoints" :loading="weightLoading" />

        <div class="timeline-panel">
          <a-alert
            class="detail-alert"
            type="info"
            show-icon
            message="农户会定期更新生长日记"
            description="你可以在这里查看最新图片、视频和体重趋势，了解认养对象的成长状态。"
          />

          <adopt-log-timeline :logs="logs" :loading="logsLoading">
            <template #extra>
              <a-space>
                <a-select
                  v-model:value="logFilters.logType"
                  allow-clear
                  placeholder="全部类型"
                  style="width: 160px"
                  @change="handleLogFilterChange"
                >
                  <a-select-option
                    v-for="option in ADOPT_LOG_TYPE_OPTIONS"
                    :key="option.value"
                    :value="option.value"
                  >
                    {{ option.label }}
                  </a-select-option>
                </a-select>
                <a-button @click="fetchLogs">刷新</a-button>
              </a-space>
            </template>
          </adopt-log-timeline>

          <div class="timeline-pagination" v-if="logsPagination.total > 0">
            <a-pagination
              v-model:current="logsPagination.current"
              :total="logsPagination.total"
              :page-size="logsPagination.pageSize"
              :show-size-changer="false"
              :show-total="(total) => `共 ${total} 条日记`"
              @change="handleLogPageChange"
            />
          </div>
        </div>
      </section>
    </a-spin>
  </div>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'
import { getAdoptInstanceDetail, getAdoptWeightTrend, pageAdoptLogs } from '@/api/adopt'
import AdoptInstanceSummaryCard from '@/components/adopt/AdoptInstanceSummaryCard.vue'
import AdoptLogTimeline from '@/components/adopt/AdoptLogTimeline.vue'
import WeightTrendCard from '@/components/adopt/WeightTrendCard.vue'
import { ADOPT_LOG_TYPE_OPTIONS } from '@/constants/adopt'

const route = useRoute()
const router = useRouter()

const pageLoading = ref(false)
const logsLoading = ref(false)
const weightLoading = ref(false)
const instanceDetail = ref({})
const logs = ref([])
const weightPoints = ref([])

const logsPagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

const logFilters = reactive({
  logType: undefined
})

const instanceId = computed(() => Number(route.params.instanceId))
const isSuccessCode = (code) => ['0', '200'].includes(String(code))

const ensureValidInstanceId = () => {
  if (Number.isInteger(instanceId.value) && instanceId.value > 0) {
    return true
  }
  message.error('认养实例不存在')
  return false
}

const fetchInstanceDetail = async () => {
  if (!ensureValidInstanceId()) {
    return false
  }
  const response = await getAdoptInstanceDetail(instanceId.value)
  if (!isSuccessCode(response.code)) {
    message.error(response.message || '获取认养详情失败')
    return false
  }
  instanceDetail.value = response.data || {}
  return true
}

const fetchLogs = async () => {
  if (!ensureValidInstanceId()) {
    return
  }
  logsLoading.value = true
  try {
    const response = await pageAdoptLogs({
      instanceId: instanceId.value,
      current: logsPagination.current,
      size: logsPagination.pageSize,
      logType: logFilters.logType
    })
    if (!isSuccessCode(response.code)) {
      message.error(response.message || '获取生长日记失败')
      return
    }
    const data = response.data || {}
    logs.value = Array.isArray(data.records) ? data.records : []
    logsPagination.total = Number(data.total || 0)
    logsPagination.current = Number(data.current || logsPagination.current)
  } catch (error) {
    console.error('获取生长日记失败', error)
    message.error('获取生长日记失败，请稍后重试')
  } finally {
    logsLoading.value = false
  }
}

const fetchWeightTrend = async () => {
  if (!ensureValidInstanceId()) {
    return
  }
  weightLoading.value = true
  try {
    const response = await getAdoptWeightTrend(instanceId.value)
    if (!isSuccessCode(response.code)) {
      message.error(response.message || '获取体重曲线失败')
      return
    }
    weightPoints.value = Array.isArray(response.data) ? response.data : []
  } catch (error) {
    console.error('获取体重曲线失败', error)
    message.error('获取体重曲线失败，请稍后重试')
  } finally {
    weightLoading.value = false
  }
}

const fetchPageData = async () => {
  if (!ensureValidInstanceId()) {
    return
  }
  pageLoading.value = true
  try {
    const detailLoaded = await fetchInstanceDetail()
    if (!detailLoaded) {
      return
    }
    await Promise.all([fetchLogs(), fetchWeightTrend()])
  } catch (error) {
    console.error('加载认养详情失败', error)
    message.error('加载认养详情失败，请稍后重试')
  } finally {
    pageLoading.value = false
  }
}

const handleLogFilterChange = () => {
  logsPagination.current = 1
  fetchLogs()
}

const handleLogPageChange = (page) => {
  logsPagination.current = page
  fetchLogs()
}

const backToList = () => {
  const tab = Array.isArray(route.query.tab) ? route.query.tab[0] : route.query.tab
  router.push({
    path: '/usercenter/adopts',
    query: tab ? { tab } : {}
  })
}

watch(
  () => route.params.instanceId,
  () => {
    logsPagination.current = 1
    logsPagination.total = 0
    logFilters.logType = undefined
    fetchPageData()
  },
  { immediate: true }
)
</script>

<style scoped>
.adopt-detail-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 100%;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  padding: 22px 24px;
  background: #fff;
  border: 1px solid #edf2ee;
  border-radius: 18px;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.04);
}

.back-button {
  padding-left: 0;
  margin-bottom: 8px;
}

.page-title {
  margin: 0 0 8px;
  color: #17311f;
  font-size: 28px;
  font-weight: 700;
}

.page-desc {
  margin: 0;
  color: #6d7c73;
  font-size: 14px;
}

.detail-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 16px;
}

.timeline-panel {
  display: flex;
  flex-direction: column;
}

.detail-alert {
  margin-bottom: 16px;
}

.timeline-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    padding: 18px;
  }
}
</style>
