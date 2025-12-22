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
            <h1 class="product-title">{{ productInfo.name || '商品名称加载中...' }}</h1>
            <!-- 商品基本信息 -->
            <div class="product-basic-info">
              <div class="info-item">
                <span class="info-label">分类：</span>
                <span class="info-value">{{ productInfo.productCategory || '未分类' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">产地：</span>
                <span class="info-value">{{ productInfo.originPlace || '未知' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">规格：</span>
                <span class="info-value">{{ productInfo.specification || '标准规格' }}</span>
              </div>
            </div>
            <!-- 商品描述 -->
            <p class="product-description">{{ productInfo.description || '商品描述加载中...' }}</p>
            <!-- 商品价格 -->
            <div class="product-price">
              <span class="price-symbol">¥</span>
              <span class="price-value">{{ productInfo.price }}</span>
            </div>
            <!-- 购买数量选择器 -->
            <div class="quantity-selector">
              <span class="quantity-label">数量</span>
              <a-input-number
                v-model:value="quantity"
                :min="1"
                @change="handleQuantityChange"
              />
              <span class="stock-info">{{ productInfo.stock > 0 ? '有货' : '售无' }}</span>
            </div>
          </div>
        </div>
      </div>
    </a-spin>

    <!-- 固定悬浮操作按钮区域 -->
    <div class="fixed-action-buttons">
      <a-button @click="addToCart" class="action-btn cart-btn">
        <template #icon>
          <ShoppingCartOutlined />
        </template>
        加入购物车
      </a-button>
      <a-button type="primary" @click="buyNow" class="action-btn buy-btn">
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
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { getProductDetail } from '@/api/product'
import {
  ShopOutlined,
  ShoppingCartOutlined,
  ShoppingOutlined,
  StarOutlined
} from '@ant-design/icons-vue'

// 获取路由参数
const route = useRoute()
const productId = route.params.id as string

// 商品图片数据
const productImages = ref<string[]>([])

// 当前选中的图片索引
const currentImageIndex = ref(0)

// 商品信息
const productInfo = reactive({
  name: '',
  productCategory: '',
  originPlace: '',
  specification: '',
  description: '',
  price: '0',
  stock: 0
})

// 购买数量
const quantity = ref(1)

// 是否已收藏
const isFavorited = ref(false)

// 加载状态
const loading = ref(false)

// 根据商品ID获取商品详情
const fetchProductDetail = async (id: string) => {
  loading.value = true
  try {
    // 调用API获取商品详情
    const response = await getProductDetail(id)
    if (response.code === '0' && response.data) {
      const productData = response.data
      // 更新商品信息
      productInfo.name = productData.productName || '';
      productInfo.description = productData.description || '';
      productInfo.price = productData.price || '0';
      productInfo.stock = productData.stock || 0;
      productInfo.productCategory = productData.productCategory || '';
      productInfo.originPlace = productData.originPlace || '';
      productInfo.specification = productData.specification || '';
      
      // 更新商品图片，如果有多个图片则分割，否则使用单张图片
      if (productData.productImg) {
        const images = productData.productImg.split(',').filter(img => img.trim())
        productImages.value = images
      } else {
        // 如果没有图片，设置为空数组
        productImages.value = []
      }
    } else {
      message.error('获取商品详情失败：' + (response.message || '未知错误'))
    }
  } catch (error) {
    console.error('获取商品详情失败:', error)
  } finally {
    loading.value = false
  }
}

// 组件挂载时获取商品详情
onMounted(() => {
  if (productId) {
    fetchProductDetail(productId)
  }
})

// 选择图片
const selectImage = (index: number) => {
  currentImageIndex.value = index
}

// 处理数量变化
const handleQuantityChange = (value: number | null) => {
  if (value === null) {
    quantity.value = 1
  } else if (value < 1) {
    quantity.value = 1
    message.warning('购买数量不能小于1')
  } else if (value > productInfo.stock) {
    quantity.value = productInfo.stock
    message.warning(`购买数量不能超过库存数量${productInfo.stock}`)
  }
}

// 加入购物车
const addToCart = () => {
  message.success('已成功加入购物车')
}

// 立即购买
const buyNow = () => {
  message.success('正在跳转到结算页面...')
}

// 切换收藏状态
const toggleFavorite = () => {
  isFavorited.value = !isFavorited.value
  message.success(isFavorited.value ? '已收藏' : '已取消收藏')
}
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
  flex: 0 0 60%;
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

.thumbnail-item:hover {
  border-color: #1890ff;
}

.thumbnail-item.active {
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
}

.main-image img {
  max-width: 500px;
  max-height: 500px;
  width: 100%;
  height: 100%;
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
  flex: 0 0 40%;
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
  flex-wrap: wrap;
  gap: 20px;
  margin: 15px 0;
  padding: 15px 0;
  border-top: 1px solid #f0f0f0;
  border-bottom: 1px solid #f0f0f0;
}

.info-item {
  display: flex;
  align-items: center;
  min-width: 150px;
}

.info-label {
  font-size: 14px;
  color: #666;
  font-weight: 500;
  margin-right: 8px;
}

.info-value {
  font-size: 14px;
  color: #333;
}

.product-description {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin: 0;
}

.product-price {
  display: flex;
  align-items: baseline;
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

.quantity-selector {
  display: flex;
  align-items: center;
  gap: 15px;
}

.quantity-label {
  font-size: 14px;
  color: #333;
}

.stock-info {
  font-size: 14px;
  color: #999;
}

/* 固定悬浮操作按钮区域样式 */
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

.cart-btn:hover {
  background-color: #fff2f0;
  border-color: #ff4d4f;
}

.buy-btn {
  background-color: #ff4d4f;
  border-color: #ff4d4f;
}

.buy-btn:hover {
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

/* 响应式设计 */
@media (max-width: 992px) {
  .main-content {
    flex-direction: column;
  }
  
  .left-section,
  .right-section {
    flex: 1;
    width: 100%;
  }
  
  .fixed-action-buttons {
    position: static;
    margin-top: 20px;
    justify-content: center;
  }
  
  .product-basic-info {
    flex-direction: column;
    gap: 10px;
  }
  
  .info-item {
    min-width: auto;
  }
}
</style>