<template>
  <div class="product-detail-container">
    <!-- 加载状态 -->
    <a-spin :spinning="loading" tip="加载中...">
      <!-- 主体内容区域 -->
      <div class="main-content">
        <!-- 左侧商品图片展示区域 -->
        <div class="left-section">
          <!-- 店铺信息 -->
          <div class="shop-info">
            <a-avatar :size="32" src="" class="shop-logo">
              <template #icon>
                <ShopOutlined />
              </template>
            </a-avatar>
            <span class="shop-name">优选农场直供店</span>
          </div>
          <!-- 图片展示区 -->
          <div class="image-display">
            <!-- 缩略图列表 -->
            <div class="thumbnail-list" v-if="productImages.length > 1">
              <div
                v-for="(image, index) in productImages"
                :key="index"
                class="thumbnail-item"
                :class="{ active: currentImageIndex === index }"
                @click="selectImage(index)"
              >
                <img :src="image" :alt="`商品图片${index + 1}`" />
              </div>
            </div>
            <!-- 大图展示 -->
            <div class="main-image">
              <img
                v-if="productImages.length > 0"
                :src="productImages[currentImageIndex]"
                alt="商品大图"
              />
              <div v-else class="no-image-placeholder">
                <span>暂无商品图片</span>
              </div>
            </div>
          </div>
        </div>
        <!-- 右侧商品信息展示区域 -->
        <div class="right-section">
          <div class="product-info">
            <!-- 商品名称 -->
            <h1 class="product-title">{{ spuInfo.title || '商品名称加载中...' }}</h1>
            
            <!-- 商品基本信息 -->
            <div class="product-basic-info">
              <!-- 基础属性 -->
              <div class="info-item" v-for="(value, key) in spuInfo.baseAttrs" :key="key">
                <span class="info-label">{{ key }}：</span>
                <span class="info-value">{{ value }}</span>
              </div>
            </div>
            
            <!-- 商品描述 -->
            <!-- <p class="product-description">{{ spuInfo.description || '暂无描述' }}</p> -->

            <!-- 商品价格 -->
            <div class="product-price">
              <span class="price-symbol">¥</span>
              <span class="price-value">{{ displayPrice }}</span>
            </div>

            <!-- SKU 规格选择 -->
            <div class="sku-selector" v-if="specKeys.length > 0">
              <div v-for="key in specKeys" :key="key" class="spec-row">
                <div class="spec-label">{{ key }}</div>
                <div class="spec-values">
                  <a-tag
                    v-for="val in specs[key]" 
                    :key="val"
                    :color="selectedSpecs[key] === val ? 'blue' : 'default'"
                    class="spec-tag"
                    @click="selectSpec(key, val)"
                  >
                    {{ val }}
                  </a-tag>
                </div>
              </div>
            </div>

            <!-- 购买数量选择器 -->
            <div class="quantity-selector">
              <span class="quantity-label">数量</span>
              <a-input-number
                v-model:value="quantity"
                :min="1"
                :max="currentSku ? currentSku.stock : 9999"
                @change="handleQuantityChange"
                :disabled="!currentSku && specKeys.length > 0"
              />
              <span class="stock-info">
                {{ currentSku ? `库存: ${currentSku.stock}` : (specKeys.length > 0 ? '请选择规格' : '暂无库存') }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </a-spin>

    <!-- 固定悬浮操作按钮区域 -->
    <div class="fixed-action-buttons">
      <a-button @click="addToCart" class="action-btn cart-btn" :disabled="!canBuy">
        <template #icon>
          <ShoppingCartOutlined />
        </template>
        加入购物车
      </a-button>
      <a-button type="primary" @click="buyNow" class="action-btn buy-btn" :disabled="!canBuy">
        <template #icon>
          <ShoppingOutlined />
        </template>
        立即购买
      </a-button>
      <a-button
        @click="toggleFavorite"
        class="action-btn favorite-btn"
        :type="isFavorited ? 'primary' : 'default'"
      >
        <template #icon>
          <StarOutlined />
        </template>
        {{ isFavorited ? '已收藏' : '收藏' }}
      </a-button>
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
  StarOutlined
} from '@ant-design/icons-vue'

// 获取路由参数
const route = useRoute()
const spuId = route.params.id as string

// 数据状态
const loading = ref(false)
const spuInfo = ref<Partial<SpuDetailRespDTO>>({})
const skuList = ref<SkuRespDTO[]>([])
const productImages = ref<string[]>([])
const currentImageIndex = ref(0)
const quantity = ref(1)
const isFavorited = ref(false)

// 规格选择状态
// specs: { '颜色': ['红色', '蓝色'], '尺寸': ['L', 'M'] }
const specs = reactive<Record<string, string[]>>({})
const specKeys = computed(() => Object.keys(specs))
// selectedSpecs: { '颜色': '红色' }
const selectedSpecs = reactive<Record<string, string>>({})

// 计算当前选中的 SKU
const currentSku = computed(() => {
  if (specKeys.value.length === 0) {
    // 如果没有规格，且有唯一的 SKU，则直接返回
    if (skuList.value.length === 1) return skuList.value[0];
    return null;
  }
  
  // 检查是否所有规格都已选择
  for (const key of specKeys.value) {
    if (!selectedSpecs[key]) return null;
  }

  // 查找匹配的 SKU
  return skuList.value.find(sku => {
    for (const key of specKeys.value) {
      if (sku.saleAttrs[key] !== selectedSpecs[key]) return false;
    }
    return true;
  }) || null
})

// 计算展示价格
const displayPrice = computed(() => {
  if (currentSku.value) {
    return currentSku.value.price;
  }
  // 计算价格区间
  if (skuList.value.length > 0) {
    const prices = skuList.value.map(s => s.price).sort((a, b) => a - b);
    const min = prices[0];
    const max = prices[prices.length - 1];
    if (min === max) return min;
    return `${min} ~ ${max}`;
  }
  return spuInfo.value.price || '待定';
})

// 是否可购买
const canBuy = computed(() => {
  return !!currentSku.value && currentSku.value.stock > 0;
})

// 获取商品详情
const fetchProductDetail = async (id: string) => {
  loading.value = true
  try {
    const response = await getSpuDetail(Number(id))
    if (response.code === '0' && response.data) {
      const data = response.data
      spuInfo.value = data
      skuList.value = data.skuList || []
      
      // 处理图片
      if (data.image) {
        productImages.value = data.image.split(',').filter(img => img.trim())
      }

      // 提取规格信息
      extractSpecs(data.skuList || [])
    } else {
      message.error('获取商品详情失败：' + (response.message || '未知错误'))
    }
  } catch (error) {
    console.error('获取商品详情失败:', error)
  } finally {
    loading.value = false
  }
}

// 提取规格
const extractSpecs = (skus: SkuRespDTO[]) => {
  // 清空现有规格
  for (const key in specs) delete specs[key];
  
  if (!skus || skus.length === 0) return;

  const tempSpecs: Record<string, Set<string>> = {};
  
  skus.forEach(sku => {
    if (sku.saleAttrs) {
      Object.entries(sku.saleAttrs).forEach(([key, val]) => {
        if (!tempSpecs[key]) tempSpecs[key] = new Set();
        tempSpecs[key].add(val);
      });
    }
  });

  Object.entries(tempSpecs).forEach(([key, valSet]) => {
    specs[key] = Array.from(valSet);
  });
}

// 选择规格
const selectSpec = (key: string, val: string) => {
  if (selectedSpecs[key] === val) {
    // 取消选择
    delete selectedSpecs[key];
  } else {
    // 选择
    selectedSpecs[key] = val;
  }
}

// 图片选择
const selectImage = (index: number) => {
  currentImageIndex.value = index
}

// 数量变化
const handleQuantityChange = (value: number | null) => {
  if (!value) quantity.value = 1;
}

// 加入购物车
const addToCart = async () => {
  if (!currentSku.value) {
    message.warning('请先选择规格');
    return;
  }
  
  const res = await addToCartApi({
      skuId: String(currentSku.value.id),
      quantity: quantity.value,
      selected: false
    }
  )
  if (res.code === '0') {
    message.success('已成功加入购物车')
  } else {
    message.error('加入购物车失败，' + res.message)
  }
}

// 立即购买
const buyNow = () => {
  message.success('正在跳转到结算页面...')
}

// 收藏
const toggleFavorite = () => {
  isFavorited.value = !isFavorited.value
  message.success(isFavorited.value ? '已收藏' : '已取消收藏')
}

onMounted(() => {
  if (spuId) {
    fetchProductDetail(spuId)
  }
})
</script>

<style scoped>
.product-detail-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 20px 0;
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  gap: 30px;
  padding: 0 20px;
}

/* 左侧区域样式 */
.left-section {
  flex: 0 0 50%;
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.shop-info {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.shop-logo {
  margin-right: 10px;
}

.shop-name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.image-display {
  display: flex;
  gap: 15px;
}

.thumbnail-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.thumbnail-item {
  width: 80px;
  height: 80px;
  border: 2px solid transparent;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
}

.thumbnail-item:hover, .thumbnail-item.active {
  border-color: #1890ff;
}

.thumbnail-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.main-image {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f8f8f8;
  border-radius: 8px;
  overflow: hidden;
  height: 500px;
}

.main-image img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.no-image-placeholder {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 300px;
  background-color: #f5f5f5;
  border-radius: 8px;
  color: #999;
  font-size: 16px;
}

/* 右侧区域样式 */
.right-section {
  flex: 0 0 50%;
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.product-info {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.product-title {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin: 0;
  line-height: 1.4;
}

.product-basic-info {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 15px 0;
  border-top: 1px solid #f0f0f0;
  border-bottom: 1px solid #f0f0f0;
}

.info-item {
  display: flex;
  align-items: center;
}

.info-label {
  font-size: 14px;
  color: #666;
  font-weight: 500;
  min-width: 60px;
}

.info-value {
  font-size: 14px;
  color: #333;
}

.product-price {
  display: flex;
  align-items: baseline;
  margin-top: 10px;
}

.price-symbol {
  font-size: 18px;
  color: #ff4d4f;
  font-weight: 500;
}

.price-value {
  font-size: 28px;
  color: #ff4d4f;
  font-weight: 600;
  margin-left: 2px;
}

/* SKU Selector */
.sku-selector {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.spec-row {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.spec-label {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.spec-values {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.spec-tag {
  cursor: pointer;
  padding: 4px 12px;
  font-size: 14px;
}

.quantity-selector {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-top: 10px;
}

.quantity-label {
  font-size: 14px;
  color: #333;
}

.stock-info {
  font-size: 14px;
  color: #999;
}

/* Fixed Action Buttons */
.fixed-action-buttons {
  position: fixed;
  bottom: 32px;
  right: 30px;
  display: flex;
  gap: 10px;
  z-index: 1000;
}

.action-btn {
  height: 40px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.cart-btn {
  background-color: #fff;
  border-color: #ff7875;
  color: #ff4d4f;
}

.cart-btn:hover:not(:disabled) {
  background-color: #fff2f0;
  border-color: #ff4d4f;
}

.buy-btn {
  background-color: #ff4d4f;
  border-color: #ff4d4f;
}

.buy-btn:hover:not(:disabled) {
  background-color: #ff7875;
  border-color: #ff7875;
}

.favorite-btn {
  background-color: #fff;
  border-color: #d9d9d9;
  color: #666;
}

.favorite-btn:hover {
  border-color: #1890ff;
  color: #1890ff;
}

.favorite-btn.ant-btn-primary {
  background-color: #1890ff;
  border-color: #1890ff;
  color: #fff;
}

/* Responsive */
@media (max-width: 992px) {
  .main-content {
    flex-direction: column;
  }
  
  .left-section, .right-section {
    flex: 1 1 100%;
  }
}
</style>