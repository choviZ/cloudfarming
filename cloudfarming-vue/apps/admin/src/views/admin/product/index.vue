<template>
  <div class="product-list-container">
    <!-- 筛选区 -->
    <a-card :bordered="false" class="filter-card">
      <a-form layout="inline" :model="queryParams" @finish="handleSearch">
        <a-form-item label="商品名称">
          <a-input v-model:value="queryParams.spuName" placeholder="请输入商品名称" allow-clear />
        </a-form-item>
        <a-form-item label="商品分类">
           <a-tree-select
            v-model:value="queryParams.categoryId"
            style="width: 200px"
            :dropdown-style="{ maxHeight: '400px', overflow: 'auto' }"
            :tree-data="categoryTree"
            placeholder="请选择分类"
            tree-default-expand-all
            allow-clear
            :field-names="{ label: 'name', value: 'id', children: 'children' }"
          />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="queryParams.status" placeholder="请选择状态" allow-clear style="width: 120px">
            <a-select-option :value="1">上架</a-select-option>
            <a-select-option :value="0">下架</a-select-option>
            <a-select-option :value="2">待审核</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit">查询</a-button>
          <a-button style="margin-left: 8px" @click="handleReset">重置</a-button>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 列表区 -->
    <a-card :bordered="false" class="table-card">
      <a-table
        :columns="columns"
        :data-source="list"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'image'">
            <a-image
              :width="60"
              :src="record.image ? record.image.split(',')[0] : ''"
              fallback="https://via.placeholder.com/60"
            />
          </template>
          
          <template v-if="column.key === 'price'">
            <span v-if="record.priceSummary">
              ¥{{ record.priceSummary.minPrice }} 
              <span v-if="record.priceSummary.minPrice !== record.priceSummary.maxPrice">
                - {{ record.priceSummary.maxPrice }}
              </span>
            </span>
            <span v-else>-</span>
          </template>

          <template v-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ getStatusText(record.status) }}
            </a-tag>
          </template>
          
          <template v-if="column.key === 'createTime'">
             {{ record.createTime ? dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') : '-' }}
          </template>

          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" @click="handleDetail(record)">详情</a-button>
              
              <a-popconfirm
                v-if="record.status === 1"
                title="确定要强制下架该商品吗？"
                @confirm="handleUpdateStatus(record, 0)"
              >
                <a-button type="link" danger>下架</a-button>
              </a-popconfirm>
              
              <a-popconfirm
                v-if="record.status === 0"
                title="确定要上架该商品吗？"
                @confirm="handleUpdateStatus(record, 1)"
              >
                <a-button type="link" class="success-btn">上架</a-button>
              </a-popconfirm>
              
              <!-- 待审核状态 -->
               <a-popconfirm
                v-if="record.status === 2"
                title="确定通过审核并上架吗？"
                @confirm="handleUpdateStatus(record, 1)"
              >
                <a-button type="link" class="success-btn">通过</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import dayjs from 'dayjs';
import { listSpuByPage, updateSpuStatus } from '@/api/spu';
import { getCategoryTree } from '@/api/category';

const router = useRouter();
const loading = ref(false);
const list = ref([]);
const categoryTree = ref([]);

const queryParams = reactive({
  current: 1,
  size: 10,
  spuName: '',
  categoryId: undefined,
  status: undefined,
});

const pagination = reactive({
  total: 0,
  current: 1,
  pageSize: 10,
  showSizeChanger: true,
  showTotal: (total) => `共 ${total} 条`,
});

const columns = [
  {
    title: '商品主图',
    key: 'image',
    width: 360,
  },
  {
    title: '商品名称',
    dataIndex: 'title',
    key: 'title',
    ellipsis: true,
  },
  {
    title: '价格',
    key: 'price',
    width: 150,
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 100,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 180,
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
    align: 'center',
  },
];

const fetchList = async () => {
  loading.value = true;
  try {
    const res = await listSpuByPage({
      ...queryParams,
      current: pagination.current,
      size: pagination.pageSize,
    });
    if (res.code === '0' && res.data) {
      list.value = res.data.records;
      pagination.total = res.data.total;
    } else {
      list.value = [];
      pagination.total = 0;
    }
  } catch (error) {
    console.error(error);
    message.error('获取商品列表失败');
  } finally {
    loading.value = false;
  }
};

const fetchCategoryTree = async () => {
  try {
    const res = await getCategoryTree();
    if (res.code === '0' && res.data) {
      categoryTree.value = res.data;
    }
  } catch (error) {
    console.error('获取分类树失败', error);
  }
};

const handleSearch = () => {
  pagination.current = 1;
  fetchList();
};

const handleReset = () => {
  queryParams.spuName = '';
  queryParams.categoryId = undefined;
  queryParams.status = undefined;
  handleSearch();
};

const handleTableChange = (pag) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  fetchList();
};

const handleDetail = (record) => {
  router.push(`/admin/product/detail/${record.id}`);
};

const handleUpdateStatus = async (record, status) => {
  try {
    const res = await updateSpuStatus(record.id, status);
    if (res.code === '0') {
      message.success('操作成功');
      fetchList();
    } else {
      message.error(res.message || '操作失败');
    }
  } catch (error) {
    console.error(error);
    message.error('操作异常');
  }
};

const getStatusColor = (status) => {
  switch (status) {
    case 1: return 'success';
    case 0: return 'error';
    case 2: return 'warning';
    default: return 'default';
  }
};

const getStatusText = (status) => {
  switch (status) {
    case 1: return '上架';
    case 0: return '下架';
    case 2: return '待审核';
    default: return '未知';
  }
};

onMounted(() => {
  fetchList();
  fetchCategoryTree();
});
</script>

<style scoped>
.product-list-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.success-btn {
  color: #52c41a;
}
</style>
