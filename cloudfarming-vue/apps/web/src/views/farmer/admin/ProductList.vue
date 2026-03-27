<template>
  <div class="product-list-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">我的商品</h2>
        <p class="page-desc">
          管理当前农户账号发布的商品，支持查询、上下架和修改基础信息。
        </p>
      </div>
      <a-button type="primary" @click="router.push('/farmer/spu/create')">
        创建商品
      </a-button>
    </div>

    <a-card :bordered="false" class="search-card">
      <a-form layout="inline" :model="searchForm" @finish="handleSearch">
        <a-form-item label="商品名称">
          <a-input
            v-model:value="searchForm.spuName"
            placeholder="请输入商品名称"
            allow-clear
            style="width: 220px"
          />
        </a-form-item>
        <a-form-item label="上架状态">
          <a-select
            v-model:value="searchForm.status"
            allow-clear
            placeholder="全部"
            style="width: 140px"
          >
            <a-select-option :value="1">已上架</a-select-option>
            <a-select-option :value="0">已下架</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="审核状态">
          <a-select
            v-model:value="searchForm.auditStatus"
            allow-clear
            placeholder="全部"
            style="width: 140px"
          >
            <a-select-option :value="0">待审核</a-select-option>
            <a-select-option :value="1">已通过</a-select-option>
            <a-select-option :value="2">已驳回</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item class="search-actions">
          <a-space>
            <a-button type="primary" html-type="submit">查询</a-button>
            <a-button @click="handleReset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card :bordered="false" class="table-card">
      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'images'">
            <a-image
              :src="getCoverImage(record.images)"
              :width="64"
              :height="64"
              class="cover-image"
              fallback="https://placeholder.com/64x64"
            />
          </template>

          <template v-else-if="column.key === 'categoryId'">
            {{ categoryNameMap[record.categoryId] || `分类 #${record.categoryId}` }}
          </template>

          <template v-else-if="column.key === 'minPrice'">
            {{ formatPrice(record.minPrice) }}
          </template>

          <template v-else-if="column.key === 'auditStatus'">
            <a-tag :color="auditStatusColorMap[record.auditStatus] || 'default'">
              {{ auditStatusTextMap[record.auditStatus] || '未知' }}
            </a-tag>
          </template>

          <template v-else-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'blue' : 'default'">
              {{ record.status === 1 ? '已上架' : '已下架' }}
            </a-tag>
          </template>

          <template v-else-if="column.key === 'createTime'">
            {{ formatDateTime(record.createTime) }}
          </template>

          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleEdit(record)">
                修改
              </a-button>

              <a-popconfirm
                v-if="record.auditStatus === 1 && record.status === 0"
                title="确认上架该商品吗？"
                ok-text="确认"
                cancel-text="取消"
                @confirm="handleToggleStatus(record, 1)"
              >
                <a-button type="link" size="small">上架</a-button>
              </a-popconfirm>

              <a-popconfirm
                v-if="record.auditStatus === 1 && record.status === 1"
                title="确认下架该商品吗？"
                ok-text="确认"
                cancel-text="取消"
                @confirm="handleToggleStatus(record, 0)"
              >
                <a-button type="link" danger size="small">下架</a-button>
              </a-popconfirm>

              <span v-if="record.auditStatus !== 1" class="muted-text">
                {{ record.auditStatus === 0 ? '审核中' : '修改后需重新提交审核' }}
              </span>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:open="editVisible"
      title="修改商品"
      ok-text="保存修改"
      cancel-text="取消"
      :confirm-loading="editSubmitting"
      destroy-on-close
      @ok="handleEditSubmit"
    >
      <a-form layout="vertical">
        <a-form-item label="商品分类" required>
          <CategorySelect v-model="editForm.categoryId" />
        </a-form-item>
        <a-form-item label="商品名称" required>
          <a-input
            v-model:value="editForm.title"
            placeholder="请输入商品名称"
            :maxlength="50"
            show-count
          />
        </a-form-item>
        <a-form-item label="商品主图">
          <ImageUpload v-model:value="editForm.images" biz-code="PRODUCT_SPU_COVER" />
        </a-form-item>
        <div class="edit-tip">
          只有在保存了实际修改后，商品才会自动下架并重新进入审核流程。
        </div>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { createVNode, onMounted, reactive, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { ExclamationCircleOutlined } from '@ant-design/icons-vue'
import { useRouter } from 'vue-router'
import { getCategoryTree } from '@/api/category'
import { listMySpuByPage, updateSpu, updateSpuStatus } from '@/api/spu'
import CategorySelect from './components/CategorySelect.vue'
import ImageUpload from '@/components/Upload/ImageUpload.vue'

const router = useRouter()

const loading = ref(false)
const editVisible = ref(false)
const editSubmitting = ref(false)
const dataSource = ref([])
const categoryNameMap = ref({})
const editSnapshot = ref(null)

const searchForm = reactive({
  spuName: '',
  status: undefined,
  auditStatus: undefined
})

const editForm = reactive({
  id: undefined,
  title: '',
  categoryId: '',
  images: ''
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total) => `共 ${total} 条`
})

const auditStatusTextMap = {
  0: '待审核',
  1: '已通过',
  2: '已驳回'
}

const auditStatusColorMap = {
  0: 'orange',
  1: 'green',
  2: 'red'
}

const columns = [
  {
    title: '主图',
    dataIndex: 'images',
    key: 'images',
    width: 96
  },
  {
    title: '商品名称',
    dataIndex: 'title',
    key: 'title',
    ellipsis: true
  },
  {
    title: '商品分类',
    dataIndex: 'categoryId',
    key: 'categoryId',
    width: 160
  },
  {
    title: '最低价',
    dataIndex: 'minPrice',
    key: 'minPrice',
    width: 120
  },
  {
    title: '审核状态',
    dataIndex: 'auditStatus',
    key: 'auditStatus',
    width: 120
  },
  {
    title: '上架状态',
    dataIndex: 'status',
    key: 'status',
    width: 120
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 180
  },
  {
    title: '操作',
    key: 'action',
    width: 220,
    fixed: 'right'
  }
]

const normalizeEditValue = (value) => String(value ?? '').trim()

const createEditSnapshot = (payload) => ({
  title: normalizeEditValue(payload.title),
  categoryId: payload.categoryId ? String(payload.categoryId) : '',
  images: normalizeEditValue(payload.images)
})

const hasEditChanged = () => {
  if (!editSnapshot.value) {
    return true
  }

  const currentSnapshot = createEditSnapshot(editForm)
  return (
    currentSnapshot.title !== editSnapshot.value.title ||
    currentSnapshot.categoryId !== editSnapshot.value.categoryId ||
    currentSnapshot.images !== editSnapshot.value.images
  )
}

const fetchCategoryTree = async () => {
  try {
    const res = await getCategoryTree()
    const map = {}
    traverseCategories(res?.data || [], map)
    categoryNameMap.value = map
  } catch (error) {
    console.error('加载分类树失败', error)
  }
}

const traverseCategories = (nodes, map) => {
  nodes.forEach((node) => {
    map[node.id] = node.name
    if (Array.isArray(node.children) && node.children.length > 0) {
      traverseCategories(node.children, map)
    }
  })
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await listMySpuByPage({
      current: pagination.current,
      size: pagination.pageSize,
      spuName: searchForm.spuName || undefined,
      status: searchForm.status,
      auditStatus: searchForm.auditStatus
    })

    if (res.code === '0' && res.data) {
      dataSource.value = res.data.records || []
      pagination.total = res.data.total || 0
      return
    }

    message.error(res.message || '加载商品列表失败')
  } catch (error) {
    console.error('加载商品列表失败', error)
    message.error('加载商品列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchData()
}

const handleReset = () => {
  searchForm.spuName = ''
  searchForm.status = undefined
  searchForm.auditStatus = undefined
  handleSearch()
}

const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchData()
}

const handleToggleStatus = async (record, status) => {
  try {
    const res = await updateSpuStatus(record.id, status)
    if (res.code === '0') {
      message.success(status === 1 ? '商品已上架' : '商品已下架')
      fetchData()
      return
    }

    message.error(res.message || '更新商品状态失败')
  } catch (error) {
    console.error('更新商品状态失败', error)
    message.error('更新商品状态失败')
  }
}

const openEditModal = (record) => {
  const coverImage = getCoverImage(record.images)
  editForm.id = record.id
  editForm.title = record.title || ''
  editForm.categoryId = record.categoryId ? String(record.categoryId) : ''
  editForm.images = coverImage
  editSnapshot.value = createEditSnapshot({
    title: record.title || '',
    categoryId: record.categoryId ? String(record.categoryId) : '',
    images: coverImage
  })
  editVisible.value = true
}

const handleEdit = (record) => {
  if (record.status === 1) {
    Modal.confirm({
      title: '确认修改商品吗？',
      icon: createVNode(ExclamationCircleOutlined),
      content: '只有在保存了实际修改后，商品才会自动下架并重新进入审核流程。',
      okText: '继续修改',
      cancelText: '取消',
      onOk: () => openEditModal(record)
    })
    return
  }

  openEditModal(record)
}

const handleEditSubmit = async () => {
  if (!editForm.categoryId) {
    message.warning('请选择商品分类')
    return
  }
  if (!editForm.title.trim()) {
    message.warning('请输入商品名称')
    return
  }
  if (!hasEditChanged()) {
    message.info('商品信息未发生变化，无需重新提交审核')
    return
  }

  editSubmitting.value = true
  try {
    const res = await updateSpu({
      id: editForm.id,
      title: editForm.title.trim(),
      categoryId: Number(editForm.categoryId),
      images: editForm.images
    })

    if (res.code === '0') {
      message.success('商品修改成功，已重新提交审核')
      editVisible.value = false
      fetchData()
      return
    }

    message.error(res.message || '修改商品失败')
  } catch (error) {
    console.error('修改商品失败', error)
    message.error('修改商品失败')
  } finally {
    editSubmitting.value = false
  }
}

const getCoverImage = (images) => {
  if (!images) return ''
  return String(images)
    .split(',')
    .map((item) => item.trim())
    .find(Boolean) || ''
}

const formatPrice = (price) => {
  if (price === null || price === undefined || price === '') {
    return '--'
  }
  const numericPrice = Number(price)
  return Number.isNaN(numericPrice) ? '--' : `￥${numericPrice.toFixed(2)}`
}

const formatDateTime = (value) => {
  if (!value) return '--'
  return String(value).replace('T', ' ')
}

onMounted(() => {
  fetchCategoryTree()
  fetchData()
})
</script>

<style scoped>
.product-list-page {
  max-width: 1280px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 24px;
}

.page-title {
  margin: 0 0 8px;
  font-size: 24px;
  font-weight: 600;
  color: #1f1f1f;
}

.page-desc {
  margin: 0;
  color: #8c8c8c;
  font-size: 14px;
}

.search-card {
  margin-bottom: 16px;
}

.search-actions {
  margin-left: auto;
}

.table-card {
  min-height: 420px;
}

:deep(.cover-image img) {
  object-fit: cover;
}

.muted-text {
  color: #999;
  font-size: 12px;
}

.edit-tip {
  margin-top: 8px;
  color: #8c8c8c;
  font-size: 12px;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: stretch;
  }

  .search-actions {
    margin-left: 0;
  }
}
</style>
