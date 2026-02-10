<template>
  <div class="product-detail-container">
    <a-page-header title="商品详情" @back="() => router.back()">
      <template #extra>
        <a-button v-if="detail?.status === 2" type="primary" @click="handleAudit(1)">通过审核</a-button>
        <a-button v-if="detail?.status === 2" danger @click="handleAudit(0)">拒绝审核</a-button>
      </template>
    </a-page-header>

    <div v-if="loading" class="loading-wrapper">
      <a-spin size="large" />
    </div>

    <div v-else-if="detail" class="detail-content">
      <!-- 1. 基本信息 -->
      <a-card title="基本信息" :bordered="false" class="section-card">
        <a-descriptions :column="2">
          <a-descriptions-item label="商品名称">{{ detail.title }}</a-descriptions-item>
          <a-descriptions-item label="所属分类">{{ categoryName }}</a-descriptions-item>
          <a-descriptions-item label="商品状态">
            <a-tag :color="getStatusColor(detail.status)">
              {{ getStatusText(detail.status) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="创建时间">
            {{ detail.createTime ? dayjs(detail.createTime).format('YYYY-MM-DD HH:mm:ss') : '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="商品图片" :span="2">
            <a-image-preview-group>
              <a-space>
                <a-image v-for="(img, index) in (detail.image ? detail.image.split(',') : [])" :key="index" :width="100"
                  :src="img" />
              </a-space>
            </a-image-preview-group>
          </a-descriptions-item>
        </a-descriptions>
      </a-card>

      <!-- 2. 基础属性 -->
      <a-card title="基础属性" :bordered="false" class="section-card">
        <a-descriptions :column="3" size="small" bordered>
          <a-descriptions-item v-for="(value, key) in detail.baseAttrs" :key="key" :label="key">
            {{ value }}
          </a-descriptions-item>
        </a-descriptions>
        <div v-if="!detail.baseAttrs || Object.keys(detail.baseAttrs).length === 0" class="empty-text">
          暂无基础属性
        </div>
      </a-card>

      <!-- 3. SKU 列表 -->
      <a-card title="SKU 规格列表" :bordered="false" class="section-card">
        <a-table :columns="skuColumns" :data-source="detail.skuList" row-key="id" :pagination="false" bordered>
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'image'">
              <a-image :width="50" :src="record.image" v-if="record.image" />
              <span v-else>-</span>
            </template>
            <template v-if="column.key === 'spec'">
              <a-tag v-for="(val, key) in record.saleAttrs" :key="key" color="blue">
                {{ key }}: {{ val }}
              </a-tag>
            </template>
            <template v-if="column.key === 'status'">
              <a-badge :status="record.status === 1 ? 'success' : 'default'"
                :text="record.status === 1 ? '启用' : '禁用'" />
            </template>
          </template>
        </a-table>
      </a-card>
    </div>

    <div v-else class="error-wrapper">
      <a-empty description="未找到商品详情" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import dayjs from 'dayjs';
import { getSpuDetail, updateSpuStatus } from '@/api/spu';
import { getCategoryById } from '@/api/category';

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const detail = ref(null);
const categoryName = ref('');

const skuColumns = [
  {
    title: 'SKU 图片',
    key: 'image',
    width: 380,
    align: 'center',
  },
  {
    title: '规格组合',
    key: 'spec',
  },
  {
    title: '销售价格',
    dataIndex: 'price',
    key: 'price',
    width: 150,
    customRender: ({ text }) => `¥${text}`
  },
  {
    title: '库存',
    dataIndex: 'stock',
    key: 'stock',
    width: 120,
  },
  {
    title: '状态',
    key: 'status',
    width: 100,
    align: 'center',
  },
];

const fetchDetail = async (id) => {
  loading.value = true;
  try {
    const res = await getSpuDetail(id);
    if (res.code == '0' && res.data) {
      detail.value = res.data;
      // 获取分类名称
      if (res.data.categoryId) {
        fetchCategoryName(res.data.categoryId);
      }
    } else {
      message.error(res.message || '获取详情失败');
    }
  } catch (error) {
    message.error('获取详情异常,' + error);
  } finally {
    loading.value = false;
  }
};

const fetchCategoryName = async (id) => {
  const res = await getCategoryById(id);
  if (res.code == '0' && res.data) {
    categoryName.value = res.data.name;
  }
};

const handleAudit = async (status) => {
  if (!detail.value) return;
  const res = await updateSpuStatus(detail.value.id, status);
  if (res.code == '0') {
    message.success('操作成功');
    // 刷新详情
    fetchDetail(detail.value.id);
  } else {
    message.error(res.message || '操作失败');
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
  const id = Number(route.params.id);
  if (id) {
    fetchDetail(id);
  } else {
    message.error('参数错误');
    router.back();
  }
});
</script>

<style scoped>
.product-detail-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.detail-content {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.section-card {
  width: 100%;
}

.loading-wrapper,
.error-wrapper {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-text {
  color: #999;
  text-align: center;
  padding: 16px;
}
</style>
