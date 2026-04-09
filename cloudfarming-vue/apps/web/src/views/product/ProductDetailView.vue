<template>
  <div class="product-detail-page">
    <div class="container">
      <a-breadcrumb class="breadcrumb">
        <a-breadcrumb-item><a href="/">首页</a></a-breadcrumb-item>
        <a-breadcrumb-item><a href="/product/list">生鲜市集</a></a-breadcrumb-item>
        <a-breadcrumb-item>商品详情</a-breadcrumb-item>
      </a-breadcrumb>

      <a-spin :spinning="loading" tip="加载中...">
        <a-result
          v-if="productMissing"
          status="404"
          title="商品不存在或已下架"
          sub-title="该商品可能已删除、下架或链接已失效。"
        >
          <template #extra>
            <a-button type="primary" @click="goToProductList">返回生鲜市集</a-button>
          </template>
        </a-result>

        <template v-else>
        <section class="product-hero">
          <div class="gallery-section">
            <div class="main-image-wrapper">
              <img
                v-if="productImages.length > 0"
                :src="productImages[currentImageIndex]"
                alt="商品主图"
                class="main-image"
              >
              <div v-else class="no-image">
                <ShopOutlined class="no-image-icon" />
                <span>暂无图片</span>
              </div>
              <div class="tag-badge">产地直采</div>
            </div>

            <div v-if="productImages.length > 1" class="thumbnail-list">
              <button
                v-for="(image, index) in productImages"
                :key="`${image}-${index}`"
                type="button"
                class="thumbnail-item"
                :class="{ active: currentImageIndex === index }"
                @mouseenter="currentImageIndex = index"
              >
                <img :src="image" alt="缩略图">
              </button>
            </div>

          </div>

          <div class="info-section">
            <h1 class="product-title">{{ spuInfo.title || '商品名称加载中...' }}</h1>
            <p class="product-desc">{{ spuInfo.description || '暂无描述' }}</p>

            <div class="price-box">
              <div class="price-row">
                <span class="price-label">价格</span>
                <div class="price-value-wrapper">
                  <span class="currency">¥</span>
                  <span class="amount">{{ displayPrice }}</span>
                </div>
              </div>
            </div>

            <div v-if="specKeys.length > 0" class="sku-section">
              <div v-for="key in specKeys" :key="key" class="sku-row">
                <span class="sku-label">{{ key }}</span>
                <div class="sku-options">
                  <button
                    v-for="value in specs[key]"
                    :key="value"
                    type="button"
                    class="sku-chip"
                    :class="{ active: selectedSpecs[key] === value }"
                    @click="selectSpec(key, value)"
                  >
                    {{ value }}
                    <CheckOutlined v-if="selectedSpecs[key] === value" class="check-icon" />
                  </button>
                </div>
              </div>
            </div>

            <div class="quantity-section">
              <span class="sku-label">数量</span>
              <div class="quantity-wrapper">
                <a-input-number
                  v-model:value="quantity"
                  :min="1"
                  :max="currentSku ? currentSku.stock : 9999"
                  class="quantity-input"
                  :disabled="!currentSku && specKeys.length > 0"
                />
              </div>
            </div>

            <div class="services-row">
              <div class="service-item">
                <SafetyCertificateOutlined class="service-icon" />
                <span>坏果包赔</span>
              </div>
              <div class="service-item">
                <ClockCircleOutlined class="service-icon" />
                <span>48h 发货</span>
              </div>
            </div>

            <div class="action-buttons">
              <button class="btn btn-secondary" :disabled="!canBuy" @click="addToCart">加入购物车</button>
              <button class="btn btn-primary" :disabled="!canBuy" @click="buyNow">立即购买</button>
            </div>
          </div>
        </section>

        <section class="content-layout">
          <aside class="sidebar">
            <shop-summary-card :shop-info="shopInfo" @enter="goToShopHome" />
          </aside>

          <div class="detail-container">
            <div class="sticky-tabs">
              <div class="tab-item" :class="{ active: activeTab === 'detail' }" @click="activeTab = 'detail'">
                商品详情
              </div>
              <div class="tab-item" :class="{ active: activeTab === 'reviews' }" @click="activeTab = 'reviews'">
                累计评价 ({{ reviewSummary.totalReviewCount || 0 }})
              </div>
            </div>

            <div class="tab-content">
              <div v-if="activeTab === 'detail'">
                <section class="params-table">
                  <h3 class="section-title">
                    <BarsOutlined class="icon" />
                    产品参数
                  </h3>
                  <div class="params-grid">
                    <div
                      v-for="(value, key) in spuInfo.baseAttrs"
                      :key="key"
                      class="param-item"
                    >
                      <span class="param-label">{{ key }}</span>
                      <span class="param-value">{{ value }}</span>
                    </div>
                    <div
                      v-if="!spuInfo.baseAttrs || Object.keys(spuInfo.baseAttrs).length === 0"
                      class="no-params"
                    >
                      暂无详细参数
                    </div>
                  </div>
                </section>

                <section class="detail-images">
                  <div class="prose">
                    <p v-if="spuInfo.description">{{ spuInfo.description }}</p>
                    <div v-if="productImages.length > 0" class="image-stack">
                      <img
                        v-for="(image, index) in productImages"
                        :key="`${image}-${index}`"
                        :src="image"
                        class="detail-img"
                      >
                    </div>
                  </div>
                </section>
              </div>

              <product-review-section
                v-else
                :spu-id="Number(spuId)"
                @summary-change="handleReviewSummaryChange"
              />
            </div>
          </div>
        </section>
        </template>
      </a-spin>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { addToCart as addToCartApi } from '@/api/cart'
import { getSpuReviewSummary } from '@/api/order'
import { getSpuDetail } from '@/api/spu'
import { getShopInfo } from '@/api/shop'
import ProductReviewSection from '@/components/review/ProductReviewSection.vue'
import ShopSummaryCard from '@/components/shop/ShopSummaryCard.vue'
import {
  BarsOutlined,
  CheckOutlined,
  ClockCircleOutlined,
  SafetyCertificateOutlined,
  ShopOutlined
} from '@ant-design/icons-vue'

const route = useRoute()
const router = useRouter()
const spuId = route.params.id

const loading = ref(false)
const productMissing = ref(false)
const spuInfo = ref({})
const skuList = ref([])
const productImages = ref([])
const currentImageIndex = ref(0)
const quantity = ref(1)
const activeTab = ref('detail')
const shopInfo = ref({})
const reviewSummary = ref({
  totalReviewCount: 0
})

const specs = reactive({})
const selectedSpecs = reactive({})
const specKeys = computed(() => Object.keys(specs))

const currentSku = computed(() => {
  if (specKeys.value.length === 0) {
    return skuList.value.length === 1 ? skuList.value[0] : null
  }
  for (const key of specKeys.value) {
    if (!selectedSpecs[key]) {
      return null
    }
  }
  return skuList.value.find((sku) => {
    return specKeys.value.every((key) => sku.saleAttrs?.[key] === selectedSpecs[key])
  }) || null
})

const displayPrice = computed(() => {
  if (currentSku.value) {
    return currentSku.value.price
  }
  if (skuList.value.length > 0) {
    const prices = skuList.value.map((sku) => Number(sku.price)).filter((price) => !Number.isNaN(price)).sort((a, b) => a - b)
    const min = prices[0]
    const max = prices[prices.length - 1]
    return min === max ? min : `${min} ~ ${max}`
  }
  return spuInfo.value.price || '待定'
})

const canBuy = computed(() => !!currentSku.value && Number(currentSku.value.stock) > 0)

const normalizeProductImages = (images) => {
  if (!images) {
    return []
  }
  return String(images)
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
}

const normalizeBaseAttrs = (attributes) => {
  if (!attributes) {
    return {}
  }
  if (typeof attributes === 'string') {
    try {
      const parsed = JSON.parse(attributes)
      if (Array.isArray(parsed)) {
        return parsed.reduce((result, item) => {
          if (item?.name) {
            result[item.name] = item.attr_value || item.value || ''
          }
          return result
        }, {})
      }
      if (parsed && typeof parsed === 'object') {
        if (parsed.name) {
          return {
            [parsed.name]: parsed.attr_value || parsed.value || ''
          }
        }
        return parsed
      }
    } catch (error) {
      return {}
    }
  }
  return typeof attributes === 'object' ? attributes : {}
}

const normalizeSkuSaleAttrs = (skus = []) => {
  return skus.map((sku) => {
    if (sku.saleAttribute && typeof sku.saleAttribute === 'string') {
      try {
        return {
          ...sku,
          saleAttrs: JSON.parse(sku.saleAttribute)
        }
      } catch (error) {
        return {
          ...sku,
          saleAttrs: {}
        }
      }
    }
    return {
      ...sku,
      saleAttrs: sku.saleAttribute || sku.saleAttrs || {}
    }
  })
}

const extractSpecs = (skus = []) => {
  Object.keys(specs).forEach((key) => delete specs[key])
  Object.keys(selectedSpecs).forEach((key) => delete selectedSpecs[key])
  const temp = {}
  skus.forEach((sku) => {
    Object.entries(sku.saleAttrs || {}).forEach(([key, value]) => {
      if (!temp[key]) {
        temp[key] = new Set()
      }
      temp[key].add(value)
    })
  })
  Object.entries(temp).forEach(([key, value]) => {
    specs[key] = Array.from(value)
    selectedSpecs[key] = specs[key][0]
  })
}

const fetchProductDetail = async (id) => {
  loading.value = true
  productMissing.value = false
  try {
    const response = await getSpuDetail(Number(id))
    if (response.code !== '0' || !response.data) {
      productMissing.value = true
      message.warning('商品不存在或已下架')
      return
    }
    const { productSpu, productSkus } = response.data
    const normalizedSkus = normalizeSkuSaleAttrs(productSkus || [])
    spuInfo.value = {
      ...productSpu,
      baseAttrs: normalizeBaseAttrs(productSpu?.attributes)
    }
    skuList.value = normalizedSkus
    productImages.value = normalizeProductImages(productSpu?.images)
    currentImageIndex.value = 0
    extractSpecs(normalizedSkus)
    fetchShopInfo(productSpu?.shopId)
  } catch (error) {
    console.error(error)
    message.error('获取商品详情失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const fetchShopInfo = async (shopId) => {
  if (!shopId) {
    return
  }
  try {
    const response = await getShopInfo(shopId)
    if (response.code === '0' && response.data) {
      shopInfo.value = response.data
    }
  } catch (error) {
    console.error(error)
  }
}

const fetchReviewSummary = async (id) => {
  if (!id) {
    reviewSummary.value = { totalReviewCount: 0 }
    return
  }
  try {
    const response = await getSpuReviewSummary(Number(id))
    if (response.code === '0' && response.data) {
      reviewSummary.value = response.data
      return
    }
  } catch (error) {
    console.error(error)
  }
  reviewSummary.value = { totalReviewCount: 0 }
}

const handleReviewSummaryChange = (summary) => {
  reviewSummary.value = {
    ...reviewSummary.value,
    ...(summary || {})
  }
}

const selectSpec = (key, value) => {
  if (selectedSpecs[key] === value) {
    delete selectedSpecs[key]
    return
  }
  selectedSpecs[key] = value
}

const addToCart = async () => {
  if (!currentSku.value) {
    message.warning('请先选择规格')
    return
  }
  try {
    const response = await addToCartApi({
      skuId: currentSku.value.id,
      quantity: quantity.value,
      selected: true
    })
    if (response.code === '0') {
      message.success('已成功加入购物车')
      return
    }
    message.error(response.message || '加入购物车失败')
  } catch (error) {
    console.error(error)
    message.error('加入购物车失败，请稍后重试')
  }
}

const buyNow = () => {
  if (!currentSku.value) {
    message.warning('请先选择规格')
    return
  }
  router.push({
    path: '/order/create',
    query: {
      source: 'buy-now',
      type: 'product',
      spuId,
      skuId: String(currentSku.value.id),
      quantity: quantity.value
    }
  })
}

const goToShopHome = () => {
  const targetShopId = shopInfo.value.id || spuInfo.value.shopId
  if (!targetShopId) {
    message.warning('店铺信息加载中，请稍后再试')
    return
  }
  router.push(`/shop/${targetShopId}`)
}

const goToProductList = () => {
  router.push('/product/list')
}

onMounted(() => {
  if (!spuId) {
    message.error('未获取到商品ID')
    return
  }
  fetchProductDetail(spuId)
  fetchReviewSummary(spuId)
})
</script>

<style scoped>
.product-detail-page {
  min-height: 100vh;
  padding-bottom: 40px;
  color: #1f2937;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 16px;
}

.breadcrumb {
  padding: 24px 0;
  font-size: 14px;
}

.product-hero {
  display: grid;
  grid-template-columns: 5fr 7fr;
  gap: 48px;
  padding: 40px;
  margin-bottom: 36px;
  border-radius: 20px;
  background: #ffffff;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.08), 0 18px 40px rgba(31, 122, 63, 0.06);
}

.gallery-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.main-image-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  aspect-ratio: 1;
  overflow: hidden;
  border-radius: 18px;
  border: 1px solid #edf3ee;
  background: #f8faf8;
}

.main-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.no-image {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: #9ca3af;
}

.no-image-icon {
  font-size: 32px;
}

.tag-badge {
  position: absolute;
  top: 16px;
  left: 16px;
  display: inline-flex;
  align-items: center;
  height: 30px;
  padding: 0 12px;
  border-radius: 999px;
  background: #10b981;
  color: #ffffff;
  font-size: 12px;
  font-weight: 700;
  box-shadow: 0 10px 20px rgba(16, 185, 129, 0.18);
}

.thumbnail-list {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
}

.thumbnail-item {
  padding: 0;
  border: 2px solid transparent;
  border-radius: 12px;
  overflow: hidden;
  background: #f7faf7;
  cursor: pointer;
}

.thumbnail-item.active {
  border-color: #12a150;
}

.thumbnail-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  aspect-ratio: 1;
}

.info-section {
  display: flex;
  flex-direction: column;
}

.product-title {
  margin: 0;
  font-size: 30px;
  line-height: 1.35;
  font-weight: 800;
  color: #17231c;
}

.product-desc {
  margin: 14px 0 0;
  font-size: 14px;
  line-height: 1.8;
  color: #6b7280;
}

.price-box {
  margin-top: 24px;
  padding: 22px 24px;
  border-radius: 18px;
  background: linear-gradient(135deg, #f5fbf6 0%, #f8fcf9 100%);
  border: 1px solid #e4efe6;
}

.price-row {
  display: flex;
  align-items: flex-end;
  gap: 14px;
}

.price-label {
  font-size: 14px;
  color: #5f7064;
  margin-bottom: 8px;
}

.price-value-wrapper {
  color: #ef4444;
  font-weight: 800;
}

.currency {
  font-size: 20px;
}

.amount {
  font-size: 38px;
}

.price-hint {
  margin: 10px 0 0;
  font-size: 12px;
  color: #7b887f;
}

.sku-section {
  display: flex;
  flex-direction: column;
  gap: 24px;
  margin-top: 28px;
}

.sku-row {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.sku-label {
  font-size: 14px;
  font-weight: 600;
  color: #23342a;
}

.sku-options {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.sku-chip {
  position: relative;
  padding: 8px 16px;
  border-radius: 12px;
  border: 1px solid #dce8de;
  background: #ffffff;
  color: #374151;
  cursor: pointer;
  transition: all 0.2s ease;
}

.sku-chip:hover,
.sku-chip.active {
  border-color: #12a150;
  color: #0f8a4b;
  background: #f3fbf5;
}

.check-icon {
  position: absolute;
  top: -7px;
  right: -7px;
  padding: 2px;
  border-radius: 50%;
  background: #12a150;
  color: #ffffff;
  font-size: 10px;
}

.quantity-section {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: 28px;
}

.quantity-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stock-text {
  font-size: 12px;
  color: #8a958e;
}

.services-row {
  display: flex;
  flex-wrap: wrap;
  gap: 18px;
  margin-top: 28px;
  padding-top: 20px;
  border-top: 1px solid #edf3ee;
}

.service-item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #55675a;
}

.service-icon {
  color: #10b981;
}

.action-buttons {
  display: flex;
  gap: 16px;
  margin-top: auto;
  padding-top: 28px;
}

.btn {
  flex: 1;
  height: 50px;
  border: none;
  border-radius: 16px;
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, background-color 0.2s ease;
}

.btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.btn:not(:disabled):hover {
  transform: translateY(-1px);
}

.btn-secondary {
  border: 1px solid #12a150;
  background: #ffffff;
  color: #0f8a4b;
}

.btn-primary {
  background: linear-gradient(90deg, #0f8a4b 0%, #2ea661 100%);
  color: #ffffff;
  box-shadow: 0 14px 24px rgba(15, 138, 75, 0.18);
}

.content-layout {
  display: grid;
  grid-template-columns: 3fr 9fr;
  gap: 32px;
}

.detail-container {
  overflow: hidden;
  min-height: 560px;
  border-radius: 18px;
  border: 1px solid #eef3ef;
  background: #ffffff;
}

.sticky-tabs {
  display: flex;
  padding: 0 24px;
  border-bottom: 1px solid #e7efe8;
  background: #ffffff;
}

.tab-item {
  padding: 16px 24px;
  border-bottom: 2px solid transparent;
  color: #67776c;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}

.tab-item.active {
  border-bottom-color: #0f8a4b;
  color: #0f8a4b;
}

.tab-content {
  padding: 32px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 16px;
  font-size: 18px;
  font-weight: 700;
  color: #17231c;
}

.section-title .icon {
  color: #10b981;
}

.params-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
  margin-bottom: 36px;
}

.param-item {
  display: flex;
  gap: 10px;
  padding: 14px 16px;
  border-radius: 14px;
  background: #f8fbf8;
}

.param-label {
  color: #7a867f;
  min-width: 72px;
}

.param-value {
  color: #314439;
}

.no-params {
  color: #9ca3af;
  font-size: 14px;
}

.prose {
  color: #4b5d50;
  line-height: 1.9;
}

.image-stack {
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin-top: 22px;
}

.detail-img {
  width: 100%;
  display: block;
  border-radius: 14px;
}

@media (max-width: 1100px) {
  .product-hero,
  .content-layout {
    grid-template-columns: 1fr;
  }

  .sidebar {
    display: none;
  }
}

@media (max-width: 768px) {
  .product-hero {
    padding: 22px;
    gap: 28px;
  }

  .thumbnail-list {
    grid-template-columns: repeat(4, 1fr);
  }

  .action-buttons,
  .price-row,
  .quantity-section {
    flex-direction: column;
    align-items: flex-start;
  }

  .tab-content {
    padding: 22px;
  }
}
</style>
