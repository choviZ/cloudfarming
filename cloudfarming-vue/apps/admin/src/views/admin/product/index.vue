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
          </a-select>
        </a-form-item>
        <a-form-item label="审核状态">
          <a-select v-model:value="queryParams.auditStatus" placeholder="请选择审核状态" allow-clear style="width: 140px">
            <a-select-option :value="0">待审核</a-select-option>
            <a-select-option :value="1">已通过</a-select-option>
            <a-select-option :value="2">未通过</a-select-option>
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
        class="product-table"
        :columns="columns"
        :data-source="list"
        :loading="loading"
        :pagination="pagination"
        :scroll="{ x: 1100 }"
        size="middle"
        @change="handleTableChange"
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'image'">
            <a-image v-if="resolveSpuCover(record)" :width="60" :src="resolveSpuCover(record)" />
            <div v-else class="image-placeholder">暂无主图</div>
          </template>

          <template v-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ getStatusText(record.status) }}
            </a-tag>
          </template>

          <template v-if="column.key === 'id'">
            <span class="spu-id">#{{ record.id }}</span>
          </template>

          <template v-if="column.key === 'auditStatus'">
            <a-tag :color="getAuditStatusColor(record.auditStatus)">
              {{ getAuditStatusText(record.auditStatus) }}
            </a-tag>
          </template>
          
          <template v-if="column.key === 'createTime'">
             {{ record.createTime ? dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') : '-' }}
          </template>

          <template v-if="column.key === 'action'">
            <div class="action-stack">
              <a-button type="link" @click="handleDetail(record)">详情</a-button>

              <a-popconfirm
                v-if="record.auditStatus === 0"
                title="确定通过该商品审核吗？"
                @confirm="handleUpdateAuditStatus(record, 1)"
              >
                <a-button type="link" class="success-btn">通过审核</a-button>
              </a-popconfirm>

              <a-popconfirm
                v-if="record.auditStatus === 0"
                title="确定拒绝该商品审核吗？"
                @confirm="handleUpdateAuditStatus(record, 2)"
              >
                <a-button type="link" danger>拒绝审核</a-button>
              </a-popconfirm>

              <a-popconfirm
                v-if="record.auditStatus === 1 && record.status === 1"
                title="确定要强制下架该商品吗？"
                @confirm="handleUpdateStatus(record, 0)"
              >
                <a-button type="link" danger>下架</a-button>
              </a-popconfirm>

              <a-popconfirm
                v-if="record.auditStatus === 1 && record.status === 0"
                title="确定要上架该商品吗？"
                @confirm="handleUpdateStatus(record, 1)"
              >
                <a-button type="link" class="success-btn">上架</a-button>
              </a-popconfirm>
            </div>
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
import { approveAudit, getLatestAuditRecord, rejectAudit } from '@/api/audit';
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
  auditStatus: undefined,
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
    width: 110,
  },
  {
    title: 'SPU ID',
    dataIndex: 'id',
    key: 'id',
    width: 110,
  },
  {
    title: '商品名称',
    dataIndex: 'title',
    key: 'title',
    width: 340,
    ellipsis: true,
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 110,
  },
  {
    title: '审核状态',
    dataIndex: 'auditStatus',
    key: 'auditStatus',
    width: 120,
    align: 'center',
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
    width: 130,
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
      list.value = Array.isArray(res.data.records) ? res.data.records : [];
      pagination.total = Number(res.data.total) || 0;
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
  queryParams.auditStatus = undefined;
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

const handleUpdateAuditStatus = async (record, targetAuditStatus) => {
  try {
    const latestRecordRes = await getLatestAuditRecord({
      bizType: 1,
      bizId: record.id,
    });
    if (latestRecordRes.code !== '0' || !latestRecordRes.data?.id) {
      message.error(latestRecordRes.message || '获取审核记录失败');
      return;
    }
    if (latestRecordRes.data.auditStatus !== 0) {
      message.warning('该商品当前不是待审核状态');
      fetchList();
      return;
    }

    const auditId = latestRecordRes.data.id;
    const actionRes = targetAuditStatus === 1
      ? await approveAudit(auditId)
      : await rejectAudit(auditId, '管理员审核未通过');
    if (actionRes.code === '0') {
      message.success(targetAuditStatus === 1 ? '审核通过成功' : '审核拒绝成功');
      fetchList();
      return;
    }
    message.error(actionRes.message || '修改审核状态失败');
  } catch (error) {
    console.error(error);
    message.error('修改审核状态异常');
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
    default: return '未知';
  }
};

const getAuditStatusColor = (auditStatus) => {
  switch (auditStatus) {
    case 0: return 'warning';
    case 1: return 'success';
    case 2: return 'error';
    default: return 'default';
  }
};

const getAuditStatusText = (auditStatus) => {
  switch (auditStatus) {
    case 0: return '待审核';
    case 1: return '已通过';
    case 2: return '未通过';
    default: return '未知';
  }
};

const resolveSpuCover = (record) => {
  if (!record) {
    return '';
  }
  const rawImages = typeof record.images === 'string' ? record.images : '';
  if (!rawImages.trim()) {
    return '';
  }
  return rawImages.split(',').map((item) => item.trim()).find(Boolean) || '';
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

.table-card :deep(.ant-table-tbody > tr > td) {
  vertical-align: middle;
}

.table-card :deep(.ant-table-thead > tr > th) {
  white-space: nowrap;
}

.table-card :deep(.ant-btn-link) {
  padding: 0;
  height: 24px;
  line-height: 24px;
}

.success-btn {
  color: #52c41a;
}

.image-placeholder {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  border: 1px dashed #d9d9d9;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 12px;
  background: #fafafa;
}

.spu-id {
  color: #8c8c8c;
  font-size: 12px;
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
}

.action-stack {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}
</style>
