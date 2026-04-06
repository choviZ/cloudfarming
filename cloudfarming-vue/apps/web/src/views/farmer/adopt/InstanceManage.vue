<template>
  <div class="adopt-instance-page">
    <section class="page-header">
      <div>
        <h1 class="page-title">养殖实例</h1>
        <p class="page-desc">查看当前农户已分配的养殖实例，列表按认养项目 ID 升序、项目内创建时间倒序展示。</p>
      </div>
      <a-button type="primary" :loading="loading" @click="fetchInstances">
        刷新列表
      </a-button>
    </section>

    <section class="toolbar-card">
      <div class="summary-grid">
        <div class="summary-item">
          <span class="summary-label">实例总数</span>
          <strong class="summary-value">{{ pagination.total }}</strong>
          <p class="summary-hint">当前筛选条件下的养殖实例总量</p>
        </div>
        <div class="summary-item">
          <span class="summary-label">当前页项目数</span>
          <strong class="summary-value">{{ currentPageItemCount }}</strong>
          <p class="summary-hint">当前页共涉及 {{ instanceRecords.length }} 条养殖实例</p>
        </div>
        <div class="summary-item">
          <span class="summary-label">当前筛选</span>
          <strong class="summary-value">{{ activeStatusLabel }}</strong>
          <p class="summary-hint">支持按状态、认养项目 ID、关联订单 ID 快速定位</p>
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
          <p class="table-desc">耳标号用于标识唯一牲畜，后续养殖日志、履约记录都将围绕该实例展开。</p>
        </div>
      </div>

      <a-table
        :columns="columns"
        :data-source="instanceRecords"
        :loading="loading"
        :pagination="false"
        :scroll="{ x: 1220 }"
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
                fallback="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='72' height='72' viewBox='0 0 72 72'%3E%3Crect width='72' height='72' rx='16' fill='%23f3f6f4'/%3E%3Cpath d='M22 46c4-7 9-11 14-11s10 4 14 11' fill='none' stroke='%2390a69a' stroke-width='3' stroke-linecap='round'/%3E%3Ccircle cx='30' cy='28' r='6' fill='%23c8d7ce'/%3E%3Ccircle cx='44' cy='28' r='6' fill='%23dbe5df'/%3E%3C/svg%3E"
                :preview="false"
              />
              <div class="project-meta">
                <a-button type="link" class="project-link" @click="handleViewAdopt(record.itemId)">
                  {{ resolveTitle(record) }}
                </a-button>
                <p class="secondary-text">项目 ID：{{ record.itemId || '--' }}</p>
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
              {{ getStatusText(record.status) }}
            </a-tag>
          </template>

          <template v-else-if="column.key === 'ownerOrderId'">
            <div class="order-cell">
              <p class="primary-text">订单 ID：{{ record.ownerOrderId || '--' }}</p>
              <p class="secondary-text">实例 ID：{{ record.id || '--' }}</p>
            </div>
          </template>

          <template v-else-if="column.key === 'instanceImage'">
            <a-image
              :width="64"
              :height="64"
              :src="record.image || resolveCover(record)"
              class="project-image"
              :preview="false"
            />
          </template>

          <template v-else-if="column.key === 'timeInfo'">
            <div class="time-cell">
              <p class="primary-text">创建：{{ formatDate(record.createTime) }}</p>
              <p class="secondary-text">更新：{{ formatDate(record.updateTime) }}</p>
            </div>
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
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { batchAdoptItemDetails, pageMyAdoptInstances } from '@/api/adopt'

const router = useRouter()

const STATUS_OPTIONS = [
  { label: '可认养', value: 0 },
  { label: '已认养', value: 1 },
  { label: '已履约完成', value: 2 }
]

const columns = [
  {
    title: '认养项目',
    key: 'projectInfo',
    width: 340,
    fixed: 'left'
  },
  {
    title: '耳标号',
    dataIndex: 'earTagNo',
    key: 'earTagNo',
    width: 180
  },
  {
    title: '实例状态',
    dataIndex: 'status',
    key: 'status',
    width: 120,
    align: 'center'
  },
  {
    title: '关联信息',
    dataIndex: 'ownerOrderId',
    key: 'ownerOrderId',
    width: 220
  },
  {
    title: '实例图片',
    key: 'instanceImage',
    width: 120,
    align: 'center'
  },
  {
    title: '时间信息',
    key: 'timeInfo',
    width: 220
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
const adoptItemMap = ref({})

const instanceRecords = computed(() => (Array.isArray(instancePage.records) ? instancePage.records : []))

const currentPageItemCount = computed(() => {
  return new Set(instanceRecords.value.map((item) => item.itemId).filter(Boolean)).size
})

const activeStatusLabel = computed(() => {
  const matchedOption = STATUS_OPTIONS.find((option) => option.value === searchForm.status)
  return matchedOption?.label || '全部状态'
})

const isSuccessCode = (code) => String(code) === '0'

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
  return normalizedValue.replace(/^0+(?=\d)/, '')
}

const applyPageData = (data = {}) => {
  instancePage.records = Array.isArray(data.records) ? data.records : []
  instancePage.total = Number(data.total || 0)
  instancePage.current = Number(data.current || pagination.current)
  instancePage.size = Number(data.size || pagination.pageSize)

  pagination.current = instancePage.current
  pagination.total = instancePage.total
}

const fetchAdoptItems = async (records) => {
  const itemIds = [...new Set(records.map((record) => record.itemId).filter(Boolean))]
  if (!itemIds.length) {
    adoptItemMap.value = {}
    return
  }
  try {
    const response = await batchAdoptItemDetails(itemIds)
    if (!isSuccessCode(response.code)) {
      adoptItemMap.value = {}
      return
    }
    adoptItemMap.value = (Array.isArray(response.data) ? response.data : []).reduce((result, item) => {
      result[item.id] = item
      return result
    }, {})
  } catch (error) {
    console.error('获取认养项目详情失败', error)
    adoptItemMap.value = {}
  }
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
    await fetchAdoptItems(instanceRecords.value)
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

const handleViewAdopt = (itemId) => {
  if (!itemId) {
    return
  }
  router.push({
    path: '/farmer/adopt/create',
    query: { id: itemId }
  })
}

const resolveTitle = (record) => {
  return adoptItemMap.value[record.itemId]?.title || '认养项目'
}

const resolveCover = (record) => {
  return record.image || adoptItemMap.value[record.itemId]?.coverImage || ''
}

const getStatusText = (status) => {
  return STATUS_OPTIONS.find((option) => option.value === status)?.label || '未知状态'
}

const getStatusColor = (status) => {
  if (status === 0) {
    return 'default'
  }
  if (status === 1) {
    return 'gold'
  }
  if (status === 2) {
    return 'green'
  }
  return 'default'
}

const formatDate = (value) => {
  return value ? dayjs(value).format('YYYY-MM-DD HH:mm') : '--'
}

onMounted(() => {
  fetchInstances()
})
</script>

<style scoped>
.adopt-instance-page {
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
  margin: 0;
  color: #17311f;
  font-size: 28px;
  font-weight: 700;
}

.page-desc {
  margin: 8px 0 0;
  color: #6b7c72;
  font-size: 14px;
}

.toolbar-card {
  padding: 20px 24px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.summary-item {
  padding: 16px 18px;
  border-radius: 14px;
  background: #f7faf8;
  border: 1px solid #e7efe9;
}

.summary-label {
  display: block;
  margin-bottom: 8px;
  color: #8a9b90;
  font-size: 12px;
}

.summary-value {
  display: block;
  color: #17311f;
  font-size: 22px;
  font-weight: 700;
  line-height: 1.2;
}

.summary-hint {
  margin: 8px 0 0;
  color: #7b8b7f;
  font-size: 12px;
  line-height: 1.6;
}

.filter-form {
  row-gap: 12px;
}

.filter-actions {
  margin-left: auto;
}

.table-card {
  padding: 20px 24px;
}

.table-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.table-title {
  margin: 0;
  color: #17311f;
  font-size: 20px;
  font-weight: 700;
}

.table-desc {
  margin: 6px 0 0;
  color: #7b8b7f;
  font-size: 13px;
}

.project-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.project-meta {
  min-width: 0;
}

.project-link {
  padding: 0;
  height: auto;
  color: #17311f;
  font-size: 14px;
  font-weight: 700;
  white-space: normal;
  text-align: left;
}

.project-link:hover {
  color: #3d7a59;
}

.project-image {
  border-radius: 16px;
  overflow: hidden;
  flex-shrink: 0;
}

.ear-tag-cell,
.order-cell,
.time-cell {
  line-height: 1.7;
}

.ear-tag-label {
  display: block;
  margin-bottom: 4px;
  color: #7b8b7f;
  font-size: 12px;
}

.ear-tag-value {
  color: #17311f;
  font-size: 18px;
  font-weight: 700;
}

.primary-text {
  margin: 0;
  color: #1f2937;
  font-size: 13px;
  font-weight: 600;
  word-break: break-all;
}

.secondary-text {
  margin: 0;
  color: #7b8b7f;
  font-size: 12px;
  word-break: break-word;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.instance-table :deep(.ant-table-thead > tr > th) {
  color: #445449;
  font-weight: 600;
  background: #f7faf8;
}

.instance-table :deep(.ant-table-tbody > tr:hover > td) {
  background: #fbfdfb;
}

@media (max-width: 992px) {
  .summary-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .page-header,
  .toolbar-card,
  .table-card {
    padding-left: 16px;
    padding-right: 16px;
  }

  .page-header {
    flex-direction: column;
    align-items: stretch;
  }

  .page-title {
    font-size: 24px;
  }

  .filter-actions {
    margin-left: 0;
  }

  .project-cell {
    align-items: flex-start;
  }
}
</style>
