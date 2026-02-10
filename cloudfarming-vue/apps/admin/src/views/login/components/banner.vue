<template>
  <div class="banner">
    <div class="inner-banner">
      <a-carousel autoplay>
        <div v-for="item in carouselItem" :key="item.slogan" :dot-position="dotPosition">
          <div class="title">{{ item.slogan }}</div>
          <div class="sub-title">{{ item.subSlogan }}</div>
          <img class="img" :src="item.image" />
        </div>
      </a-carousel>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import bannerImage from '@/assets/images/login-banner.png'
import bannerImage2 from '@/assets/images/login-banner2.png'
import bannerImage3 from '@/assets/images/login-banner3.png'

const dotPosition = ref('bottom')

const carouselItem = [
  {
    slogan: '欢迎使用',
    subSlogan: '体验高效工作流程',
    image: bannerImage
  },
  {
    slogan: '	高效运营',
    subSlogan: '一键掌控商品、订单与用户，轻松管理千万级业务',
    image: bannerImage2
  },
  {
    slogan: '智能决策',
    subSlogan: '数据驱动增长，智能分析助你洞察商机',
    image: bannerImage3
  }
]
</script>

<style lang="less" scoped>
.banner {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
}

.inner-banner {
  width: 100%;
  height: 600px;

  .title {
    color: #ffffff;
    font-weight: 500;
    font-size: 24px;
    line-height: 28px;
    margin-bottom: 8px;
  }

  .sub-title {
    color: #BFCBD8;
    font-size: 14px;
    line-height: 22px;
    margin-bottom: 30px;
  }

  .img {
    padding-top: 48px;
    display: flex;
    width: 320px;
    margin: 0 auto;
  }
}

:deep(.slick-slide) {
  text-align: center;
  height: 600px;
  overflow: hidden;
}

:deep(.slick-slide h3) {
  color: #fff;
}
</style>
