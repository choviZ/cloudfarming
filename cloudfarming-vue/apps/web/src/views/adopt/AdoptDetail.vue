<template>
  <div class="adopt-detail-container">
    <a-spin :spinning="loading">
      <div v-if="detail" class="detail-wrapper">
        <!-- 顶部用户信息栏  -->
        <div class="user-bar">
          <div class="user-left">
            <div class="avatar">
              {{ detail.userId ? detail.userId.charAt(0).toUpperCase() : 'U' }}
            </div>
            <div class="user-info">
              <!-- TODO 替换真实数据 -->
              <div class="username-row">
                <span class="username">农场主_{{ detail.userId ? detail.userId.slice(0, 6) : 'Unknown' }}</span>
                <span class="credit-badge">信用极好</span>
              </div>
              <div class="user-status">33分钟前来过 | 发布于 {{ formatDate(detail.createTime) }}</div>
            </div>
          </div>
          <div class="user-right">
             <!-- 占位功能 -->
             <a-button type="text" class="report-btn">
               举报
             </a-button>
          </div>
        </div>

        <!-- 主体内容区 -->
        <div class="main-content">
          <!-- 左侧图片区 -->
          <div class="image-section">
            <div class="main-image-box">
              <img :src="detail.coverImage" :alt="detail.title" />
            </div>
            <!-- 如果有多图，这里可以放缩略图列表，目前API只有一张图 -->
          </div>

          <!-- 右侧详情区 -->
          <div class="info-section">
            <div class="price-header">
              <div class="price-box">
                <span class="currency">¥</span>
                <span class="price">{{ detail.price }}</span>
              </div>
              <!-- TODO 替换真实数据 -->
              <div class="heat-info">
                <span>{{ Math.floor(Math.random() * 200) + 50 }}人想要</span>
                <span class="separator">|</span>
                <span>{{ Math.floor(Math.random() * 1000) + 100 }}次浏览</span>
              </div>
            </div>

            <h1 class="item-title">{{ detail.title }}</h1>

            <!-- 核心参数 -->
            <div class="params-grid">
               <div class="param-item">
                 <span class="label">认养周期</span>
                 <span class="value">{{ detail.adoptDays }}天</span>
               </div>
               <div class="param-item">
                 <span class="label">预计收益</span>
                 <span class="value">{{ detail.expectedYield || '以实际收获为准' }}</span>
               </div>
               <div class="param-item">
                 <span class="label">动物分类</span>
                 <span class="value">{{ detail.animalCategory }}</span>
               </div>
               <div class="param-item">
                 <span class="label">当前状态</span>
                 <span class="value" :class="detail.status === 1 ? 'status-active' : 'status-over'">
                    {{ detail.status === 1 ? '可认养' : '已结束' }}
                 </span>
               </div>
            </div>

            <div class="divider"></div>

            <!-- 描述信息 -->
            <div class="description-section">
               <div class="section-title">认养说明</div>
               <div class="description-text">
                 {{ detail.description || '暂无详细说明' }}
               </div>
            </div>

            <div class="divider"></div>

            <!-- 底部保障栏 -->
            <div class="guarantee-bar">
               <div class="guarantee-item">
                 <check-circle-outlined style="color: #1677ff" /> 实名认证
               </div>
               <div class="guarantee-item">
                 <check-circle-outlined style="color: #1677ff" /> 担保交易
               </div>
               <div class="guarantee-item">
                 <check-circle-outlined style="color: #1677ff" /> 溯源可查
               </div>
            </div>

            <!-- 操作按钮 -->
            <div class="action-bar">
               <a-button type="primary" size="large" class="buy-btn" :disabled="detail.status !== 1" @click="handleBuy">
                 {{ detail.status === 1 ? '立即认养' : '认养已结束' }}
               </a-button>
               <a-button size="large" class="chat-btn" @click="handleChat">
                 联系农场主
               </a-button>
            </div>
          </div>
        </div>
      </div>
      
      <div v-else-if="!loading" class="empty-wrapper">
        <a-empty description="该认养项目不存在或已被删除" />
        <a-button type="primary" @click="router.push('/adopt/list')">返回列表</a-button>
      </div>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getAdoptItemDetail } from '@cloudfarming/core';
import type { AdoptItemResp } from '@cloudfarming/core';
import { message } from 'ant-design-vue';
import { CheckCircleOutlined } from '@ant-design/icons-vue';

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const detail = ref<AdoptItemResp | null>(null);

const fetchDetail = async (id: string) => {
  loading.value = true;
  try {
    const res = await getAdoptItemDetail(id);
    if (res.code === '0' && res.data) {
      detail.value = res.data;
    } else {
      message.error(res.message || '获取详情失败');
    }
  } catch (error) {
    message.error('系统繁忙，请稍后重试');
  } finally {
    loading.value = false;
  }
};

const formatDate = (dateStr: string) => {
  if (!dateStr) return '';
  return dateStr.split(' ')[0];
};

const handleBuy = () => {
  message.success('正在创建订单...（功能待开发）');
};

const handleChat = () => {
  message.info('正在连接客服系统...（功能待开发）');
};

onMounted(() => {
  const id = route.params.id as string;
  if (id) {
    fetchDetail(id);
  } else {
    message.error('参数错误');
    router.push('/adopt/list');
  }
});
</script>

<style scoped>
.adopt-detail-container {
  width: 100%;
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.detail-wrapper {
  width: 100%;
  max-width: 1200px;
  background: #fff;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}

/* 顶部用户栏 */
.user-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 32px;
}

.user-left {
  display: flex;
  align-items: center;
}

.avatar {
  width: 48px;
  height: 48px;
  background: #ffda44;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: bold;
  color: #333;
  margin-right: 12px;
}

.user-info {
  display: flex;
  flex-direction: column;
}

.username-row {
  display: flex;
  align-items: center;
  margin-bottom: 4px;
}

.username {
  font-size: 16px;
  font-weight: 600;
  color: #111;
  margin-right: 8px;
}

.credit-badge {
  font-size: 10px;
  background: #e6f7ff;
  color: #1677ff;
  padding: 1px 4px;
  border-radius: 4px;
  border: 1px solid #91caff;
}

.user-status {
  font-size: 12px;
  color: #999;
}

.report-btn {
  color: #999;
}

/* 主体内容布局 */
.main-content {
  display: flex;
  gap: 48px;
}

.image-section {
  width: 550px;
  flex-shrink: 0;
}

.main-image-box {
  width: 100%;
  height: 550px;
  background: #f4f4f4;
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.main-image-box img {
  width: 100%;
  height: 100%;
  object-fit: contain; /* 保持图片比例 */
}

.info-section {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.price-header {
  display: flex;
  align-items: baseline;
  margin-bottom: 12px;
}

.price-box {
  color: #ff4400;
  font-weight: bold;
  margin-right: 16px;
}

.currency {
  font-size: 18px;
  margin-right: 2px;
}

.price {
  font-size: 36px;
}

.heat-info {
  font-size: 13px;
  color: #999;
}

.separator {
  margin: 0 8px;
  color: #eee;
}

.item-title {
  font-size: 24px;
  font-weight: 600;
  color: #111;
  line-height: 1.4;
  margin-bottom: 24px;
}

/* 参数网格 */
.params-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  background: #f9f9f9;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 24px;
}

.param-item {
  display: flex;
  flex-direction: column;
}

.param-item .label {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}

.param-item .value {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.status-active {
  color: #52c41a !important;
}

.status-over {
  color: #999 !important;
}

.divider {
  height: 1px;
  background: #f0f0f0;
  margin: 0 0 24px 0;
}

.description-section {
  margin-bottom: 24px;
  flex: 1;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 12px;
  position: relative;
  padding-left: 10px;
}

.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 4px;
  width: 3px;
  height: 14px;
  background: #ffda44;
  border-radius: 2px;
}

.description-text {
  font-size: 14px;
  color: #333;
  line-height: 1.6;
  white-space: pre-wrap;
}

.guarantee-bar {
  display: flex;
  gap: 20px;
  margin-bottom: 24px;
}

.guarantee-item {
  font-size: 12px;
  color: #666;
  display: flex;
  align-items: center;
  gap: 4px;
}

.action-bar {
  display: flex;
  gap: 16px;
  margin-top: auto; /* 推到底部 */
}

.buy-btn {
  flex: 1;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  background: #ffda44; 
  border-color: #ffda44;
  color: #111;
}

.buy-btn:hover:not(:disabled) {
  background: #ffcd11;
  border-color: #ffcd11;
  color: #111;
}

.buy-btn:disabled {
  background: #f5f5f5;
  border-color: #d9d9d9;
  color: rgba(0, 0, 0, 0.25);
}

.chat-btn {
  width: 140px;
  height: 48px;
  font-size: 16px;
  background: #f0f0f0;
  border: none;
  color: #333;
}

.chat-btn:hover {
  background: #e6e6e6;
  color: #333;
}

.empty-wrapper {
  text-align: center;
  padding: 100px 0;
}

@media (max-width: 768px) {
  .main-content {
    flex-direction: column;
  }
  
  .image-section {
    width: 100%;
  }
  
  .main-image-box {
    height: 300px;
  }
  
  .action-bar {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    background: #fff;
    padding: 12px 20px;
    box-shadow: 0 -2px 10px rgba(0,0,0,0.05);
    z-index: 100;
  }
  
  .adopt-detail-container {
    padding-bottom: 80px;
  }
}
</style>
