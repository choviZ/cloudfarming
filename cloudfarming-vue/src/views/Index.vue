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

    <!-- 下部商品展示区域（占位） -->
    <div class="product-section">
      <div class="product-types">
        <!-- 商品类型tag占位 -->
      </div>
      <div class="product-list">
        <!-- 商品展示占位 -->
        <a-card v-for="i in 8" :key="i" hoverable class="product-card">
          <template #cover>
            <img alt="商品图片" src="https://via.placeholder.com/200x200?text=Product" />
          </template>
          <a-card-meta title="商品名称" description="商品描述" />
        </a-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { getShowAdverts } from '@/api/advert';
import type { AdvertRespDTO } from '@/types/advert';
import { message } from 'ant-design-vue';

const adverts = ref<AdvertRespDTO[]>([]);
const loading = ref(false);

// 获取广告数据
const fetchAdverts = async () => {
  loading.value = true;
  try {
    const response = await getShowAdverts();
    if (response.code === '0' && response.data) {
      // 按照displayOrder排序广告
      adverts.value = response.data.sort((a, b) => a.displayOrder - b.displayOrder);
    } else {
      message.error('获取广告数据失败：' + (response.message || '未知错误'));
    }
  }finally {
    loading.value = false;
  }
};

// 组件挂载时获取广告数据
onMounted(() => {
  fetchAdverts();
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
  max-width: 1200px;
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
  max-width: 1200px;
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
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.product-card {
  height: 100%;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: box-shadow 0.3s ease;
}

.product-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}
</style>