<template>
  <div class="article-page">
    <a-card :bordered="false" class="search-card">
      <a-form layout="inline" :model="queryParams">
        <a-form-item label="标题">
          <a-input
            v-model:value="queryParams.title"
            allow-clear
            placeholder="请输入标题关键词"
            @pressEnter="handleSearch"
          />
        </a-form-item>
        <a-form-item label="类型">
          <a-select
            v-model:value="queryParams.articleType"
            allow-clear
            placeholder="请选择类型"
            style="width: 160px"
            :options="articleTypeOptions"
          />
        </a-form-item>
        <a-form-item label="状态">
          <a-select
            v-model:value="queryParams.status"
            allow-clear
            placeholder="请选择状态"
            style="width: 140px"
            :options="statusOptions"
          />
        </a-form-item>
        <a-form-item label="置顶">
          <a-select
            v-model:value="queryParams.isTop"
            allow-clear
            placeholder="请选择"
            style="width: 140px"
            :options="topOptions"
          />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">查询</a-button>
            <a-button @click="handleReset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <div class="action-bar">
      <a-button type="primary" @click="handleCreate">发布文章</a-button>
    </div>

    <a-card :bordered="false">
      <a-table
        :columns="columns"
        :data-source="articleList"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'title'">
            <div class="title-cell">
              <div class="title-cell__title">{{ record.title }}</div>
              <div class="title-cell__summary">{{ record.summary || '暂无摘要' }}</div>
            </div>
          </template>

          <template v-else-if="column.key === 'articleType'">
            <a-tag color="processing">{{ record.articleTypeDesc || getArticleTypeText(record.articleType) }}</a-tag>
          </template>

          <template v-else-if="column.key === 'isTop'">
            <a-tag :color="record.isTop ? 'gold' : 'default'">
              {{ record.isTop ? '置顶' : '普通' }}
            </a-tag>
          </template>

          <template v-else-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'success' : 'default'">
              {{ record.statusDesc || getStatusText(record.status) }}
            </a-tag>
          </template>

          <template v-else-if="column.key === 'publishTime'">
            {{ formatDateTime(record.publishTime) }}
          </template>

          <template v-else-if="column.key === 'updateTime'">
            {{ formatDateTime(record.updateTime) }}
          </template>

          <template v-else-if="column.key === 'action'">
            <a-space wrap>
              <a-button type="link" size="small" @click="handleEdit(record)">编辑</a-button>

              <a-popconfirm
                v-if="record.status === 1"
                title="确认下线该文章吗？"
                ok-text="确认"
                cancel-text="取消"
                @confirm="handleStatusChange(record, 0)"
              >
                <a-button type="link" size="small">下线</a-button>
              </a-popconfirm>

              <a-popconfirm
                v-else
                title="确认发布该文章吗？"
                ok-text="确认"
                cancel-text="取消"
                @confirm="handleStatusChange(record, 1)"
              >
                <a-button type="link" size="small">发布</a-button>
              </a-popconfirm>

              <a-popconfirm
                title="删除后不可恢复，确认删除吗？"
                ok-text="确认"
                cancel-text="取消"
                @confirm="handleDelete(record)"
              >
                <a-button type="link" size="small" danger>删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <ArticleModal
      v-model:open="modalState.open"
      :mode="modalState.mode"
      :initial-data="modalState.initialData"
      @success="handleModalSuccess"
    />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import ArticleModal from './components/ArticleModal.vue'
import {
  deleteArticle,
  getManageArticleDetail,
  pageManageArticles,
  updateArticleStatus
} from '@/api/article'

const loading = ref(false)
const articleList = ref([])

const queryParams = reactive({
  title: '',
  articleType: undefined,
  status: undefined,
  isTop: undefined
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total) => `共 ${total} 条`
})

const modalState = reactive({
  open: false,
  mode: 'create',
  initialData: null
})

const articleTypeOptions = [
  { label: '平台公告', value: 1 },
  { label: '农业政策', value: 2 },
  { label: '养殖知识', value: 3 }
]

const statusOptions = [
  { label: '已发布', value: 1 },
  { label: '已下线', value: 0 }
]

const topOptions = [
  { label: '置顶', value: true },
  { label: '普通', value: false }
]

const columns = [
  {
    title: '标题 / 摘要',
    dataIndex: 'title',
    key: 'title',
    ellipsis: true
  },
  {
    title: '类型',
    dataIndex: 'articleType',
    key: 'articleType',
    width: 120
  },
  {
    title: '置顶',
    dataIndex: 'isTop',
    key: 'isTop',
    width: 100
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 100
  },
  {
    title: '发布时间',
    dataIndex: 'publishTime',
    key: 'publishTime',
    width: 180
  },
  {
    title: '更新时间',
    dataIndex: 'updateTime',
    key: 'updateTime',
    width: 180
  },
  {
    title: '操作',
    key: 'action',
    width: 220
  }
]

const fetchArticleList = async () => {
  loading.value = true
  try {
    const res = await pageManageArticles({
      current: pagination.current,
      size: pagination.pageSize,
      title: queryParams.title || undefined,
      articleType: queryParams.articleType,
      status: queryParams.status,
      isTop: queryParams.isTop
    })
    if (res.code === '0' && res.data) {
      articleList.value = res.data.records || []
      pagination.total = Number(res.data.total || 0)
      return
    }
    articleList.value = []
    pagination.total = 0
    message.error(res.message || '获取文章列表失败')
  } catch (error) {
    articleList.value = []
    pagination.total = 0
    message.error('获取文章列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchArticleList()
}

const handleReset = () => {
  queryParams.title = ''
  queryParams.articleType = undefined
  queryParams.status = undefined
  queryParams.isTop = undefined
  handleSearch()
}

const handleTableChange = (pager) => {
  pagination.current = pager.current
  pagination.pageSize = pager.pageSize
  fetchArticleList()
}

const handleCreate = () => {
  modalState.mode = 'create'
  modalState.initialData = null
  modalState.open = true
}

const handleEdit = async (record) => {
  try {
    const res = await getManageArticleDetail(record.id)
    if (res.code !== '0' || !res.data) {
      message.error(res.message || '获取文章详情失败')
      return
    }
    modalState.mode = 'edit'
    modalState.initialData = res.data
    modalState.open = true
  } catch (error) {
    message.error('获取文章详情失败')
  }
}

const handleStatusChange = async (record, status) => {
  try {
    const res = await updateArticleStatus({
      id: record.id,
      status
    })
    if (res.code === '0') {
      message.success(status === 1 ? '文章发布成功' : '文章下线成功')
      fetchArticleList()
      return
    }
    message.error(res.message || '状态更新失败')
  } catch (error) {
    message.error('状态更新失败')
  }
}

const handleDelete = async (record) => {
  try {
    const res = await deleteArticle(record.id)
    if (res.code === '0') {
      message.success('文章删除成功')
      if (articleList.value.length === 1 && pagination.current > 1) {
        pagination.current -= 1
      }
      fetchArticleList()
      return
    }
    message.error(res.message || '文章删除失败')
  } catch (error) {
    message.error('文章删除失败')
  }
}

const handleModalSuccess = () => {
  pagination.current = 1
  fetchArticleList()
}

const formatDateTime = (value) => {
  if (!value) {
    return '-'
  }
  return dayjs(value).format('YYYY-MM-DD HH:mm:ss')
}

const getArticleTypeText = (value) => {
  const option = articleTypeOptions.find((item) => item.value === value)
  return option?.label || '-'
}

const getStatusText = (value) => {
  const option = statusOptions.find((item) => item.value === value)
  return option?.label || '-'
}

onMounted(() => {
  fetchArticleList()
})
</script>

<style scoped>
.article-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.action-bar {
  display: flex;
  justify-content: flex-end;
}

.title-cell__title {
  font-weight: 600;
  color: #1f1f1f;
}

.title-cell__summary {
  margin-top: 6px;
  color: #666;
  font-size: 13px;
  line-height: 1.6;
}
</style>
