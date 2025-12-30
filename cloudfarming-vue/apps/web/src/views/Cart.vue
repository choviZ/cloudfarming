<template>
  <div class="cart-container">
    <!-- 页面标题 -->
    <div class="cart-header">
      <h1 class="cart-title">购物车</h1>
      <span class="cart-count" v-if="cartData.cartItems?.length > 0">
        共 {{ cartData.cartItems.length }} 件商品
      </span>
    </div>

    <!-- 加载状态 -->
    <a-spin :spinning="loading" tip="加载中...">
      <!-- 空状态 -->
      <div v-if="!loading && cartData.cartItems?.length === 0" class="empty-cart">
        <a-empty
          description="购物车还是空的"
          :image="Empty.PRESENTED_IMAGE_SIMPLE"
        >
          <a-button type="primary" @click="goToShop">去逛逛</a-button>
        </a-empty>
      </div>

      <!-- 购物车内容 -->
      <div v-else class="cart-content">
        <!-- 顶部操作栏 -->
        <div class="cart-toolbar">
          <a-checkbox
            v-model:checked="selectAllFlag"
            :indeterminate="indeterminate"
            @change="handleSelectAll"
          >
            全选
          </a-checkbox>
          <a-button
            type="text"
            danger
            :disabled="!hasSelectedItems"
            @click="handleBatchDelete"
          >
            删除选中
          </a-button>
        </div>

        <!-- 商品列表 -->
        <div class="cart-items">
          <div
            v-for="item in cartData.cartItems"
            :key="item.productId"
            class="cart-item"
            :class="{ 'no-stock': !item.hasStock }"
          >
            <!-- 选择框 -->
            <div class="item-checkbox">
              <a-checkbox
                v-model:checked="item.selected"
                @change="handleItemSelect(item)"
              />
            </div>

            <!-- 商品图片 -->
            <div class="item-image">
              <img :src="item.productImage" :alt="item.productName" />
            </div>

            <!-- 商品信息 -->
            <div class="item-info">
              <h3 class="item-name">{{ item.productName }}</h3>
              <p class="item-shop">{{ item.shopName }}</p>
              <div class="item-price">
                <span class="price-symbol">¥</span>
                <span class="price-value">{{ item.price }}</span>
              </div>
            </div>

            <!-- 数量选择器 -->
            <div class="item-quantity">
              <a-button
                size="small"
                :disabled="item.quantity <= 1"
                @click="handleQuantityChange(item, -1)"
              >
                <template #icon>
                  <MinusOutlined />
                </template>
              </a-button>
              <span class="quantity-value">{{ item.quantity }}</span>
              <a-button
                size="small"
                @click="handleQuantityChange(item, 1)"
              >
                <template #icon>
                  <PlusOutlined />
                </template>
              </a-button>
            </div>

            <!-- 小计 -->
            <div class="item-subtotal">
              <span class="subtotal-symbol">¥</span>
              <span class="subtotal-value">{{ item.totalPrice }}</span>
            </div>

            <!-- 删除按钮 -->
            <div class="item-actions">
              <a-button
                type="text"
                danger
                size="small"
                @click="handleDelete(item)"
              >
                <template #icon>
                  <DeleteOutlined />
                </template>
                删除
              </a-button>
            </div>
          </div>
        </div>
      </div>
    </a-spin>

    <!-- 底部结算栏 -->
    <div class="cart-footer" v-if="cartData.cartItems?.length > 0">
      <div class="footer-content">
        <div class="footer-left">
          <span class="selected-count">
            已选 <strong>{{ selectedCount }}</strong> 件
          </span>
        </div>
        <div class="footer-right">
          <div class="total-price">
            <span class="total-label">合计：</span>
            <span class="total-symbol">¥</span>
            <span class="total-value">{{ cartData.totalAmount }}</span>
          </div>
          <a-button
            type="primary"
            size="large"
            :disabled="selectedCount === 0"
            @click="handleCheckout"
          >
            结算
          </a-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message, Modal, Empty } from 'ant-design-vue'
import type { CartItemRespDTO, CartRespDTO } from '@/types/cart'
import {
  DeleteOutlined,
  PlusOutlined,
  MinusOutlined
} from '@ant-design/icons-vue'
import {
  batchRemoveFromCart,
  getCart,
  removeFromCart,
  selectAllCartItems,
  selectCartItem,
  updateCartItem
} from '@/api/cart.ts'

const router = useRouter()

const loading = ref(false)
const cartData = reactive<CartRespDTO>({
  cartItems: [],
  totalQuantity: 0,
  totalAmount: '0',
  allHasStock: true
})

const selectAllFlag = ref(false)
const indeterminate = ref(false)

const selectedCount = computed(() => {
  return cartData.cartItems?.filter(item => item.selected).length
})

const hasSelectedItems = computed(() => {
  return selectedCount.value > 0
})

const fetchCartData = async () => {
  loading.value = true
  try {
    const response = await getCart()
    if (response.code === '0' && response.data) {
      const data = response.data

      cartData.cartItems = data.cartItems.map(item => ({
        ...item,
        price: String(item.price),
        totalPrice: String(item.totalPrice),
        shopName: item.shopName || '未知店铺'
      }))

      cartData.totalQuantity = data.totalQuantity
      cartData.totalAmount = String(data.totalAmount)
      cartData.allHasStock = data.allHasStock

      updateSelectAllState()
    } else {
      message.error('获取购物车失败：' + (response.message || '未知错误'))
    }
  } finally {
    loading.value = false
  }
}

const updateSelectAllState = () => {
  const selectedItems = cartData.cartItems.filter(item => item.selected)
  selectAllFlag.value = selectedItems.length === cartData.cartItems.length && cartData.cartItems.length > 0
  indeterminate.value = selectedItems.length > 0 && selectedItems.length < cartData.cartItems.length
}

const handleSelectAll = () => {
  const checked = selectAllFlag.value
  indeterminate.value = false
  cartData.cartItems.forEach(item => {
    item.selected = checked
  })
  updateTotalAmount()
  selectAllCartItems(checked)
}

const handleItemSelect = async (item: CartItemRespDTO) => {
  updateSelectAllState()
  updateTotalAmount()
  try {
    await selectCartItem(String(item.productId), item.selected)
  } catch (error) {
    message.error('更新选择状态失败')
  }
}

const handleQuantityChange = async (item: CartItemRespDTO, delta: number) => {
  const newQuantity = item.quantity + delta
  if (newQuantity < 1) return

  item.quantity = newQuantity
  item.totalPrice = (parseFloat(item.price) * newQuantity).toFixed(2)
  updateTotalAmount()

  try {
    await updateCartItem({
      skuId: item.productId.toString(),
      quantity: newQuantity,
      selected: item.selected
    })
  } catch (error) {
    message.error('更新数量失败')
    await fetchCartData()
  }
}

const handleDelete = (item: CartItemRespDTO) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除"${item.productName}"吗？`,
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        const response = await removeFromCart(String(item.productId))
        if (response.code === '0') {
          message.success('删除成功')
          await fetchCartData()
        } else {
          message.error('删除失败：' + (response.message || '未知错误'))
        }
      } catch (error) {
        message.error('删除失败')
      }
    }
  })
}

const handleBatchDelete = () => {
  const selectedItems = cartData.cartItems.filter(item => item.selected)
  if (selectedItems.length === 0) return

  Modal.confirm({
    title: '确认删除',
    content: `确定要删除选中的 ${selectedItems.length} 件商品吗？`,
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        const skuIds = selectedItems.map(item => String(item.productId))
        const response = await batchRemoveFromCart(skuIds)
        if (response.code === '0') {
          message.success('删除成功')
          await fetchCartData()
        } else {
          message.error('删除失败：' + (response.message || '未知错误'))
        }
      } catch (error) {
        message.error('删除失败')
      }
    }
  })
}

const updateTotalAmount = () => {
  const selectedItems = cartData.cartItems.filter(item => item.selected)
  cartData.totalAmount = selectedItems
    .reduce((total, item) => total + parseFloat(item.totalPrice), 0)
    .toFixed(2)
  cartData.totalQuantity = selectedItems.reduce((total, item) => total + item.quantity, 0)
}

const handleCheckout = () => {
  if (selectedCount.value === 0) {
    message.warning('请先选择要结算的商品')
    return
  }
  message.success('正在跳转到结算页面...')
}

const goToShop = () => {
  router.push('/')
}

onMounted(() => {
  fetchCartData()
})
</script>

<style scoped>
.cart-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 140px;
}

.cart-header {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px 20px 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.cart-title {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.cart-count {
  font-size: 14px;
  color: #666;
}

.cart-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.cart-toolbar {
  background-color: #fff;
  padding: 15px 20px;
  border-radius: 8px 8px 0 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #f0f0f0;
}

.cart-items {
  background-color: #fff;
  border-radius: 0 0 8px 8px;
  overflow: hidden;
}

.cart-item {
  display: flex;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.3s;
}

.cart-item:hover {
  background-color: #fafafa;
}

.cart-item.no-stock {
  opacity: 0.6;
}

.item-checkbox {
  flex: 0 0 50px;
  display: flex;
  justify-content: center;
}

.item-image {
  flex: 0 0 100px;
  height: 100px;
  margin-right: 20px;
  border-radius: 4px;
  overflow: hidden;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin: 0 0 8px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-shop {
  font-size: 13px;
  color: #999;
  margin: 0 0 12px 0;
}

.item-price {
  display: flex;
  align-items: baseline;
}

.price-symbol {
  font-size: 14px;
  color: #ff4d4f;
  font-weight: 500;
}

.price-value {
  font-size: 18px;
  color: #ff4d4f;
  font-weight: 600;
  margin-left: 2px;
}

.item-quantity {
  flex: 0 0 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.quantity-value {
  font-size: 14px;
  color: #333;
  min-width: 30px;
  text-align: center;
}

.item-subtotal {
  flex: 0 0 100px;
  text-align: right;
}

.subtotal-symbol {
  font-size: 14px;
  color: #ff4d4f;
  font-weight: 500;
}

.subtotal-value {
  font-size: 18px;
  color: #ff4d4f;
  font-weight: 600;
  margin-left: 2px;
}

.item-actions {
  flex: 0 0 80px;
  display: flex;
  justify-content: flex-end;
}

.cart-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: #fff;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.08);
  z-index: 1000;
  height: 80px;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 15px 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.footer-left {
  display: flex;
  align-items: center;
}

.selected-count {
  font-size: 14px;
  color: #666;
}

.selected-count strong {
  color: #ff4d4f;
  font-size: 18px;
  margin: 0 4px;
}

.footer-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.total-price {
  display: flex;
  align-items: baseline;
}

.total-label {
  font-size: 14px;
  color: #666;
}

.total-symbol {
  font-size: 16px;
  color: #ff4d4f;
  font-weight: 500;
}

.total-value {
  font-size: 24px;
  color: #ff4d4f;
  font-weight: 600;
  margin-left: 2px;
}

.empty-cart {
  max-width: 1200px;
  margin: 0 auto;
  padding: 100px 20px;
  background-color: #fff;
  border-radius: 8px;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .cart-header {
    padding: 15px 15px 10px;
  }

  .cart-title {
    font-size: 20px;
  }

  .cart-content {
    padding: 0 15px;
  }

  .cart-toolbar {
    padding: 12px 15px;
  }

  .cart-item {
    flex-wrap: wrap;
    padding: 15px;
  }

  .item-checkbox {
    flex: 0 0 30px;
  }

  .item-image {
    flex: 0 0 80px;
    height: 80px;
    margin-right: 12px;
  }

  .item-info {
    flex: 1;
    min-width: 0;
    margin-bottom: 10px;
  }

  .item-name {
    font-size: 14px;
  }

  .item-shop {
    font-size: 12px;
  }

  .item-price {
    display: none;
  }

  .item-quantity {
    flex: 0 0 100px;
  }

  .item-subtotal {
    flex: 0 0 80px;
  }

  .subtotal-value {
    font-size: 16px;
  }

  .item-actions {
    flex: 0 0 60px;
  }

  .footer-content {
    padding: 12px 15px;
  }

  .selected-count {
    font-size: 13px;
  }

  .selected-count strong {
    font-size: 16px;
  }

  .total-label {
    display: none;
  }

  .total-value {
    font-size: 20px;
  }

  .empty-cart {
    padding: 60px 15px;
  }
}

@media (max-width: 480px) {
  .cart-item {
    position: relative;
  }

  .item-checkbox {
    position: absolute;
    top: 15px;
    left: 15px;
    z-index: 1;
  }

  .item-image {
    flex: 0 0 70px;
    height: 70px;
    margin-left: 30px;
  }

  .item-info {
    flex: 1;
    margin-left: 10px;
  }

  .item-quantity {
    flex: 0 0 auto;
  }

  .item-subtotal {
    flex: 0 0 auto;
  }

  .item-actions {
    flex: 0 0 auto;
  }
}
</style>
