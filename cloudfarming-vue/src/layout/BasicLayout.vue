<template>
  <a-layout class="basic-layout">
    <!-- 顶部头部区域 -->
    <a-layout-header class="layout-header">
      <global-header />
    </a-layout-header>
    <!-- 中间内容区域 -->
    <a-layout-content class="layout-content">
      <div class="content-wrapper">
        <router-view />
      </div>
    </a-layout-content>
    <!-- 底部区域 -->
    <a-layout-footer class="layout-footer">
      <div class="footer-content">
        <p>网站信息和备案信息占位</p>
      </div>
    </a-layout-footer>
  </a-layout>
</template>

<script setup lang="ts">
import GlobalHeader from '../components/GlobalHeader.vue'
</script>

<style scoped>
.basic-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.layout-header {
  background-color: #ffffff;
  padding: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.09);
  z-index: 10;
}

.layout-content {
  flex: 1;
  padding: 20px 0;
  background-color: #f5f5f5;
  display: flex;
  justify-content: center;
}

.content-wrapper {
  width: 100%;
  max-width: 1550px;
  padding: 0 20px;
  box-sizing: border-box;
}

.layout-footer {
  background-color: #ffffff;
  padding: 24px 0;
  border-top: 1px solid #e8e8e8;
}

.footer-content {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  text-align: center;
  color: #666;
  font-size: 14px;
}
</style>