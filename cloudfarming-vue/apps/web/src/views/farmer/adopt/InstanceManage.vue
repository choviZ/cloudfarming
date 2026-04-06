<template>
  <div class="instance-manage-page">
    <section class="page-header">
      <div>
        <h1 class="page-title">养殖实例</h1>
        <p class="page-desc">集中查看已分配的认养实例，并从这里进入详情页维护生长日记。</p>
      </div>
      <a-button type="primary" :loading="loading" @click="fetchInstances">
        刷新列表
      </a-button>
    </section>

    <section class="toolbar-card">
      <div class="summary-grid">
        <div class="summary-item">
          <span class="summary-label">当前筛选</span>
          <strong class="summary-value">{{ activeStatusLabel }}</strong>
          <p class="summary-hint">支持按实例状态、认养项目 ID、订单 ID 快速定位。</p>
        </div>
        <div class="summary-item">
          <span class="summary-label">实例总数</span>
          <strong class="summary-value">{{ pagination.total }}</strong>
          <p class="summary-hint">当前条件下已分配的养殖实例数量。</p>
        </div>
        <div class="summary-item">
          <span class="summary-label">当前页项目数</span>
          <strong class="summary-value">{{ currentPageItemCount }}</strong>
          <p class="summary-hint">本页共展示 {{ instanceRecords.length }} 条实例记录。</p>
        </div>
      </div>

      <a-form layout="inline" :model="searchForm" class="filter-form" @finish="handleSearch">
        <a-form-item label="实例状态">
          <a-select
            v-model:value="searchForm.status"
            placeholder="全部状态"
            allow-clear
            style="width: 160px"
          >
            <a-select-option v-for="option in STATUS_OPTIONS" :key="option.value" :value="option.value">
              {{ option.label }}
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="认养项目 ID">
          <a-input
            v-model:value="searchForm.itemId"
            placeholder="请输入项目 ID"
            allow-clear
            style="width: 180px"
          />
        </a-form-item>

        <a-form-item label="关联订单 ID">
          <a-input
            v-model:value="searchForm.ownerOrderId"
            placeholder="请输入订单 ID"
            allow-clear
            style="width: 180px"
          />
        </a-form-item>

        <a-form-item class="filter-actions">
          <a-space>
            <a-button type="primary" html-type="submit">查询</a-button>
            <a-button @click="handleReset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </section>

    <section class="table-card">
      <div class="table-head">
        <div>
          <h2 class="table-title">实例列表</h2>
          <p class="table-desc">耳标号是后续养殖日志、体重记录和履约追踪的唯一标识。</p>
        </div>
      </div>

      <a-table
        :columns="columns"
        :data-source="instanceRecords"
        :loading="loading"
        :pagination="false"
        :scroll="{ x: 1420 }"
        :locale="{ emptyText: '当前筛选条件下暂无养殖实例' }"
        row-key="id"
        class="instance-table"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'projectInfo'">
            <div class="project-cell">
              <a-image
                :width="72"
                :height="72"
                :src="resolveCover(record)"
                class="project-image"
                :preview="false"
                fallback="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='72' height='72' viewBox='0 0 72 72'%3E%3Crect width='72' height='72' rx='18' fill='%23f3f6f4'/%3E%3Cpath d='M20 46c5-7 10-11 16-11 5 0 10 4 16 11' fill='none' stroke='%238ca292' stroke-width='3' stroke-linecap='round'/%3E%3Ccircle cx='29' cy='28' r='6' fill='%23c8d7ce'/%3E%3Ccircle cx='43' cy='28' r='6' fill='%23dbe5df'/%3E%3C/svg%3E"
              />
              <div class="project-meta">
                <button class="project-link" type="button" @click="handleViewDetail(record.id)">
                  {{ record.itemTitle || '认养项目' }}
                </button>
                <p class="secondary-text">项目 ID：{{ record.itemId || '--' }}</p>
                <p class="secondary-text">最新日记：{{ formatDate(record.latestLogTime, '暂无更新') }}</p>
              </div>
            </div>
          </template>

          <template v-else-if="column.key === 'earTagNo'">
            <div class="ear-tag-cell">
              <span class="ear-tag-label">耳标号</span>
              <strong class="ear-tag-value">{{ record.earTagNo || '--' }}</strong>
            </div>
          </template>

          <template v-else-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ record.statusDesc || getStatusText(record.status) }}
            </a-tag>
          </template>

          <template v-else-if="column.key === 'orderInfo'">
            <div class="order-cell">
              <p class="primary-text">订单号：{{ record.orderNo || '--' }}</p>
              <p class="secondary-text">实例 ID：{{ record.id || '--' }}</p>
            </div>
          </template>

          <template v-else-if="column.key === 'createTime'">
            <div class="time-cell">
              <p class="primary-text">创建：{{ formatDate(record.createTime) }}</p>
              <p class="secondary-text">更新：{{ formatDate(record.updateTime) }}</p>
            </div>
          </template>

          <template v-else-if="column.key === 'action'">
            <a-space size="small">
              <a-button type="link" size="small" @click="handleViewDetail(record.id)">
                查看详情
              </a-button>
              <a-button
                v-if="canManageStatus(record)"
                type="link"
                size="small"
                class="danger-link"
                @click="openStatusActionModal(record)"
              >
                状态处理
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>

      <div class="pagination-wrap">
        <a-pagination
          v-model:current="pagination.current"
          :total="pagination.total"
          :page-size="pagination.pageSize"
          :show-size-changer="false"
          :show-total="(total) => `共 ${total} 条养殖实例`"
          @change="handlePageChange"
        />
      </div>
    </section>

    <a-modal
      v-model:open="statusActionVisible"
      title="状态处理"
      :confirm-loading="statusActionSubmitting"
      :ok-text="statusActionForm.actionType === 'dead' ? '确认异常处理' : '确认完成履约'"
      cancel-text="取消"
      @ok="handleStatusActionSubmit"
      @cancel="handleStatusActionCancel"
    >
      <div class="status-modal-record">
        <p class="status-modal-title">{{ currentActionRecord?.itemTitle || '认养项目' }}</p>
        <p class="status-modal-subtitle">
          耳标号：{{ currentActionRecord?.earTagNo || '--' }}
          <span class="status-modal-divider">|</span>
          当前状态：{{ currentActionRecord?.statusDesc || getStatusText(currentActionRecord?.status) }}
        </p>
      </div>

      <a-radio-group
        v-model:value="statusActionForm.actionType"
        button-style="solid"
        class="status-action-group"
      >
        <a-radio-button value="fulfill" :disabled="!canFulfillRecord(currentActionRecord)">
          完成履约
        </a-radio-button>
        <a-radio-button value="dead">
          异常处理
        </a-radio-button>
      </a-radio-group>

      <a-alert
        class="status-action-alert"
        :type="statusActionForm.actionType === 'dead' ? 'warning' : 'info'"
        show-icon
        :message="statusActionForm.actionType === 'dead' ? '异常处理会将实例标记为异常死亡' : '完成履约后，若同订单下全部实例都已履约，订单会自动进入待发货'"
        :description="statusActionForm.actionType === 'dead' ? '请填写死亡时间和原因，便于后续追溯与用户侧展示。' : '该操作通常用于认养周期正常结束，提交后实例状态会改为已履约完成，并进入物流发货环节。'"
      />

      <a-form v-if="statusActionForm.actionType === 'dead'" layout="vertical" class="status-action-form">
        <a-form-item label="异常类型">
          <a-input value="牲畜死亡" disabled />
        </a-form-item>
        <a-form-item label="死亡时间" required>
          <a-date-picker
            v-model:value="statusActionForm.deathTime"
            show-time
            style="width: 100%"
            placeholder="请选择死亡时间"
          />
        </a-form-item>
        <a-form-item label="原因说明" required>
          <a-textarea
            v-model:value="statusActionForm.deathReason"
            :rows="4"
            :maxlength="120"
            placeholder="请输入死亡原因或处理说明"
            show-count
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { markAdoptInstanceDead, pageMyAdoptInstances } from '@/api/adopt'
import { fulfillFarmerAdoptInstance } from '@/api/order'
import { ADOPT_INSTANCE_STATUS, ADOPT_INSTANCE_STATUS_OPTIONS, getAdoptInstanceStatusOption } from '@/constants/adopt'

const router = useRouter()

const STATUS_OPTIONS = ADOPT_INSTANCE_STATUS_OPTIONS

const columns = [
  {
    title: '认养项目',
    key: 'projectInfo',
    width: 360,
    fixed: 'left'
  },
  {
    title: '耳标号',
    key: 'earTagNo',
    width: 180
  },
  {
    title: '实例状态',
    key: 'status',
    width: 120,
    align: 'center'
  },
  {
    title: '关联信息',
    key: 'orderInfo',
    width: 220
  },
  {
    title: '时间信息',
    key: 'createTime',
    width: 240
  },
  {
    title: '操作',
    key: 'action',
    width: 160,
    align: 'center',
    fixed: 'right'
  }
]

const loading = ref(false)
const instancePage = reactive({
  records: [],
  total: 0,
  current: 1,
  size: 10
})
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})
const searchForm = reactive({
  status: undefined,
  itemId: '',
  ownerOrderId: ''
})
const statusActionVisible = ref(false)
const statusActionSubmitting = ref(false)
const currentActionRecord = ref(null)
const statusActionForm = reactive({
  actionType: 'fulfill',
  deathTime: null,
  deathReason: ''
})

const instanceRecords = computed(() => (Array.isArray(instancePage.records) ? instancePage.records : []))

const currentPageItemCount = computed(() => {
  return new Set(instanceRecords.value.map((item) => item.itemId).filter(Boolean)).size
})

const activeStatusLabel = computed(() => {
  const matchedOption = STATUS_OPTIONS.find((option) => option.value === searchForm.status)
  return matchedOption?.label || '全部状态'
})

const isSuccessCode = (code) => ['0', '200'].includes(String(code))

const normalizePositiveInteger = (value) => {
  const normalizedValue = String(value || '').trim()
  if (!normalizedValue) {
    return undefined
  }
  if (!/^\d+$/.test(normalizedValue)) {
    throw new Error('筛选项仅支持输入正整数')
  }
  if (/^0+$/.test(normalizedValue)) {
    throw new Error('筛选项请输入有效的正整数')
  }
  return Number(normalizedValue.replace(/^0+(?=\d)/, ''))
}

const applyPageData = (data = {}) => {
  instancePage.records = Array.isArray(data.records) ? data.records : []
  instancePage.total = Number(data.total || 0)
  instancePage.current = Number(data.current || pagination.current)
  instancePage.size = Number(data.size || pagination.pageSize)

  pagination.current = instancePage.current
  pagination.total = instancePage.total
}

const fetchInstances = async () => {
  let itemId
  let ownerOrderId
  try {
    itemId = normalizePositiveInteger(searchForm.itemId)
    ownerOrderId = normalizePositiveInteger(searchForm.ownerOrderId)
  } catch (error) {
    message.error(error.message || '请检查筛选条件')
    return
  }

  loading.value = true
  try {
    const response = await pageMyAdoptInstances({
      current: pagination.current,
      size: pagination.pageSize,
      status: searchForm.status,
      itemId,
      ownerOrderId
    })
    if (!isSuccessCode(response.code)) {
      message.error(response.message || '获取养殖实例失败')
      return
    }
    applyPageData(response.data || {})
  } catch (error) {
    console.error('获取养殖实例失败', error)
    message.error('获取养殖实例失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchInstances()
}

const handleReset = () => {
  searchForm.status = undefined
  searchForm.itemId = ''
  searchForm.ownerOrderId = ''
  pagination.current = 1
  fetchInstances()
}

const handlePageChange = (page) => {
  pagination.current = page
  fetchInstances()
}

const handleViewDetail = (instanceId) => {
  if (!instanceId) {
    return
  }
  router.push(`/farmer/adopt/instances/${instanceId}`)
}

const canFulfillRecord = (record) => Number(record?.status) === ADOPT_INSTANCE_STATUS.ADOPTED

const canManageStatus = (record) => {
  const status = Number(record?.status)
  return status === ADOPT_INSTANCE_STATUS.ADOPTED || status === ADOPT_INSTANCE_STATUS.DEAD
}

const resetStatusActionForm = () => {
  statusActionForm.actionType = 'fulfill'
  statusActionForm.deathTime = null
  statusActionForm.deathReason = ''
}

const openStatusActionModal = (record) => {
  currentActionRecord.value = record || null
  resetStatusActionForm()
  if (!canFulfillRecord(record)) {
    statusActionForm.actionType = 'dead'
  }
  statusActionForm.deathTime = record?.deathTime ? dayjs(record.deathTime) : dayjs()
  statusActionForm.deathReason = record?.deathReason || ''
  statusActionVisible.value = true
}

const handleStatusActionCancel = () => {
  statusActionVisible.value = false
  currentActionRecord.value = null
  resetStatusActionForm()
}

const shouldMoveToPreviousPage = (nextStatus, previousStatus) => {
  return (
    pagination.current > 1 &&
    instanceRecords.value.length === 1 &&
    searchForm.status !== undefined &&
    Number(searchForm.status) === Number(previousStatus) &&
    Number(nextStatus) !== Number(previousStatus)
  )
}

const handleStatusActionSubmit = async () => {
  const record = currentActionRecord.value
  if (!record?.id) {
    message.error('当前养殖实例不存在')
    return
  }

  statusActionSubmitting.value = true
  try {
    if (statusActionForm.actionType === 'dead') {
      const deathReason = String(statusActionForm.deathReason || '').trim()
      if (!statusActionForm.deathTime) {
        message.warning('请选择死亡时间')
        return
      }
      if (!deathReason) {
        message.warning('请输入死亡原因')
        return
      }
      const response = await markAdoptInstanceDead({
        instanceId: record.id,
        deathTime: statusActionForm.deathTime.toDate(),
        deathReason
      })
      if (!isSuccessCode(response.code)) {
        message.error(response.message || '更新异常状态失败')
        return
      }
      if (shouldMoveToPreviousPage(ADOPT_INSTANCE_STATUS.DEAD, record.status)) {
        pagination.current -= 1
      }
      message.success(Number(record.status) === ADOPT_INSTANCE_STATUS.DEAD ? '异常信息已更新' : '已标记为异常死亡')
    } else {
      if (!canFulfillRecord(record)) {
        message.warning('当前实例状态不支持完成履约')
        return
      }
      const response = await fulfillFarmerAdoptInstance({
        instanceId: record.id
      })
      if (!isSuccessCode(response.code)) {
        message.error(response.message || '完成履约失败')
        return
      }
      if (shouldMoveToPreviousPage(ADOPT_INSTANCE_STATUS.FULFILLED, record.status)) {
        pagination.current -= 1
      }
      message.success('当前养殖实例已完成履约')
    }

    handleStatusActionCancel()
    fetchInstances()
  } catch (error) {
    console.error('处理实例状态失败', error)
    message.error('处理实例状态失败，请稍后重试')
  } finally {
    statusActionSubmitting.value = false
  }
}

const resolveCover = (record) => {
  return record?.image || record?.itemCoverImage || ''
}

const getStatusText = (status) => {
  return getAdoptInstanceStatusOption(status)?.label || '未知状态'
}

const getStatusColor = (status) => {
  return getAdoptInstanceStatusOption(status)?.color || 'default'
}

const formatDate = (value, fallback = '--') => {
  return value ? dayjs(value).format('YYYY-MM-DD HH:mm') : fallback
}

onMounted(() => {
  fetchInstances()
})
</script>

<style scoped>
.instance-manage-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-width: 1440px;
  margin: 0 auto;
}

.page-header,
.toolbar-card,
.table-card {
  background: #fff;
  border: 1px solid #edf2ee;
  border-radius: 18px;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.04);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
}

.page-title {
  margin: 0 0 8px;
  color: #17311f;
  font-size: 26px;
  font-weight: 700;
}

.page-desc {
  margin: 0;
  color: #6d7c73;
  font-size: 14px;
}

.toolbar-card,
.table-card {
  padding: 22px 24px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  margin-bottom: 20px;
}

.summary-item {
  padding: 18px 20px;
  background: #f8fbf9;
  border: 1px solid #e7efe9;
  border-radius: 16px;
}

.summary-label {
  display: block;
  margin-bottom: 8px;
  color: #7b8b7f;
  font-size: 13px;
}

.summary-value {
  display: block;
  margin-bottom: 10px;
  color: #17311f;
  font-size: 24px;
  font-weight: 700;
}

.summary-hint {
  margin: 0;
  color: #6d7c73;
  font-size: 13px;
  line-height: 1.6;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 0;
}

.filter-actions {
  margin-left: auto;
}

.table-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 18px;
}

.table-title {
  margin: 0 0 8px;
  color: #17311f;
  font-size: 20px;
  font-weight: 700;
}

.table-desc {
  margin: 0;
  color: #6d7c73;
  font-size: 14px;
}

.project-cell {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
}

.project-image {
  border-radius: 18px;
  overflow: hidden;
  background: #f3f6f4;
}

.project-meta {
  min-width: 0;
}

.project-link {
  padding: 0;
  margin: 0 0 8px;
  border: 0;
  background: transparent;
  color: #17311f;
  font-size: 16px;
  font-weight: 600;
  line-height: 1.5;
  text-align: left;
  cursor: pointer;
  overflow-wrap: anywhere;
}

.project-link:hover {
  color: #3f8f61;
}

.primary-text,
.secondary-text {
  margin: 0;
  line-height: 1.7;
}

.primary-text {
  color: #24372a;
}

.secondary-text {
  color: #7b8b7f;
  font-size: 13px;
}

.ear-tag-cell {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.ear-tag-label {
  color: #7b8b7f;
  font-size: 12px;
}

.ear-tag-value {
  color: #17311f;
  font-size: 16px;
  font-weight: 700;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 18px;
}

.danger-link {
  color: #cf1322;
}

.status-modal-record {
  margin-bottom: 16px;
  padding: 14px 16px;
  background: #f8fbf9;
  border: 1px solid #e7efe9;
  border-radius: 14px;
}

.status-modal-title,
.status-modal-subtitle {
  margin: 0;
}

.status-modal-title {
  color: #17311f;
  font-size: 16px;
  font-weight: 600;
}

.status-modal-subtitle {
  margin-top: 6px;
  color: #6d7c73;
  font-size: 13px;
}

.status-modal-divider {
  margin: 0 8px;
  color: #c2cdc6;
}

.status-action-group {
  display: flex;
  margin-bottom: 16px;
}

.status-action-group :deep(.ant-radio-button-wrapper) {
  flex: 1;
  text-align: center;
}

.status-action-alert {
  margin-bottom: 16px;
}

@media (max-width: 1200px) {
  .summary-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .toolbar-card,
  .table-card {
    padding: 18px;
  }

  .filter-actions {
    margin-left: 0;
  }
}
</style>
