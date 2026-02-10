<template>
  <div class="attribute-container">
    <div class="filter-section">
      <a-card :bordered="false" class="filter-card">
        <a-flex align="center" gap="middle">
          <span>请选择分类：</span>
          <a-tree-select v-model:value="selectedCategoryId" style="width: 300px"
            :dropdown-style="{ maxHeight: '400px', overflow: 'auto' }" :tree-data="categoryTree" placeholder="请选择分类"
            tree-default-expand-all :field-names="{ label: 'name', value: 'id', children: 'children' }"
            @change="handleCategoryChange" />
        </a-flex>
      </a-card>
    </div>

    <div class="content-section" v-if="selectedCategoryId">
      <a-card :bordered="false">
        <template #title>
          <a-space>
            <a-button type="primary" @click="handleAdd">
              <template #icon>
                <PlusOutlined />
              </template>
              新增属性
            </a-button>
            <a-button @click="fetchAttributes">
              <template #icon>
                <ReloadOutlined />
              </template>
              刷新
            </a-button>
          </a-space>
        </template>

        <a-table :columns="columns" :data-source="attributeList" :loading="loading" row-key="id" :pagination="false">
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'attrType'">
              <a-tag :color="record.attrType === 0 ? 'blue' : 'green'">
                {{ record.attrType === 0 ? '基本属性' : '销售属性' }}
              </a-tag>
            </template>
            <template v-if="column.key === 'updateTime'">
              {{ record.updateTime ? dayjs(record.updateTime).format('YYYY-MM-DD HH:mm:ss') : '-' }}
            </template>
            <template v-if="column.key === 'action'">
              <a-space>
                <a-button type="link" @click="handleEdit(record)">编辑</a-button>
                <a-popconfirm title="确定要删除该属性吗？" @confirm="handleDelete(record.id)">
                  <a-button type="link" danger>删除</a-button>
                </a-popconfirm>
              </a-space>
            </template>
          </template>
        </a-table>
      </a-card>
    </div>

    <div v-else class="empty-placeholder">
      <a-empty description="请先选择一个分类以管理其属性" />
    </div>

    <AttributeModal v-model:open="modalVisible" :edit-data="currentAttribute" :category-id="selectedCategoryId || ''"
      @success="fetchAttributes" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { PlusOutlined, ReloadOutlined } from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';
import dayjs from 'dayjs';
import { getCategoryTree } from '@/api/category';
import { getAttributesByCategoryId, deleteAttribute } from '@/api/attribute';
import AttributeModal from './components/AttributeModal.vue';

const categoryTree = ref([]);
const selectedCategoryId = ref();
const attributeList = ref([]);
const loading = ref(false);

const modalVisible = ref(false);
const currentAttribute = ref(null);

const columns = [
  {
    title: '属性名称',
    dataIndex: 'name',
    key: 'name',
  },
  {
    title: '属性类型',
    dataIndex: 'attrType',
    key: 'attrType',
    width: 150,
  },
  {
    title: '排序',
    dataIndex: 'sort',
    key: 'sort',
    width: 120,
  },
  {
    title: '更新时间',
    dataIndex: 'updateTime',
    key: 'updateTime',
    width: 200,
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
    align: 'center',
  },
];

// 获取分类树
const fetchCategoryTree = async () => {
  const res = await getCategoryTree();
  if (res.code == '0' && res.data) {
    categoryTree.value = res.data;
  } else {
    message.error('获取分类树失败' + res.message)
  }
};

// 获取属性列表
const fetchAttributes = async () => {
  if (!selectedCategoryId.value) return;

  loading.value = true;
  try {
    const res = await getAttributesByCategoryId(selectedCategoryId.value);
    if (res.code === '0' && res.data) {
      attributeList.value = res.data;
    } else {
      attributeList.value = [];
    }
  } catch (error) {
    message.error('获取属性列表失败' + error);
  } finally {
    loading.value = false;
  }
};

const handleCategoryChange = () => {
  fetchAttributes();
};

const handleAdd = () => {
  currentAttribute.value = null;
  modalVisible.value = true;
};

const handleEdit = (record) => {
  currentAttribute.value = record;
  modalVisible.value = true;
};

const handleDelete = async (id) => {
  try {
    const res = await deleteAttribute(id);
    if (res.code === '0') {
      message.success('删除成功');
      fetchAttributes();
    } else {
      message.error(res.message || '删除失败');
    }
  } catch (error) {
    message.error('删除失败',error);
  }
};

onMounted(() => {
  fetchCategoryTree();
});
</script>

<style scoped>
.attribute-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.filter-card {
  margin-bottom: 16px;
}

.empty-placeholder {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  border-radius: 8px;
  min-height: 400px;
}
</style>
