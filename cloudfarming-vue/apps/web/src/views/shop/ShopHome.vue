<template>
  <div class="shop-home-page">
    <a-spin :spinning="pageLoading" class="shop-home-spin">
      <a-result
        v-if="loadFailed"
        status="404"
        title="店铺不存在或暂时不可访问"
        sub-title="请确认店铺链接是否正确，或稍后再试。"
      >
        <template #extra>
          <a-button type="primary" @click="router.push('/')">返回首页</a-button>
        </template>
      </a-result>

      <template v-else>
        <section class="shop-hero" :style="heroBannerStyle">
          <div class="shop-hero__mask" />
          <div class="shop-hero__content">
            <a-breadcrumb class="shop-breadcrumb">
              <a-breadcrumb-item>
                <span class="breadcrumb-link" @click="router.push('/')">首页</span>
              </a-breadcrumb-item>
              <a-breadcrumb-item>店铺首页</a-breadcrumb-item>
            </a-breadcrumb>

            <div class="shop-summary">
              <div class="shop-avatar">
                <img v-if="shopInfo.shopAvatar" :src="shopInfo.shopAvatar" alt="店铺头像" />
                <span v-else>{{ shopInitial }}</span>
              </div>
              <div class="shop-meta">
                <div class="shop-meta__eyebrow">SHOP HOME</div>
                <h1>{{ shopInfo.shopName || '店铺首页' }}</h1>
                <p>{{ shopDescription }}</p>
              </div>
            </div>
          </div>
        </section>

        <section v-if="shopInfo.announcement" class="notice-section">
          <div class="notice-card">
            <div class="notice-card__label">店铺公告</div>
            <div class="notice-card__text">{{ shopInfo.announcement }}</div>
          </div>
        </section>

        <section class="content-section">
          <a-tabs v-model:activeKey="activeTab" class="shop-tabs">
            <a-tab-pane key="products" tab="在售商品">
              <a-spin :spinning="productLoading">
                <div v-if="productList.length" class="card-grid">
                  <article
                    v-for="item in productList"
                    :key="item.id"
                    class="content-card"
                    @click="router.push(`/product/${item.id}`)"
                  >
                    <div class="content-card__image-wrap">
                      <img :src="getProductImage(item.images)" :alt="item.title" class="content-card__image" />
                    </div>
                    <div class="content-card__body">
                      <div class="content-card__tag">商品</div>
                      <h3 class="content-card__title">{{ item.title }}</h3>
                      <div class="content-card__meta">
                        <span class="price">￥{{ formatPrice(item.minPrice) }}</span>
                      </div>
                      <button type="button" class="enter-btn">查看商品</button>
                    </div>
                  </article>
                </div>
                <a-empty v-else description="当前店铺暂无在售商品" />
                <div v-if="productPagination.total > productPagination.pageSize" class="pagination-wrap">
                  <a-pagination
                    :current="productPagination.current"
                    :page-size="productPagination.pageSize"
                    :total="productPagination.total"
                    :show-size-changer="false"
                    @change="handleProductPageChange"
                  />
                </div>
              </a-spin>
            </a-tab-pane>

            <a-tab-pane key="adopts" tab="认养项目">
              <a-spin :spinning="adoptLoading">
                <div v-if="adoptList.length" class="card-grid">
                  <article
                    v-for="item in adoptList"
                    :key="item.id"
                    class="content-card"
                    @click="router.push(`/adopt/detail/${item.id}`)"
                  >
                    <div class="content-card__image-wrap">
                      <img :src="item.coverImage" :alt="item.title" class="content-card__image" />
                    </div>
                    <div class="content-card__body">
                      <div class="content-card__tag content-card__tag--adopt">认养</div>
                      <h3 class="content-card__title">{{ item.title }}</h3>
                      <div class="content-card__meta">
                        <span class="price">￥{{ formatPrice(item.price) }}</span>
                        <span class="hint">{{ item.adoptDays || '--' }} 天周期</span>
                      </div>
                      <button type="button" class="enter-btn">查看项目</button>
                    </div>
                  </article>
                </div>
                <a-empty v-else description="当前店铺暂无可认养项目" />
                <div v-if="adoptPagination.total > adoptPagination.pageSize" class="pagination-wrap">
                  <a-pagination
                    :current="adoptPagination.current"
                    :page-size="adoptPagination.pageSize"
                    :total="adoptPagination.total"
                    :show-size-changer="false"
                    @change="handleAdoptPageChange"
                  />
                </div>
              </a-spin>
            </a-tab-pane>
          </a-tabs>
        </section>
      </template>
    </a-spin>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'
import { pageAdoptItems } from '@/api/adopt'
import { getShopInfo } from '@/api/shop'
import { listSpuByPage } from '@/api/spu'

const route = useRoute()
const router = useRouter()

const pageLoading = ref(false)
const productLoading = ref(false)
const adoptLoading = ref(false)
const loadFailed = ref(false)
const activeTab = ref('products')

const shopInfo = ref({})
const productList = ref([])
const adoptList = ref([])

const productPagination = reactive({
  current: 1,
  pageSize: 8,
  total: 0
})

const adoptPagination = reactive({
  current: 1,
  pageSize: 8,
  total: 0
})

const currentShopId = computed(() => {
  const value = Array.isArray(route.params.shopId) ? route.params.shopId[0] : route.params.shopId
  const shopId = Number(value)
  return Number.isFinite(shopId) ? shopId : 0
})

const shopInitial = computed(() => {
  const name = (shopInfo.value.shopName || '店').trim()
  return name ? name.charAt(0) : '店'
})

const shopDescription = computed(() => {
  const description = (shopInfo.value.description || '').trim()
  return description || '店主还没有填写店铺简介，欢迎先逛逛店铺里的商品和认养项目。'
})

const heroBannerStyle = computed(() => {
  if (shopInfo.value.shopBanner) {
    return {
      backgroundImage: `url(${shopInfo.value.shopBanner})`
    }
  }
  return {}
})

const formatPrice = (price) => {
  if (price === null || price === undefined || price === '') {
    return '--'
  }
  const numericPrice = Number(price)
  return Number.isNaN(numericPrice) ? '--' : numericPrice.toFixed(2)
}

const getProductImage = (images) => {
  if (!images) {
    return 'https://via.placeholder.com/480x480?text=Cloud+Farming'
  }
  return String(images)
    .split(',')
    .map((item) => item.trim())
    .find(Boolean) || 'https://via.placeholder.com/480x480?text=Cloud+Farming'
}

const fetchShopDetail = async () => {
  const res = await getShopInfo(currentShopId.value)
  if (res.code === '0' && res.data) {
    shopInfo.value = res.data
    return
  }
  throw new Error(res.message || '加载店铺信息失败')
}

const fetchProducts = async () => {
  productLoading.value = true
  try {
    const res = await listSpuByPage({
      current: productPagination.current,
      size: productPagination.pageSize,
      shopId: currentShopId.value,
      status: 1,
      auditStatus: 1
    })

    if (res.code === '0' && res.data) {
      productList.value = res.data.records || []
      productPagination.total = Number(res.data.total || 0)
      return
    }

    productList.value = []
    productPagination.total = 0
  } finally {
    productLoading.value = false
  }
}

const fetchAdopts = async () => {
  adoptLoading.value = true
  try {
    const res = await pageAdoptItems({
      current: adoptPagination.current,
      size: adoptPagination.pageSize,
      shopId: currentShopId.value,
      status: 1,
      reviewStatus: 1
    })

    if (res.code === '0' && res.data) {
      adoptList.value = res.data.records || []
      adoptPagination.total = Number(res.data.total || 0)
      return
    }

    adoptList.value = []
    adoptPagination.total = 0
  } finally {
    adoptLoading.value = false
  }
}

const fetchPageData = async () => {
  if (!currentShopId.value) {
    loadFailed.value = true
    return
  }

  pageLoading.value = true
  loadFailed.value = false
  productPagination.current = 1
  adoptPagination.current = 1

  try {
    await fetchShopDetail()
    await Promise.all([fetchProducts(), fetchAdopts()])
  } catch (error) {
    console.error('加载店铺首页失败', error)
    message.error('加载店铺首页失败')
    loadFailed.value = true
  } finally {
    pageLoading.value = false
  }
}

const handleProductPageChange = (page) => {
  productPagination.current = page
  fetchProducts()
}

const handleAdoptPageChange = (page) => {
  adoptPagination.current = page
  fetchAdopts()
}

onMounted(() => {
  fetchPageData()
})

watch(
  () => route.params.shopId,
  () => {
    fetchPageData()
  }
)
</script>

<style scoped>
.shop-home-page {
  min-height: calc(100vh - 120px);
  padding-bottom: 32px;
}

.shop-home-spin {
  width: 100%;
}

.shop-hero {
  position: relative;
  overflow: hidden;
  min-height: 320px;
  border-radius: 28px;
  background:
    radial-gradient(circle at top left, rgba(255, 255, 255, 0.16), transparent 28%),
    linear-gradient(135deg, #1b5e39 0%, #2f8b49 55%, #6fbf73 100%);
  background-size: cover;
  background-position: center;
}

.shop-hero__mask {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(10, 17, 13, 0.05) 0%, rgba(10, 17, 13, 0.52) 100%);
}

.shop-hero__content {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 320px;
  padding: 28px 32px 32px;
}

.shop-breadcrumb {
  color: rgba(255, 255, 255, 0.86);
}

.breadcrumb-link {
  cursor: pointer;
}

.shop-summary {
  display: flex;
  align-items: flex-end;
  gap: 20px;
}

.shop-avatar {
  width: 104px;
  height: 104px;
  flex-shrink: 0;
  border-radius: 28px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.18);
  border: 2px solid rgba(255, 255, 255, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 36px;
  font-weight: 700;
}

.shop-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.shop-meta__eyebrow {
  color: rgba(255, 255, 255, 0.78);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 1px;
}

.shop-meta h1 {
  margin: 10px 0 0;
  font-size: 38px;
  font-weight: 800;
  color: #fff;
}

.shop-meta p {
  max-width: 760px;
  margin: 14px 0 0;
  color: rgba(255, 255, 255, 0.9);
  line-height: 1.8;
}

.notice-section {
  margin-top: 18px;
}

.notice-card {
  padding: 22px 24px;
  border-radius: 22px;
  background: #f8fbf7;
  border: 1px solid #ebf4eb;
}

.notice-card__label {
  color: #2f8b49;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 1px;
}

.notice-card__text {
  margin-top: 10px;
  color: #1f2937;
  line-height: 1.8;
}

.content-section {
  margin-top: 20px;
  padding: 6px 0 0;
}

.shop-tabs :deep(.ant-tabs-nav) {
  margin-bottom: 20px;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 18px;
}

.content-card {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border-radius: 22px;
  background: #fff;
  border: 1px solid #edf2ed;
  box-shadow: 0 16px 36px rgba(17, 24, 39, 0.06);
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.content-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 20px 42px rgba(17, 24, 39, 0.1);
}

.content-card__image-wrap {
  aspect-ratio: 1 / 1;
  overflow: hidden;
  background: #f3f6f3;
}

.content-card__image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.content-card__body {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 12px;
  padding: 18px;
}

.content-card__tag {
  width: fit-content;
  min-height: 28px;
  padding: 0 12px;
  border-radius: 999px;
  background: #eef8f0;
  color: #1b6a3a;
  font-size: 12px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
}

.content-card__tag--adopt {
  background: #fff5eb;
  color: #c56a16;
}

.content-card__title {
  margin: 0;
  font-size: 16px;
  line-height: 1.6;
  color: #111827;
}

.content-card__meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  color: #6b7280;
  font-size: 13px;
}

.price {
  color: #c2410c;
  font-size: 22px;
  font-weight: 800;
}

.hint {
  color: #6b7280;
}

.enter-btn {
  margin-top: auto;
  height: 38px;
  border: none;
  border-radius: 999px;
  background: #17212b;
  color: #fff;
  font-weight: 600;
  cursor: pointer;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

@media (max-width: 768px) {
  .shop-hero__content {
    padding: 20px;
  }

  .shop-summary {
    flex-direction: column;
    align-items: flex-start;
  }

  .shop-meta h1 {
    font-size: 28px;
  }
}
</style>
