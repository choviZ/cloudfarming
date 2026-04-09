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

        <div class="address-box" @click="handleAddressClick">
          <EnvironmentOutlined class="address-icon" />
          <span class="address-text">{{ address }}</span>
          <RightOutlined class="address-arrow" />
        </div>

        <div class="stats-grid">
          <button
            v-for="stat in statCards"
            :key="stat.key"
            type="button"
            class="stat-item group"
            @click="navigateTo(stat.path, stat.query)"
          >
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </button>
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

    <section class="showcase-card surface-card" @click="goToFarmerList">
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
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { EnvironmentOutlined, RightOutlined, TeamOutlined, UserOutlined } from '@ant-design/icons-vue'
import { getCurrentUserDefaultReceiveAddress } from '@/api/address'
import { pagePublishedArticles } from '@/api/article'
import { getCart } from '@/api/cart'
import { getMyPendingReviewOrders, getOrderList, ORDER_STATUS } from '@/api/order'
import { useUserStore } from '@/stores/useUserStore'

const router = useRouter()
const userStore = useUserStore()

const address = ref('暂未设置默认收货地址')
const noticeList = ref([])
const stats = reactive({
  cartCount: 0,
  pendingReceiveCount: 0,
  pendingShipCount: 0,
  pendingReviewCount: 0
})

const userName = computed(() => userStore.loginUser?.username || '请登录')
const userAvatar = computed(() => userStore.loginUser?.avatar || '')
const hasAvatar = computed(() => {
  const avatar = userAvatar.value
  return avatar && avatar !== '' && !avatar.includes('Avatar')
})

const statCards = computed(() => ([
  {
    key: 'cart',
    label: '购物车',
    value: stats.cartCount,
    path: '/cart'
  },
  {
    key: 'receive',
    label: '待收货',
    value: stats.pendingReceiveCount,
    path: '/usercenter/orders',
    query: { tab: 'shipped' }
  },
  {
    key: 'ship',
    label: '待发货',
    value: stats.pendingShipCount,
    path: '/usercenter/orders',
    query: { tab: 'pendingShip' }
  },
  {
    key: 'review',
    label: '待评价',
    value: stats.pendingReviewCount,
    path: '/usercenter/orders',
    query: { tab: 'pendingReview' }
  }
]))

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

const navigateTo = (path, query) => {
  if (!userStore.loginUser) {
    router.push('/user/login')
    return
  }
  router.push({
    path,
    query
  })
}

const handleAddressClick = () => {
  if (!userStore.loginUser) {
    router.push('/user/login')
    return
  }
  router.push('/user/info/address')
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

const resetStats = () => {
  stats.cartCount = 0
  stats.pendingReceiveCount = 0
  stats.pendingShipCount = 0
  stats.pendingReviewCount = 0
}

const loadDefaultAddress = async () => {
  if (!userStore.loginUser) {
    address.value = '暂未设置默认收货地址'
    return
  }
  try {
    const res = await getCurrentUserDefaultReceiveAddress()
    if (res.code === '0' && res.data) {
      const { provinceName, cityName, districtName, detailAddress } = res.data
      address.value = `${provinceName}${cityName}${districtName}${detailAddress}`
      return
    }
    address.value = '暂未设置默认收货地址'
  } catch (error) {
    console.error(error)
    address.value = '暂未设置默认收货地址'
  }
}

const loadUserStats = async () => {
  if (!userStore.loginUser) {
    resetStats()
    return
  }
  try {
    const [cartRes, pendingShipRes, pendingReceiveRes, pendingReviewRes] = await Promise.all([
      getCart(),
      getOrderList({
        userId: userStore.loginUser.id,
        orderStatus: ORDER_STATUS.PENDING_SHIPMENT,
        current: 1,
        size: 1
      }),
      getOrderList({
        userId: userStore.loginUser.id,
        orderStatus: ORDER_STATUS.SHIPPED,
        current: 1,
        size: 1
      }),
      getMyPendingReviewOrders({
        current: 1,
        size: 1
      })
    ])

    stats.cartCount = Number(cartRes?.data?.totalQuantity) || 0
    stats.pendingShipCount = Number(pendingShipRes?.data?.total) || 0
    stats.pendingReceiveCount = Number(pendingReceiveRes?.data?.total) || 0
    stats.pendingReviewCount = Number(pendingReviewRes?.data?.total) || 0
  } catch (error) {
    console.error(error)
    resetStats()
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

const loadPageData = async () => {
  await Promise.all([loadDefaultAddress(), loadUserStats(), loadNotices()])
}

watch(
  () => userStore.loginUser?.id,
  () => {
    loadPageData()
  }
)

onMounted(() => {
  loadPageData()
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
  overflow: visible;
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
  margin-top: -14px;
  position: relative;
  text-align: center;
  display: flex;
  flex-direction: column;
}

.avatar-wrapper {
  display: inline-block;
  width: fit-content;
  margin: 0 auto 10px;
  padding: 5px;
  background-color: #ffffff;
  border-radius: 50%;
  position: relative;
  z-index: 1;
}

.avatar-img,
.avatar-placeholder {
  width: 68px;
  height: 68px;
  border-radius: 50%;
}

.avatar-img {
  object-fit: cover;
  display: block;
}

.avatar-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f3f4f6 0%, #e5e7eb 100%);
  color: #6b7280;
}

.avatar-icon {
  font-size: 30px;
}

.user-name {
  margin: 0 0 12px;
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
}

.address-box {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  margin-bottom: 14px;
  padding: 10px 12px;
  border-radius: 12px;
  background: #f6faf7;
  border: 1px solid #e2eee5;
  cursor: pointer;
}

.address-icon,
.address-arrow {
  color: #2f8b49;
  font-size: 14px;
}

.address-text {
  flex: 1;
  min-width: 0;
  font-size: 12px;
  color: #52625a;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  text-align: left;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
  margin-top: auto;
}

.stat-item {
  border: none;
  padding: 10px 0;
  border-radius: 12px;
  background: #f8fbf8;
  cursor: pointer;
  transition: transform 0.2s ease, background-color 0.2s ease;
}

.stat-item:hover {
  transform: translateY(-1px);
  background: #f1f8f3;
}

.stat-value {
  font-size: 18px;
  font-weight: 700;
  color: #1f7a3f;
}

.stat-label {
  margin-top: 4px;
  font-size: 12px;
  color: #6a7b70;
}

.notice-card,
.showcase-card {
  padding: 14px 16px;
}

.notice-header,
.showcase-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.notice-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: #1f2937;
}

.title-indicator {
  width: 4px;
  height: 14px;
  border-radius: 999px;
  background: linear-gradient(180deg, #159947 0%, #0c7c38 100%);
}

.notice-more,
.showcase-entry {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #2f8b49;
  cursor: pointer;
}

.notice-list {
  list-style: none;
  margin: 12px 0 0;
  padding: 0;
}

.notice-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
  cursor: pointer;
}

.notice-item + .notice-item {
  border-top: 1px dashed #ecf1ee;
}

.notice-tag {
  flex-shrink: 0;
  min-width: 34px;
  height: 22px;
  padding: 0 8px;
  border-radius: 999px;
  font-size: 12px;
  line-height: 22px;
  text-align: center;
}

.notice-tag.hot {
  background: #fff0e0;
  color: #c77414;
}

.notice-tag.new {
  background: #edf9f0;
  color: #1d7f44;
}

.notice-tag.normal {
  background: #f3f4f6;
  color: #6b7280;
}

.notice-text {
  min-width: 0;
  flex: 1;
  font-size: 13px;
  color: #46574d;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notice-empty {
  padding-top: 12px;
  font-size: 13px;
  color: #8a968f;
}

.showcase-card {
  cursor: pointer;
}

.showcase-copy {
  margin: 10px 0 0;
  font-size: 13px;
  line-height: 1.7;
  color: #5d6f64;
}

.showcase-tags {
  display: flex;
  gap: 8px;
  margin-top: 10px;
}

.showcase-tag {
  display: inline-flex;
  align-items: center;
  height: 24px;
  padding: 0 10px;
  border-radius: 999px;
  background: #f1f7f2;
  color: #1f7a3f;
  font-size: 12px;
}
</style>
