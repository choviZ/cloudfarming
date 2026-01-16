<template>
  <div class="adopt-featured-section">
    <div class="section-header">
      <h2 class="section-title">云养殖精选</h2>
      <a-button type="link" @click="goToAdoptList">查看全部 ></a-button>
    </div>

    <a-skeleton :loading="loading" active>
      <div v-if="adoptItems.length > 0" class="adopt-list">
        <a-row :gutter="[20, 20]">
          <a-col
            v-for="item in adoptItems"
            :key="item.id"
            :xs="24"
            :sm="12"
            :md="8"
            :lg="6"
          >
            <a-card hoverable class="adopt-card" @click="goToDetail(item.id)">
              <template #cover>
                <div class="image-wrapper">
                  <img :alt="item.title" :src="item.coverImage" class="adopt-image" />
                  <div class="status-tag" :class="getStatusClass(item.status)">
                    {{ getStatusText(item.status) }}
                  </div>
                </div>
              </template>
              <div class="adopt-info">
                <h3 class="adopt-title" :title="item.title">{{ item.title }}</h3>
                <p class="adopt-desc">{{ item.description || '暂无描述' }}</p>
                <div class="adopt-meta">
                  <span class="adopt-price">¥{{ item.price }}</span>
                  <span class="adopt-period">周期: {{ item.adoptDays }}天</span>
                </div>
              </div>
            </a-card>
          </a-col>
        </a-row>
      </div>
      <a-empty v-else description="暂无精选认养项目" />
    </a-skeleton>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { pageAdoptItems } from '@cloudfarming/core';
import type { AdoptItemResp } from '@cloudfarming/core';
import { message } from 'ant-design-vue';

const router = useRouter();
const loading = ref(false);
const adoptItems = ref<AdoptItemResp[]>([]);

const fetchFeaturedItems = async () => {
  loading.value = true;
  try {
    // 模拟精选：查询前4条
    const res = await pageAdoptItems({
      current: 1,
      size: 4,
      status: 1 // 仅查询上架的
    });
    
    if (res.code === '0' && res.data) {
      adoptItems.value = res.data.records;
    }
  } catch (error) {
    console.error('获取精选认养项目失败', error);
  } finally {
    loading.value = false;
  }
};

const goToAdoptList = () => {
  router.push('/adopt/list');
};

const goToDetail = (id: string) => {
  router.push(`/adopt/detail/${id}`);
};

const getStatusText = (status: number) => {
  // 简单映射，实际应根据业务规则
  return status === 1 ? '可认养' : '已结束';
};

const getStatusClass = (status: number) => {
  return status === 1 ? 'status-active' : 'status-inactive';
};

onMounted(() => {
  fetchFeaturedItems();
});
</script>

<style scoped>
.adopt-featured-section {
  width: 100%;
  max-width: 1550px;
  margin: 0 auto;
  padding: 20px 0;
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
  background-color: #1890ff; /* Ant Design Blue */
  border-radius: 2px;
}

.adopt-list {
  padding: 0 20px;
}

.adopt-card {
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s;
}

.adopt-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.image-wrapper {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.adopt-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.adopt-card:hover .adopt-image {
  transform: scale(1.05);
}

.status-tag {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  color: #fff;
  font-weight: 500;
}

.status-active {
  background-color: rgba(82, 196, 26, 0.9); /* Green */
}

.status-inactive {
  background-color: rgba(0, 0, 0, 0.5);
}

.adopt-info {
  padding: 12px;
}

.adopt-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.adopt-desc {
  font-size: 13px;
  color: #666;
  margin-bottom: 12px;
  height: 20px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.adopt-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.adopt-price {
  font-size: 18px;
  font-weight: 700;
  color: #ff4d4f;
}

.adopt-period {
  font-size: 12px;
  color: #999;
  background-color: #f5f5f5;
  padding: 2px 6px;
  border-radius: 4px;
}
</style>
