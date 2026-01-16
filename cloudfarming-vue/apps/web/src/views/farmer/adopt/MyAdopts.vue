<template>
  <div class="my-adopts-container">
    <div class="page-header">
      <h2 class="page-title">我的发布</h2>
      <span class="page-desc">管理您发布的认养项目</span>
    </div>

    <!-- 筛选区域 -->
    <a-card class="search-card" :bordered="false">
      <a-form layout="inline" :model="searchForm" @finish="onSearch" class="search-form">
        <a-form-item label="动物分类" name="animalCategory">
          <a-select
            v-model:value="searchForm.animalCategory"
            placeholder="请选择"
            style="width: 140px"
            allow-clear
          >
            <a-select-option
              v-for="cat in categories"
              :key="cat.code"
              :value="cat.code"
            >
              {{ cat.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        
        <a-form-item label="审核状态" name="reviewStatus">
          <a-select
            v-model:value="searchForm.reviewStatus"
            placeholder="请选择"
            style="width: 120px"
            allow-clear
          >
            <a-select-option :value="0">待审核</a-select-option>
            <a-select-option :value="1">审核通过</a-select-option>
            <a-select-option :value="2">审核拒绝</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="上架状态" name="status">
          <a-select
            v-model:value="searchForm.status"
            placeholder="请选择"
            style="width: 120px"
            allow-clear
          >
            <a-select-option :value="1">已上架</a-select-option>
            <a-select-option :value="0">已下架</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="标题" name="title">
          <a-input
            v-model:value="searchForm.title"
            placeholder="请输入标题关键字"
            allow-clear
            style="width: 180px"
          />
        </a-form-item>

        <a-form-item class="search-actions">
          <a-space>
            <a-button type="primary" html-type="submit">查询</a-button>
            <a-button @click="onReset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 列表区域 -->
    <a-card :bordered="false" class="table-card">
      <a-table
        :columns="columns"
        :data-source="dataSource"
        :pagination="pagination"
        :loading="loading"
        @change="handleTableChange"
        row-key="id"
      >
        <!-- 封面图片 -->
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'coverImage'">
            <div class="cover-image-wrapper">
              <a-image
                :width="60"
                :height="60"
                :src="record.coverImage"
                fallback="https://placeholder.com/60x60"
                class="cover-image"
              />
            </div>
          </template>

          <!-- 价格 -->
          <template v-else-if="column.key === 'price'">
            <span>¥{{ record.price.toFixed(2) }}</span>
          </template>

          <!-- 认养周期 -->
          <template v-else-if="column.key === 'adoptDays'">
            <span>{{ record.adoptDays }} 天</span>
          </template>
          
          <!-- 审核状态 -->
          <template v-else-if="column.key === 'reviewStatus'">
            <a-tag v-if="record.reviewStatus === 0" color="orange">待审核</a-tag>
            <a-tag v-else-if="record.reviewStatus === 1" color="green">通过</a-tag>
            <a-tooltip v-else-if="record.reviewStatus === 2" :title="record.reviewText">
               <a-tag color="red" style="cursor: help">拒绝 <info-circle-outlined /></a-tag>
            </a-tooltip>
          </template>

          <!-- 上架状态 -->
          <template v-else-if="column.key === 'status'">
            <a-tag v-if="record.status === 1" color="blue">已上架</a-tag>
            <a-tag v-else color="default">已下架</a-tag>
          </template>

          <!-- 操作 -->
          <template v-else-if="column.key === 'action'">
            <a-space>
              <!-- 审核通过：可编辑 -->
              <a-button 
                v-if="record.reviewStatus === 1" 
                type="link" 
                size="small"
                @click="handleEdit(record)"
              >
                编辑
              </a-button>

              <!-- 审核通过且已下架：可上架 -->
              <a-popconfirm
                v-if="record.reviewStatus === 1 && record.status === 0"
                title="确定要上架该项目吗？"
                @confirm="handleOnShelf(record)"
              >
                <a-button type="link" size="small">上架</a-button>
              </a-popconfirm>

              <!-- 审核通过且已上架：可下架 -->
              <a-popconfirm
                v-if="record.reviewStatus === 1 && record.status === 1"
                title="确定要下架该项目吗？"
                @confirm="handleOffShelf(record)"
              >
                <a-button type="link" danger size="small">下架</a-button>
              </a-popconfirm>

              <!-- 审核拒绝：可编辑重提 -->
              <a-button 
                v-if="record.reviewStatus === 2" 
                type="link" 
                size="small"
                @click="handleEdit(record)"
              >
                编辑
              </a-button>

              <!-- 审核拒绝：可删除 -->
              <a-popconfirm
                v-if="record.reviewStatus === 2"
                title="确定要删除该项目吗？"
                @confirm="handleDelete(record)"
              >
                <a-button type="link" danger size="small">删除</a-button>
              </a-popconfirm>

              <!-- 待审核：仅查看 (暂无详情页，先用文字占位或禁用按钮) -->
               <span v-if="record.reviewStatus === 0" style="color: #999; font-size: 12px">
                 审核中
               </span>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, createVNode } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { InfoCircleOutlined, ExclamationCircleOutlined } from '@ant-design/icons-vue'
import { useRouter } from 'vue-router'
import { 
  pageMyAdoptItems, 
  listAnimalCategories, 
  onShelfAdoptItem, 
  offShelfAdoptItem, 
  deleteAdoptItem 
} from '@cloudfarming/core'
import type { AdoptItemResp, AdoptItemPageReq, AnimalCategoryResp } from '@cloudfarming/core'

const router = useRouter()

// 状态定义
const loading = ref(false)
const dataSource = ref<AdoptItemResp[]>([])
const categories = ref<AnimalCategoryResp[]>([])

// 查询表单
const searchForm = reactive({
  animalCategory: undefined as string | undefined,
  reviewStatus: undefined as number | undefined,
  status: undefined as number | undefined,
  title: ''
})

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条`
})

// 表格列定义
const columns = [
  {
    title: '封面',
    dataIndex: 'coverImage',
    key: 'coverImage',
    width: 100
  },
  {
    title: '认养标题',
    dataIndex: 'title',
    key: 'title',
    ellipsis: true
  },
  {
    title: '动物分类',
    dataIndex: 'animalCategory', // 实际展示时可能需要映射名称，但这里先展示 code，或者需要从 categories 匹配
    key: 'animalCategory',
    customRender: ({ text }: { text: string }) => {
      const cat = categories.value.find(c => c.code === text)
      return cat ? cat.name : text
    }
  },
  {
    title: '认养周期',
    dataIndex: 'adoptDays',
    key: 'adoptDays',
    width: 120
  },
  {
    title: '价格',
    dataIndex: 'price',
    key: 'price',
    width: 120
  },
  {
    title: '审核状态',
    dataIndex: 'reviewStatus',
    key: 'reviewStatus',
    width: 120
  },
  {
    title: '上架状态',
    dataIndex: 'status',
    key: 'status',
    width: 100
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
    fixed: 'right'
  }
]

// 获取动物分类
const fetchCategories = async () => {
  try {
    const res = await listAnimalCategories()
    if (res.code === '0' && res.data) {
      categories.value = res.data
    }
  } catch (error) {
    console.error('获取分类失败', error)
  }
}

// 获取列表数据
const fetchData = async () => {
  loading.value = true
  try {
    const req: AdoptItemPageReq = {
      current: pagination.current,
      size: pagination.pageSize,
      animalCategory: searchForm.animalCategory,
      reviewStatus: searchForm.reviewStatus,
      status: searchForm.status,
      title: searchForm.title
    }
    
    const res = await pageMyAdoptItems(req)
    if (res.code === '0' && res.data) {
      dataSource.value = res.data.records
      pagination.total = res.data.total
    } else {
      message.error(res.message || '获取数据失败')
    }
  } catch (error) {
    console.error('查询异常', error)
    message.error('系统异常，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 查询操作
const onSearch = () => {
  pagination.current = 1
  fetchData()
}

// 重置操作
const onReset = () => {
  searchForm.animalCategory = undefined
  searchForm.reviewStatus = undefined
  searchForm.status = undefined
  searchForm.title = ''
  onSearch()
}

// 表格分页变化
const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchData()
}

// 上架
const handleOnShelf = async (record: AdoptItemResp) => {
  try {
    const res = await onShelfAdoptItem(record.id)
    if (res.code === '0') {
      message.success('上架成功')
      fetchData()
    } else {
      message.error(res.message || '上架失败')
    }
  } catch (error) {
    message.error('操作异常')
  }
}

// 下架
const handleOffShelf = async (record: AdoptItemResp) => {
  try {
    const res = await offShelfAdoptItem(record.id)
    if (res.code === '0') {
      message.success('下架成功')
      fetchData()
    } else {
      message.error(res.message || '下架失败')
    }
  } catch (error) {
    message.error('操作异常')
  }
}

// 删除
const handleDelete = async (record: AdoptItemResp) => {
  try {
    const res = await deleteAdoptItem(record.id)
    if (res.code === '0') {
      message.success('删除成功')
      fetchData()
    } else {
      message.error(res.message || '删除失败')
    }
  } catch (error) {
    message.error('操作异常')
  }
}

// 编辑
const handleEdit = (record: AdoptItemResp) => {
  // 如果是已上架状态，需要确认
  if (record.status === 1) {
    Modal.confirm({
      title: '提示',
      icon: createVNode(ExclamationCircleOutlined),
      content: '该项目当前处于上架状态。修改信息后项目将自动下架并重新进入审核流程，是否继续？',
      onOk() {
        router.push({ 
          path: '/farmer/adopt/create',
          query: { id: record.id } 
        })
      }
    })
    return
  }

  // 其他状态直接跳转
  router.push({ 
    path: '/farmer/adopt/create',
    query: { id: record.id } 
  })
}

// 初始化
onMounted(() => {
  fetchCategories()
  fetchData()
})
</script>

<style scoped>
.my-adopts-container {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 500;
  color: #1f1f1f;
  margin-bottom: 8px;
}

.page-desc {
  color: #8c8c8c;
  font-size: 14px;
}

.search-card {
  margin-bottom: 16px;
  border-radius: 8px;
}

.search-form .ant-form-item {
  margin-bottom: 16px;
}

.search-actions {
  margin-left: auto;
}

.table-card {
  border-radius: 8px;
  min-height: 400px;
}

.cover-image-wrapper {
  border-radius: 4px;
  overflow: hidden;
  border: 1px solid #f0f0f0;
  display: inline-block;
  line-height: 1;
}

:deep(.cover-image) {
  object-fit: cover;
}
</style>
