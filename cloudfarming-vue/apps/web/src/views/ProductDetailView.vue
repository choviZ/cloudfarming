<template>
  <div class="product-detail-page">
    <div class="container">
      <!-- Breadcrumbs -->
      <a-breadcrumb class="breadcrumb">
        <a-breadcrumb-item><a href="/">首页</a></a-breadcrumb-item>
        <a-breadcrumb-item><a href="/product/list">生鲜市集</a></a-breadcrumb-item>
        <a-breadcrumb-item>商品详情</a-breadcrumb-item>
      </a-breadcrumb>
      <!-- Loading State -->
      <a-spin :spinning="loading" tip="加载中...">
        <!-- Hero Section -->
        <div class="product-hero">
          <!-- Left: Gallery -->
          <div class="gallery-section">
            <div class="main-image-wrapper">
              <img 
                v-if="productImages.length > 0"
                :src="productImages[currentImageIndex]" 
                alt="Product" 
                class="main-image"
              >
              <div v-else class="no-image">
                <ShopOutlined class="no-image-icon" />
                <span>暂无图片</span>
              </div>
              <div class="tag-badge">产地直采</div>
            </div>
            
            <div class="thumbnail-list" v-if="productImages.length > 1">
              <div 
                v-for="(img, index) in productImages" 
                :key="index"
                class="thumbnail-item"
                :class="{ active: currentImageIndex === index }"
                @mouseenter="currentImageIndex = index"
              >
                <img :src="img" alt="thumbnail">
              </div>
            </div>
            
            <div class="social-share">
              <span class="share-item" @click="toggleFavorite">
                <StarFilled v-if="isFavorited" class="icon active" />
                <StarOutlined v-else class="icon" />
                收藏商品
              </span>
              <span class="share-item">
                <ShareAltOutlined class="icon" />
                分享
              </span>
            </div>
          </div>

          <!-- Right: Info -->
          <div class="info-section">
            <h1 class="product-title">{{ spuInfo.title || '商品名称加载中...' }}</h1>
            <p class="product-desc">{{ spuInfo.description || '暂无描述' }}</p>

            <!-- Price Box -->
            <div class="price-box">
              <div class="price-row">
                <span class="price-label">价格</span>
                <div class="price-value-wrapper">
                  <span class="currency">¥</span>
                  <span class="amount">{{ displayPrice }}</span>
                </div>
              </div>
            </div>

            <!-- SKU Selection -->
            <div class="sku-section" v-if="specKeys.length > 0">
              <div v-for="key in specKeys" :key="key" class="sku-row">
                <span class="sku-label">{{ key }}</span>
                <div class="sku-options">
                  <div 
                    v-for="val in specs[key]" 
                    :key="val"
                    class="sku-chip"
                    :class="{ active: selectedSpecs[key] === val }"
                    @click="selectSpec(key, val)"
                  >
                    {{ val }}
                    <CheckOutlined v-if="selectedSpecs[key] === val" class="check-icon" />
                  </div>
                </div>
              </div>
            </div>

            <!-- Quantity -->
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

            <!-- Services -->
            <div class="services-row">
              <div class="service-item">
                <CarOutlined class="service-icon" />
                <span>顺丰包邮</span>
              </div>
              <div class="service-item">
                <SafetyCertificateOutlined class="service-icon" />
                <span>坏果包赔</span>
              </div>
              <div class="service-item">
                <ClockCircleOutlined class="service-icon" />
                <span>48h发货</span>
              </div>
            </div>

            <!-- Actions -->
            <div class="action-buttons">
              <button class="btn btn-secondary" @click="addToCart" :disabled="!canBuy">加入购物车</button>
              <button class="btn btn-primary" @click="buyNow" :disabled="!canBuy">立即购买</button>
            </div>
          </div>
        </div>

        <!-- Content Layout -->
        <div class="content-layout">
          <!-- Sidebar -->
          <aside class="sidebar">
            <div class="shop-card">
              <div class="shop-header">
                <div class="shop-avatar">
                  <ShopOutlined />
                </div>
                <div class="shop-meta">
                  <div class="shop-name">云端果园直营店</div>
                  <div class="shop-rating">
                    <StarFilled class="star" /> 4.9 | 2.3万粉丝
                  </div>
                </div>
              </div>
              <div class="shop-stats">
                <div class="stat-item">
                  <div class="label">商品</div>
                  <div class="value">128</div>
                </div>
                <div class="stat-item">
                  <div class="label">销量</div>
                  <div class="value">5万+</div>
                </div>
                <div class="stat-item">
                  <div class="label">评价</div>
                  <div class="value">9.8</div>
                </div>
              </div>
              <div class="shop-actions">
                <button class="btn-small btn-outline">进店逛逛</button>
                <button class="btn-small btn-filled">关注店铺</button>
              </div>
            </div>
          </aside>

          <!-- Main Detail -->
          <div class="detail-container">
            <div class="sticky-tabs">
              <div 
                class="tab-item" 
                :class="{ active: activeTab === 'detail' }"
                @click="activeTab = 'detail'"
              >
                商品详情
              </div>
              <div 
                class="tab-item" 
                :class="{ active: activeTab === 'reviews' }"
                @click="activeTab = 'reviews'"
              >
                累计评价 (TODO)
              </div>
            </div>

            <div class="tab-content">
              <!-- Detail Tab -->
              <div v-if="activeTab === 'detail'">
                <div class="params-table">
                  <h3 class="section-title">
                    <BarsOutlined class="icon" /> 产品参数
                  </h3>
                  <div class="params-grid">
                    <div class="param-item" v-for="(value, key) in spuInfo.baseAttrs" :key="key">
                      <span class="param-label">{{ key }}</span>
                      <span class="param-value">{{ value }}</span>
                    </div>
                    <!-- Fallback if no attrs -->
                    <div v-if="!spuInfo.baseAttrs || Object.keys(spuInfo.baseAttrs).length === 0" class="no-params">
                      暂无详细参数
                    </div>
                  </div>
                </div>

                <div class="detail-images">
                   <!-- Since we don't have rich text HTML description, we show images if they exist in description or just placeholder -->
                   <!-- Assuming spuInfo.description might be text. If backend returns HTML, use v-html. For now, text. -->
                   <!-- Usually product details are images. If we only have 'images' field (main images), we might not have detail images. -->
                   <!-- Let's just use the main images again as detail images if no dedicated field, or just show text -->
                   <div class="prose">
                      <p v-if="spuInfo.description">{{ spuInfo.description }}</p>
                      <div class="image-stack" v-if="productImages.length > 0">
                        <img v-for="(img, idx) in productImages" :key="idx" :src="img" class="detail-img">
                      </div>
                   </div>
                </div>
              </div>

              <!-- Reviews Tab (TODO) -->
              <div v-else class="reviews-placeholder">
                <a-empty description="评论功能开发中..." />
              </div>
            </div>
          </div>
        </div>
      </a-spin>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { addToCart as addToCartApi } from '@/api/cart'
import { getSpuDetail } from '@cloudfarming/core/api/spu'
import type { SpuDetailRespDTO } from '@cloudfarming/core/api/spu'
import type { SkuRespDTO } from '@cloudfarming/core/api/sku'
import {
  ShopOutlined,
  ShoppingCartOutlined,
  ShoppingOutlined,
  StarOutlined,
  StarFilled,
  ShareAltOutlined,
  CheckOutlined,
  CarOutlined,
  SafetyCertificateOutlined,
  ClockCircleOutlined,
  BarsOutlined
} from '@ant-design/icons-vue'

// Route & Data
const route = useRoute()
const spuId = route.params.id as string

const loading = ref(false)
const spuInfo = ref<Partial<SpuDetailRespDTO>>({})
const skuList = ref<SkuRespDTO[]>([])
const productImages = ref<string[]>([])
const currentImageIndex = ref(0)
const quantity = ref(1)
const isFavorited = ref(false)
const activeTab = ref('detail')

// Specs Logic
const specs = reactive<Record<string, string[]>>({})
const specKeys = computed(() => Object.keys(specs))
const selectedSpecs = reactive<Record<string, string>>({})

const currentSku = computed(() => {
  if (specKeys.value.length === 0) {
    if (skuList.value.length === 1) return skuList.value[0];
    return null;
  }
  for (const key of specKeys.value) {
    if (!selectedSpecs[key]) return null;
  }
  return skuList.value.find(sku => {
    for (const key of specKeys.value) {
      if (sku.saleAttrs[key] !== selectedSpecs[key]) return false;
    }
    return true;
  }) || null
})

const displayPrice = computed(() => {
  if (currentSku.value) return currentSku.value.price;
  if (skuList.value.length > 0) {
    const prices = skuList.value.map(s => s.price).sort((a, b) => a - b);
    const min = prices[0];
    const max = prices[prices.length - 1];
    return min === max ? min : `${min} ~ ${max}`;
  }
  return spuInfo.value.price || '待定';
})

const canBuy = computed(() => !!currentSku.value && currentSku.value.stock > 0)

// Methods
const fetchProductDetail = async (id: string) => {
  loading.value = true
  try {
    const response = await getSpuDetail(Number(id))
    if (response.code == '0' && response.data) {
      const data = response.data
      console.log(data)
      spuInfo.value = data
      skuList.value = data.skuList || []
      if (data.images) {
        // 处理逗号分隔的图像并修剪空白
        productImages.value = data.images.split(',').map(i => i.trim()).filter(Boolean)
        if (productImages.value.length === 0 && (data as any).images) {
           productImages.value = (data as any).images.split(',').map((i: string) => i.trim()).filter(Boolean)
        }
      }
      extractSpecs(data.skuList || [])
    } else {
      message.error('获取商品详情失败')
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const extractSpecs = (skus: SkuRespDTO[]) => {
  for (const key in specs) delete specs[key];
  if (!skus?.length) return;
  const temp: Record<string, Set<string>> = {};
  skus.forEach(sku => {
    if (sku.saleAttrs) {
      Object.entries(sku.saleAttrs).forEach(([key, val]) => {
        if (!temp[key]) temp[key] = new Set();
        temp[key].add(val);
      });
    }
  });
  Object.entries(temp).forEach(([k, v]) => specs[k] = Array.from(v));
}

const selectSpec = (key: string, val: string) => {
  if (selectedSpecs[key] === val) delete selectedSpecs[key];
  else selectedSpecs[key] = val;
}

const addToCart = async () => {
  if (!currentSku.value) {
    message.warning('请先选择规格');
    return;
  }
  const res = await addToCartApi({
      skuId: String(currentSku.value.id),
      quantity: quantity.value,
      selected: false
  })
  if (res.code === '0') message.success('已成功加入购物车')
  else message.error('加入失败: ' + res.message)
}

const buyNow = () => {
  if (!currentSku.value) {
    message.warning('请先选择规格');
    return;
  }
  message.success('跳转结算页...')
}

const toggleFavorite = () => {
  isFavorited.value = !isFavorited.value
  message.success(isFavorited.value ? '已收藏' : '已取消收藏')
}

onMounted(() => {
  if (spuId) fetchProductDetail(spuId)
})
</script>

<style scoped>
/* Reset & Utilities */
.product-detail-page {
  min-height: 100vh;
  padding-bottom: 40px;
  color: #1f2937; /* gray-900 */
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

/* Hero Section */
.product-hero {
  background: white;
  border-radius: 16px;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px -1px rgba(0, 0, 0, 0.1);
  padding: 40px;
  display: grid;
  grid-template-columns: 5fr 7fr;
  gap: 48px;
  margin-bottom: 40px;
}

@media (max-width: 1024px) {
  .product-hero {
    grid-template-columns: 1fr;
    padding: 24px;
  }
}

/* Left: Gallery */
.gallery-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.main-image-wrapper {
  aspect-ratio: 1;
  background-color: #f9fafb;
  border-radius: 12px;
  overflow: hidden;
  position: relative;
  border: 1px solid #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: zoom-in;
}

.main-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.main-image-wrapper:hover .main-image {
  transform: scale(1.05);
}

.no-image {
  color: #9ca3af;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}
.no-image-icon { font-size: 32px; }

.tag-badge {
  position: absolute;
  top: 16px;
  left: 16px;
  background-color: #10b981; /* brand-500 */
  color: white;
  font-size: 12px;
  font-weight: 700;
  padding: 4px 12px;
  border-radius: 9999px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  z-index: 10;
}

.thumbnail-list {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
}

.thumbnail-item {
  aspect-ratio: 1;
  border-radius: 8px;
  border: 2px solid transparent;
  overflow: hidden;
  cursor: pointer;
  background-color: #f9fafb;
  padding: 2px;
}

.thumbnail-item.active {
  border-color: #10b981;
}

.thumbnail-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 6px;
}

.social-share {
  display: flex;
  gap: 24px;
  font-size: 14px;
  color: #6b7280;
  margin-top: 8px;
}

.share-item {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: color 0.2s;
}
.share-item:hover { color: #059669; }
.share-item .icon { font-size: 16px; }
.share-item .icon.active { color: #f59e0b; }

/* Right: Info */
.info-section {
  display: flex;
  flex-direction: column;
}

.product-title {
  font-size: 28px;
  font-weight: 700;
  color: #111827;
  line-height: 1.3;
  margin-bottom: 12px;
}

.product-desc {
  color: #6b7280;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 24px;
}

.price-box {
  background-color: #f9fafb;
  border: 1px solid #f3f4f6;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 32px;
}

.price-row {
  display: flex;
  align-items: flex-end;
  gap: 12px;
}

.price-label {
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 6px;
}

.price-value-wrapper {
  color: #ef4444; /* price red */
  font-weight: 700;
  line-height: 1;
}

.currency { font-size: 20px; margin-right: 2px; }
.amount { font-size: 36px; }

/* SKU */
.sku-section {
  margin-bottom: 32px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.sku-row {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.sku-label {
  font-size: 14px;
  font-weight: 500;
  color: #111827;
}

.sku-options {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.sku-chip {
  padding: 8px 16px;
  border-radius: 8px;
  border: 2px solid #e5e7eb;
  background: white;
  color: #374151;
  font-size: 14px;
  cursor: pointer;
  position: relative;
  transition: all 0.2s;
}

.sku-chip:hover {
  border-color: #10b981;
  color: #059669;
}

.sku-chip.active {
  border-color: #10b981;
  background-color: #ecfdf5; /* brand-50 */
  color: #047857;
  font-weight: 500;
}

.check-icon {
  font-size: 10px;
  position: absolute;
  top: -6px;
  right: -6px;
  background: #10b981;
  color: white;
  border-radius: 50%;
  padding: 2px;
}

/* Quantity */
.quantity-section {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 32px;
}

.quantity-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stock-text {
  font-size: 12px;
  color: #9ca3af;
}

/* Services */
.services-row {
  display: grid;
  grid-template-columns: repeat(4, auto);
  gap: 16px;
  padding-top: 24px;
  border-top: 1px solid #f3f4f6;
  margin-bottom: 32px;
  justify-content: start;
}

.service-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #4b5563;
}

.service-icon { color: #10b981; }

/* Actions */
.action-buttons {
  display: flex;
  gap: 16px;
  margin-top: auto;
}

.btn {
  flex: 1;
  height: 48px;
  border-radius: 12px;
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-secondary {
  background: white;
  border: 2px solid #059669;
  color: #059669;
}
.btn-secondary:hover:not(:disabled) { background: #ecfdf5; }

.btn-primary {
  background: #059669;
  color: white;
  box-shadow: 0 4px 6px -1px rgba(5, 150, 105, 0.4);
}
.btn-primary:hover:not(:disabled) { background: #047857; }

/* Content Layout */
.content-layout {
  display: grid;
  grid-template-columns: 3fr 9fr;
  gap: 32px;
}

@media (max-width: 1024px) {
  .content-layout {
    grid-template-columns: 1fr;
  }
  .sidebar { display: none; }
}

/* Sidebar */
.shop-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #f3f4f6;
}

.shop-header {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.shop-avatar {
  width: 48px;
  height: 48px;
  background: #f3f4f6;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: #6b7280;
}

.shop-meta {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.shop-name {
  font-weight: 700;
  color: #111827;
  font-size: 14px;
}

.shop-rating {
  font-size: 12px;
  color: #6b7280;
  margin-top: 4px;
}
.star { color: #facc15; }

.shop-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  text-align: center;
  margin-bottom: 16px;
}

.shop-stats .label { font-size: 12px; color: #9ca3af; }
.shop-stats .value { font-size: 14px; font-weight: 700; color: #111827; }

.shop-actions {
  display: flex;
  gap: 8px;
}

.btn-small {
  flex: 1;
  padding: 8px 0;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  border: none;
}

.btn-outline {
  background: white;
  border: 1px solid #e5e7eb;
  color: #374151;
}
.btn-outline:hover { background: #f9fafb; }

.btn-filled {
  background: #059669;
  color: white;
}
.btn-filled:hover { background: #047857; }

/* Main Detail Content */
.detail-container {
  background: white;
  border-radius: 12px;
  border: 1px solid #f3f4f6;
  overflow: hidden;
  min-height: 600px;
}

.sticky-tabs {
  position: sticky;
  top: 0;
  background: white;
  z-index: 10;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  padding: 0 24px;
}

.tab-item {
  padding: 16px 24px;
  font-size: 14px;
  font-weight: 500;
  color: #6b7280;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
}

.tab-item:hover { color: #059669; }

.tab-item.active {
  color: #059669;
  border-bottom-color: #059669;
}

.tab-content {
  padding: 32px;
}

.section-title {
  font-size: 18px;
  font-weight: 700;
  color: #111827;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.section-title .icon { color: #10b981; }

.params-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 16px;
  margin-bottom: 40px;
}

.param-item {
  display: flex;
  font-size: 14px;
}

.param-label {
  color: #9ca3af;
  width: 80px;
  flex-shrink: 0;
}

.param-value {
  color: #374151;
}

.no-params {
  color: #9ca3af;
  font-size: 14px;
  grid-column: 1 / -1;
}

.detail-images .prose {
  color: #4b5563;
  line-height: 1.8;
}

.image-stack {
  display: flex;
  flex-direction: column;
  gap: 0; /* Seamless vertical stack usually */
  margin-top: 24px;
}

.detail-img {
  width: 100%;
  display: block;
  border-radius: 8px;
  margin-bottom: 16px;
}

.reviews-placeholder {
  padding: 40px;
  text-align: center;
}
</style>