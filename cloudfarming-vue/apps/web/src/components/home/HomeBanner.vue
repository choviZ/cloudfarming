<template>
  <div class="home-banner">
    <a-carousel :autoplay="true" :dots="true" :autoplay-speed="5000" effect="fade" class="banner-carousel">
      <div v-for="advert in adverts" :key="advert.id" class="banner-item">
        <a v-if="advert.linkUrl" :href="advert.linkUrl" target="_blank" rel="noopener noreferrer" class="banner-link">
          <img :src="advert.imageUrl" :alt="advert.altText || 'Banner'" class="banner-img" />
        </a>
        <div v-else class="banner-link">
          <img :src="advert.imageUrl" :alt="advert.altText || 'Banner'" class="banner-img" />
        </div>
        
        <!-- Optional: Overlay (Static text from prototype, only shown if no ad text? Or always?) 
             For now, I'll omit the heavy text overlay to avoid conflict with ad images, 
             but keep the gradient for text readability if needed. 
        -->
        <div class="banner-overlay"></div>
      </div>
    </a-carousel>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { getShowAdverts } from '@/api/advert';

const adverts = ref([]);
const loading = ref(false);

const fetchAdverts = async () => {
  loading.value = true;
  try {
    const response = await getShowAdverts();
    if (response.code === '0' && response.data) {
      adverts.value = response.data.sort((a, b) => a.displayOrder - b.displayOrder);
    }
  } catch (error) {
    console.error('Failed to fetch adverts', error);
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchAdverts();
});
</script>

<style scoped>
.home-banner {
  flex: 1;
  height: 100%;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px -2px rgba(0, 0, 0, 0.05);
  position: relative;
  background-color: #e2e8f0;
}

.banner-carousel {
  height: 100%;
}

:deep(.slick-slider),
:deep(.slick-list),
:deep(.slick-track),
:deep(.slick-slide),
:deep(.slick-slide > div) {
  height: 100%;
}

.banner-item {
  height: 100%;
  position: relative;
  overflow: hidden;
}

.banner-link {
  display: block;
  width: 100%;
  height: 100%;
}

.banner-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 2s ease;
}

.banner-item:hover .banner-img {
  transform: scale(1.05);
}

.banner-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(15, 23, 42, 0.4) 0%, transparent 30%);
  pointer-events: none;
}

/* Custom Dots */
:deep(.slick-dots) {
  bottom: 24px !important;
  right: 32px !important;
  width: auto !important;
  text-align: right !important;
  left: auto !important;
}

:deep(.slick-dots li button) {
  width: 10px !important;
  height: 6px !important;
  background: rgba(255, 255, 255, 0.4) !important;
  border-radius: 4px !important;
  opacity: 1 !important;
  transition: all 0.3s !important;
}

:deep(.slick-dots li.slick-active button) {
  width: 32px !important;
  background: #ffffff !important;
}
</style>
