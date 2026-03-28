<template>
  <div class="feedback-management">
    <a-card class="search-card" :bordered="false">
      <a-form layout="horizontal" :model="searchForm" class="search-form">
        <a-space :size="16" wrap>
          <a-form-item label="反馈ID">
            <a-input
              v-model:value="searchForm.id"
              placeholder="请输入反馈ID"
              allow-clear
              @pressEnter="handleSearch"
            />
          </a-form-item>
          <a-form-item label="问题类型">
            <a-select
              v-model:value="searchForm.feedbackType"
              placeholder="请选择问题类型"
              allow-clear
              style="width: 180px"
              :options="typeOptions"
            />
          </a-form-item>
          <a-form-item label="提交身份">
            <a-select
              v-model:value="searchForm.submitterType"
              placeholder="请选择提交身份"
              allow-clear
              style="width: 150px"
              :options="submitterTypeOptions"
            />
          </a-form-item>
          <a-form-item label="处理状态">
            <a-select
              v-model:value="searchForm.status"
              placeholder="请选择处理状态"
              allow-clear
              style="width: 150px"
              :options="statusOptions"
            />
          </a-form-item>
          <a-form-item label="关键词">
            <a-input
              v-model:value="searchForm.keyword"
              placeholder="请输入反馈内容关键词"
              allow-clear
              @pressEnter="handleSearch"
            />
          </a-form-item>
          <a-form-item>
            <a-space>
              <a-button type="primary" @click="handleSearch">
                <template #icon>
                  <SearchOutlined />
                </template>
                查询
              </a-button>
              <a-button @click="handleReset">
                <template #icon>
                  <ReloadOutlined />
                </template>
                重置
              </a-button>
            </a-space>
          </a-form-item>
        </a-space>
      </a-form>
    </a-card>

    <a-card :bordered="false" style="margin-top: 16px">
      <a-table
        :columns="columns"
        :data-source="feedbackList"
        :loading="loading"
        :pagination="false"
        row-key="id"
        :scroll="{ x: 'max-content' }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'submitterInfo'">
            <div class="submitter-cell">
              <div class="submitter-name">{{ record.username || `用户${record.userId}` }}</div>
              <div class="submitter-meta">
                <a-tag color="blue">{{ record.submitterTypeName || getSubmitterTypeText(record.submitterType) }}</a-tag>
                <span>ID：{{ record.userId }}</span>
              </div>
            </div>
          </template>
          <template v-else-if="column.key === 'feedbackType'">
            <a-tag color="green">{{ record.feedbackTypeName || record.feedbackType }}</a-tag>
          </template>
          <template v-else-if="column.key === 'content'">
            <div class="content-cell">
              {{ record.content }}
            </div>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'success' : 'orange'">
              {{ record.statusName || getStatusText(record.status) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'createTime'">
            {{ formatTime(record.createTime) }}
          </template>
          <template v-else-if="column.key === 'handleTime'">
            {{ record.handleTime ? formatTime(record.handleTime) : '未处理' }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-button type="link" size="small" @click="openProcessModal(record)">
              {{ record.status === 1 ? '查看/更新' : '处理' }}
            </a-button>
          </template>
        </template>
      </a-table>

      <div class="pagination">
        <a-pagination
          v-model:current="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :show-total="(total) => `共 ${total} 条`"
          show-size-changer
          show-quick-jumper
          @change="handlePageChange"
        />
      </div>
    </a-card>

    <a-modal
      v-model:open="processModalVisible"
      :title="currentRecord?.status === 1 ? '查看/更新处理结果' : '处理反馈'"
      :confirm-loading="processing"
      width="760px"
      ok-text="提交处理结果"
      cancel-text="取消"
      @ok="handleProcessSubmit"
    >
      <div v-if="currentRecord" class="process-panel">
        <div class="detail-grid">
          <div class="detail-item">
            <span class="detail-label">反馈ID</span>
            <span>{{ currentRecord.id }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">提交用户</span>
            <span>{{ currentRecord.username || `用户${currentRecord.userId}` }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">提交身份</span>
            <span>{{ currentRecord.submitterTypeName || getSubmitterTypeText(currentRecord.submitterType) }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">联系电话</span>
            <span>{{ currentRecord.contactPhone }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">问题类型</span>
            <span>{{ currentRecord.feedbackTypeName || currentRecord.feedbackType }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">提交时间</span>
            <span>{{ formatTime(currentRecord.createTime) }}</span>
          </div>
        </div>

        <div class="detail-block">
          <div class="detail-label">反馈内容</div>
          <div class="detail-content">{{ currentRecord.content }}</div>
        </div>

        <div v-if="currentRecord.handleTime || currentRecord.handlerName" class="detail-grid detail-grid--history">
          <div class="detail-item">
            <span class="detail-label">当前处理人</span>
            <span>{{ currentRecord.handlerName || '-' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">处理时间</span>
            <span>{{ currentRecord.handleTime ? formatTime(currentRecord.handleTime) : '-' }}</span>
          </div>
        </div>

        <a-form ref="processFormRef" layout="vertical" :model="processForm" :rules="processRules">
          <a-form-item label="处理回复" name="replyContent">
            <a-textarea
              v-model:value="processForm.replyContent"
              :rows="6"
              :maxlength="500"
              show-count
              placeholder="请输入处理结果、跟进说明或平台回复内容"
            />
          </a-form-item>
        </a-form>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { ReloadOutlined, SearchOutlined } from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import { getFeedbackPage, getFeedbackTypes, processFeedback } from '@/api/feedback'

const fallbackTypeOptions = [
  { value: 'PRODUCT', label: '商品问题' },
  { value: 'ADOPT', label: '认养项目问题' },
  { value: 'ORDER', label: '订单/支付' },
  { value: 'LOGISTICS', label: '物流/售后' },
  { value: 'SHOP_SERVICE', label: '店铺/服务' },
  { value: 'ACCOUNT', label: '账号/登录' },
  { value: 'COMPLAINT', label: '投诉举报' },
  { value: 'SUGGESTION', label: '功能建议' },
  { value: 'OTHER', label: '其他' }
]

const submitterTypeOptions = [
  { value: 0, label: '普通用户' },
  { value: 1, label: '农户' }
]

const statusOptions = [
  { value: 0, label: '待处理' },
  { value: 1, label: '已处理' }
]

const columns = [
  { title: '反馈ID', dataIndex: 'id', key: 'id', width: 110 },
  { title: '提交信息', key: 'submitterInfo', width: 220 },
  { title: '问题类型', dataIndex: 'feedbackType', key: 'feedbackType', width: 150 },
  { title: '联系电话', dataIndex: 'contactPhone', key: 'contactPhone', width: 140 },
  { title: '反馈内容', dataIndex: 'content', key: 'content', width: 360 },
  { title: '处理状态', dataIndex: 'status', key: 'status', width: 110 },
  { title: '提交时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '处理时间', dataIndex: 'handleTime', key: 'handleTime', width: 180 },
  { title: '操作', key: 'action', width: 120, fixed: 'right' }
]

const searchForm = reactive({
  id: '',
  feedbackType: undefined,
  submitterType: undefined,
  status: undefined,
  keyword: ''
})

const loading = ref(false)
const processing = ref(false)
const feedbackList = ref([])
const typeOptions = ref([...fallbackTypeOptions])
const processModalVisible = ref(false)
const currentRecord = ref(null)
const processFormRef = ref()

const processForm = reactive({
  replyContent: ''
})

const processRules = {
  replyContent: [
    { required: true, message: '请输入处理回复', trigger: 'blur' },
    { min: 4, message: '处理回复至少 4 个字', trigger: 'blur' }
  ]
}

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const loadTypeOptions = async () => {
  try {
    const response = await getFeedbackTypes()
    if (response.code === '0' && Array.isArray(response.data) && response.data.length) {
      typeOptions.value = response.data.map((item) => ({
        value: item.code,
        label: item.name
      }))
    }
  } catch (error) {
    console.log(error)
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size
    }

    if (searchForm.id !== '') {
      params.id = Number(searchForm.id)
    }
    if (searchForm.feedbackType !== undefined) {
      params.feedbackType = searchForm.feedbackType
    }
    if (searchForm.submitterType !== undefined) {
      params.submitterType = searchForm.submitterType
    }
    if (searchForm.status !== undefined) {
      params.status = searchForm.status
    }
    if (searchForm.keyword) {
      params.keyword = searchForm.keyword
    }

    const response = await getFeedbackPage(params)
    if (response.code === '0' && response.data) {
      feedbackList.value = response.data.records || []
      pagination.total = Number(response.data.total) || 0
      return
    }
    message.error(response.message || '获取反馈列表失败')
  } catch (error) {
    message.error('获取反馈列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchData()
}

const handleReset = () => {
  searchForm.id = ''
  searchForm.feedbackType = undefined
  searchForm.submitterType = undefined
  searchForm.status = undefined
  searchForm.keyword = ''
  handleSearch()
}

const handlePageChange = (page, pageSize) => {
  pagination.current = page
  pagination.size = pageSize
  fetchData()
}

const openProcessModal = (record) => {
  currentRecord.value = { ...record }
  processForm.replyContent = record.replyContent || ''
  processModalVisible.value = true
}

const handleProcessSubmit = async () => {
  try {
    await processFormRef.value?.validate()
  } catch (error) {
    return
  }

  processing.value = true
  try {
    const response = await processFeedback({
      id: currentRecord.value.id,
      replyContent: processForm.replyContent
    })
    if (response.code === '0') {
      message.success('处理结果已提交')
      processModalVisible.value = false
      currentRecord.value = null
      await fetchData()
      return
    }
    message.error(response.message || '提交处理结果失败')
  } catch (error) {
    message.error('提交处理结果失败，请稍后重试')
  } finally {
    processing.value = false
  }
}

const getSubmitterTypeText = (value) => {
  return value === 1 ? '农户' : '普通用户'
}

const getStatusText = (value) => {
  return value === 1 ? '已处理' : '待处理'
}

const formatTime = (value) => {
  return value ? dayjs(value).format('YYYY-MM-DD HH:mm:ss') : '-'
}

onMounted(async () => {
  await loadTypeOptions()
  await fetchData()
})
</script>

<style scoped>
.feedback-management {
  padding: 24px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
}

.submitter-cell {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.submitter-name {
  color: #1f2937;
  font-weight: 600;
}

.submitter-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  color: #6b7280;
  font-size: 12px;
}

.content-cell {
  max-width: 360px;
  color: #374151;
  line-height: 1.7;
  word-break: break-all;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.pagination {
  margin-top: 16px;
  text-align: right;
}

.process-panel {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px 18px;
}

.detail-grid--history {
  padding: 16px;
  border-radius: 14px;
  background: #fafafa;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}

.detail-label {
  color: #6b7280;
  font-size: 12px;
}

.detail-block {
  padding: 16px;
  border-radius: 14px;
  background: #fafafa;
}

.detail-content {
  margin-top: 8px;
  color: #1f2937;
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-word;
}

@media (max-width: 768px) {
  .feedback-management {
    padding: 16px;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>
