<template>
  <div class="index-page">
    <header class="hero-header">
      <div class="hero-inner">
        <div class="brand-block" @click="router.push('/')">
          <h1 class="brand-title">云养殖助农平台</h1>
          <span class="brand-subtitle">优质农产品与认养项目</span>
        </div>

        <div class="hero-search">
          <div class="hero-search-box">
            <SearchOutlined class="search-icon" />
            <div ref="searchModeRef" class="search-mode">
              <button type="button" class="search-mode-trigger" @click="toggleSearchModeMenu">
                <span>{{ currentSearchModeLabel }}</span>
                <DownOutlined class="search-mode-arrow" :class="{ open: isSearchModeMenuOpen }" />
              </button>
              <transition name="search-mode-fade">
                <div v-if="isSearchModeMenuOpen" class="search-mode-menu">
                  <button
                    v-for="option in SEARCH_MODE_OPTIONS"
                    :key="option.value"
                    type="button"
                    class="search-mode-option"
                    :class="{ active: searchMode === option.value }"
                    @click="handleSearchModeSelect(option.value)"
                  >
                    <span>{{ option.label }}</span>
                    <CheckOutlined v-if="searchMode === option.value" class="search-mode-check" />
                  </button>
                </div>
              </transition>
            </div>
            <span class="search-divider"></span>
            <input
              v-model="searchKeyword"
              type="text"
              class="search-input"
              :placeholder="searchPlaceholder"
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
        <button
          class="view-all-button"
          @click="router.push({ name: 'productList', query: { mode: 'product' } })"
        >
          查看全部
          <RightOutlined />
        </button>
      </div>

      <a-skeleton :loading="productLoading" active :paragraph="{ rows: 8 }">
        <div v-if="productList.length" class="product-grid">
          <a-row :gutter="[12, 20]">
            <a-col
              v-for="product in productList"
              :key="product.id"
              :xs="24"
              :sm="12"
              :md="8"
              :lg="6"
              :xl="4"
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

      <div class="pagination-wrap" v-if="false">
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
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  CheckOutlined,
  DownOutlined,
  RightOutlined,
  SearchOutlined,
  ShoppingOutlined
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { listSpuByPage } from '@/api/spu'
import AdoptFeaturedSection from '@/components/adopt/AdoptFeaturedSection.vue'
import HomeBanner from '@/components/home/HomeBanner.vue'
import HomeCategory from '@/components/home/HomeCategory.vue'
import HomeUserCard from '@/components/home/HomeUserCard.vue'

const SEARCH_MODE = Object.freeze({
  PRODUCT: 'product',
  ADOPT: 'adopt'
})

const SEARCH_MODE_OPTIONS = [
  { value: SEARCH_MODE.PRODUCT, label: '农产品' },
  { value: SEARCH_MODE.ADOPT, label: '认养项目' }
]

const router = useRouter()
const searchKeyword = ref('')
const searchMode = ref(SEARCH_MODE.PRODUCT)
const isSearchModeMenuOpen = ref(false)
const searchModeRef = ref(null)
const productList = ref([])
const productLoading = ref(false)

const HOME_PRODUCT_LIMIT = 6

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

const searchPlaceholder = computed(() =>
  searchMode.value === SEARCH_MODE.ADOPT
    ? '搜索认养项目，例如：生态黑猪、散养土鸡'
    : '搜索农产品，例如：有机草莓、生态鲜蛋'
)

const currentSearchModeLabel = computed(() => {
  return SEARCH_MODE_OPTIONS.find((item) => item.value === searchMode.value)?.label || '农产品'
})

const fetchProducts = async () => {
  productLoading.value = true
  try {
    const queryParam = {
      current: 1,
      size: HOME_PRODUCT_LIMIT,
      status: 1
    }
    const response = await listSpuByPage(queryParam)
    if (response.code === '0' && response.data) {
      productList.value = response.data.records || []
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
    name: 'productList',
    query: {
      mode: searchMode.value,
      keyword
    }
  })
}

const closeSearchModeMenu = () => {
  isSearchModeMenuOpen.value = false
}

const toggleSearchModeMenu = () => {
  isSearchModeMenuOpen.value = !isSearchModeMenuOpen.value
}

const handleSearchModeSelect = (mode) => {
  searchMode.value = mode
  closeSearchModeMenu()
}

const handleDocumentClick = (event) => {
  if (!searchModeRef.value) {
    return
  }

  const target = event.target
  if (target instanceof Node && !searchModeRef.value.contains(target)) {
    closeSearchModeMenu()
  }
}

const goToProductDetail = (productId) => {
  router.push({
    name: 'productDetail',
    params: { id: productId.toString() }
  })
}

onMounted(() => {
  fetchProducts()
  document.addEventListener('mousedown', handleDocumentClick)
})

onBeforeUnmount(() => {
  document.removeEventListener('mousedown', handleDocumentClick)
})
</script>

<style scoped>
.index-page {
  min-height: 100vh;
  padding-bottom: 40px;
  background: #ffffff;
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
  font-size: 32px;
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
  padding: 0 12px 0 18px;
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
  flex-shrink: 0;
}

.search-mode {
  position: relative;
  flex-shrink: 0;
}

.search-mode-trigger {
  width: 96px;
  height: 36px;
  padding: 0 10px 0 14px;
  border: 1px solid #e3ebe5;
  border-radius: 12px;
  background: linear-gradient(180deg, #ffffff 0%, #f6faf7 100%);
  color: #355241;
  font-size: 14px;
  font-weight: 700;
  outline: none;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  box-shadow: 0 6px 16px rgba(31, 109, 61, 0.08);
  transition:
    border-color 0.2s ease,
    box-shadow 0.2s ease,
    transform 0.2s ease;
}

.search-mode-trigger:hover {
  border-color: #c8d8cd;
  box-shadow: 0 10px 24px rgba(31, 109, 61, 0.12);
}

.search-mode-trigger:focus-visible {
  box-shadow:
    0 0 0 4px rgba(47, 139, 73, 0.12),
    0 10px 24px rgba(31, 109, 61, 0.12);
}

.search-mode-arrow {
  font-size: 12px;
  color: #7e8f84;
  transition: transform 0.2s ease, color 0.2s ease;
}

.search-mode-arrow.open {
  transform: rotate(180deg);
  color: #2f8b49;
}

.search-mode-menu {
  position: absolute;
  top: calc(100% + 10px);
  left: 0;
  min-width: 128px;
  padding: 8px;
  border-radius: 16px;
  background: #ffffff;
  border: 1px solid #e7eeea;
  box-shadow: 0 22px 44px rgba(23, 33, 43, 0.12);
  z-index: 20;
}

.search-mode-option {
  width: 100%;
  height: 42px;
  padding: 0 12px;
  border: none;
  border-radius: 12px;
  background: transparent;
  color: #40544a;
  font-size: 14px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
  transition:
    background-color 0.2s ease,
    color 0.2s ease,
    transform 0.2s ease;
}

.search-mode-option:hover {
  background: #f4f8f5;
  color: #1f6d3d;
  transform: translateX(2px);
}

.search-mode-option.active {
  background: linear-gradient(135deg, rgba(47, 139, 73, 0.12) 0%, rgba(47, 139, 73, 0.08) 100%);
  color: #1f6d3d;
}

.search-mode-check {
  font-size: 14px;
}

.search-mode-fade-enter-active,
.search-mode-fade-leave-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
}

.search-mode-fade-enter-from,
.search-mode-fade-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}

.search-divider {
  width: 1px;
  height: 24px;
  background: #dbe5dd;
  flex-shrink: 0;
}

.search-input {
  flex: 1;
  width: auto;
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
  width: 253px;
  height: 335px;
  margin: 0 auto;
  border: none;
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
  box-shadow: 0 10px 26px rgba(33, 53, 39, 0.08);
  transition: all 0.3s;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.product-image-wrap {
  width: 253px;
  height: 253px;
  background: #eef3ea;
  overflow: hidden;
  display: flex;
  justify-content: center;
}

.product-image {
  width: 253px;
  height: 253px;
  object-fit: cover;
  display: block;
  transition: transform 0.3s ease;
}

.product-card:hover .product-image {
  transform: scale(1.04);
}

.product-card :deep(.ant-card-body) {
  padding: 8px 12px;
  height: 82px;
  display: flex;
  flex-direction: column;
}

.product-body {
  display: flex;
  flex-direction: column;
  gap: 2px;
  height: 100%;
  min-height: 0;
}

.product-title {
  display: block;
  width: 100%;
  margin: 0;
  font-size: 14px;
  font-weight: 500;
  color: #1f1f1f;
  line-height: 20px;
  min-height: 20px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex-shrink: 0;
}

.product-tag-row {
  display: flex;
  gap: 6px;
  min-height: 18px;
  flex-shrink: 0;
}

.product-tag {
  padding: 1px 6px;
  border-radius: 4px;
  background: #eef8ef;
  color: #24713f;
  font-size: 12px;
  line-height: 16px;
  font-weight: 600;
  width: fit-content;
}

.product-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: auto;
  min-height: 20px;
}

.product-price {
  display: flex;
  align-items: flex-end;
  color: #ff5000;
  font-size: 18px;
  font-weight: 700;
  line-height: 20px;
}

.product-extra {
  font-size: 12px;
  color: #999;
  background-color: #f5f5f5;
  padding: 1px 6px;
  line-height: 16px;
  border-radius: 4px;
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

  .hero-search-box {
    width: 100%;
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

  .hero-search-box {
    padding-right: 14px;
  }

  .search-mode-trigger {
    width: 92px;
    padding-left: 10px;
    padding-right: 10px;
  }

  .search-button,
  .view-all-button {
    width: 100%;
    justify-content: center;
  }
}
</style>
