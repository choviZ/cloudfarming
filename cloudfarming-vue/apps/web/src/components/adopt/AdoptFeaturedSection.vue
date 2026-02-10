<template>
  <div class="adopt-featured-section">
    <div class="section-header">
      <div class="header-left">
        <h2 class="section-title">
          <FireFilled class="title-icon" /> 热门认养
        </h2>
        <p class="section-subtitle">做一回云端农场主，享受耕种的乐趣与收获</p>
      </div>
      <button class="view-all-btn" @click="goToAdoptList">
        查看全部 <RightOutlined class="btn-icon" />
      </button>
    </div>

    <a-skeleton :loading="loading" active>
      <div v-if="adoptItems.length > 0" class="adopt-list">
        <a-row :gutter="[12, 20]">
          <a-col
            v-for="item in adoptItems"
            :key="item.id"
            :xs="24"
            :sm="12"
            :md="8"
            :lg="6"
            :xl="4"
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

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { FireFilled, RightOutlined } from '@ant-design/icons-vue';
import { pageAdoptItems } from '@/api/adopt';

const router = useRouter();
const loading = ref(false);
const adoptItems = ref([]);

const fetchFeaturedItems = async () => {
  loading.value = true;
  try {
    const res = await pageAdoptItems({
      current: 1,
      size: 6,
      status: 1,
      reviewStatus: 1
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

const goToDetail = (id) => {
  router.push(`/adopt/detail/${id}`);
};

const getStatusText = (status) => {
  // 简单映射，实际应根据业务规则
  return status === 1 ? '可认养' : '已结束';
};

const getStatusClass = (status) => {
  return status === 1 ? 'status-active' : 'status-inactive';
};

onMounted(() => {
  fetchFeaturedItems();
});
</script>

<style scoped>
.adopt-featured-section {
  width: 100%;
  max-width: 1620px;
  margin: 0 auto;
}

.section-header {
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
  color: #ef4444;
  font-size: 24px;
}

.section-subtitle {
  font-size: 14px;
  color: #64748b;
}

.view-all-btn {
  padding: 4px 20px;
  height: 36px;
  border-radius: 9999px;
  font-size: 14px;
  color: #475569;
  border: 1px solid #e2e8f0;
  background: white;
  display: flex;
  align-items: center;
  gap: 4px;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
  cursor: pointer;
}

.view-all-btn:hover {
  color: #15803d;
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

.adopt-list {
  /* No special styles needed for grid container */
}

.adopt-card {
  width: 253px;
  height: 335px;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s;
  margin: 0 auto;
}

.adopt-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

:deep(.ant-card-body) {
  padding: 10px 12px;
  height: 82px; /* 335 - 253 */
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.image-wrapper {
  position: relative;
  width: 253px;
  height: 253px;
  overflow: hidden;
  display: flex;
  justify-content: center;
}

.adopt-image {
  width: 253px;
  height: 253px;
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

.adopt-info {
  /* padding removed from here as it is now on ant-card-body */
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.adopt-title {
  color: #1f1f1f;
  display: inline-block;
  font-size: 14px;
  font-weight: 500;
  line-height: 22px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  width: 100%;
}

.adopt-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.adopt-price {
  align-items: flex-end;
  color: #ff5000;
  display: flex;
  flex-wrap: wrap;
  font-size: 20px;
  font-weight: 700;
  line-height: 24px;
  margin: 0;
}

.adopt-period {
  font-size: 12px;
  color: #999;
  background-color: #f5f5f5;
  padding: 2px 6px;
  border-radius: 4px;
}
</style>
