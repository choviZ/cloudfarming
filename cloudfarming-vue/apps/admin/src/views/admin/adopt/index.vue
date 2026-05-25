<template>
  <div class="admin-page adopt-management">
    <a-card :bordered="false" class="admin-card">
      <a-form class="admin-search-form" layout="inline" :model="searchForm">
        <a-form-item label="标题">
          <a-input
            v-model:value="searchForm.title"
            placeholder="请输入标题关键词"
            allow-clear
            @pressEnter="handleSearch"
          />
        </a-form-item>
        <a-form-item label="动物分类">
          <a-select
            v-model:value="searchForm.animalCategory"
            placeholder="请选择动物分类"
            allow-clear
            style="width: 150px"
          >
            <a-select-option v-for="cat in animalCategories" :key="cat.code" :value="cat.code">
              {{ cat.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="审核状态">
          <a-select
            v-model:value="searchForm.reviewStatus"
            placeholder="请选择审核状态"
            allow-clear
            style="width: 150px"
          >
            <a-select-option :value="0">待审核</a-select-option>
            <a-select-option :value="1">已通过</a-select-option>
            <a-select-option :value="2">未通过</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="上架状态">
          <a-select
            v-model:value="searchForm.status"
            placeholder="请选择上架状态"
            allow-clear
            style="width: 150px"
          >
            <a-select-option :value="1">上架</a-select-option>
            <a-select-option :value="0">下架</a-select-option>
          </a-select>
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
      </a-form>
    </a-card>

    <a-card :bordered="false" class="admin-card admin-table-card">
      <a-table
        :columns="columns"
        :data-source="adoptList"
        :loading="loading"
        :pagination="false"
        row-key="id"
        :scroll="{ x: 1500 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'animalCategory'">
            {{ getCategoryName(record.animalCategory) }}
          </template>
          <template v-else-if="column.key === 'price'">
            ¥{{ record.price }}
          </template>
          <template v-else-if="column.key === 'stock'">
            {{ record.availableCount }} / {{ record.totalCount }}
          </template>
          <template v-else-if="column.key === 'coverImage'">
            <a-image v-if="record.coverImage" :src="record.coverImage" :width="56" />
            <span v-else>-</span>
          </template>
          <template v-else-if="column.key === 'auditStatus'">
            <a-tag :color="getAuditStatusColor(record.auditStatus)">
              {{ getAuditStatusText(record.auditStatus) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'green' : 'default'">
              {{ record.status === 1 ? '上架' : '下架' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'createTime'">
            {{ formatTime(record.createTime) }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <template v-if="record.auditStatus === 0">
                <a-popconfirm
                  title="确认审核通过？"
                  ok-text="确认"
                  cancel-text="取消"
                  @confirm="handleApprove(record)"
                >
                  <a-button type="link" size="small">
                    通过
                  </a-button>
                </a-popconfirm>
                <a-button type="link" size="small" danger @click="showRejectModal(record)">
                  拒绝
                </a-button>
              </template>
              <template v-if="record.auditStatus === 1">
                <a-popconfirm
                  v-if="record.status === 0"
                  title="确认上架？"
                  ok-text="确认"
                  cancel-text="取消"
                  @confirm="handleOnShelf(record)"
                >
                  <a-button type="link" size="small">
                    上架
                  </a-button>
                </a-popconfirm>
                <a-popconfirm
                  v-else
                  title="确认下架？"
                  ok-text="确认"
                  cancel-text="取消"
                  @confirm="handleOffShelf(record)"
                >
                  <a-button type="link" size="small" danger>
                    下架
                  </a-button>
                </a-popconfirm>
              </template>
              <a-popconfirm
                title="确认删除该认养项目？"
                ok-text="确认"
                cancel-text="取消"
                @confirm="handleDelete(record)"
              >
                <a-button type="link" size="small" danger>
                  删除
                </a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>

      <div class="admin-pagination">
        <a-pagination
          v-model:current="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :show-total="(total) => `共 ${total} 条`"
          :show-size-changer="true"
          :show-quick-jumper="true"
          @change="handlePageChange"
        />
      </div>
    </a-card>

    <a-modal
      v-model:open="rejectModalVisible"
      title="审核拒绝"
      @ok="handleReject"
      @cancel="rejectModalVisible = false"
    >
      <a-form :model="rejectForm">
        <a-form-item label="拒绝原因" required>
          <a-textarea
            v-model:value="rejectForm.rejectReason"
            placeholder="请输入拒绝原因"
            :rows="4"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import { ReloadOutlined, SearchOutlined } from '@ant-design/icons-vue'
import { pageAdoptItems, onShelfAdoptItem, offShelfAdoptItem, deleteAdoptItem, listAnimalCategories } from '@/api/adopt'
import { getLatestAuditRecord, approveAudit, rejectAudit } from '@/api/audit'

const loading = ref(false)
const adoptList = ref([])
const animalCategories = ref([])

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const searchForm = reactive({
  title: undefined,
  animalCategory: undefined,
  reviewStatus: undefined,
  status: undefined
})

const rejectModalVisible = ref(false)
const rejectForm = reactive({
  auditId: null,
  rejectReason: ''
})

const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
  { title: '标题', dataIndex: 'title', key: 'title', width: 180, ellipsis: true },
  { title: '封面', dataIndex: 'coverImage', key: 'coverImage', width: 80 },
  { title: '动物分类', dataIndex: 'animalCategory', key: 'animalCategory', width: 100 },
  { title: '认养天数', dataIndex: 'adoptDays', key: 'adoptDays', width: 90 },
  { title: '价格', dataIndex: 'price', key: 'price', width: 100 },
  { title: '可认养/总数', key: 'stock', width: 110 },
  { title: '审核状态', dataIndex: 'auditStatus', key: 'auditStatus', width: 100 },
  { title: '上架状态', dataIndex: 'status', key: 'status', width: 90 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 170 },
  { title: '操作', key: 'action', width: 180, fixed: 'right' }
]

const fetchAnimalCategories = async () => {
  try {
    const res = await listAnimalCategories()
    if (res.code === '0' && res.data) {
      animalCategories.value = res.data
    }
  } catch (e) {
    console.error('加载动物分类失败', e)
  }
}

const fetchAdoptList = async () => {
  loading.value = true
  try {
    const res = await pageAdoptItems({
      current: pagination.current,
      size: pagination.size,
      title: searchForm.title || undefined,
      animalCategory: searchForm.animalCategory || undefined,
      reviewStatus: searchForm.reviewStatus,
      status: searchForm.status
    })
    if (res.code === '0' && res.data) {
      adoptList.value = res.data.records || []
      pagination.total = Number(res.data.total || 0)
      return
    }
    message.error(res.message || '加载认养列表失败')
  } catch (error) {
    console.error('加载认养列表失败', error)
    message.error('加载认养列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchAdoptList()
}

const handleReset = () => {
  searchForm.title = undefined
  searchForm.animalCategory = undefined
  searchForm.reviewStatus = undefined
  searchForm.status = undefined
  pagination.current = 1
  fetchAdoptList()
}

const handlePageChange = (page, pageSize) => {
  pagination.current = page
  pagination.size = pageSize
  fetchAdoptList()
}

const handleApprove = async (record) => {
  try {
    const auditRes = await getLatestAuditRecord({ bizType: 0, bizId: record.id })
    if (auditRes.code !== '0' || !auditRes.data) {
      message.error('获取审核记录失败')
      return
    }
    const res = await approveAudit(auditRes.data.id)
    if (res.code === '0') {
      message.success('审核通过')
      fetchAdoptList()
    } else {
      message.error(res.message || '操作失败')
    }
  } catch (e) {
    message.error('操作失败')
  }
}

const showRejectModal = async (record) => {
  try {
    const auditRes = await getLatestAuditRecord({ bizType: 0, bizId: record.id })
    if (auditRes.code !== '0' || !auditRes.data) {
      message.error('获取审核记录失败')
      return
    }
    rejectForm.auditId = auditRes.data.id
    rejectForm.rejectReason = ''
    rejectModalVisible.value = true
  } catch (e) {
    message.error('获取审核记录失败')
  }
}

const handleReject = async () => {
  if (!rejectForm.rejectReason?.trim()) {
    message.warning('请输入拒绝原因')
    return
  }
  try {
    const res = await rejectAudit(rejectForm.auditId, rejectForm.rejectReason)
    if (res.code === '0') {
      message.success('已拒绝')
      rejectModalVisible.value = false
      fetchAdoptList()
    } else {
      message.error(res.message || '操作失败')
    }
  } catch (e) {
    message.error('操作失败')
  }
}

const handleOnShelf = async (record) => {
  try {
    const res = await onShelfAdoptItem(record.id)
    if (res.code === '0') {
      message.success('上架成功')
      fetchAdoptList()
    } else {
      message.error(res.message || '上架失败')
    }
  } catch (e) {
    message.error('上架失败')
  }
}

const handleOffShelf = async (record) => {
  try {
    const res = await offShelfAdoptItem(record.id)
    if (res.code === '0') {
      message.success('下架成功')
      fetchAdoptList()
    } else {
      message.error(res.message || '下架失败')
    }
  } catch (e) {
    message.error('下架失败')
  }
}

const handleDelete = async (record) => {
  try {
    const res = await deleteAdoptItem(record.id)
    if (res.code === '0') {
      message.success('删除成功')
      fetchAdoptList()
    } else {
      message.error(res.message || '删除失败')
    }
  } catch (e) {
    message.error('删除失败')
  }
}

const getCategoryName = (code) => {
  const cat = animalCategories.value.find(c => c.code === code)
  return cat ? cat.name : code
}

const getAuditStatusText = (status) => {
  const map = { 0: '待审核', 1: '已通过', 2: '未通过' }
  return map[status] || '未知'
}

const getAuditStatusColor = (status) => {
  const map = { 0: 'processing', 1: 'success', 2: 'error' }
  return map[status] || 'default'
}

const formatTime = (time) => {
  return time ? dayjs(time).format('YYYY-MM-DD HH:mm:ss') : '-'
}

onMounted(() => {
  fetchAnimalCategories()
  fetchAdoptList()
})
</script>
