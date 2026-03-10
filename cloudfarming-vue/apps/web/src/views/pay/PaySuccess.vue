<template>
  <div class="pay-success">
    <!-- 支付成功主容器 -->
    <div class="success-card">
      <!-- 成功图标区域 -->
      <div class="success-icon">
        <svg viewBox="0 0 100 100" class="check-icon">
          <circle cx="50" cy="50" r="45" fill="#f0f9f0" stroke="#4CAF50" stroke-width="2"/>
          <path
              d="M30,50 L45,65 L75,35"
              fill="none"
              stroke="#4CAF50"
              stroke-width="6"
              stroke-linecap="round"
              stroke-linejoin="round"
          />
        </svg>
      </div>

      <!-- 成功提示文本 -->
      <div class="success-text">
        <h2 class="title">支付成功</h2>
        <p class="desc">您的农资订单已支付完成，感谢您的购买</p>
      </div>

      <!-- 订单信息（示例） -->
      <div class="order-info">
        <div class="info-item">
          <span class="label">订单编号：</span>
          <span class="value">AG20260209{{ Math.floor(Math.random() * 10000) }}</span>
        </div>
        <div class="info-item">
          <span class="label">支付金额：</span>
          <span class="value">¥{{ (Math.random() * 1000 + 100).toFixed(2) }}</span>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="btn-group">
        <button class="btn btn-primary" @click="goToOrder">查看订单详情</button>
        <button class="btn btn-secondary" @click="goToHome">返回首页</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref} from 'vue'

// 模拟跳转订单详情
const goToOrder = () => {
  // 实际项目中替换为路由跳转逻辑
  console.log('跳转到订单详情页')
  // 示例：router.push('/order/detail')
}

// 模拟返回首页
const goToHome = () => {
  // 实际项目中替换为路由跳转逻辑
  console.log('返回首页')
  // 示例：router.push('/home')
}
</script>

<style scoped>
/* 页面整体样式 */
.pay-success {
  min-height: 100vh;
  background-color: #f8fbf8;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  box-sizing: border-box;
}

/* 成功卡片容器 */
.success-card {
  width: 100%;
  max-width: 500px;
  background: #ffffff;
  border-radius: 12px;
  padding: 40px 30px;
  box-shadow: 0 4px 20px rgba(76, 175, 80, 0.08);
  text-align: center;
  box-sizing: border-box;
}

/* 成功图标样式 */
.success-icon {
  margin-bottom: 24px;
}

.check-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto;
}

/* 文本样式 */
.success-text .title {
  font-size: 24px;
  color: #2e7d32; /* 深绿色，贴合农业主题 */
  margin: 0 0 12px 0;
  font-weight: 600;
}

.success-text .desc {
  font-size: 16px;
  color: #558b2f; /* 橄榄绿，辅助色 */
  margin: 0;
  line-height: 1.5;
}

/* 订单信息区域 */
.order-info {
  margin: 30px 0;
  padding: 20px;
  background-color: #f0f9f0; /* 浅绿背景 */
  border-radius: 8px;
  text-align: left;
}

.info-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 14px;
}

.info-item:last-child {
  margin-bottom: 0;
}

.info-item .label {
  color: #388e3c; /* 中绿色 */
  font-weight: 500;
}

.info-item .value {
  color: #212121;
  font-family: monospace;
}

/* 按钮组样式 */
.btn-group {
  display: flex;
  gap: 16px;
  margin-top: 10px;
}

.btn {
  flex: 1;
  height: 48px;
  border-radius: 8px;
  border: none;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

/* 主按钮（查看订单） */
.btn-primary {
  background-color: #4CAF50; /* 主绿色 */
  color: #ffffff;
}

.btn-primary:hover {
  background-color: #388e3c; /* 加深绿色，hover效果 */
}

/* 次要按钮（返回首页） */
.btn-secondary {
  background-color: #ffffff;
  color: #4CAF50;
  border: 1px solid #4CAF50;
}

.btn-secondary:hover {
  background-color: #f0f9f0;
}

/* 响应式适配 */
@media (max-width: 480px) {
  .success-card {
    padding: 30px 20px;
  }

  .btn-group {
    flex-direction: column;
    gap: 12px;
  }

  .success-text .title {
    font-size: 20px;
  }
}
</style>