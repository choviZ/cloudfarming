<template>
  <div class="adopt-list-container">
    <!-- Header Section -->
    <section class="header-section">
      <!-- Logo/Home Link -->
      <div class="logo-container" @click="router.push('/')">
        <div class="logo-icon">
          <i class="fas fa-leaf"></i>
        </div>
        <span class="logo-text">云养殖平台</span>
      </div>

      <div class="header-content">
        <h1 class="page-title">探索优质认养项目</h1>
        <p class="page-subtitle">真实的农场体验，看得见的绿色健康</p>
        
        <!-- Search Bar -->
        <div class="search-container group">
          <div class="search-icon-wrapper">
            <i class="fas fa-search search-icon"></i>
          </div>
          <input 
            type="text" 
            class="search-input"
            v-model="searchParams.title"
            @keyup.enter="handleSearch"
            placeholder="搜索动物、农场或产品名称..."
          >
          <button class="search-btn" @click="handleSearch">
            搜索
          </button>
        </div>

        <!-- Filter Tags -->
        <div class="filter-tags">
          <button 
            class="filter-tag" 
            :class="{ active: searchParams.status === undefined }"
            @click="handleFilterChange(undefined)"
          >
            全部
          </button>
          <!-- User requested to keep only "All" for now -->
        </div>
      </div>
    </section>

    <!-- Project List Section -->
    <main class="main-content">
      <a-spin :spinning="loading">
        <div class="card-grid">
          <div 
            v-for="item in list" 
            :key="item.id" 
            class="project-card group"
            @click="goToDetail(item.id)"
          >
            <!-- Image Area -->
            <div class="card-image-wrapper">
              <img :src="item.coverImage" :alt="item.title" class="card-image">
              <!-- Status Tag -->
              <div 
                class="status-badge"
                :class="item.status === 1 ? 'status-active' : 'status-ended'"
              >
                {{ item.status === 1 ? '可认养' : '已结束' }}
              </div>
            </div>
            
            <!-- Content Area -->
            <div class="card-content">
              <h3 class="card-title" :title="item.title">{{ item.title }}</h3>
              
              <div class="price-row">
                <div class="price-container">
                  <span class="currency">¥</span>
                  <span class="amount">{{ item.price }}</span>
                </div>
                <div class="period-badge">
                  {{ item.adoptDays }}天周期
                </div>
              </div>
              
              <p class="yield-info">预计收益 {{ item.expectedYield || '暂无' }}</p>
              
              <div class="card-footer">
                <div class="footer-left">
                  <div class="pure-icon">纯</div>
                  <span class="footer-text">云农场精选</span>
                </div>
                <span class="footer-right">云端牧场</span>
              </div>
            </div>
          </div>
        </div>

        <div v-if="list.length === 0 && !loading" class="empty-state">
          <a-empty description="暂无认养项目" />
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
import { pageAdoptItems } from '@cloudfarming/core';
import type { AdoptItemResp, AdoptItemPageReq } from '@cloudfarming/core';
import { message } from 'ant-design-vue';

const router = useRouter();
const loading = ref(false);
const list = ref<AdoptItemResp[]>([]);
const total = ref(0);

const pagination = reactive({
  current: 1,
  size: 20
});

const searchParams = reactive({
  title: '',
  status: undefined as number | undefined
});

const fetchData = async () => {
  loading.value = true;
  try {
    const req: AdoptItemPageReq = {
      current: pagination.current,
      size: pagination.size,
      title: searchParams.title,
      status: searchParams.status
    };
    
    const res = await pageAdoptItems(req);
    if (res.code === '0' && res.data) {
      list.value = res.data.records;
      total.value = res.data.total;
    } else {
      message.error(res.message || '获取列表失败');
    }
  } catch (err) {
    message.error('系统繁忙，请稍后重试');
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  pagination.current = 1;
  fetchData();
};

const handleFilterChange = (status: number | undefined) => {
  searchParams.status = status;
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

const goToDetail = (id: string) => {
  router.push(`/adopt/detail/${id}`);
};

onMounted(() => {
  fetchData();
});
</script>

<style scoped>
/* FontAwesome for icons (ensure it's loaded in index.html or use Ant icons if preferred, keeping existing link if any) */
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

.adopt-list-container {
  min-height: 100vh;
  background-color: #f9fafb; /* gray-50 */
  font-family: 'Inter', system-ui, -apple-system, sans-serif;
}

/* Header Section */
.header-section {
  background: #fff;
  border-bottom: 1px solid #f3f4f6;
  padding: 3rem 1rem;
  position: relative;
}

.logo-container {
  position: absolute;
  top: 1.5rem;
  left: 2rem;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  cursor: pointer;
  transition: opacity 0.3s;
  z-index: 10;
}

.logo-container:hover {
  opacity: 0.8;
}

.logo-icon {
  width: 2.5rem;
  height: 2.5rem;
  background-color: #10b981;
  color: white;
  border-radius: 0.75rem;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.25rem;
  box-shadow: 0 4px 6px -1px rgba(16, 185, 129, 0.2);
}

.logo-text {
  font-size: 1.25rem;
  font-weight: 700;
  color: #111827;
  letter-spacing: -0.025em;
}

@media (max-width: 768px) {
  .logo-container {
    position: static;
    justify-content: center;
    margin-bottom: 2rem;
  }
  
  .header-section {
    padding-top: 1.5rem;
  }
}

.header-content {
  max-width: 48rem; /* max-w-3xl */
  margin: 0 auto;
  text-align: center;
}

.page-title {
  font-size: 1.875rem; /* 3xl */
  font-weight: 700;
  color: #111827; /* gray-900 */
  margin-bottom: 1rem;
}

.page-subtitle {
  color: #6b7280; /* gray-500 */
  margin-bottom: 2rem;
}

/* Search Bar */
.search-container {
  position: relative;
  margin-bottom: 2rem;
  max-width: 100%;
}

.search-icon-wrapper {
  position: absolute;
  top: 0;
  bottom: 0;
  left: 0;
  padding-left: 1rem;
  display: flex;
  align-items: center;
  pointer-events: none;
}

.search-icon {
  color: #9ca3af; /* gray-400 */
  transition: color 0.3s;
}

.search-container:focus-within .search-icon {
  color: #10b981; /* primary */
}

.search-input {
  display: block;
  width: 100%;
  padding-left: 3rem;
  padding-right: 1rem;
  padding-top: 1rem;
  padding-bottom: 1rem;
  border: 1px solid #e5e7eb; /* gray-200 */
  border-radius: 9999px;
  line-height: 1.25rem;
  color: #1f2937;
  outline: none;
  transition: all 0.3s;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
}

.search-input:focus {
  background-color: #fff;
  border-color: #10b981;
  box-shadow: 0 0 0 4px rgba(16, 185, 129, 0.1);
}

.search-btn {
  position: absolute;
  top: 0.375rem;
  bottom: 0.375rem;
  right: 0.375rem;
  padding: 0 1.5rem;
  background-color: #10b981; /* primary */
  color: white;
  border-radius: 9999px;
  font-weight: 500;
  border: none;
  cursor: pointer;
  transition: background-color 0.3s;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.search-btn:hover {
  background-color: #059669; /* emerald-600 */
}

/* Filter Tags */
.filter-tags {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 0.75rem;
}

.filter-tag {
  padding: 0.375rem 1rem;
  border-radius: 9999px;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
}

.filter-tag.active {
  background-color: #10b981;
  color: white;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
}

.filter-tag:not(.active) {
  background-color: #fff;
  color: #4b5563; /* gray-600 */
  border-color: #e5e7eb; /* gray-200 */
}

.filter-tag:not(.active):hover {
  border-color: #10b981;
  color: #10b981;
}

/* Main Content */
.main-content {
  background-color: white;
  max-width: 1920px;
  margin: 0 auto;
  padding: 2rem 1rem;
}

@media (min-width: 640px) {
  .main-content { padding: 2rem 1.5rem; }
}
@media (min-width: 1024px) {
  .main-content { padding: 2rem 2rem; }
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.section-title {
  font-size: 1.25rem;
  font-weight: 700;
  color: #1f2937;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.title-indicator {
  width: 0.375rem;
  height: 1.5rem;
  background-color: #10b981;
  border-radius: 9999px;
}

/* Card Grid */
.card-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1.25rem; /* gap-5 */
}

@media (min-width: 768px) {
  .card-grid { grid-template-columns: repeat(3, 1fr); }
}
@media (min-width: 1024px) {
  .card-grid { grid-template-columns: repeat(4, 1fr); }
}
@media (min-width: 1280px) {
  .card-grid { grid-template-columns: repeat(5, 1fr); }
}
@media (min-width: 1536px) {
  .card-grid { grid-template-columns: repeat(6, 1fr); }
}

/* Project Card */
.project-card {
  background: #fff;
  border-radius: 0.75rem; /* rounded-xl */
  border: 1px solid #f3f4f6; /* gray-100 */
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
}

.project-card:hover {
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
  transform: translateY(-0.25rem);
}

.card-image-wrapper {
  position: relative;
  aspect-ratio: 4 / 3;
  background-color: #f3f4f6;
  overflow: hidden;
}

.card-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s;
}

.project-card:hover .card-image {
  transform: scale(1.05);
}

.status-badge {
  position: absolute;
  top: 0.5rem;
  left: 0.5rem;
  padding: 0.125rem 0.5rem;
  border-radius: 9999px;
  font-size: 0.625rem;
  font-weight: 500;
  color: white;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
}

.status-active {
  background: linear-gradient(to right, #22c55e, #059669); /* green-500 to emerald-600 */
}

.status-ended {
  background: linear-gradient(to right, #6b7280, #4b5563); /* gray-500 to gray-600 */
}

.card-content {
  padding: 0.75rem; /* p-3 */
}

.card-title {
  font-size: 0.9375rem; /* 15px */
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 0.5rem;
  line-height: 1.25rem;
  
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color 0.3s;
}

.project-card:hover .card-title {
  color: #10b981;
}

.price-row {
  display: flex;
  align-items: baseline;
  gap: 0.5rem;
  margin-bottom: 0.25rem;
}

.price-container {
  color: #ef4444; /* red-500 */
  font-weight: 700;
  font-size: 1.125rem; /* lg */
}

.currency {
  font-size: 0.875rem; /* sm */
}

.period-badge {
  background-color: #f3f4f6; /* gray-100 */
  color: #6b7280; /* gray-500 */
  font-size: 0.625rem; /* 10px */
  padding: 0.125rem 0.375rem;
  border-radius: 0.25rem;
}

.yield-info {
  font-size: 0.75rem; /* xs */
  color: #6b7280; /* gray-500 */
  margin-bottom: 0.75rem;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid #f9fafb; /* gray-50 */
  padding-top: 0.5rem;
}

.footer-left {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.pure-icon {
  width: 0.875rem;
  height: 0.875rem;
  background-color: #e5e7eb; /* gray-200 */
  border-radius: 9999px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.5rem; /* 8px */
  color: #6b7280; /* gray-500 */
}

.footer-text {
  font-size: 0.625rem; /* 10px */
  color: #6b7280; /* gray-500 */
}

.footer-right {
  font-size: 0.625rem; /* 10px */
  color: #9ca3af; /* gray-400 */
}

.empty-state {
  padding: 60px 0;
}

.pagination-wrapper {
  margin-top: 2rem;
  text-align: center;
  display: flex;
  justify-content: center;
}
</style>
