<template>
  <div class="adopt-detail-page">
    <a-spin :spinning="loading">
      <div v-if="detail" class="detail-container">
        <nav class="breadcrumb">
          <span class="crumb-item hover-text" @click="router.push('/')">首页</span>
          <span class="separator">/</span>
          <span class="crumb-item hover-text" @click="goBackToAdoptList">认养列表</span>
          <span class="separator">/</span>
          <span class="crumb-item active">{{ detail.title }}</span>
        </nav>

        <div class="main-layout">
          <div class="gallery-section">
            <div class="main-image-wrapper">
              <img :src="detail.coverImage" :alt="detail.title" class="main-image" />
            </div>
          </div>

          <div class="info-section">
            <h1 class="product-title">{{ detail.title }}</h1>

            <div class="price-card">
              <div class="price-header">
                <div class="price-main">
                  <span class="currency">¥</span>
                  <span class="amount">{{ detail.price }}</span>
                </div>
              </div>
              <div v-if="detail.expectedYield" class="price-footer">
                <div class="yield-info">
                  预计收益: <span class="highlight">{{ detail.expectedYield }}</span>
                </div>
              </div>
            </div>

            <div class="params-grid">
              <div class="param-item">
                <span class="label">认养周期</span>
                <span class="value">{{ detail.adoptDays }} 天</span>
              </div>
              <div class="param-item">
                <span class="label">动物分类</span>
                <span class="value">{{ getCategoryName(detail.animalCategory) }}</span>
              </div>
            </div>

            <div class="action-group">
              <button class="btn-primary" :disabled="detail.status !== 1" @click="handleBuy">
                {{ detail.status === 1 ? '立即认养' : '认养已结束' }}
              </button>
            </div>
          </div>
        </div>

        <div class="details-tabs">
          <div class="tab-header">
            <div class="tab-item active">项目详情</div>
          </div>

          <div class="details-content-layout">
            <aside class="details-sidebar" v-if="detail.shopId || shopInfo.shopName">
              <ShopSummaryCard :shop-info="shopInfo" @enter="goToShopHome" />
            </aside>

            <div class="tab-body">
              <div class="rich-text-content">
                <h3 class="section-title">认养说明</h3>
                <p class="desc-text">{{ detail.description || '暂无详细说明' }}</p>
              </div>

              <div class="comments-section-placeholder">
                <div class="section-divider">
                  <span>用户评论</span>
                </div>
                <div class="empty-comment">
                  <CommentOutlined class="empty-icon" />
                  <p>评论功能暂未开放，敬请期待...</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-else-if="!loading" class="empty-state">
        <a-empty description="该认养项目不存在或已被删除" />
        <a-button type="primary" @click="goBackToAdoptList">返回列表</a-button>
      </div>
    </a-spin>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { CommentOutlined } from '@ant-design/icons-vue'
import ShopSummaryCard from '@/components/shop/ShopSummaryCard.vue'
import { getAdoptItemDetail, listAnimalCategories } from '@/api/adopt'
import { getShopInfo } from '@/api/shop'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref(null)
const categories = ref([])
const shopInfo = ref({})

const fetchDetail = async (id) => {
  loading.value = true
  try {
    const res = await getAdoptItemDetail(id)
    if (res.code === '0' && res.data) {
      detail.value = res.data
      await fetchShopInfo(res.data.shopId)
    } else {
      message.error(res.message || '获取详情失败')
    }
  } catch (error) {
    message.error('系统繁忙，请稍后重试')
  } finally {
    loading.value = false
  }
}

const fetchCategories = async () => {
  try {
    const res = await listAnimalCategories()
    if (res.code === '0' && res.data) {
      categories.value = res.data
    }
  } catch (error) {
    console.error('Failed to fetch categories', error)
  }
}

const fetchShopInfo = async (shopId) => {
  if (!shopId) {
    shopInfo.value = {}
    return
  }
  try {
    const response = await getShopInfo(shopId)
    if (response.code === '0' && response.data) {
      shopInfo.value = response.data
    }
  } catch (error) {
    shopInfo.value = {}
  }
}

const getCategoryName = (code) => {
  const category = categories.value.find(item => item.code === code)
  return category ? category.name : code
}

const handleBuy = () => {
  if (detail.value) {
    router.push(`/adopt/order/create/${detail.value.id}`)
  }
}

const goBackToAdoptList = () => {
  router.push({
    name: 'productList',
    query: { mode: 'adopt' }
  })
}

const goToShopHome = () => {
  const targetShopId = shopInfo.value.id || detail.value?.shopId
  if (!targetShopId) {
    message.warning('店铺信息加载中，请稍后重试')
    return
  }
  router.push(`/shop/${targetShopId}`)
}

const getRouteId = () => {
  const id = route.params.id
  return Array.isArray(id) ? id[0] : id
}

onMounted(() => {
  const id = getRouteId()
  if (id) {
    fetchDetail(id)
    fetchCategories()
  } else {
    message.error('参数错误')
    goBackToAdoptList()
  }
})
</script>

<style scoped>
.adopt-detail-page {
  min-height: 100vh;
  padding-bottom: 40px;
  font-family: 'Inter', system-ui, sans-serif;
  color: #111827;
}

.detail-container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 16px;
}

.breadcrumb {
  padding: 16px 0;
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #6b7280;
}

.crumb-item.hover-text:hover {
  color: #111827;
  cursor: pointer;
}

.crumb-item.active {
  color: #111827;
  font-weight: 500;
}

.separator {
  margin: 0 8px;
  color: #d1d5db;
}

.main-layout {
  display: grid;
  grid-template-columns: 7fr 5fr;
  gap: 32px;
  margin-top: 16px;
}

@media (max-width: 1024px) {
  .main-layout {
    grid-template-columns: 1fr;
  }
}

.gallery-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.main-image-wrapper {
  position: relative;
  width: 100%;
  aspect-ratio: 4 / 3;
  background: #e5e7eb;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.main-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s;
}

.main-image:hover {
  transform: scale(1.05);
}

.info-section {
  display: flex;
  flex-direction: column;
}

.product-title {
  font-size: 28px;
  font-weight: 700;
  color: #111827;
  line-height: 1.3;
  margin-bottom: 24px;
}

.price-card {
  background: #f9fafb;
  border: 1px solid #f3f4f6;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 24px;
}

.price-header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}

.price-main {
  display: flex;
  align-items: baseline;
}

.currency {
  font-size: 18px;
  color: #6b7280;
  margin-right: 2px;
}

.amount {
  font-size: 36px;
  font-weight: 700;
  color: #111827;
  letter-spacing: -0.02em;
}

.price-footer {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #4b5563;
  margin-top: 12px;
}

.highlight {
  color: #111827;
  font-weight: 500;
}

.params-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 32px;
}

.param-item {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 16px;
  transition: border-color 0.2s;
}

.param-item:hover {
  border-color: #10b981;
}

.param-item .label {
  display: block;
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 4px;
}

.param-item .value {
  display: block;
  font-size: 16px;
  font-weight: 700;
  color: #111827;
}

.action-group {
  display: flex;
  gap: 16px;
}

.btn-primary {
  flex: 1;
  background: #10b981;
  color: white;
  border: none;
  padding: 16px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  transition: background 0.2s;
  box-shadow: 0 4px 6px -1px rgba(16, 185, 129, 0.3);
}

.btn-primary:hover:not(:disabled) {
  background: #059669;
}

.btn-primary:disabled {
  background: #e5e7eb;
  color: #9ca3af;
  cursor: not-allowed;
  box-shadow: none;
}

.details-tabs {
  margin-top: 64px;
}

.tab-header {
  display: flex;
  border-bottom: 1px solid #e5e7eb;
  margin-bottom: 32px;
  overflow-x: auto;
}

.tab-item {
  padding: 16px 4px;
  margin-right: 32px;
  font-size: 14px;
  font-weight: 500;
  color: #6b7280;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  white-space: nowrap;
}

.tab-item.active {
  color: #10b981;
  border-bottom-color: #10b981;
}

.tab-item:hover:not(.active) {
  color: #374151;
  border-bottom-color: #d1d5db;
}

.details-content-layout {
  display: grid;
  grid-template-columns: 3fr 9fr;
  gap: 32px;
}

@media (max-width: 768px) {
  .details-content-layout {
    grid-template-columns: 1fr;
  }

  .action-group {
    flex-direction: column;
  }
}

.details-sidebar {
  align-self: start;
}

.rich-text-content {
  color: #4b5563;
  line-height: 1.75;
}

.section-title {
  font-size: 18px;
  font-weight: 700;
  color: #111827;
  margin-bottom: 16px;
}

.comments-section-placeholder {
  margin-top: 48px;
  padding-top: 32px;
  border-top: 1px dashed #e5e7eb;
}

.section-divider {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
}

.section-divider span {
  font-size: 18px;
  font-weight: 700;
  color: #111827;
}

.empty-comment {
  background: #f9fafb;
  border-radius: 12px;
  padding: 40px;
  text-align: center;
  color: #9ca3af;
}

.empty-icon {
  font-size: 32px;
  margin-bottom: 12px;
  color: #d1d5db;
}

.empty-state {
  text-align: center;
  padding: 100px 0;
}
</style>
