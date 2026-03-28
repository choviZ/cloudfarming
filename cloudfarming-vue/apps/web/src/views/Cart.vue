<template>
  <div class="cart-container">
    <div class="cart-shell">
      <div class="cart-header">
        <div>
          <h1 class="cart-title">购物车</h1>
          <p class="cart-subtitle" v-if="cartItems.length">共 {{ cartItems.length }} 种商品</p>
        </div>
        <a-button v-if="cartItems.length" danger ghost @click="handleClearCart">
          清空购物车
        </a-button>
      </div>

      <a-alert
        v-if="cartData.hasInvalidItems"
        class="cart-alert"
        type="warning"
        show-icon
        message="购物车中有失效商品"
        description="失效商品不会参与结算，你可以取消选择、调整数量或直接删除。"
      />

      <a-spin :spinning="loading">
        <div v-if="!cartItems.length" class="empty-cart">
          <a-empty description="购物车还是空的">
            <a-button type="primary" @click="goShopping">去逛逛</a-button>
          </a-empty>
        </div>

        <div v-else class="cart-panel">
          <div class="cart-toolbar">
            <a-checkbox
              :checked="selectAllFlag"
              :indeterminate="indeterminate"
              @change="handleSelectAll"
            >
              全选可结算商品
            </a-checkbox>
            <div class="toolbar-actions">
              <a-button
                type="text"
                danger
                :disabled="!selectedCartItems.length"
                @click="handleBatchDelete"
              >
                删除选中
              </a-button>
            </div>
          </div>

          <div class="cart-list">
            <div
              v-for="item in cartItems"
              :key="item.skuId"
              class="cart-item"
              :class="{ 'cart-item--invalid': !item.canCheckout }"
            >
              <div class="item-check">
                <a-checkbox
                  :checked="item.selected"
                  @change="(event) => handleItemSelect(item, event.target.checked)"
                />
              </div>

              <div class="item-image" @click="goToProduct(item)">
                <img v-if="item.productImage" :src="item.productImage" :alt="item.productName" />
                <div v-else class="item-image__placeholder">暂无图片</div>
              </div>

              <div class="item-main">
                <div class="item-title-row">
                  <a class="item-title" @click.prevent="goToProduct(item)">{{ item.productName }}</a>
                  <a-tag v-if="!item.canCheckout" color="warning">{{ item.invalidReason }}</a-tag>
                </div>
                <div class="item-shop">{{ item.shopName }}</div>
                <div class="item-price">单价：¥{{ formatMoney(item.price) }}</div>
              </div>

              <div class="item-quantity">
                <a-button
                  size="small"
                  :disabled="item.quantity <= 1 || !canAdjustQuantity(item)"
                  @click="handleQuantityChange(item, -1)"
                >
                  -
                </a-button>
                <span class="quantity-value">{{ item.quantity }}</span>
                <a-button
                  size="small"
                  :disabled="!canAdjustQuantity(item)"
                  @click="handleQuantityChange(item, 1)"
                >
                  +
                </a-button>
              </div>

              <div class="item-subtotal">
                ¥{{ formatMoney(item.totalPrice) }}
              </div>

              <div class="item-actions">
                <a-button type="link" danger @click="handleDelete(item)">删除</a-button>
              </div>
            </div>
          </div>
        </div>
      </a-spin>
    </div>

    <div v-if="cartItems.length" class="cart-footer">
      <div class="footer-left">
        <div class="footer-meta">
          <span>已选 {{ selectedItemCount }} 种商品</span>
          <span>共 {{ selectedQuantity }} 件</span>
        </div>
        <span v-if="invalidSelectedCount" class="footer-warning">
          有 {{ invalidSelectedCount }} 种失效商品仍处于选中状态
        </span>
      </div>
      <div class="footer-right">
        <div class="footer-amount">
          <span class="amount-label">合计</span>
          <span class="amount-value">¥{{ formatMoney(cartData.totalAmount) }}</span>
        </div>
        <a-button type="primary" size="large" :disabled="!canCheckout" @click="handleCheckout">
          去结算
        </a-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Modal, message } from 'ant-design-vue'
import {
  batchRemoveFromCart,
  clearCart,
  getCart,
  removeFromCart,
  selectAllCartItems,
  selectCartItem,
  updateCartItem
} from '@/api/cart.js'

const router = useRouter()

const loading = ref(false)
const cartData = reactive({
  cartItems: [],
  totalQuantity: 0,
  totalAmount: 0,
  hasInvalidItems: false
})

const cartItems = computed(() => cartData.cartItems || [])
const validItems = computed(() => cartItems.value.filter(item => item.canCheckout))
const selectedCartItems = computed(() => cartItems.value.filter(item => item.selected))
const selectedValidItems = computed(() => selectedCartItems.value.filter(item => item.canCheckout))
const invalidSelectedCount = computed(() => selectedCartItems.value.filter(item => !item.canCheckout).length)
const selectedItemCount = computed(() => selectedValidItems.value.length)
const selectedQuantity = computed(() => selectedValidItems.value.reduce((total, item) => total + item.quantity, 0))
const selectAllFlag = computed(() => validItems.value.length > 0 && validItems.value.every(item => item.selected))
const indeterminate = computed(() => {
  const selectedCount = validItems.value.filter(item => item.selected).length
  return selectedCount > 0 && selectedCount < validItems.value.length
})
const canCheckout = computed(() => selectedValidItems.value.length > 0 && invalidSelectedCount.value === 0)

const formatMoney = (value) => {
  const amount = Number(value || 0)
  return amount.toFixed(2)
}

const fetchCartData = async () => {
  loading.value = true
  try {
    const response = await getCart()
    if (response.code === '0' && response.data) {
      cartData.cartItems = response.data.cartItems || []
      cartData.totalQuantity = response.data.totalQuantity || 0
      cartData.totalAmount = response.data.totalAmount || 0
      cartData.hasInvalidItems = !!response.data.hasInvalidItems
    } else {
      message.error(response.message || '获取购物车失败')
    }
  } catch (error) {
    message.error('获取购物车失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const canAdjustQuantity = (item) => {
  return item.canCheckout || item.invalidReason === '库存不足'
}

const handleSelectAll = async (event) => {
  try {
    await selectAllCartItems(event.target.checked)
    await fetchCartData()
  } catch (error) {
    message.error('更新全选状态失败')
  }
}

const handleItemSelect = async (item, checked) => {
  try {
    await selectCartItem(item.skuId, checked)
    await fetchCartData()
  } catch (error) {
    message.error('更新选中状态失败')
  }
}

const handleQuantityChange = async (item, delta) => {
  const nextQuantity = item.quantity + delta
  if (nextQuantity < 1) {
    return
  }
  try {
    const response = await updateCartItem(item.skuId, { quantity: nextQuantity })
    if (response.code === '0') {
      await fetchCartData()
      return
    }
    message.error(response.message || '更新数量失败')
  } catch (error) {
    message.error('更新数量失败')
  }
}

const handleDelete = (item) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除“${item.productName}”吗？`,
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        const response = await removeFromCart(item.skuId)
        if (response.code === '0') {
          message.success('删除成功')
          await fetchCartData()
          return
        }
        message.error(response.message || '删除失败')
      } catch (error) {
        message.error('删除失败')
      }
    }
  })
}

const handleBatchDelete = () => {
  if (!selectedCartItems.value.length) {
    return
  }
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除选中的 ${selectedCartItems.value.length} 种商品吗？`,
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        const response = await batchRemoveFromCart(selectedCartItems.value.map(item => item.skuId))
        if (response.code === '0') {
          message.success('删除成功')
          await fetchCartData()
          return
        }
        message.error(response.message || '删除失败')
      } catch (error) {
        message.error('删除失败')
      }
    }
  })
}

const handleClearCart = () => {
  Modal.confirm({
    title: '确认清空',
    content: '确定要清空整个购物车吗？',
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        const response = await clearCart()
        if (response.code === '0') {
          message.success('购物车已清空')
          await fetchCartData()
          return
        }
        message.error(response.message || '清空购物车失败')
      } catch (error) {
        message.error('清空购物车失败')
      }
    }
  })
}

const handleCheckout = () => {
  if (!selectedValidItems.value.length) {
    message.warning('请先选择要结算的商品')
    return
  }
  if (invalidSelectedCount.value > 0) {
    message.warning('请先处理已选中的失效商品')
    return
  }
  router.push({
    path: '/order/create',
    query: {
      source: 'cart'
    }
  })
}

const goToProduct = (item) => {
  if (!item.spuId) {
    return
  }
  router.push(`/product/${item.spuId}`)
}

const goShopping = () => {
  router.push('/product/list')
}

onMounted(() => {
  fetchCartData()
})
</script>

<style scoped>
.cart-container {
  min-height: 100vh;
  padding: 24px 20px 132px;
  background: linear-gradient(180deg, #f7faf7 0%, #f3f5f7 100%);
}

.cart-shell {
  max-width: 1200px;
  margin: 0 auto;
}

.cart-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
}

.cart-title {
  margin: 0;
  font-size: 30px;
  line-height: 1.2;
  color: #143226;
}

.cart-subtitle {
  margin: 8px 0 0;
  color: #688173;
  font-size: 14px;
}

.cart-alert {
  margin-bottom: 16px;
}

.cart-panel {
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(24, 55, 40, 0.08);
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 18px 40px rgba(18, 42, 30, 0.08);
}

.cart-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 18px 24px;
  border-bottom: 1px solid #eef2ef;
}

.cart-list {
  display: flex;
  flex-direction: column;
}

.cart-item {
  display: grid;
  grid-template-columns: 44px 112px minmax(0, 1fr) 140px 120px 76px;
  gap: 20px;
  align-items: center;
  padding: 24px;
  border-bottom: 1px solid #f2f4f3;
  transition: background-color 0.2s ease;
}

.cart-item:last-child {
  border-bottom: none;
}

.cart-item:hover {
  background: rgba(22, 163, 74, 0.03);
}

.cart-item--invalid {
  background: rgba(250, 173, 20, 0.05);
}

.item-check {
  display: flex;
  justify-content: center;
}

.item-image {
  width: 112px;
  height: 112px;
  border-radius: 18px;
  overflow: hidden;
  background: #f5f7f6;
  cursor: pointer;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-image__placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #92a395;
  font-size: 13px;
}

.item-main {
  min-width: 0;
}

.item-title-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.item-title {
  color: #173524;
  font-size: 17px;
  font-weight: 600;
  line-height: 1.5;
}

.item-title:hover {
  color: #2c7a4b;
}

.item-shop {
  margin-top: 10px;
  color: #6e8377;
  font-size: 13px;
}

.item-price {
  margin-top: 8px;
  color: #d6483d;
  font-size: 15px;
  font-weight: 600;
}

.item-quantity {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.quantity-value {
  min-width: 24px;
  text-align: center;
  color: #173524;
  font-weight: 600;
}

.item-subtotal {
  color: #173524;
  font-size: 18px;
  font-weight: 700;
  text-align: right;
}

.item-actions {
  display: flex;
  justify-content: flex-end;
}

.empty-cart {
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(24, 55, 40, 0.08);
  border-radius: 24px;
  padding: 72px 24px;
}

.cart-footer {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 30;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 32px;
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(12px);
  border-top: 1px solid rgba(24, 55, 40, 0.08);
  box-shadow: 0 -12px 30px rgba(18, 42, 30, 0.08);
}

.footer-left {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.footer-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  color: #314d3c;
  font-size: 14px;
}

.footer-warning {
  color: #ad6800;
  font-size: 13px;
}

.footer-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.footer-amount {
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.amount-label {
  color: #6e8377;
  font-size: 14px;
}

.amount-value {
  color: #d6483d;
  font-size: 28px;
  font-weight: 700;
}

@media (max-width: 960px) {
  .cart-item {
    grid-template-columns: 36px 88px minmax(0, 1fr);
    gap: 16px;
  }

  .item-quantity,
  .item-subtotal,
  .item-actions {
    grid-column: 2 / 4;
    justify-content: flex-start;
  }

  .item-image {
    width: 88px;
    height: 88px;
  }

  .cart-footer {
    flex-direction: column;
    align-items: stretch;
  }

  .footer-right {
    justify-content: space-between;
  }
}

@media (max-width: 640px) {
  .cart-container {
    padding: 18px 12px 156px;
  }

  .cart-header,
  .cart-toolbar,
  .cart-item {
    padding-left: 16px;
    padding-right: 16px;
  }

  .cart-title {
    font-size: 24px;
  }

  .item-title-row {
    flex-direction: column;
    gap: 8px;
  }
}
</style>
