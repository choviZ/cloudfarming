<template>
  <div class="instance-detail-page">
    <section class="page-header">
      <div>
        <a-button type="link" class="back-button" @click="backToList">
          返回养殖实例
        </a-button>
        <h1 class="page-title">实例详情与生长日记</h1>
        <p class="page-desc">围绕耳标号维护养殖过程，发布后用户会在“我的认养”中立即看到最新记录。</p>
      </div>
      <a-button @click="fetchPageData">刷新数据</a-button>
    </section>

    <a-spin :spinning="pageLoading">
      <adopt-instance-summary-card :instance="instanceDetail">
        <template #extra>
          <a-popconfirm
            v-if="canFulfill"
            title="确认完成当前实例履约？全部实例都完成后，认养订单会自动进入待发货。"
            ok-text="确认"
            cancel-text="取消"
            @confirm="handleFulfill"
          >
            <a-button type="primary" :loading="fulfillSubmitting">
              完成履约
            </a-button>
          </a-popconfirm>
        </template>
      </adopt-instance-summary-card>

      <section class="detail-grid">
        <div class="main-column">
          <weight-trend-card :points="weightPoints" :loading="weightLoading" />

          <div class="timeline-wrapper">
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
        </div>

        <aside class="publish-card">
          <div class="publish-head">
            <div>
              <p class="publish-caption">农户操作台</p>
              <h2 class="publish-title">发布新日记</h2>
            </div>
            <a-tag :color="canPublishDiary ? 'green' : 'default'">
              {{ canPublishDiary ? '用户实时可见' : '当前不可更新' }}
            </a-tag>
          </div>

          <a-alert
            class="publish-alert"
            :type="canPublishDiary ? 'info' : 'warning'"
            show-icon
            :message="canPublishDiary ? '支持文字、多图、单视频和体重记录' : '当前实例已结束，不能继续更新生长日记'"
            :description="canPublishDiary ? '体重记录会自动进入曲线，图片与视频将按时间顺序展示在成长时间线中。' : publishDisabledText"
          />

          <a-form v-if="canPublishDiary" layout="vertical" class="publish-form">
            <a-form-item label="日记类型" required>
              <a-select v-model:value="diaryForm.logType" placeholder="请选择日记类型">
                <a-select-option
                  v-for="option in ADOPT_LOG_TYPE_OPTIONS"
                  :key="option.value"
                  :value="option.value"
                >
                  {{ option.label }}
                </a-select-option>
              </a-select>
            </a-form-item>

            <a-form-item label="文字描述">
              <a-textarea
                v-model:value="diaryForm.content"
                :rows="5"
                :maxlength="300"
                placeholder="记录今天的喂养情况、状态变化或异常说明"
                show-count
              />
            </a-form-item>

            <a-form-item v-if="isWeightLog" label="体重（kg）" required>
              <a-input-number
                v-model:value="diaryForm.weight"
                :min="0.1"
                :step="0.1"
                :precision="1"
                placeholder="请输入本次体重"
                style="width: 100%"
              />
            </a-form-item>

            <a-form-item label="日记图片">
              <multi-image-upload
                v-model:value="diaryForm.imageUrls"
                biz-code="ANIMAL_DIARY"
                :max-count="9"
                :max-size="5"
              />
            </a-form-item>

            <a-form-item label="日记视频">
              <video-upload
                v-model:value="diaryForm.videoUrl"
                biz-code="ANIMAL_DIARY_VIDEO"
                :max-size="50"
              />
            </a-form-item>

            <div class="publish-actions">
              <a-button @click="resetDiaryForm">清空内容</a-button>
              <a-button type="primary" :loading="submitting" @click="handleSubmitDiary">
                发布日记
              </a-button>
            </div>
          </a-form>

          <div v-else class="publish-disabled-state">
            <p class="publish-disabled-title">{{ publishDisabledText }}</p>
            <p class="publish-disabled-desc">你仍然可以在左侧继续查看历史日记和体重曲线，但当前实例不能再追加新记录。</p>
          </div>
        </aside>
      </section>
    </a-spin>
  </div>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'
import { createAdoptLog, getAdoptInstanceDetail, getAdoptWeightTrend, pageAdoptLogs } from '@/api/adopt'
import { fulfillFarmerAdoptInstance } from '@/api/order'
import MultiImageUpload from '@/components/Upload/MultiImageUpload.vue'
import VideoUpload from '@/components/Upload/VideoUpload.vue'
import AdoptInstanceSummaryCard from '@/components/adopt/AdoptInstanceSummaryCard.vue'
import AdoptLogTimeline from '@/components/adopt/AdoptLogTimeline.vue'
import WeightTrendCard from '@/components/adopt/WeightTrendCard.vue'
import { ADOPT_INSTANCE_STATUS, ADOPT_LOG_TYPE, ADOPT_LOG_TYPE_OPTIONS } from '@/constants/adopt'

const route = useRoute()
const router = useRouter()

const pageLoading = ref(false)
const logsLoading = ref(false)
const weightLoading = ref(false)
const submitting = ref(false)
const fulfillSubmitting = ref(false)
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

const diaryForm = reactive({
  logType: undefined,
  content: '',
  imageUrls: [],
  videoUrl: '',
  weight: undefined
})

const instanceId = computed(() => Number(route.params.instanceId))
const isWeightLog = computed(() => diaryForm.logType === ADOPT_LOG_TYPE.WEIGHT_RECORD)
const canFulfill = computed(() => Number(instanceDetail.value?.status) === ADOPT_INSTANCE_STATUS.ADOPTED)
const canPublishDiary = computed(() => {
  if (!instanceDetail.value?.id) {
    return true
  }
  return Number(instanceDetail.value?.status) === ADOPT_INSTANCE_STATUS.ADOPTED
})
const publishDisabledText = computed(() => {
  const status = Number(instanceDetail.value?.status)
  if (status === ADOPT_INSTANCE_STATUS.FULFILLED) {
    return '该实例已履约完成，后续生长日记已锁定。'
  }
  if (status === ADOPT_INSTANCE_STATUS.DEAD) {
    return '该实例已异常结束，后续生长日记已锁定。'
  }
  return '当前实例状态不支持继续发布生长日记。'
})

const isSuccessCode = (code) => ['0', '200'].includes(String(code))

const resetDiaryForm = () => {
  diaryForm.logType = undefined
  diaryForm.content = ''
  diaryForm.imageUrls = []
  diaryForm.videoUrl = ''
  diaryForm.weight = undefined
}

const ensureValidInstanceId = () => {
  if (Number.isInteger(instanceId.value) && instanceId.value > 0) {
    return true
  }
  message.error('养殖实例不存在')
  return false
}

const fetchInstanceDetail = async () => {
  if (!ensureValidInstanceId()) {
    return false
  }
  const response = await getAdoptInstanceDetail(instanceId.value)
  if (!isSuccessCode(response.code)) {
    message.error(response.message || '获取实例详情失败')
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
    console.error('加载养殖实例详情失败', error)
    message.error('加载养殖实例详情失败，请稍后重试')
  } finally {
    pageLoading.value = false
  }
}

const validateDiaryForm = () => {
  if (!diaryForm.logType) {
    message.warning('请选择日记类型')
    return false
  }
  if (isWeightLog.value && (!diaryForm.weight || diaryForm.weight <= 0)) {
    message.warning('体重记录必须填写大于 0 的体重')
    return false
  }
  const hasContent = Boolean(String(diaryForm.content || '').trim())
    || diaryForm.imageUrls.length > 0
    || Boolean(diaryForm.videoUrl)
    || diaryForm.weight !== undefined
  if (!hasContent) {
    message.warning('请至少填写文字、图片、视频或体重中的一种内容')
    return false
  }
  return true
}

const handleSubmitDiary = async () => {
  if (!ensureValidInstanceId()) {
    return
  }
  if (!canPublishDiary.value) {
    message.warning(publishDisabledText.value)
    return
  }
  if (!validateDiaryForm()) {
    return
  }
  submitting.value = true
  try {
    const response = await createAdoptLog({
      instanceId: instanceId.value,
      logType: diaryForm.logType,
      content: String(diaryForm.content || '').trim() || undefined,
      imageUrls: diaryForm.imageUrls,
      videoUrl: diaryForm.videoUrl || undefined,
      weight: isWeightLog.value ? diaryForm.weight : undefined
    })
    if (!isSuccessCode(response.code)) {
      message.error(response.message || '发布日记失败')
      return
    }
    message.success('生长日记已发布')
    resetDiaryForm()
    logsPagination.current = 1
    await fetchPageData()
  } catch (error) {
    console.error('发布日记失败', error)
    message.error('发布日记失败，请稍后重试')
  } finally {
    submitting.value = false
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

const handleFulfill = async () => {
  if (!ensureValidInstanceId()) {
    return
  }
  fulfillSubmitting.value = true
  try {
    const response = await fulfillFarmerAdoptInstance({
      instanceId: instanceId.value
    })
    if (!isSuccessCode(response.code)) {
      message.error(response.message || '完成履约失败')
      return
    }
    message.success('当前养殖实例已完成履约，认养订单将进入待发货')
    await fetchPageData()
  } catch (error) {
    console.error('完成履约失败', error)
    message.error('完成履约失败，请稍后重试')
  } finally {
    fulfillSubmitting.value = false
  }
}

const backToList = () => {
  router.push('/farmer/adopt/instances')
}

watch(
  () => route.params.instanceId,
  () => {
    logsPagination.current = 1
    logsPagination.total = 0
    logFilters.logType = undefined
    resetDiaryForm()
    fetchPageData()
  },
  { immediate: true }
)
</script>

<style scoped>
.instance-detail-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-width: 1440px;
  margin: 0 auto;
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
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: 16px;
  align-items: start;
  margin-top: 16px;
}

.main-column {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.timeline-wrapper {
  background: transparent;
}

.timeline-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.publish-card {
  position: sticky;
  top: 24px;
  padding: 22px 24px;
  background: #fff;
  border: 1px solid #edf2ee;
  border-radius: 22px;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.05);
}

.publish-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.publish-caption {
  margin: 0 0 8px;
  color: #7b8b7f;
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.publish-title {
  margin: 0;
  color: #17311f;
  font-size: 24px;
  font-weight: 700;
}

.publish-alert {
  margin: 18px 0;
}

.publish-form {
  margin-top: 4px;
}

.publish-disabled-state {
  padding-top: 4px;
}

.publish-disabled-title {
  margin: 0 0 8px;
  color: #17311f;
  font-size: 15px;
  font-weight: 600;
}

.publish-disabled-desc {
  margin: 0;
  color: #6d7c73;
  font-size: 13px;
  line-height: 1.8;
}

.publish-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

@media (max-width: 1100px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }

  .publish-card {
    position: static;
  }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    padding: 18px;
  }

  .publish-card {
    padding: 18px;
  }
}
</style>
