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

    <!-- 核心区域：分类 + Banner + 用户卡片 -->
    <div class="core-section">
      <HomeCategory />
      <HomeBanner />
      <HomeUserCard />
    </div>

    <!-- 认养精选区域 -->
    <AdoptFeaturedSection style="margin-top:48px"/>

    <!-- 下部商品展示区域 -->
    <div class="fresh-market-section">
      <!-- 商品列表头部 -->
      <div class="section-header">
        <div class="header-left">
          <h2 class="section-title">
            <ShoppingOutlined class="title-icon" /> 生鲜市集
          </h2>
          <p class="section-subtitle">原产地直供，新鲜采摘，让健康走进千家万户</p>
        </div>
        <div class="header-right">
          <!-- <div class="product-tabs">
            <div
              v-for="tag in productTags"
              :key="tag.key"
              class="tab-item"
              :class="{ active: activeTag === tag.key }"
              @click="handleTagClick(tag.key)"
            >
              {{ tag.name }}
            </div>
          </div> -->
          <button class="view-all-btn" @click="router.push('/product/list')">
            去逛逛 <RightOutlined class="btn-icon" />
          </button>
        </div>
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
              :lg="4" 
              :xl="4" 
            >
              <div class="product-item">
                <a-card hoverable class="product-card" @click="goToProductDetail(product.id)">
                  <template #cover>
                    <div class="product-image-wrapper">
                      <img
                        :alt="product.title"
                        :src="getFirstImage(product.images || product.image)"
                        class="product-image"
                      />
                    </div>
                  </template>
                  <div class="product-info">
                    <div class="product-title">{{ product.title }}</div>
                    <div class="product-tags">
                       <span class="product-tag">产地直发</span>
                    </div>
                    <div class="product-bottom">
                      <div class="product-price">
                        <span v-if="product.priceSummary">¥{{ product.priceSummary.minPrice }}</span>
                        <span v-else style="font-size: 14px; color: #999;">暂无价格</span>
                      </div>
                      <div class="product-sold">已售 {{ Math.floor(Math.random() * 1000) }}+</div>
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
          :page-size-options="['20', '40']"
          :show-total="(total) => `共 ${total} 件商品`"
          @change="handlePageChange"
          @show-size-change="handleSizeChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { SearchOutlined, RightOutlined, ShoppingOutlined } from '@ant-design/icons-vue';
import { listSpuByPage } from '@/api/spu';
import { message } from 'ant-design-vue';
import AdoptFeaturedSection from '@/components/adopt/AdoptFeaturedSection.vue';
import HomeCategory from '@/components/home/HomeCategory.vue';
import HomeBanner from '@/components/home/HomeBanner.vue';
import HomeUserCard from '@/components/home/HomeUserCard.vue';

// 路由实例
const router = useRouter();

// 商品相关
const productList = ref([]);
const productLoading = ref(false);
const activeTag = ref('all');

// 分页相关
const pagination = ref({
  current: 1,
  size: 20,
  total: 0,
});

// 处理商品图片，只返回第一张
const getFirstImage = (imageStr) => {
  if (!imageStr) {
    return 'https://via.placeholder.com/200x200?text=No+Image';
  }
  return imageStr.trim().split(',')[0].trim() || imageStr;
};

// 获取商品数据
const fetchProducts = async () => {
  productLoading.value = true;
  try {
    // 构建查询参数
    const queryParam = {
      current: pagination.value.current,
      size: pagination.value.size,
      status: 1, // 上架商品
    };

    const response = await listSpuByPage(queryParam);
    if (response.code === '0' && response.data) {
      const pageData = response.data;
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
const handleTagClick = (tagKey) => {
  activeTag.value = tagKey;
  pagination.value.current = 1;
  fetchProducts();
};

// 分页变化事件
const handlePageChange = (page) => {
  pagination.value.current = page;
  fetchProducts();
};

// 每页条数变化事件
const handleSizeChange = (current, size) => {
  pagination.value.current = current;
  pagination.value.size = size;
  fetchProducts();
};

// 跳转到商品详情页
const goToProductDetail = (productId) => {
  router.push({
    name: 'productDetail',
    params: { id: productId.toString() }
  });
};

// 组件挂载时获取数据
onMounted(() => {
  fetchProducts();
});
</script>

<style scoped>
.index-container {
  min-height: 100vh;
  padding-bottom: 40px;
}

.header-container {
  width: 100%;
  max-width: 1620px;
  margin: 0 auto;
  padding: 0 20px;
}

/* Section Header Styles */
.section-header {
  margin-top: 36px;
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 32px;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.section-title {
  font-size: 24px;
  font-weight: 800;
  color: #0f172a;
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
}

.title-icon {
  color: #f59e0b;
  font-size: 24px;
}

.section-subtitle {
  margin-top: 4px;
  font-size: 14px;
  color: #64748b;
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.view-all-btn {
  padding: 8px 20px;
  height: 40px;
  border-radius: 9999px;
  font-size: 14px;
  font-weight: 500;
  color: #475569;
  border: 1px solid #e2e8f0;
  background: white;
  display: flex;
  align-items: center;
  gap: 8px;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
  cursor: pointer;
}

.view-all-btn:hover {
  color: #16a34a;
  border-color: #bbf7d0;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
}

.btn-icon {
  font-size: 12px;
  transition: transform 0.3s;
}

.view-all-btn:hover .btn-icon {
  transform: translateX(4px);
}

/* Main Header */
.main-header {
  padding: 24px 0;
  background-color: #FFFFFF;
  margin-bottom: 24px;
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
  font-weight: 800;
  color: #1e293b;
  margin: 0;
  line-height: 1;
  letter-spacing: -1px;
}

.logo-sub {
  font-size: 12px;
  color: #16a34a;
  font-weight: 600;
  display: block;
  letter-spacing: 1px;
  margin-top: 4px;
  text-transform: uppercase;
}

/* Search Box - Centered as requested */
.search-section {
  flex: 1;
  max-width: 700px;
  margin: 0 auto; /* Centering */
}

.search-box {
  display: flex;
  position: relative;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
  border-radius: 9999px;
}

.search-input-wrapper {
  flex: 1;
  border: 2px solid #e2e8f0;
  border-right: none;
  border-radius: 9999px 0 0 9999px;
  overflow: hidden;
  display: flex;
  align-items: center;
  padding-left: 20px;
  background-color: #f8fafc;
  height: 48px;
  transition: all 0.2s;
}

.search-input-wrapper:focus-within {
  border-color: #22c55e;
  background-color: #fff;
  box-shadow: 0 0 0 3px rgba(34, 197, 94, 0.1);
}

.search-icon {
  color: #94a3b8;
  margin-right: 12px;
  font-size: 18px;
}

.search-input {
  width: 100%;
  height: 100%;
  border: none;
  outline: none;
  font-size: 14px;
  color: #1e293b;
  background: transparent;
}

.search-button {
  background-color: #15803d; /* primary-700 */
  color: #FFFFFF;
  padding: 0 36px;
  border-radius: 0 9999px 9999px 0;
  font-weight: 600;
  font-size: 16px;
  border: none;
  cursor: pointer;
  transition: background-color 0.2s;
  height: 48px;
}

.search-button:hover {
  background-color: #14532d;
}

/* Core Section (Grid) */
.core-section {
  width: 100%;
  max-width: 1620px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  height: 460px;
  gap: 24px;
}

/* Product List */
.product-list {
  width: 100%;
}

.product-card {
  border: none;
  border-radius: 16px;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  overflow: hidden;
  background-color: #FFFFFF;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
}

.product-image-wrapper {
  width: 100%;
  height: 240px;
  overflow: hidden;
  background-color: #f8fafc;
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.product-card:hover .product-image {
  transform: scale(1.05);
}

.product-card :deep(.ant-card-body) {
  padding: 16px !important;
}

.product-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.product-title {
  font-size: 14px;
  font-weight: 500;
  color: #0f172a;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.5;
}

.product-tags {
  display: flex;
  gap: 4px;
  height: 20px;
}

.product-tag {
  font-size: 10px;
  color: #16a34a;
  background-color: #f0fdf4;
  border: 1px solid #dcfce7;
  padding: 0 4px;
  border-radius: 4px;
}

.product-bottom {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-top: 4px;
}

.product-price {
  font-size: 20px;
  font-weight: 700;
  color: #dc2626;
  line-height: 1;
}

.product-sold {
  font-size: 10px;
  color: #94a3b8;
}

/* Pagination */
.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 48px;
}
</style>