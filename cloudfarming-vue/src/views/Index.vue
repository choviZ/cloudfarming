<template>
  <div class="index-container">
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

    <!-- 下部商品展示区域 -->
    <div class="product-section">
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
              :lg="8"
              :xl="6"
              :xxl="4"
              class="product-item-wrapper"
            >
              <div class="product-item">
                <a-card hoverable class="product-card">
                  <template #cover>
                    <img
                      :alt="product.productName"
                      :src="getFirstImage(product.productImg)"
                      class="product-image"
                    />
                  </template>
                  <div class="product-info">
                    <div class="product-title">{{ product.productName }}</div>
                    <div class="product-price">¥{{ product.price }}</div>
                    <a-flex align="center" justify="space-between">
                      <a-space>
                        <a-avatar :src="product.createUser.avatar" v-if="product.createUser.avatar"/>
                        <a-avatar size="small" v-else>
                          <template #icon><UserOutlined /></template>
                        </a-avatar>
                        <div class="product-origin">{{ product.createUser.username }}</div>
                      </a-space>
                      <div class="product-origin">{{ product.originPlace }}</div>
                    </a-flex>

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
import { getShowAdverts } from '@/api/advert';
import type { AdvertRespDTO } from '@/types/advert';
import { getProductList } from '@/api/product';
import type { ProductPageQueryReqDTO, ProductRespDTO } from '@/types/product';
import type { IPage } from '@/types/common';
import { message } from 'ant-design-vue';

// 广告相关
const adverts = ref<AdvertRespDTO[]>([]);
const advertLoading = ref(false);

// 商品相关
const productList = ref<ProductRespDTO[]>([]);
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
    const queryParam: ProductPageQueryReqDTO = {
      current: pagination.value.current,
      size: pagination.value.size,
      stock: 0,
    };

    const response = await getProductList(queryParam);
    if (response.code === '0' && response.data) {
      const pageData: IPage<ProductRespDTO> = response.data;
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

// 组件挂载时获取数据
onMounted(() => {
  fetchAdverts();
  fetchProducts();
});
</script>

<style scoped>
.index-container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

/* 广告区域样式 */
.advert-section {
  width: 100%;
  max-width: 1550px;
  margin: 0 auto;
  padding: 20px 0;
}

.advert-item {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 400px;
  overflow: hidden;
}

.advert-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
  transition: transform 0.3s ease;
}

.advert-image:hover {
  transform: scale(1.02);
}

/* 商品区域样式 */
.product-section {
  width: 100%;
  max-width: 100%;
  margin: 0 auto;
  padding: 20px 0 40px;
}

.product-types {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 24px;
  padding: 20px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
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
  max-width: 240px;
  margin: 0 auto;
}

.product-card {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: box-shadow 0.3s ease;
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
}

.product-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
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
  border-radius: 0;
}

.product-image {
  width: 100%;
  height: 240px;
  object-fit: cover;
  object-position: center;
  transition: transform 0.3s ease;
}

.product-card:hover .product-image {
  transform: scale(1.05);
}

/* 使用Vue深度选择器覆盖Ant Design卡片默认样式 */
.product-card :deep(.ant-card-body) {
  padding: 2px !important;
  border-radius: 0 0 8px 8px !important;
}

/* 商品信息样式 */
.product-info {
  padding: 8px 10px;
  display: flex;
  flex-direction: column;
}

.product-title {
  font-size: 15px;
  font-weight: 500;
  margin-bottom: 3px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.product-price {
  font-size: 17px;
  font-weight: 600;
  color: #ff4d4f;
  margin-bottom: 2px;
}

.product-origin {
  font-size: 14px;
  color: #999;
}

/* 分页容器样式 */
.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}
</style>