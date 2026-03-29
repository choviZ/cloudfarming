<template>
  <div class="index-page">
    <header class="hero-header">
      <div class="hero-inner">
        <div class="brand-block" @click="router.push('/')">
          <h1 class="brand-title">云农场</h1>
          <span class="brand-subtitle">优质农产品与认养项目</span>
        </div>

        <div class="hero-search">
          <div class="hero-search-box">
            <SearchOutlined class="search-icon" />
            <input
              v-model="searchKeyword"
              type="text"
              class="search-input"
              placeholder="搜索农产品 / 认养项目"
              @keyup.enter="handleGlobalSearch"
            >
          </div>
          <button class="search-button" @click="handleGlobalSearch">搜索</button>
        </div>
      </div>
    </header>

    <section class="core-section">
      <HomeCategory />
      <HomeBanner />
      <HomeUserCard />
    </section>

    <section class="featured-section">
      <AdoptFeaturedSection />
    </section>

    <section class="market-section">
      <div class="section-header">
        <div>
          <h2 class="section-title">
            <ShoppingOutlined class="title-icon" />
            生鲜市集
          </h2>
          <p class="section-subtitle">原产地直供，支持从认养到采购的一体化选购</p>
        </div>
        <button class="view-all-button" @click="router.push('/product/list')">
          查看全部
          <RightOutlined />
        </button>
      </div>

      <a-skeleton :loading="productLoading" active :paragraph="{ rows: 8 }">
        <div v-if="productList.length" class="product-grid">
          <a-row :gutter="[20, 20]">
            <a-col
              v-for="product in productList"
              :key="product.id"
              :xs="24"
              :sm="12"
              :md="8"
              :lg="6"
              :xl="6"
            >
              <a-card class="product-card" hoverable @click="goToProductDetail(product.id)">
                <template #cover>
                  <div class="product-image-wrap">
                    <img
                      :src="getFirstImage(product.images || product.image)"
                      :alt="product.title"
                      class="product-image"
                    >
                  </div>
                </template>

                <div class="product-body">
                  <h3 class="product-title">{{ product.title }}</h3>
                  <div class="product-tag-row">
                    <span class="product-tag">农产品</span>
                  </div>
                  <div class="product-footer">
                    <div class="product-price">
                      <span v-if="product.minPrice">￥{{ product.minPrice }}</span>
                      <span v-else>价格待定</span>
                    </div>
                    <span class="product-extra">产地直发</span>
                  </div>
                </div>
              </a-card>
            </a-col>
          </a-row>
        </div>

        <a-empty v-else description="暂无农产品" />
      </a-skeleton>

      <div class="pagination-wrap" v-if="pagination.total > 0">
        <a-pagination
          v-model:current="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :show-size-changer="true"
          :page-size-options="['20', '40']"
          :show-total="(total) => `共 ${total} 件商品`"
          @change="handlePageChange"
          @show-size-change="handleSizeChange"
        />
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { RightOutlined, SearchOutlined, ShoppingOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { listSpuByPage } from '@/api/spu'
import AdoptFeaturedSection from '@/components/adopt/AdoptFeaturedSection.vue'
import HomeBanner from '@/components/home/HomeBanner.vue'
import HomeCategory from '@/components/home/HomeCategory.vue'
import HomeUserCard from '@/components/home/HomeUserCard.vue'

const router = useRouter()
const searchKeyword = ref('')
const productList = ref([])
const productLoading = ref(false)

const pagination = ref({
  current: 1,
  size: 20,
  total: 0
})

const defaultProductPlaceholder = `data:image/svg+xml;charset=UTF-8,${encodeURIComponent(`
<svg xmlns="http://www.w3.org/2000/svg" width="420" height="320" viewBox="0 0 420 320">
  <defs>
    <linearGradient id="bg" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="#eef7ee" />
      <stop offset="100%" stop-color="#d7ead8" />
    </linearGradient>
  </defs>
  <rect width="420" height="320" rx="24" fill="url(#bg)" />
  <circle cx="118" cy="126" r="46" fill="#b4d1b7" />
  <rect x="170" y="90" width="148" height="18" rx="9" fill="#8db694" />
  <rect x="170" y="124" width="118" height="14" rx="7" fill="#a8c8ad" />
  <rect x="98" y="206" width="224" height="16" rx="8" fill="#8db694" opacity="0.85" />
  <text x="210" y="270" text-anchor="middle" font-size="24" fill="#2d5a36" font-family="Microsoft YaHei, sans-serif">云养殖助农平台</text>
</svg>
`)}`

const getFirstImage = (imageStr) => {
  if (!imageStr) {
    return defaultProductPlaceholder
  }
  return imageStr.trim().split(',')[0].trim() || imageStr
}

const fetchProducts = async () => {
  productLoading.value = true
  try {
    const queryParam = {
      current: pagination.value.current,
      size: pagination.value.size,
      status: 1
    }
    const response = await listSpuByPage(queryParam)
    if (response.code === '0' && response.data) {
      productList.value = response.data.records || []
      pagination.value.total = Number(response.data.total || 0)
      return
    }
    message.error(response.message || '获取商品数据失败')
  } catch (error) {
    console.error(error)
    message.error('系统繁忙，请稍后重试')
  } finally {
    productLoading.value = false
  }
}

const handleGlobalSearch = () => {
  const keyword = searchKeyword.value.trim()
  if (!keyword) {
    message.warning('请输入搜索关键词')
    return
  }
  router.push({
    name: 'search',
    query: { q: keyword }
  })
}

const handlePageChange = (page) => {
  pagination.value.current = page
  fetchProducts()
}

const handleSizeChange = (current, size) => {
  pagination.value.current = current
  pagination.value.size = size
  fetchProducts()
}

const goToProductDetail = (productId) => {
  router.push({
    name: 'productDetail',
    params: { id: productId.toString() }
  })
}

onMounted(() => {
  fetchProducts()
})
</script>

<style scoped>
.index-page {
  min-height: 100vh;
  padding-bottom: 40px;
  background:
    radial-gradient(circle at top left, rgba(163, 230, 53, 0.15), transparent 24%),
    linear-gradient(180deg, #f8fbf5 0%, #ffffff 100%);
}

.hero-header {
  background: #ffffff;
  border-bottom: 1px solid rgba(15, 23, 42, 0.06);
}

.hero-inner {
  max-width: 1620px;
  margin: 0 auto;
  padding: 26px 20px;
  display: flex;
  align-items: center;
  gap: 32px;
}

.brand-block {
  width: 220px;
  cursor: pointer;
  flex-shrink: 0;
}

.brand-title {
  margin: 0;
  font-size: 34px;
  line-height: 1;
  letter-spacing: -1px;
  color: #17212b;
  font-weight: 800;
}

.brand-subtitle {
  display: block;
  margin-top: 8px;
  color: #2f7d46;
  font-size: 13px;
  letter-spacing: 1px;
}

.hero-search {
  flex: 1;
  max-width: 760px;
  margin: 0 auto;
  display: flex;
  gap: 12px;
}

.hero-search-box {
  flex: 1;
  height: 50px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 18px;
  border-radius: 999px;
  border: 2px solid #dbe5dd;
  background: #f8fbf7;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.hero-search-box:focus-within {
  border-color: #2f8b49;
  box-shadow: 0 0 0 4px rgba(47, 139, 73, 0.12);
  background: #fff;
}

.search-icon {
  color: #8aa08f;
  font-size: 18px;
}

.search-input {
  width: 100%;
  border: none;
  outline: none;
  background: transparent;
  font-size: 15px;
  color: #17212b;
}

.search-button {
  height: 50px;
  padding: 0 28px;
  border: none;
  border-radius: 999px;
  background: linear-gradient(135deg, #1b6a3a 0%, #2f8b49 100%);
  color: #fff;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
}

.core-section,
.market-section,
.featured-section {
  width: 100%;
  max-width: 1620px;
  margin: 0 auto;
  padding: 0 20px;
}

.core-section {
  display: flex;
  gap: 24px;
  height: 460px;
  margin-top: 24px;
}

.featured-section {
  margin-top: 44px;
}

.market-section {
  margin-top: 48px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 24px;
  margin-bottom: 28px;
}

.section-title {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 26px;
  font-weight: 800;
  color: #17212b;
}

.title-icon {
  color: #d97706;
}

.section-subtitle {
  margin: 8px 0 0;
  font-size: 14px;
  color: #5f6d64;
}

.view-all-button {
  height: 42px;
  padding: 0 18px;
  border-radius: 999px;
  border: 1px solid #dbe5dd;
  background: #fff;
  color: #355241;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.product-grid {
  width: 100%;
}

.product-card {
  border: none;
  border-radius: 18px;
  overflow: hidden;
  background: #fff;
  box-shadow: 0 10px 26px rgba(33, 53, 39, 0.08);
}

.product-image-wrap {
  width: 100%;
  height: 240px;
  background: #eef3ea;
  overflow: hidden;
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.3s ease;
}

.product-card:hover .product-image {
  transform: scale(1.04);
}

.product-card :deep(.ant-card-body) {
  padding: 16px;
}

.product-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.product-title {
  margin: 0;
  font-size: 16px;
  color: #17212b;
  line-height: 1.5;
  overflow: hidden;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.product-tag-row {
  display: flex;
  gap: 8px;
}

.product-tag {
  padding: 3px 8px;
  border-radius: 999px;
  background: #eef8ef;
  color: #24713f;
  font-size: 12px;
  font-weight: 600;
}

.product-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.product-price {
  font-size: 22px;
  font-weight: 800;
  color: #c2410c;
}

.product-extra {
  font-size: 12px;
  color: #7a8a7d;
}

.pagination-wrap {
  margin-top: 34px;
  display: flex;
  justify-content: center;
}

@media (max-width: 1200px) {
  .core-section {
    height: auto;
    flex-direction: column;
  }
}

@media (max-width: 900px) {
  .hero-inner,
  .section-header {
    flex-direction: column;
    align-items: stretch;
  }

  .hero-search {
    max-width: none;
    margin: 0;
  }

  .brand-block {
    width: auto;
  }
}

@media (max-width: 640px) {
  .hero-inner,
  .core-section,
  .market-section,
  .featured-section {
    padding-left: 16px;
    padding-right: 16px;
  }

  .hero-search {
    flex-direction: column;
  }

  .search-button,
  .view-all-button {
    width: 100%;
    justify-content: center;
  }
}
</style>
