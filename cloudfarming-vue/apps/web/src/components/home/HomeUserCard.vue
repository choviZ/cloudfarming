<template>
  <div class="home-user-card">
    <div class="user-card-inner">
      <!-- Background Decoration -->
      <div class="card-bg">
        <div class="bg-circle"></div>
        <div class="bg-gradient"></div>
      </div>

      <!-- User Info -->
      <div class="user-info-section">
        <div class="avatar-wrapper">
          <img :src="userAvatar" alt="Avatar" class="avatar-img">
        </div>
        <h3 class="user-name">{{ userName }}</h3>

        <!-- Address Section -->
        <div class="address-box" @click="router.push('/user/info/address')">
          <EnvironmentOutlined class="address-icon" />
          <span class="address-text">{{ address }}</span>
          <RightOutlined class="address-arrow" />
        </div>

        <!-- Stats Grid -->
        <div class="stats-grid">
          <!-- TODO: Backend stats not ready, using mock data -->
          <div class="stat-item group">
            <div class="stat-value">2</div>
            <div class="stat-label">购物车</div>
          </div>
          <div class="stat-item group">
            <div class="stat-value">1</div>
            <div class="stat-label">待收货</div>
          </div>
          <div class="stat-item group">
            <div class="stat-value">0</div>
            <div class="stat-label">待发货</div>
          </div>
          <div class="stat-item group">
            <div class="stat-value">5</div>
            <div class="stat-label">待评价</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Platform Notice -->
    <div class="platform-notice">
      <div class="notice-header">
        <h4 class="notice-title">
          <span class="title-indicator"></span>
          平台公告
        </h4>
        <span class="notice-more">
          更多
          <RightOutlined class="more-icon" />
        </span>
      </div>
      <ul class="notice-list">
        <!-- Mock Notices -->
        <li class="notice-item">
          <span class="notice-tag hot">热</span>
          <span class="notice-text">云端牧场APP 2.0 版本正式上线！</span>
        </li>
        <li class="notice-item">
          <span class="notice-tag new">新</span>
          <span class="notice-text">春季果树认养计划开启预定</span>
        </li>
        <li class="notice-item">
          <span class="notice-tag normal">通</span>
          <span class="notice-text">关于部分偏远地区物流配送延迟</span>
        </li>
      </ul>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/useUserStore';
import { 
  EnvironmentOutlined, 
  RightOutlined 
} from '@ant-design/icons-vue';
import { getCurrentUserDefaultReceiveAddress } from '@cloudfarming/core/api/address/recieveAddress';

const router = useRouter();
const userStore = useUserStore();
const address = ref('暂未设置默认收货地址');

const userName = computed(() => {
  return userStore.loginUser?.username || '请登录';
});

const userAvatar = computed(() => {
  return userStore.loginUser?.avatar || 'https://api.dicebear.com/7.x/avataaars/svg?seed=Guest';
});

onMounted(async () => {
  if (userStore.loginUser) {
    const res = await getCurrentUserDefaultReceiveAddress();
    if ((res.code == '0') && res.data) {
      const { provinceName, cityName, districtName, detailAddress } = res.data;
      address.value = `${provinceName}${cityName}${districtName}${detailAddress}`;
    }
  }
});
</script>

<style scoped>
.home-user-card {
  width: 300px;
  height: 460px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.user-card-inner {
  background-color: #ffffff;
  border-radius: 16px;
  box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.03), 0 2px 8px rgba(0, 0, 0, 0.04);
  overflow: hidden;
  position: relative;
  /* height: 260px; */
  /* Let content define height or fixed? Prototype had specific structure */
}

/* Background Decoration */
.card-bg {
  height: 96px;
  background: linear-gradient(135deg, #15803d 0%, #14532d 100%);
  /* primary-700 to 900 */
  position: relative;
  overflow: hidden;
}

.bg-circle {
  position: absolute;
  top: -16px;
  right: -16px;
  width: 96px;
  height: 96px;
  background-color: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  filter: blur(20px);
}

.bg-gradient {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 50%;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.1), transparent);
}

/* User Info Section */
.user-info-section {
  padding: 0 20px 20px;
  margin-top: -48px;
  position: relative;
  text-align: center;
}

.avatar-wrapper {
  display: inline-block;
  padding: 6px;
  background-color: #ffffff;
  border-radius: 50%;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  margin-bottom: 12px;
}

.avatar-img {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background-color: #f8fafc;
}

.user-name {
  font-weight: 700;
  color: #0f172a;
  font-size: 18px;
  margin-bottom: 12px;
}

/* Address Box */
.address-box {
  margin-top: 12px;
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 13px;
  color: #64748b;
  background-color: #f8fafc;
  padding: 6px 12px;
  border-radius: 8px;
  border: 1px solid #f1f5f9;
  margin-left: 16px;
  margin-right: 16px;
  cursor: pointer;
  transition: all 0.2s;
}

.address-box:hover {
  background-color: #f1f5f9;
  border-color: #e2e8f0;
}

.address-icon {
  color: #22c55e;
  /* primary-500 */
}

.address-text {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 150px;
}

.address-arrow {
  font-size: 10px;
  color: #cbd5e1;
}

/* Stats Grid */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  border-top: 1px solid #f8fafc;
  padding-top: 20px;
}

.stat-item {
  cursor: pointer;
}

.stat-value {
  font-weight: 700;
  color: #0f172a;
  font-size: 18px;
  transition: color 0.2s;
}

.stat-item:hover .stat-value {
  color: #15803d;
  /* primary-700 */
}

.stat-label {
  font-size: 10px;
  color: #94a3b8;
  margin-top: 2px;
}

/* Platform Notice */
.platform-notice {
  background-color: #ffffff;
  border-radius: 16px;
  box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.03), 0 2px 8px rgba(0, 0, 0, 0.04);
  padding: 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.notice-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f8fafc;
}

.notice-title {
  font-weight: 700;
  font-size: 14px;
  color: #1e293b;
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
}

.title-indicator {
  width: 4px;
  height: 16px;
  background-color: #16a34a;
  /* primary-600 */
  border-radius: 9999px;
}

.notice-more {
  font-size: 12px;
  color: #94a3b8;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: color 0.2s;
}

.notice-more:hover {
  color: #15803d;
}

.notice-list {
  list-style: none;
  padding: 0;
  margin: 0;
  flex: 1;
  overflow-y: auto;
}

.notice-item {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
  cursor: pointer;
}

.notice-tag {
  font-size: 10px;
  font-weight: 700;
  padding: 1px 6px;
  border-radius: 4px;
  height: fit-content;
  margin-top: 2px;
  flex-shrink: 0;
}

.notice-tag.hot {
  color: #ef4444;
  background-color: #fef2f2;
  border: 1px solid #fee2e2;
}

.notice-tag.new {
  color: #16a34a;
  background-color: #f0fdf4;
  border: 1px solid #dcfce7;
}

.notice-tag.normal {
  color: #64748b;
  background-color: #f1f5f9;
  border: 1px solid #e2e8f0;
}

.notice-text {
  font-size: 12px;
  color: #475569;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color 0.2s;
}

.notice-item:hover .notice-text {
  color: #15803d;
}
</style>
