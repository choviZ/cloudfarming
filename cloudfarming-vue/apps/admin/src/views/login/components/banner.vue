<template>
  <div class="banner">
    <div class="banner-inner">
      <a-carousel class="carousel">
        <a-carousel-item v-for="item in carouselItem" :key="item.slogan" :dot-position="dotPosition">
          <div class="carousel-item">
            <div class="carousel-title" style="color: #f0f2f5">{{ item.slogan }}</div>
            <div class="carousel-sub-title">{{ item.subSlogan }}</div>
            <img class="carousel-image" :src="item.image" />
          </div>
        </a-carousel-item>
      </a-carousel>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue'
import bannerImage from '@/assets/images/login-banner.png'
import type { CarouselProps } from 'ant-design-vue';
const dotPosition = ref<CarouselProps['dotPosition']>('bottom');

const carouselItem = [
  {
    slogan: '欢迎使用',
    subSlogan: '体验高效工作流程',
    image: bannerImage
  },
  {
    slogan: '智能协作',
    subSlogan: '提升团队协作效率',
    image: bannerImage
  },
  {
    slogan: '安全可靠',
    subSlogan: '保障数据安全与隐私',
    image: bannerImage
  }
]
</script>

<style lang="less" scoped>
.banner {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background: #f0f2f5;
}

.banner-inner {
  width: 100%;
  max-width: 1440px;
  height: 500px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.carousel {
  height: 100%;
  width: 100%;

  &-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
  }

  &-title {
    color: #333;
    font-weight: 500;
    font-size: 20px;
    line-height: 28px;
    margin-bottom: 8px;
  }

  &-sub-title {
    color: #666;
    font-size: 14px;
    line-height: 22px;
    margin-bottom: 30px;
  }

  &-image {
    width: 320px;
  }
}


</style>
