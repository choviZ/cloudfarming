<template>
  <div class="home-user-card">
    <section class="user-card-inner surface-card">
      <div class="card-bg">
        <div class="bg-circle"></div>
        <div class="bg-gradient"></div>
      </div>

      <div class="user-info-section">
        <div class="avatar-wrapper">
          <img v-if="hasAvatar" :src="userAvatar" alt="Avatar" class="avatar-img">
          <div v-else class="avatar-placeholder">
            <UserOutlined class="avatar-icon" />
          </div>
        </div>
        <h3 class="user-name">{{ userName }}</h3>

        <div class="address-box" @click="router.push('/user/info/address')">
          <EnvironmentOutlined class="address-icon" />
          <span class="address-text">{{ address }}</span>
          <RightOutlined class="address-arrow" />
        </div>

        <div class="stats-grid">
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
    </section>

    <section class="notice-card surface-card">
      <div class="notice-header">
        <h4 class="notice-title">
          <span class="title-indicator"></span>
          平台公告
        </h4>
        <span class="notice-more" @click="goToArticleList">
          更多
          <RightOutlined class="more-icon" />
        </span>
      </div>
      <ul class="notice-list">
        <li
          v-for="notice in noticeList"
          :key="notice.id"
          class="notice-item"
          @click="goToArticleDetail(notice.id)"
        >
          <span class="notice-tag" :class="getNoticeTagClass(notice)">
            {{ getNoticeTagText(notice) }}
          </span>
          <span class="notice-text">{{ notice.title }}</span>
        </li>
        <li v-if="!noticeList.length" class="notice-empty">
          暂无平台公告
        </li>
      </ul>
    </section>

    <section class="showcase-card" @click="goToFarmerList">
      <div class="showcase-header">
        <h4 class="notice-title">
          <span class="title-indicator"></span>
          优质农户
        </h4>
        <span class="showcase-entry">
          <TeamOutlined class="showcase-icon" />
          去看看
        </span>
      </div>
      <p class="showcase-copy">
        查看平台精选农户的农场环境、资质与基础信息，挑选更安心的认养对象。
      </p>
      <div class="showcase-tags">
        <span class="showcase-tag">环境实拍</span>
        <span class="showcase-tag">资质可查</span>
        <span class="showcase-tag">平台精选</span>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { EnvironmentOutlined, RightOutlined, TeamOutlined, UserOutlined } from '@ant-design/icons-vue'
import { getCurrentUserDefaultReceiveAddress } from '@/api/address'
import { pagePublishedArticles } from '@/api/article'
import { useUserStore } from '@/stores/useUserStore'

const router = useRouter()
const userStore = useUserStore()

const address = ref('暂未设置默认收货地址')
const noticeList = ref([])

const userName = computed(() => userStore.loginUser?.username || '请登录')
const userAvatar = computed(() => userStore.loginUser?.avatar || '')
const hasAvatar = computed(() => {
  const avatar = userAvatar.value
  return avatar && avatar !== '' && !avatar.includes('Avatar')
})

const goToArticleList = () => {
  router.push({
    name: 'articleList',
    query: { type: '1' }
  })
}

const goToFarmerList = () => {
  router.push('/farmers')
}

const goToArticleDetail = (id) => {
  if (!id) {
    return
  }
  router.push({
    name: 'articleDetail',
    params: { id: String(id) }
  })
}

const getNoticeTagClass = (notice) => {
  if (notice?.isTop) {
    return 'hot'
  }
  const publishTime = new Date(notice?.publishTime || notice?.createTime || '')
  const sevenDays = 7 * 24 * 60 * 60 * 1000
  if (!Number.isNaN(publishTime.getTime()) && Date.now() - publishTime.getTime() <= sevenDays) {
    return 'new'
  }
  return 'normal'
}

const getNoticeTagText = (notice) => {
  const tagClass = getNoticeTagClass(notice)
  if (tagClass === 'hot') {
    return '热'
  }
  if (tagClass === 'new') {
    return '新'
  }
  return '公告'
}

const loadDefaultAddress = async () => {
  if (!userStore.loginUser) {
    return
  }
  try {
    const res = await getCurrentUserDefaultReceiveAddress()
    if (res.code === '0' && res.data) {
      const { provinceName, cityName, districtName, detailAddress } = res.data
      address.value = `${provinceName}${cityName}${districtName}${detailAddress}`
    }
  } catch (error) {
    console.error(error)
  }
}

const loadNotices = async () => {
  try {
    const res = await pagePublishedArticles({
      current: 1,
      size: 2,
      articleType: 1
    })
    if (res.code === '0' && res.data) {
      noticeList.value = res.data.records || []
    }
  } catch (error) {
    console.error(error)
  }
}

onMounted(async () => {
  await Promise.all([loadDefaultAddress(), loadNotices()])
})
</script>

<style scoped>
.home-user-card {
  width: 300px;
  height: 100%;
  display: grid;
  grid-template-rows: 220px 118px 94px;
  gap: 14px;
}

.surface-card {
  background-color: #ffffff;
  border-radius: 16px;
  box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.03), 0 2px 8px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

.user-card-inner {
  position: relative;
  display: flex;
  flex-direction: column;
}

.card-bg {
  height: 78px;
  background: linear-gradient(135deg, #15803d 0%, #14532d 100%);
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

.user-info-section {
  flex: 1;
  padding: 0 16px 14px;
  margin-top: -34px;
  position: relative;
  text-align: center;
  display: flex;
  flex-direction: column;
}

.avatar-wrapper {
  display: inline-block;
  width: fit-content;
  margin: 0 auto 8px;
  padding: 5px;
  background-color: #ffffff;
  border-radius: 50%;
  box-shadow: 0 4px 10px rgba(15, 23, 42, 0.12);
}

.avatar-img {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background-color: #f8fafc;
}

.avatar-placeholder {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background-color: #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-icon {
  font-size: 28px;
  color: #94a3b8;
}

.user-name {
  margin: 0 0 10px;
  font-weight: 700;
  color: #0f172a;
  font-size: 18px;
  line-height: 1.2;
}

.address-box {
  margin: 0 0 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 12px;
  color: #64748b;
  background-color: #f8fafc;
  padding: 7px 10px;
  border-radius: 10px;
  border: 1px solid #f1f5f9;
  cursor: pointer;
  transition: all 0.2s;
}

.address-box:hover {
  background-color: #f1f5f9;
  border-color: #e2e8f0;
}

.address-icon {
  color: #22c55e;
}

.address-text {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 176px;
}

.address-arrow {
  font-size: 10px;
  color: #cbd5e1;
}

.stats-grid {
  margin-top: auto;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 6px;
  border-top: 1px solid #f1f5f9;
  padding-top: 14px;
}

.stat-item {
  cursor: pointer;
}

.stat-value {
  font-weight: 700;
  color: #0f172a;
  font-size: 16px;
  transition: color 0.2s;
}

.stat-item:hover .stat-value {
  color: #15803d;
}

.stat-label {
  font-size: 11px;
  color: #94a3b8;
  margin-top: 3px;
}

.notice-card {
  padding: 14px 16px 12px;
  display: flex;
  flex-direction: column;
}

.notice-header,
.showcase-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.notice-header {
  margin-bottom: 8px;
}

.notice-title {
  font-weight: 700;
  font-size: 14px;
  color: #1e293b;
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  flex-shrink: 0;
}

.title-indicator {
  width: 4px;
  height: 16px;
  background-color: #16a34a;
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
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-height: 0;
}

.notice-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  cursor: pointer;
}

.notice-tag {
  font-size: 10px;
  font-weight: 700;
  padding: 1px 6px;
  border-radius: 4px;
  height: fit-content;
  margin-top: 1px;
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
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color 0.2s;
}

.notice-item:hover .notice-text {
  color: #15803d;
}

.notice-empty {
  display: flex;
  align-items: center;
  min-height: 36px;
  color: #94a3b8;
  font-size: 12px;
}

.showcase-card {
  padding: 12px 14px;
  border-radius: 16px;
  background:
    radial-gradient(circle at top right, rgba(101, 184, 113, 0.18), transparent 30%),
    linear-gradient(135deg, #f7fcf6 0%, #eef8ef 100%);
  border: 1px solid #d9ecd9;
  box-shadow: 0 8px 20px rgba(34, 115, 55, 0.08);
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
  display: flex;
  flex-direction: column;
}

.showcase-card:hover {
  transform: translateY(-1px);
  border-color: #b9ddb9;
  box-shadow: 0 10px 20px rgba(34, 115, 55, 0.1);
}

.showcase-entry {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 4px 8px;
  border-radius: 999px;
  background: linear-gradient(135deg, #eef8ef 0%, #dff2e2 100%);
  border: 1px solid #cce8d0;
  color: #1f7a42;
  font-size: 11px;
  font-weight: 700;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.showcase-card:hover .showcase-entry {
  transform: translateY(-1px);
  border-color: #9fd3a8;
  box-shadow: 0 8px 16px rgba(31, 122, 66, 0.12);
}

.showcase-icon {
  font-size: 13px;
}

.showcase-copy {
  margin: 6px 0 8px;
  color: #4e6a56;
  font-size: 12px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.showcase-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.showcase-tag {
  padding: 2px 6px;
  border-radius: 999px;
  background: rgba(26, 127, 58, 0.08);
  color: #1f7a42;
  font-size: 10px;
  font-weight: 600;
}

@media (max-width: 1200px) {
  .home-user-card {
    width: 100%;
    height: auto;
    grid-template-rows: none;
  }

  .user-card-inner {
    min-height: 220px;
  }

  .notice-card {
    min-height: 118px;
  }

  .showcase-card {
    min-height: 94px;
  }
}
</style>
