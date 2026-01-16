<template>
  <div class="adopt-list-container">
    <!-- 头部筛选区 -->
    <div class="filter-section">
      <div class="filter-bar">
        <a-form layout="inline" class="filter-form">
          <a-form-item>
             <a-radio-group v-model:value="searchParams.status" button-style="solid" @change="handleSearch">
                <a-radio-button :value="undefined">全部</a-radio-button>
                <a-radio-button :value="1">可认养</a-radio-button>
                <a-radio-button :value="0">已结束</a-radio-button>
             </a-radio-group>
          </a-form-item>
          
          <div class="right-filters">
             <a-input-search
              v-model:value="searchParams.title"
              placeholder="搜索认养项目"
              enter-button
              allow-clear
              @search="handleSearch"
              style="width: 260px"
            />
          </div>
        </a-form>
      </div>
    </div>

    <!-- 列表展示区 -->
    <div class="list-section">
      <a-spin :spinning="loading">
        <div class="card-grid">
          <div v-for="item in list" :key="item.id" class="card-wrapper">
            <div class="idle-card" @click="goToDetail(item.id)">
              <div class="card-cover">
                <img :src="item.coverImage" :alt="item.title" />
                <div v-if="item.status === 1" class="status-tag active">可认养</div>
                <div v-else class="status-tag over">已结束</div>
              </div>
              
              <div class="card-info">
                <h3 class="card-title" :title="item.title">{{ item.title }}</h3>
                
                <div class="card-price-row">
                  <div class="price-box">
                    <span class="currency">¥</span>
                    <span class="amount">{{ item.price }}</span>
                  </div>
                  <div class="period-tag">{{ item.adoptDays }}天周期</div>
                </div>

                <div class="card-meta">
                  <div class="meta-item">
                    <span class="label">预计收益</span>
                    <span class="value">{{ item.expectedYield || '暂无' }}</span>
                  </div>
                </div>

                <!-- 底部用户信息模拟 -->
                <!-- TODO 实际应根据用户信息展示 -->
                <div class="card-footer">
                  <div class="user-info">
                    <div class="avatar-placeholder">
                       {{ item.title.charAt(0) }}
                    </div>
                    <span class="username">云农场精选</span>
                  </div>
                  <span class="location">云端牧场</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-if="list.length === 0 && !loading" class="empty-state">
          <a-empty description="暂无认养项目" />
        </div>

        <!-- 分页 -->
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
    </div>
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
  size: 20 // 增加每页显示数量，适应瀑布流感觉
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
  message.info('详情页开发中');
};

onMounted(() => {
  fetchData();
});
</script>

<style scoped>
.adopt-list-container {
  width: 100%;
  max-width: 1400px; /* 加宽容器 */
  margin: 0 auto;
  padding: 20px;
  min-height: calc(100vh - 64px);
  background-color: #f6f7f8;
}

/* 筛选区样式优化 */
.filter-section {
  margin-bottom: 20px;
  background: #fff;
  padding: 16px 24px;
  border-radius: 12px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.02);
}

.filter-bar {
  display: flex;
  align-items: center;
}

.filter-form {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.right-filters {
  margin-left: auto;
}

/* Grid 布局模拟瀑布流卡片效果 */
.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
}

.card-wrapper {
  height: 100%;
}

/* 闲鱼风格卡片 */
.idle-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  height: 100%;
  display: flex;
  flex-direction: column;
  border: 1px solid transparent;
}

.idle-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 20px rgba(0,0,0,0.08);
  border-color: rgba(0,0,0,0.03);
}

.card-cover {
  position: relative;
  padding-top: 100%; /* 1:1 正方形图片区域，或者可以改小一点如 75% */
  background: #f4f4f4;
  overflow: hidden;
}

.card-cover img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.idle-card:hover .card-cover img {
  transform: scale(1.05);
}

.status-tag {
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  color: #fff;
  backdrop-filter: blur(4px);
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
  z-index: 2;
}

.status-tag.active {
  background: linear-gradient(135deg, #32cd32 0%, #228b22 100%);
}

.status-tag.over {
  background: rgba(0,0,0,0.6);
}

.card-info {
  padding: 12px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.card-title {
  font-size: 15px;
  font-weight: 500;
  color: #111;
  line-height: 1.4;
  height: 42px; /* 限制两行 */
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-price-row {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.price-box {
  color: #ff4400;
  font-weight: bold;
  margin-right: 8px;
  line-height: 1;
}

.currency {
  font-size: 12px;
  margin-right: 1px;
}

.amount {
  font-size: 18px;
}

.period-tag {
  font-size: 10px;
  color: #666;
  background: #f5f5f5;
  padding: 2px 6px;
  border-radius: 4px;
}

.card-meta {
  margin-bottom: 12px;
}

.meta-item {
  display: flex;
  align-items: center;
  font-size: 12px;
}

.meta-item .label {
  color: #999;
  margin-right: 4px;
}

.meta-item .value {
  color: #333;
  font-weight: 500;
}

/* 底部用户信息栏 */
.card-footer {
  margin-top: auto;
  padding-top: 8px;
  border-top: 1px solid #f8f8f8;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 11px;
  color: #999;
}

.user-info {
  display: flex;
  align-items: center;
}

.avatar-placeholder {
  width: 16px;
  height: 16px;
  background: #e0e0e0;
  border-radius: 50%;
  margin-right: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  color: #fff;
}

.location {
  transform: scale(0.9);
  transform-origin: right center;
}

.pagination-wrapper {
  margin-top: 32px;
  text-align: center;
  display: flex;
  justify-content: center;
}

.empty-state {
  padding: 60px 0;
}
</style>
