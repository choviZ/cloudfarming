<template>
  <div class="index-container">
    <!-- 头部搜索区域 -->
    <div class="main-header">
      <div class="header-container main-content">
        <!-- Logo -->
        <div class="logo" @click="router.push('/')">
          <h1 class="logo-text">
            云农场
            <span class="logo-sub">优质农产品直供</span>
          </h1>
        </div>
        <!-- Search Box -->
        <div class="search-section">
          <div class="search-box">
            <div class="search-input-wrapper">
              <SearchOutlined class="search-icon"/>
              <input
                  type="text"
                  placeholder="搜索 新鲜水果 / 蔬菜 / 农特产"
                  class="search-input"
              />
            </div>
            <button class="search-button">
              搜索
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 上部广告展示区域 -->
    <div class="advert-section">
      <a-carousel :autoplay="true" :dots="true" :autoplay-speed="3000" :effect="'fade'">
        <div v-for="advert in adverts" :key="advert.id" class="advert-item">
          <a v-if="advert.linkUrl" :href="advert.linkUrl" target="_blank" rel="noopener noreferrer">
            <img :src="advert.imageUrl" :alt="advert.altText || '广告图片'" class="advert-image" />
          </a>
          <img v-else :src="advert.imageUrl" :alt="advert.altText || '广告图片'" class="advert-image" />
        </div>
      </a-carousel>
    </div>

    <!-- 认养精选区域 -->
    <AdoptFeaturedSection style="margin-top:24px"/>

    <!-- 下部商品展示区域 -->
    <div class="product-section">
      <!-- 商品列表头部 -->
      <div class="section-header">
        <h2 class="section-title">农产品特惠</h2>
        <a-button type="link">查看全部 ></a-button>
      </div>

      <!-- 商品类型标签查询 -->
      <div class="product-types">
        <a-tag
          v-for="tag in productTags"
          :key="tag.key"
          :color="activeTag === tag.key ? 'green' : 'default'"
          @click="handleTagClick(tag.key)"
          class="tag-item"
        >
          {{ tag.name }}
        </a-tag>
      </div>

      <!-- 商品列表 -->
      <a-skeleton :loading="productLoading" :rows="8" animated>
        <div class="product-list">
          <a-row :gutter="[20, 20]">
            <a-col
              v-for="product in productList"
              :key="product.id"
              :xs="24"
              :sm="12"
              :md="8"
              :lg="6"
            >
              <div class="product-item">
                <a-card hoverable class="product-card" @click="goToProductDetail(product.id)">
                  <template #cover>
                    <img
                      :alt="product.title"
                      :src="getFirstImage(product.image)"
                      class="product-image"
                    />
                  </template>
                  <div class="product-info">
                    <div class="product-title">{{ product.title }}</div>
                    <div class="product-price">
                      <span v-if="product.priceSummary">¥{{ product.priceSummary.minPrice }}</span>
                      <span v-else style="font-size: 14px; color: #999;">暂无价格</span>
                    </div>
                  </div>
                </a-card>
              </div>
            </a-col>
          </a-row>
        </div>
      </a-skeleton>

      <!-- 分页组件 -->
      <div class="pagination-container">
        <a-pagination
          v-model:current="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :show-size-changer="true"
          :page-size-options="['24', '32']"
          :show-total="(total: number) => `共 ${total} 件商品`"
          @change="handlePageChange"
          @show-size-change="handleSizeChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import {SearchOutlined} from '@ant-design/icons-vue';
import { getShowAdverts } from '@/api/advert';
import type { AdvertRespDTO } from '@/types/advert';
import { listSpuByPage } from '@cloudfarming/core/api/spu';
import type { SpuPageQueryReqDTO, SpuRespDTO } from '@cloudfarming/core/api/spu';
import type { IPage } from '@cloudfarming/core/api/common';
import { message } from 'ant-design-vue';
import AdoptFeaturedSection from '@/components/adopt/AdoptFeaturedSection.vue';

// 路由实例
const router = useRouter();

// 广告相关
const adverts = ref<AdvertRespDTO[]>([]);
const advertLoading = ref(false);

// 商品相关
const productList = ref<SpuRespDTO[]>([]);
const productLoading = ref(false);
const activeTag = ref('all');

// 分页相关
const pagination = ref({
  current: 1,
  size: 24,
  total: 0,
});

// 商品类型标签
const productTags = ref([
  { key: 'all', name: '全部' },
  // 可以根据需要添加更多标签
]);

// 处理商品图片，只返回第一张
const getFirstImage = (imageStr: string): string => {
  if (!imageStr) {
    // TODO 随机图片临时占位
    return 'https://via.placeholder.com/200x200?text=No+Image';
  }
  return imageStr.split(',')[0] || imageStr;
};

// 获取广告数据
const fetchAdverts = async () => {
  advertLoading.value = true;
  try {
    const response = await getShowAdverts();
    if (response.code === '0' && response.data) {
      // 按照displayOrder排序广告
      adverts.value = response.data.sort((a: { displayOrder: number; }, b: { displayOrder: number; }) => a.displayOrder - b.displayOrder);
    } else {
      message.error('获取广告数据失败：' + (response.message || '未知错误'));
    }
  }finally {
    advertLoading.value = false;
  }
};

// 获取商品数据
const fetchProducts = async () => {
  productLoading.value = true;
  try {
    // 构建查询参数
    const queryParam: SpuPageQueryReqDTO = {
      current: pagination.value.current,
      size: pagination.value.size,
      status: 1, // 上架商品
    };

    const response = await listSpuByPage(queryParam);
    if (response.code === '0' && response.data) {
      const pageData: IPage<SpuRespDTO> = response.data;
      productList.value = pageData.records;
      pagination.value.total = pageData.total;
    } else {
      message.error('获取商品数据失败：' + (response.message || '未知错误'));
    }
  }finally {
    productLoading.value = false;
  }
};

// 标签点击事件
const handleTagClick = (tagKey: string) => {
  activeTag.value = tagKey;
  // 重置分页
  pagination.value.current = 1;
  // 重新获取商品数据
  fetchProducts();
};

// 分页变化事件
const handlePageChange = (page: number) => {
  pagination.value.current = page;
  fetchProducts();
};

// 每页条数变化事件
const handleSizeChange = (current: number, size: number) => {
  pagination.value.current = current;
  pagination.value.size = size;
  fetchProducts();
};

// 跳转到商品详情页
const goToProductDetail = (productId: number) => {
  router.push({
    name: 'productDetail',
    params: { id: productId.toString() }
  });
};

// 组件挂载时获取数据
onMounted(() => {
  fetchAdverts();
  fetchProducts();
});
</script>

<style scoped>
.index-container {
  min-height: 100vh;
}

.header-container {
  width: 100%;
  max-width: 1620px; /* Updated to 1620px */
  margin: 0 auto;
  padding: 0 20px;
}

/* Main Header */
.main-header {
  padding: 24px 0;
  background-color: #FFFFFF;
}

.main-content {
  display: flex;
  align-items: center;
  gap: 32px;
}

.logo {
  width: 180px;
  flex-shrink: 0;
  cursor: pointer;
}

.logo-text {
  font-size: 30px;
  font-weight: bold;
  color: #388E3C;
  margin: 0;
  line-height: 1;
  letter-spacing: -1px;
}

.logo-sub {
  font-size: 14px;
  color: #FF9800;
  font-weight: normal;
  display: block;
  letter-spacing: normal;
  margin-top: 4px;
}

/* Search Box */
.search-section {
  flex: 1;
  max-width: 800px;
  margin: 0 auto;
}

.search-box {
  display: flex;
  position: relative;
}

.search-input-wrapper {
  flex: 1;
  border: 2px solid #388E3C;
  border-radius: 24px 0 0 24px;
  overflow: hidden;
  display: flex;
  align-items: center;
  padding-left: 16px;
  background-color: #FFFFFF;
  height: 40px;
}

.search-icon {
  color: #A0AEC0;
  margin-right: 8px;
  font-size: 18px;
}

.search-input {
  width: 100%;
  height: 100%;
  border: none;
  outline: none;
  font-size: 14px;
  color: #2D3748;
}

.search-input::placeholder {
  color: #A0AEC0;
}

.search-button {
  background-color: #388E3C;
  color: #FFFFFF;
  padding: 0 32px;
  border-radius: 0 24px 24px 0;
  font-weight: bold;
  font-size: 18px;
  border: none;
  cursor: pointer;
  transition: background-color 0.2s;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.search-button:hover {
  background-color: #1B5E20;
}

/* 广告区域样式 */
.advert-section {
  width: 100%;
  max-width: 1620px;
  margin: 0 auto;
}

.advert-item {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 400px;
  overflow: hidden;
  border-radius: 16px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
}

.advert-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 16px;
  transition: transform 0.5s ease;
}

.advert-image:hover {
  transform: scale(1.02);
}

/* 商品区域样式 */
.product-section {
  width: 100%;
  max-width: 1620px;
  margin: 0 auto;
  padding: 20px 0 60px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 0 20px;
}

.section-title {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin: 0;
  position: relative;
  padding-left: 15px;
}

.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 24px;
  background-color: #388E3C;
  border-radius: 2px;
}

.product-types {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 32px;
  padding: 24px 20px;
  background-color: transparent;
  box-shadow: none;
}

.tag-item {
  cursor: pointer;
  border-radius: 8px;
  padding: 4px 16px;
  font-size: 14px;
}

.product-list {
  width: 100%;
  padding: 0 20px;
}

.product-item-wrapper {
  display: flex;
  justify-content: center;
}

.product-item {
  width: 100%;
  margin: 0 auto;
}

.product-card {
  border: none;
  border-radius: 16px;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  background-color: #FFFFFF;
  overflow: hidden;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
}

/* 商品图片样式 */
.product-card .ant-card-cover {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 240px;
  overflow: hidden;
  background-color: #f8f8f8;
  margin: 0;
  padding: 0;
}

.product-image {
  width: 100%;
  height: 240px;
  object-fit: cover;
  object-position: center;
  transition: transform 0.5s ease;
}

.product-card:hover .product-image {
  transform: scale(1.05);
}

/* 使用Vue深度选择器覆盖Ant Design卡片默认样式 */
.product-card :deep(.ant-card-body) {
  padding: 16px !important;
  border-radius: 0 0 16px 16px !important;
}

/* 商品信息样式 */
.product-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.product-title {
  font-size: 16px;
  font-weight: 500;
  color: #2D3748;
  margin-bottom: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.product-price {
  font-size: 18px;
  font-weight: 600;
  color: #FF9800;
  display: flex;
  align-items: baseline;
}

.product-price::before {
  content: '¥';
  font-size: 14px;
  margin-right: 2px;
}

/* 分页容器样式 */
.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 48px;
}
</style>