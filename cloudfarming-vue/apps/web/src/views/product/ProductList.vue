<template>
  <div class="product-list-container">
    <!-- Header Section -->
    <header class="header-section">
      <div class="header-inner">
        <!-- Logo -->
        <div class="logo-container" @click="router.push('/')">
          <div class="logo-icon">
            <i class="fas fa-leaf"></i>
          </div>
          <div class="logo-text-group">
            <h1 class="logo-title">云养殖平台</h1>
            <span class="logo-subtitle">Cloud Farming</span>
          </div>
        </div>

        <!-- Search Bar -->
        <div class="search-area">
          <div class="search-box">
            <input 
              type="text" 
              class="search-input" 
              v-model="searchParams.spuName"
              @keyup.enter="handleSearch"
              placeholder="搜索 '有机草莓' 领券立减20元"
            >
            <!-- Camera Icon (Visual Only) -->
            <button class="search-camera-btn" title="按图搜索">
              <i class="fas fa-camera"></i>
            </button>
            <button class="search-btn" @click="handleSearch">
              <i class="fas fa-search"></i> 搜索
            </button>
          </div>
          <div class="search-hot-words">
            <!-- 
            <span>热搜：</span>
            <a href="#" @click.prevent="quickSearch('新鲜采摘')">新鲜采摘</a>
             -->
          </div>
        </div>

        <!-- Cart -->
        <div class="cart-container" @click="router.push('/cart')">
          <div class="cart-btn">
            <div class="cart-icon-wrapper">
              <i class="fas fa-shopping-cart"></i>
              <span class="cart-badge">2</span>
            </div>
            <span class="cart-text">我的购物车</span>
          </div>
        </div>
      </div>
    </header>

    <!-- Filter & Sort Section -->
    <div class="filter-section-wrapper">
      <div class="filter-container">
        <div class="filter-row">
          <!-- Left: Categories -->
          <div class="category-list-area">
            <div class="category-scroll-container">
              <div 
                class="category-item" 
                :class="{ active: !searchParams.categoryId }"
                @click="handleCategoryClick('')"
              >
                <span class="category-name">全部</span>
              </div>
              <div 
                v-for="cat in categoryList" 
                :key="cat.id" 
                class="category-item"
                :class="{ active: searchParams.categoryId === cat.id }"
                @click="handleCategoryClick(cat.id)"
              >
                <!-- Placeholder icon since API doesn't return one -->
                <i class="fas fa-leaf category-icon"></i>
                <span class="category-name">{{ cat.name }}</span>
              </div>
            </div>
          </div>

          <!-- Right: Sort & Actions -->
          <div class="sort-area">
            <div class="sort-group">
              <button class="sort-btn active">综合</button>
              <button class="sort-btn">销量 <i class="fas fa-sort"></i></button>
              <button class="sort-btn">价格 <i class="fas fa-sort"></i></button>
            </div>
            <div class="divider"></div>
            <button class="more-filter-btn">
              <i class="fas fa-filter"></i> 更多筛选
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Product Grid -->
    <main class="main-content">
      <a-spin :spinning="loading">
        <div class="product-grid" v-if="list.length > 0">
          <div 
            v-for="item in list" 
            :key="item.id" 
            class="product-card"
            @click="goToDetail(item.id)"
          >
            <!-- Image -->
            <div class="card-image-wrapper">
              <img :src="getMainImage(item.images || item.image)" :alt="item.title" class="card-image">
              <div class="hover-actions">
                <button class="action-btn" @click.stop="toggleFav(item.id)">
                  <i class="far fa-heart"></i>
                </button>
              </div>
            </div>

            <!-- Content -->
            <div class="card-content">
              <!-- Price -->
              <div class="price-row">
                <span class="currency">¥</span>
                <span class="amount">{{ getPrice(item) }}</span>
              </div>

              <!-- Title -->
              <h3 class="product-title" :title="item.title">
                {{ item.title }}
              </h3>

              <!-- Tags -->
              <div class="tags-row">
                <!-- TODO 标签
                 <span class="tag-promo">次日达</span>
                <span class="tag-coupon">满99减20</span>
                -->
              </div>

              <!-- CTA -->
              <button class="add-cart-btn" @click.stop="addToCart(item)">
                <i class="fas fa-shopping-cart"></i> 加入购物车
              </button>
            </div>
          </div>
        </div>

        <!-- Empty State -->
        <div v-else-if="!loading" class="empty-state">
          <a-empty description="暂无商品" />
        </div>

        <!-- Pagination -->
        <div class="pagination-wrapper" v-if="total > 0">
          <a-pagination
            v-model:current="pagination.current"
            v-model:page-size="pagination.size"
            :total="total"
            :show-total="(total: number) => `共 ${total} 条`"
            show-size-changer
            @change="handlePageChange"
            @showSizeChange="handleSizeChange"
          />
        </div>
      </a-spin>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import { listSpuByPage } from '@cloudfarming/core/api/spu';
import type { SpuRespDTO, SpuPageQueryReqDTO } from '@cloudfarming/core/api/spu';
import { getTopLevelCategories } from '@cloudfarming/core/api/category';
import type { CategoryRespDTO } from '@cloudfarming/core/api/category';

const router = useRouter();
const loading = ref(false);
const list = ref<SpuRespDTO[]>([]);
const total = ref(0);
const categoryList = ref<CategoryRespDTO[]>([]);

const pagination = reactive({
  current: 1,
  size: 20
});

const searchParams = reactive({
  spuName: '',
  categoryId: '',
  status: 1 // 默认查询上架商品
});

// Mock index for badge (in real app, use logic)
const index = 0; // Placeholder

const getMainImage = (imgStr: string) => {
  if (!imgStr) return 'https://via.placeholder.com/400x400?text=No+Image';
  // Trim whitespace and get first image
  return imgStr.trim().split(',')[0].trim();
};

const getPrice = (item: SpuRespDTO) => {
  if (item.priceSummary) {
    return item.priceSummary.minPrice.toFixed(2);
  }
  return '待定';
};

const fetchCategories = async () => {
  try {
    const res = await getTopLevelCategories();
    if (res.code == '0') {
      categoryList.value = res.data;
    }
  } catch (error) {
    console.error('Failed to fetch categories:', error);
  }
};

const handleCategoryClick = (id: string) => {
  searchParams.categoryId = id;
  // Reset to first page when filtering
  pagination.current = 1;
  fetchData();
};

const fetchData = async () => {
  loading.value = true;
  try {
    const req: SpuPageQueryReqDTO = {
      current: pagination.current,
      size: pagination.size,
      spuName: searchParams.spuName,
      categoryId: searchParams.categoryId,
      status: searchParams.status
    };
    
    const res = await listSpuByPage(req);
    if (res.code === '0' && res.data) {
      list.value = res.data.records;
      total.value = res.data.total;
    } else {
      message.error(res.message || '获取商品列表失败');
    }
  } catch (err) {
    console.error(err);
    message.error('系统繁忙，请稍后重试');
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  pagination.current = 1;
  fetchData();
};

const quickSearch = (keyword: string) => {
  searchParams.spuName = keyword;
  handleSearch();
};

const handlePageChange = (page: number) => {
  pagination.current = page;
  fetchData();
};

const handleSizeChange = (current: number, size: number) => {
  pagination.current = 1;
  pagination.size = size;
  fetchData();
};

const goToDetail = (id: number) => {
  router.push(`/product/${id}`);
};

const toggleFav = (id: number) => {
  message.success('收藏成功');
};

const addToCart = (item: SpuRespDTO) => {
  message.success(`已将 ${item.title} 加入购物车`);
  // Implement cart logic
};

onMounted(() => {
  fetchCategories();
  fetchData();
});
</script>

<style scoped>
/* FontAwesome Import (Ensure this is loaded in index.html, if not, consider using Ant Icons) */
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css');

.product-list-container {
  min-height: 100vh;
  font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
  color: #1f2937;
}

/* Header Section */
.header-section {
  background: #fff;
  position: sticky;
  top: 0;
  z-index: 40;
}

.header-inner {
  max-width: 1620px;
  margin: 0 auto;
  padding: 1.5rem 1.5rem 1rem;
  display: flex;
  align-items: center;
  gap: 3rem;
}

/* Logo */
.logo-container {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  cursor: pointer;
  flex-shrink: 0;
}

.logo-icon {
  width: 2.5rem;
  height: 2.5rem;
  background-color: #10b981; /* brand-600 */
  color: white;
  border-radius: 0.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.25rem;
  box-shadow: 0 4px 6px -1px rgba(16, 185, 129, 0.2);
}

.logo-text-group {
  display: flex;
  flex-direction: column;
}

.logo-title {
  font-size: 1.5rem;
  font-weight: 700;
  line-height: 1;
  color: #111827;
  margin: 0;
}

.logo-subtitle {
  font-size: 0.625rem;
  color: #10b981;
  font-weight: 700;
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

/* Search Bar */
.search-area {
  flex: 1;
  /* Remove max-width to fill space like Taobao */
  /* max-width: 42rem; */
}

.search-box {
  display: flex;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
  background-color: #fff;
  border: 2px solid #10b981;
  border-radius: 0.5rem;
  overflow: hidden; /* Ensure children don't overflow corners */
}

.search-input {
  flex: 1;
  border: none; /* Border moved to container */
  padding: 0.625rem 1.25rem;
  outline: none;
  font-size: 0.875rem;
  color: #374151;
}

.search-camera-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 1rem;
  background: transparent;
  border: none;
  color: #9ca3af;
  cursor: pointer;
  transition: color 0.2s;
  font-size: 1.125rem;
}

.search-camera-btn:hover {
  color: #10b981;
}

.search-btn {
  background-color: #10b981;
  color: white;
  padding: 0 2.5rem; /* Wider button like Taobao */
  font-weight: 700;
  font-size: 1.125rem; /* Larger text */
  border: none;
  cursor: pointer;
  transition: background-color 0.2s;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  border-radius: 0; /* Container has radius */
}

.search-btn:hover {
  background-color: #059669; /* brand-700 */
}

.search-hot-words {
  margin-top: 0.5rem;
  font-size: 0.75rem;
  color: #6b7280;
  display: flex;
  gap: 1rem;
}

.search-hot-words a {
  color: #4b5563;
  text-decoration: none;
  transition: color 0.2s;
}

.search-hot-words a:hover {
  color: #10b981;
}

/* Cart */
.cart-container {
  flex-shrink: 0;
}

.cart-btn {
  border: 1px solid #e5e7eb;
  background: #fff;
  padding: 0.625rem 1.25rem;
  border-radius: 0.5rem;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}

.cart-btn:hover {
  border-color: #10b981;
  color: #10b981;
}

.cart-icon-wrapper {
  position: relative;
}

.cart-badge {
  position: absolute;
  top: -0.5rem;
  right: -0.5rem;
  background-color: #ef4444; /* price-600 */
  color: white;
  font-size: 0.625rem;
  font-weight: 700;
  width: 1rem;
  height: 1rem;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid white;
}

.cart-text {
  font-size: 0.875rem;
  font-weight: 500;
}

/* Nav Bar */
.nav-bar {
  max-width: 1620px;
  margin: 0 auto;
  padding: 0 1.5rem;
}

.nav-inner {
  display: flex;
  align-items: center;
  gap: 2rem;
  font-size: 0.9375rem;
  font-weight: 700;
  color: #374151;
  height: 3rem;
}

.nav-category {
  background-color: #111827; /* gray-900 */
  color: white;
  padding: 0.625rem 1.25rem;
  border-radius: 0.5rem;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  cursor: pointer;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.nav-link {
  color: #374151;
  text-decoration: none;
  position: relative;
  transition: color 0.2s;
}

.nav-link:hover, .nav-link.active {
  color: #10b981;
}

/* Filter Section */
.filter-section-wrapper {
  max-width: 1620px;
  margin: 1.5rem auto;
  padding: 0 1.5rem;
}

.filter-container {
  background: #fff;
  border-radius: 0.75rem;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
  border: 1px solid #f3f4f6;
  padding: 0.25rem;
}

.filter-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 1rem;
  gap: 1rem;
}

/* Category List */
.category-list-area {
  flex: 1;
  min-width: 0;
  margin-right: 1rem;
}

.category-scroll-container {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  overflow-x: auto;
  padding: 0.25rem 0;
  /* Hide scrollbar */
  -ms-overflow-style: none;
  scrollbar-width: none;
}
.category-scroll-container::-webkit-scrollbar {
  display: none;
}

.category-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.375rem 1rem;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 9999px;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
  font-size: 0.875rem;
  color: #374151;
}

.category-item:hover {
  border-color: #10b981;
  color: #10b981;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

.category-item.active {
  background-color: #ecfdf5;
  border-color: #10b981;
  color: #059669;
  font-weight: 500;
}

.category-icon {
  font-size: 0.875rem;
  color: #9ca3af;
}

.category-item:hover .category-icon,
.category-item.active .category-icon {
  color: #10b981;
}

.divider {
  width: 1px;
  height: 1rem;
  background-color: #e5e7eb;
  margin: 0 0.5rem;
}

.selected-tags {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.tag-item {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  background-color: #f9fafb;
  color: #4b5563;
  padding: 0.25rem 0.5rem;
  border-radius: 0.25rem;
  font-size: 0.75rem;
  border: 1px solid #e5e7eb;
  cursor: pointer;
  transition: all 0.2s;
}

.brand-tag {
  background-color: #ecfdf5; /* brand-50 */
  color: #047857; /* brand-700 */
  border-color: #d1fae5;
}

.tag-item:hover {
  border-color: #10b981;
}

.tag-item:hover i {
  color: #ef4444;
}

.clear-btn {
  font-size: 0.75rem;
  color: #10b981;
  cursor: pointer;
  margin-left: 0.25rem;
}

.clear-btn:hover {
  text-decoration: underline;
}

.sort-area {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex-shrink: 0;
}

.sort-group {
  display: flex;
  align-items: center;
  background-color: #f9fafb;
  border-radius: 0.5rem;
  padding: 0.25rem;
  border: 1px solid #f3f4f6;
}

.sort-btn {
  padding: 0.375rem 0.75rem;
  font-size: 0.75rem;
  font-weight: 500;
  color: #4b5563;
  background: transparent;
  border: none;
  cursor: pointer;
  border-radius: 0.375rem;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.sort-btn:hover {
  color: #10b981;
}

.sort-btn.active {
  background-color: white;
  color: #10b981;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}

.more-filter-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.875rem;
  font-weight: 500;
  color: #374151;
  background: none;
  border: none;
  cursor: pointer;
}

.more-filter-btn:hover {
  color: #10b981;
}

.quick-filter-row {
  border-top: 1px solid #f3f4f6;
  padding: 0.625rem 1rem;
  display: flex;
  align-items: center;
  gap: 1.5rem;
  font-size: 0.75rem;
  color: #4b5563;
  background-color: rgba(249, 250, 251, 0.5);
  border-bottom-left-radius: 0.75rem;
  border-bottom-right-radius: 0.75rem;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  cursor: pointer;
  transition: color 0.2s;
}

.checkbox-label:hover {
  color: #10b981;
}

.checkbox-label input {
  accent-color: #10b981;
}

/* Product Grid */
.main-content {
  max-width: 1620px;
  margin: 0 auto;
  padding: 0 1.5rem 4rem;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 1.5rem;
}

.product-card {
  background: #fff;
  border-radius: 0.75rem;
  border: 1px solid transparent; /* Softer border */
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  transition: all 0.3s;
  cursor: pointer;
  position: relative;
}

.product-card:hover {
  border-color: #c3eed2; /* brand-200 */
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
  transform: translateY(-2px);
}

.card-image-wrapper {
  position: relative;
  width: 100%;
  padding-bottom: 100%; /* Square */
  background-color: #f9fafb;
  overflow: hidden;
}

.card-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  mix-blend-mode: multiply;
  transition: transform 0.5s;
}

.product-card:hover .card-image {
  transform: scale(1.05);
}

.badge {
  background-color: #ef4444;
  color: white;
  font-size: 0.625rem;
  font-weight: 700;
  padding: 0.125rem 0.5rem;
  border-radius: 0.25rem;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}

.card-badges {
  position: absolute;
  top: 0.5rem;
  left: 0.5rem;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.hover-actions {
  position: absolute;
  top: 0.5rem;
  right: 0.5rem;
  opacity: 0;
  transition: opacity 0.3s;
}

.product-card:hover .hover-actions {
  opacity: 1;
}

.action-btn {
  width: 2rem;
  height: 2rem;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(4px);
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6b7280;
  cursor: pointer;
  box-shadow: 0 1px 2px rgba(0,0,0,0.1);
  transition: all 0.2s;
}

.action-btn:hover {
  color: #10b981;
  transform: scale(1.1);
}

.card-content {
  padding: 1rem;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.5rem; /* Added gap */
}

.price-row {
  display: flex;
  align-items: baseline;
  gap: 0.125rem;
  color: #ef4444; /* price-600 */
}

.currency {
  font-size: 0.875rem; /* Larger */
  font-weight: 700;
}

.amount {
  font-size: 1.5rem; /* Larger */
  font-weight: 700;
  line-height: 1;
}

.product-title {
  font-size: 0.875rem;
  color: #111827;
  font-weight: 500;
  line-height: 1.4;
  height: 2.5rem; /* 2 lines */
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  transition: color 0.2s;
  margin-bottom: 0; /* Handled by gap */
}

.product-card:hover .product-title {
  color: #10b981;
}

.tag-self {
  background-color: #10b981;
  color: white;
  padding: 0 0.25rem;
  border-radius: 0.125rem;
  font-size: 0.625rem;
  font-weight: 700;
  margin-right: 0.25rem;
  vertical-align: text-bottom;
}

.tags-row {
  display: flex;
  flex-wrap: wrap;
  gap: 0.25rem;
  margin-top: 0.25rem;
}

.tag-promo {
  color: #047857;
  font-size: 0.625rem;
  padding: 0 0.25rem;
  border-radius: 0.25rem;
  border: 1px solid #d1fae5;
}

.tag-coupon {
  color: #dc2626;
  font-size: 0.625rem;
  padding: 0 0.25rem;
  border-radius: 0.25rem;
  border: 1px solid #fee2e2;
}

.card-footer {
  margin-top: auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.75rem;
  color: #9ca3af;
}

.shop-info {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.shop-self {
  background-color: #ecfdf5; /* brand-50 */
  color: #10b981;
  padding: 0 0.25rem;
  border-radius: 0.125rem;
  font-size: 0.625rem;
}

.add-cart-btn {
  width: 100%;
  background-color: #10b981;
  color: white;
  border: none;
  padding: 0.625rem;
  border-radius: 0.5rem;
  font-weight: 700;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  transition: all 0.3s;
  margin-top: 0.5rem;
  box-shadow: 0 2px 4px rgba(16, 185, 129, 0.2);
}

.add-cart-btn:hover {
  background-color: #059669;
  box-shadow: 0 4px 6px rgba(16, 185, 129, 0.3);
  transform: translateY(-1px);
}

.empty-state {
  padding: 4rem 0;
}

.pagination-wrapper {
  margin-top: 2rem;
  display: flex;
  justify-content: center;
}
</style>