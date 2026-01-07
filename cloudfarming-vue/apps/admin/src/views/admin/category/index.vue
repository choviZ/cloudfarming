<template>
  <div class="category-management">
    <!-- 操作按钮 -->
    <div class="action-bar">
      <a-space>
        <a-button type="primary" @click="handleAdd">
          <template #icon>
            <PlusOutlined />
          </template>
          新增顶级分类
        </a-button>
        <a-button @click="handleRefresh">
          <template #icon>
            <ReloadOutlined />
          </template>
          刷新
        </a-button>
      </a-space>
    </div>

    <!-- 分类树形表格 -->
    <a-card>
      <a-table
        :columns="columns"
        :data-source="categoryTree"
        :loading="loading"
        :pagination="false"
        row-key="id"
        :default-expand-all-rows="true"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'name'">
            <a-space>
              <FolderOutlined v-if="!record.parentId" />
              <FileOutlined v-else />
              <span>{{ record.name }}</span>
            </a-space>
          </template>
          <template v-else-if="column.key === 'parentId'">
            <span v-if="!record.parentId" style="color: #999">顶级分类</span>
            <span v-else>{{ getParentName(record.parentId) }}</span>
          </template>
          <template v-else-if="column.key === 'sortOrder'">
            <a-tag color="blue">{{ record.sortOrder }}</a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleAddChild(record)">
                新增子分类
              </a-button>
              <a-button type="link" size="small" @click="handleEdit(record)">
                编辑
              </a-button>
              <a-popconfirm
                title="确定要删除该分类吗？删除后子分类也将被删除！"
                ok-text="确定"
                cancel-text="取消"
                @confirm="() => handleDelete(record.id)"
              >
                <a-button type="link" size="small" danger>
                  删除
                </a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 新增/编辑分类弹窗 -->
    <category-modal
      v-model:open="modalState.open"
      :mode="modalState.mode"
      :initial-data="modalState.initialData"
      :category-tree="categoryTree"
      @success="handleModalSuccess"
    />
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import {
  PlusOutlined,
  ReloadOutlined,
  FolderOutlined,
  FileOutlined
} from '@ant-design/icons-vue'
import {
  getCategoryTree,
  deleteCategory
} from '@cloudfarming/core'
import type { CategoryRespDTO } from '@cloudfarming/core'
import CategoryModal from './components/CategoryModal.vue'

const loading = ref(false)
const categoryTree = ref<CategoryRespDTO[]>([])

// 模态框状态
const modalState = reactive<{
  open: boolean
  mode: 'create' | 'create-child' | 'edit'
  initialData: CategoryRespDTO | null
}>({
  open: false,
  mode: 'create',
  initialData: null
})

const columns = [
  {
    title: '分类名称',
    dataIndex: 'name',
    key: 'name',
    width: '25%'
  },
  {
    title: '父级分类',
    dataIndex: 'parentId',
    key: 'parentId',
    width: '20%'
  },
  {
    title: '排序权重',
    dataIndex: 'sortOrder',
    key: 'sortOrder',
    width: '10%'
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: '15%',
    customRender: ({ text }: { text: string }) => {
      return dayjs(text).format('YYYY-MM-DD HH:mm:ss')
    }
  },
  {
    title: '更新时间',
    dataIndex: 'updateTime',
    key: 'updateTime',
    width: '15%',
    customRender: ({ text }: { text: string }) => {
      return dayjs(text).format('YYYY-MM-DD HH:mm:ss')
    }
  },
  {
    title: '操作',
    key: 'action',
    width: '15%'
  }
]

const getCategoryName = (id: string): string => {
  const findCategory = (list: CategoryRespDTO[], targetId: string): CategoryRespDTO | null => {
    for (const item of list) {
      if (item.id === targetId) return item
      if (item.children) {
        const found = findCategory(item.children, targetId)
        if (found) return found
      }
    }
    return null
  }
  const category = findCategory(categoryTree.value, id)
  return category?.name || ''
}

const getParentName = (parentId: string | null): string => {
  if (!parentId) return ''
  return getCategoryName(parentId)
}

const loadCategoryTree = async () => {
  loading.value = true
  try {
    const { data } = await getCategoryTree()
    categoryTree.value = data || []
  } catch (error) {
    message.error('加载分类树失败')
  } finally {
    loading.value = false
  }
}

const handleRefresh = () => {
  loadCategoryTree()
}

const handleAdd = () => {
  modalState.mode = 'create'
  modalState.initialData = null
  modalState.open = true
}

const handleAddChild = (record: CategoryRespDTO) => {
  modalState.mode = 'create-child'
  modalState.initialData = record
  modalState.open = true
}

const handleEdit = (record: CategoryRespDTO) => {
  modalState.mode = 'edit'
  modalState.initialData = record
  modalState.open = true
}

const handleDelete = async (id: string) => {
  try {
    await deleteCategory(id)
    message.success('删除成功')
    await loadCategoryTree()
  } catch (error) {
    message.error('删除失败')
  }
}

const handleModalSuccess = () => {
  loadCategoryTree()
}

onMounted(() => {
  loadCategoryTree()
})
</script>

<style scoped>
.category-management {
  padding: 24px;
}

.action-bar {
  margin-bottom: 16px;
}

:deep(.ant-table) {
  font-size: 14px;
}

:deep(.ant-table-thead > tr > th) {
  background: #fafafa;
  font-weight: 600;
}

:deep(.ant-table-tbody > tr:hover > td) {
  background: #f5f5f5;
}
</style>
