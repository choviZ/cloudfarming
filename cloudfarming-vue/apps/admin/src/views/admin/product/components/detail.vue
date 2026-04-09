<template>
  <div class="product-detail-container">
    <a-page-header title="商品详情" @back="() => router.back()">
      <template #extra>
        <a-button v-if="spu?.auditStatus === 0" type="primary" @click="handleAudit(1)">通过审核</a-button>
        <a-button v-if="spu?.auditStatus === 0" danger @click="handleAudit(2)">拒绝审核</a-button>
      </template>
    </a-page-header>

    <div v-if="loading" class="loading-wrapper">
      <a-spin size="large" />
    </div>

    <div v-else-if="hasDetail" class="detail-content">
      <a-card title="SPU 基础信息" :bordered="false" class="section-card">
        <a-descriptions :column="2" bordered size="small">
          <a-descriptions-item label="SPU ID">{{ spu.id || '-' }}</a-descriptions-item>
          <a-descriptions-item label="店铺 ID">{{ spu.shopId || '-' }}</a-descriptions-item>
          <a-descriptions-item label="商品名称">{{ spu.title || '-' }}</a-descriptions-item>
          <a-descriptions-item label="所属分类">{{ categoryName || '-' }}</a-descriptions-item>
          <a-descriptions-item label="商品状态">
            <a-tag :color="getStatusColor(spu.status)">
              {{ getStatusText(spu.status) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="创建时间">
            {{ spu.createTime ? dayjs(spu.createTime).format('YYYY-MM-DD HH:mm:ss') : '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="审核状态">
            <a-tag :color="getAuditStatusColor(spu.auditStatus)">
              {{ getAuditStatusText(spu.auditStatus) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="商品主图" :span="2">
            <a-image-preview-group v-if="imageList.length">
              <a-space wrap>
                <a-image v-for="(img, index) in imageList" :key="`${img}-${index}`" :width="100" :src="img" />
              </a-space>
            </a-image-preview-group>
            <span v-else class="empty-text">暂无商品主图</span>
          </a-descriptions-item>
        </a-descriptions>
      </a-card>

      <a-card title="SPU 基础属性" :bordered="false" class="section-card">
        <a-descriptions v-if="baseAttrEntries.length" :column="3" size="small" bordered>
          <a-descriptions-item v-for="([key, value]) in baseAttrEntries" :key="key" :label="key">
            {{ value }}
          </a-descriptions-item>
        </a-descriptions>
        <div v-else class="empty-text">暂无基础属性</div>
      </a-card>

      <a-card title="SKU 规格列表（含价格）" :bordered="false" class="section-card">
        <a-table :columns="skuColumns" :data-source="skuList" row-key="id" :pagination="false" bordered>
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'skuImage'">
              <a-image v-if="record.skuImage" :width="52" :src="record.skuImage" />
              <span v-else>-</span>
            </template>

            <template v-if="column.key === 'saleAttrs'">
              <a-space v-if="Object.keys(record.saleAttrs || {}).length" wrap>
                <a-tag v-for="(val, key) in record.saleAttrs" :key="key" color="blue">
                  {{ key }}: {{ val }}
                </a-tag>
              </a-space>
              <span v-else>-</span>
            </template>

            <template v-if="column.key === 'price'">
              {{ formatPrice(record.price) }}
            </template>

            <template v-if="column.key === 'status'">
              <a-badge :status="record.status === 1 ? 'success' : 'default'" :text="record.status === 1 ? '上架' : '下架'" />
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
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import dayjs from 'dayjs';
import { getSpuDetail } from '@/api/spu';
import { approveAudit, getLatestAuditRecord, rejectAudit } from '@/api/audit';
import { getCategoryById } from '@/api/category';

const route = useRoute();
const router = useRouter();

const loading = ref(false);
const detail = ref(null);
const categoryName = ref('');

const skuColumns = [
  {
    title: 'SKU ID',
    dataIndex: 'id',
    key: 'id',
    width: 120,
  },
  {
    title: 'SKU 图片',
    key: 'skuImage',
    width: 90,
    align: 'center',
  },
  {
    title: '销售属性',
    key: 'saleAttrs',
  },
  {
    title: '销售价格',
    dataIndex: 'price',
    key: 'price',
    width: 120,
  },
  {
    title: '库存',
    dataIndex: 'stock',
    key: 'stock',
    width: 100,
  },
  {
    title: '锁定库存',
    dataIndex: 'lockStock',
    key: 'lockStock',
    width: 100,
  },
  {
    title: '状态',
    key: 'status',
    width: 110,
    align: 'center',
  },
];

const spu = computed(() => detail.value?.spu || null);
const skuList = computed(() => detail.value?.skuList || []);
const hasDetail = computed(() => Boolean(spu.value));
const imageList = computed(() => parseImageList(spu.value?.images));
const baseAttrEntries = computed(() => Object.entries(parseBaseAttrs(spu.value?.attributes)));

const fetchDetail = async (id) => {
  loading.value = true;
  try {
    const res = await getSpuDetail(id);
    if (res.code === '0' && res.data?.productSpu) {
      const productSpu = res.data.productSpu;
      const productSkus = Array.isArray(res.data.productSkus) ? res.data.productSkus : [];
      detail.value = {
        spu: productSpu,
        skuList: productSkus.map((sku) => ({
          ...sku,
          saleAttrs: parseSaleAttrs(sku.saleAttribute),
        })),
      };
      if (productSpu.categoryId) {
        fetchCategoryName(productSpu.categoryId);
      } else {
        categoryName.value = '';
      }
      return;
    }
    detail.value = null;
    message.error(res.message || '获取详情失败');
  } catch (error) {
    detail.value = null;
    message.error(`获取详情异常: ${error?.message || error}`);
  } finally {
    loading.value = false;
  }
};

const fetchCategoryName = async (id) => {
  try {
    const res = await getCategoryById(id);
    if (res.code === '0' && res.data) {
      categoryName.value = res.data.name || '';
      return;
    }
  } catch (error) {
    console.error(error);
  }
  categoryName.value = '';
};

const handleAudit = async (status) => {
  if (!spu.value?.id) {
    return;
  }
  try {
    const latestRecordRes = await getLatestAuditRecord({
      bizType: 1,
      bizId: spu.value.id,
    });
    if (latestRecordRes.code !== '0' || !latestRecordRes.data?.id) {
      message.error(latestRecordRes.message || '获取审核记录失败');
      return;
    }
    if (latestRecordRes.data.auditStatus !== 0) {
      message.warning('该商品当前不是待审核状态');
      fetchDetail(spu.value.id);
      return;
    }

    const actionRes = status === 1
      ? await approveAudit(latestRecordRes.data.id)
      : await rejectAudit(latestRecordRes.data.id, '管理员审核未通过');
    if (actionRes.code === '0') {
      message.success(status === 1 ? '审核通过成功' : '审核拒绝成功');
      fetchDetail(spu.value.id);
      return;
    }
    message.error(actionRes.message || '操作失败');
  } catch (error) {
    message.error(`操作异常: ${error?.message || error}`);
  }
};

const getStatusColor = (status) => {
  switch (status) {
    case 1:
      return 'success';
    case 0:
      return 'error';
    case 2:
      return 'warning';
    default:
      return 'default';
  }
};

const getStatusText = (status) => {
  switch (status) {
    case 1:
      return '上架';
    case 0:
      return '下架';
    case 2:
      return '待审核';
    default:
      return '未知';
  }
};

const getAuditStatusColor = (auditStatus) => {
  switch (auditStatus) {
    case 0:
      return 'warning';
    case 1:
      return 'success';
    case 2:
      return 'error';
    default:
      return 'default';
  }
};

const getAuditStatusText = (auditStatus) => {
  switch (auditStatus) {
    case 0:
      return '待审核';
    case 1:
      return '已通过';
    case 2:
      return '未通过';
    default:
      return '未知';
  }
};

const formatPrice = (value) => {
  const number = Number(value);
  if (Number.isNaN(number)) {
    return '-';
  }
  return `¥${number.toFixed(2)}`;
};

const parseImageList = (rawImages) => {
  if (typeof rawImages !== 'string' || !rawImages.trim()) {
    return [];
  }
  return rawImages
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean);
};

const parseBaseAttrs = (rawAttrs) => {
  if (!rawAttrs) {
    return {};
  }
  if (typeof rawAttrs === 'object') {
    return rawAttrs;
  }
  if (typeof rawAttrs !== 'string') {
    return {};
  }
  try {
    const parsed = JSON.parse(rawAttrs);
    return parsed && typeof parsed === 'object' ? parsed : {};
  } catch (error) {
    return {};
  }
};

const parseSaleAttrs = (rawSaleAttr) => {
  if (!rawSaleAttr) {
    return {};
  }
  if (typeof rawSaleAttr === 'object') {
    return rawSaleAttr;
  }
  if (typeof rawSaleAttr !== 'string') {
    return {};
  }
  try {
    const parsed = JSON.parse(rawSaleAttr);
    return parsed && typeof parsed === 'object' ? parsed : {};
  } catch (error) {
    return {};
  }
};

onMounted(() => {
  const id = Number(route.params.id);
  if (!id) {
    message.error('参数错误');
    router.back();
    return;
  }
  fetchDetail(id);
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
