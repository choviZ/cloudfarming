<template>
  <div class="order-create-container">
    <div class="page-content">
      <h2 class="page-title">{{ isCartSource ? '确认购物车订单' : '确认订单' }}</h2>

      <div class="layout-grid">
        <div class="main-column">
          <div class="section-card address-section">
            <AddressSelector v-model="selectedAddressId" />
          </div>

          <div class="section-card item-section">
            <div class="section-header">
              <span class="title">{{ isCartSource ? '结算商品' : '商品信息' }}</span>
            </div>

            <a-spin :spinning="loading">
              <template v-if="isCartSource">
                <a-alert
                  v-if="cartPreview.invalidItems.length"
                  class="warning-alert"
                  type="warning"
                  show-icon
                  message="存在暂不可结算的商品"
                  description="请返回购物车取消选择、调整数量或删除后，再提交订单。"
                />

                <div v-if="cartPreview.groups.length" class="group-list">
                  <div
                    v-for="group in cartPreview.groups"
                    :key="group.shopId"
                    class="group-card"
                  >
                    <div class="group-header">
                      <span class="group-title">{{ group.shopName }}</span>
                      <span class="group-amount">小计 ¥{{ formatMoney(group.totalAmount) }}</span>
                    </div>

                    <div
                      v-for="item in group.items"
                      :key="item.skuId"
                      class="item-card"
                    >
                      <div class="item-image">
                        <img v-if="item.productImage" :src="item.productImage" :alt="item.productName" />
                        <div v-else class="image-placeholder">暂无图片</div>
                      </div>
                      <div class="item-info">
                        <h3 class="item-title">{{ item.productName }}</h3>
                        <div class="item-meta">
                          <span>数量 x {{ item.quantity }}</span>
                        </div>
                      </div>
                      <div class="item-summary">
                        <div class="price">¥{{ formatMoney(item.price) }}</div>
                        <div class="subtotal">小计 ¥{{ formatMoney(item.totalPrice) }}</div>
                      </div>
                    </div>
                  </div>
                </div>

                <div v-if="cartPreview.invalidItems.length" class="invalid-section">
                  <div class="invalid-title">暂不可结算商品</div>
                  <div
                    v-for="item in cartPreview.invalidItems"
                    :key="`invalid-${item.skuId}`"
                    class="item-card item-card--invalid"
                  >
                    <div class="item-image">
                      <img v-if="item.productImage" :src="item.productImage" :alt="item.productName" />
                      <div v-else class="image-placeholder">暂无图片</div>
                    </div>
                    <div class="item-info">
                      <h3 class="item-title">{{ item.productName }}</h3>
                      <div class="item-meta item-meta--warning">{{ item.invalidReason }}</div>
                    </div>
                    <div class="item-summary">
                      <div class="price">¥{{ formatMoney(item.price) }}</div>
                      <div class="subtotal">数量 x {{ item.quantity }}</div>
                    </div>
                  </div>
                </div>

                <a-empty
                  v-if="!cartPreview.groups.length && !cartPreview.invalidItems.length"
                  description="暂无可结算商品"
                />
              </template>

              <template v-else>
                <div v-if="displayItem" class="item-card">
                  <div class="item-image">
                    <img v-if="displayItem.image" :src="displayItem.image" :alt="displayItem.title" />
                    <div v-else class="image-placeholder">暂无图片</div>
                  </div>
                  <div class="item-info">
                    <h3 class="item-title">{{ displayItem.title }}</h3>
                    <div class="item-tags" v-if="displayItem.tags?.length">
                      <span v-for="tag in displayItem.tags" :key="tag" class="tag">
                        {{ tag }}
                      </span>
                    </div>
                    <div class="item-meta">
                      <span>数量 x {{ displayItem.quantity }}</span>
                    </div>
                  </div>
                  <div class="item-summary">
                    <div class="price">¥{{ formatMoney(displayItem.price) }}</div>
                    <div class="subtotal">小计 ¥{{ formatMoney(displayItemTotal) }}</div>
                  </div>
                </div>

                <a-empty v-else description="商品信息加载失败" />
              </template>
            </a-spin>
          </div>
        </div>

        <div class="side-column">
          <div class="checkout-card">
            <div class="price-detail">
              <div class="detail-row">
                <span>{{ isCartSource ? '商品件数' : '购买数量' }}</span>
                <span class="value">{{ isCartSource ? cartPreview.totalQuantity : buyNowQuantity }}</span>
              </div>
              <div class="detail-row">
                <span>商品金额</span>
                <span class="value">¥{{ formatMoney(summaryAmount) }}</span>
              </div>
              <div class="detail-row">
                <span>运费</span>
                <span class="value">¥0.00</span>
              </div>
              <div v-if="isCartSource && cartPreview.invalidItems.length" class="detail-row detail-row--warning">
                <span>失效商品</span>
                <span class="value">{{ cartPreview.invalidItems.length }} 种</span>
              </div>
              <div class="divider"></div>
              <div class="total-row">
                <span>合计</span>
                <span class="total-price">
                  <span class="currency">¥</span>
                  {{ formatMoney(summaryAmount) }}
                </span>
              </div>
            </div>

            <div class="action-area">
              <a-button
                type="primary"
                size="large"
                block
                class="submit-btn"
                :loading="submitting"
                :disabled="!canSubmitOrder"
                @click="handleSubmit"
              >
                提交订单
              </a-button>
              <div class="agreement-text">
                {{ isCartSource && cartPreview.invalidItems.length ? '请先处理失效商品后再提交订单' : '提交订单即表示同意《云农场用户协议》' }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import AddressSelector from '../../components/address/AddressSelector.vue'
import { getAdoptItemDetail } from '@/api/adopt'
import { batchRemoveFromCart, getCartCheckoutPreview } from '@/api/cart'
import { createOrder, ORDER_TYPE } from '@/api/order'
import { getSpuDetail } from '@/api/spu'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const submitting = ref(false)
const selectedAddressId = ref('')
const displayItem = ref(null)
const cartPreview = reactive({
  groups: [],
  invalidItems: [],
  totalQuantity: 0,
  totalAmount: 0,
  canSubmit: false,
  signature: ''
})

const isCartSource = computed(() => route.query.source === 'cart')
const orderType = computed(() => {
  if (isCartSource.value) {
    return ORDER_TYPE.GOODS
  }
  return route.query.type === 'product' ? ORDER_TYPE.GOODS : ORDER_TYPE.ADOPT
})
const quantity = computed(() => {
  const parsed = Number(route.query.quantity || 1)
  return Number.isFinite(parsed) && parsed > 0 ? parsed : 1
})
const displayItemTotal = computed(() => {
  if (!displayItem.value) {
    return 0
  }
  return Number(displayItem.value.price || 0) * Number(displayItem.value.quantity || 0)
})
const summaryAmount = computed(() => {
  return isCartSource.value ? Number(cartPreview.totalAmount || 0) : displayItemTotal.value
})
const buyNowQuantity = computed(() => {
  return displayItem.value ? displayItem.value.quantity : 0
})
const canSubmitOrder = computed(() => {
  if (isCartSource.value) {
    return cartPreview.canSubmit && cartPreview.groups.length > 0
  }
  return !!displayItem.value
})
const submitItems = computed(() => {
  if (isCartSource.value) {
    return cartPreview.groups.flatMap(group =>
      group.items.map(item => ({
        bizId: item.skuId,
        quantity: item.quantity
      }))
    )
  }

  if (!displayItem.value) {
    return []
  }

  return [{
    bizId: displayItem.value.bizId,
    quantity: displayItem.value.quantity
  }]
})

const formatMoney = (value) => {
  const amount = Number(value || 0)
  return amount.toFixed(2)
}

const resolveCover = (images) => {
  if (!images) {
    return ''
  }
  return String(images).split(',').map(item => item.trim()).find(Boolean) || ''
}

const parseSaleAttributeText = (saleAttribute) => {
  if (!saleAttribute) {
    return ''
  }
  try {
    const parsed = typeof saleAttribute === 'string' ? JSON.parse(saleAttribute) : saleAttribute
    return Object.values(parsed || {}).join(' ')
  } catch (error) {
    return ''
  }
}

const buildPreviewSignature = (data) => {
  return JSON.stringify({
    groups: (data.groups || []).map(group => ({
      shopId: group.shopId,
      totalAmount: formatMoney(group.totalAmount),
      items: (group.items || []).map(item => ({
        skuId: item.skuId,
        quantity: item.quantity,
        totalPrice: formatMoney(item.totalPrice)
      }))
    })),
    invalidItems: (data.invalidItems || []).map(item => ({
      skuId: item.skuId,
      quantity: item.quantity,
      invalidReason: item.invalidReason
    })),
    totalQuantity: Number(data.totalQuantity || 0),
    totalAmount: formatMoney(data.totalAmount)
  })
}

const applyPreview = (data, syncSignature = true) => {
  cartPreview.groups = data.groups || []
  cartPreview.invalidItems = data.invalidItems || []
  cartPreview.totalQuantity = data.totalQuantity || 0
  cartPreview.totalAmount = data.totalAmount || 0
  cartPreview.canSubmit = !!data.canSubmit
  if (syncSignature) {
    cartPreview.signature = buildPreviewSignature(data)
  }
  return data
}

const fetchCartPreview = async ({ syncSignature = true, redirectIfEmpty = false } = {}) => {
  const response = await getCartCheckoutPreview()
  if (response.code !== '0' || !response.data) {
    message.error(response.message || '获取结算预览失败')
    return null
  }

  const previewData = applyPreview(response.data, syncSignature)
  if (redirectIfEmpty && !previewData.groups.length && !previewData.invalidItems.length) {
    message.warning('请先选择要结算的商品')
    router.replace('/cart')
    return null
  }
  return previewData
}

const fetchBuyNowDetail = async () => {
  if (orderType.value === ORDER_TYPE.ADOPT) {
    const id = route.params.id || route.query.id
    if (!id) {
      throw new Error('缺少参数: id')
    }

    const response = await getAdoptItemDetail(id)
    if (response.code !== '0' || !response.data) {
      throw new Error(response.message || '获取项目详情失败')
    }

    const data = response.data
    displayItem.value = {
      bizId: data.id,
      title: data.title,
      image: data.coverImage,
      price: Number(data.price || 0),
      quantity: quantity.value,
      tags: [
        `认养周期 ${data.adoptDays} 天`,
        `预计收益 ${data.expectedYield || '以实际收获为准'}`
      ]
    }
    return
  }

  const spuId = route.query.spuId
  const skuId = route.query.skuId
  if (!spuId || !skuId) {
    throw new Error('缺少参数: spuId 或 skuId')
  }

  const response = await getSpuDetail(Number(spuId))
  if (response.code !== '0' || !response.data) {
    throw new Error(response.message || '获取商品详情失败')
  }

  const { productSpu, productSkus } = response.data
  const sku = (productSkus || []).find(item => String(item.id) === String(skuId))
  if (!sku) {
    throw new Error('未找到指定的商品规格')
  }

  const specText = parseSaleAttributeText(sku.saleAttribute)
  displayItem.value = {
    bizId: sku.id,
    title: productSpu.title,
    image: sku.skuImage || resolveCover(productSpu.images),
    price: Number(sku.price || 0),
    quantity: quantity.value,
    tags: specText ? [specText] : []
  }
}

const navigateToPay = (data) => {
  if (!data.payOrderNo) {
    message.error('订单创建异常：返回数据缺少订单号')
    return
  }
  router.push({
    path: '/pay',
    query: {
      payOrderNo: data.payOrderNo,
      amount: data.payAmount
    }
  })
}

const handleSubmit = async () => {
  if (!selectedAddressId.value) {
    message.warning('请选择收货地址')
    return
  }
  if (!submitItems.value.length) {
    message.warning('暂无可提交的商品')
    return
  }

  submitting.value = true
  try {
    if (isCartSource.value) {
      const latestPreview = await fetchCartPreview({ syncSignature: false, redirectIfEmpty: true })
      if (!latestPreview) {
        return
      }

      const latestSignature = buildPreviewSignature(latestPreview)
      if (!latestPreview.canSubmit || latestSignature !== cartPreview.signature) {
        applyPreview(latestPreview, true)
        message.warning('购物车商品信息已变更，请确认后再提交')
        return
      }
    }

    const response = await createOrder({
      orderType: orderType.value,
      receiveId: selectedAddressId.value,
      items: submitItems.value
    })

    if (response.code !== '0' || !response.data) {
      message.error(response.message || '创建订单失败')
      return
    }

    if (isCartSource.value) {
      try {
        const clearResponse = await batchRemoveFromCart(submitItems.value.map(item => item.bizId))
        if (clearResponse.code !== '0') {
          message.warning('订单已创建，购物车未完全同步，请返回购物车刷新')
        }
      } catch (error) {
        message.warning('订单已创建，购物车未完全同步，请返回购物车刷新')
      }
    }

    message.success('订单创建成功，即将跳转支付')
    navigateToPay(response.data)
  } catch (error) {
    message.error(error.message || '系统繁忙，请稍后重试')
  } finally {
    submitting.value = false
  }
}

const initPage = async () => {
  loading.value = true
  try {
    if (isCartSource.value) {
      await fetchCartPreview({ syncSignature: true, redirectIfEmpty: true })
    } else {
      await fetchBuyNowDetail()
    }
  } catch (error) {
    message.error(error.message || '页面加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  initPage()
})
</script>

<style scoped>
.order-create-container {
  width: 100%;
  min-height: 100vh;
  padding: 24px 16px 40px;
  background: linear-gradient(180deg, #f6f8f6 0%, #f2f4f6 100%);
  display: flex;
  justify-content: center;
}

.page-content {
  width: 100%;
  max-width: 1200px;
}

.page-title {
  margin-bottom: 20px;
  color: #143226;
  font-size: 24px;
  font-weight: 700;
}

.layout-grid {
  display: flex;
  align-items: flex-start;
  gap: 20px;
}

.main-column {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.side-column {
  width: 340px;
  flex-shrink: 0;
  position: sticky;
  top: 20px;
}

.section-card,
.checkout-card {
  background: rgba(255, 255, 255, 0.94);
  border: 1px solid rgba(24, 55, 40, 0.08);
  border-radius: 20px;
  box-shadow: 0 16px 36px rgba(18, 42, 30, 0.08);
}

.section-card {
  padding: 20px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
  padding-bottom: 12px;
  border-bottom: 1px solid #edf1ee;
}

.section-header .title {
  color: #173524;
  font-size: 16px;
  font-weight: 700;
}

.warning-alert {
  margin-bottom: 16px;
}

.group-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.group-card {
  border: 1px solid #eef2ef;
  border-radius: 18px;
  overflow: hidden;
}

.group-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 18px;
  background: #f8fbf9;
  color: #173524;
}

.group-title {
  font-weight: 700;
}

.group-amount {
  color: #2c7a4b;
  font-size: 13px;
}

.invalid-section {
  margin-top: 18px;
  padding-top: 18px;
  border-top: 1px solid #edf1ee;
}

.invalid-title {
  margin-bottom: 12px;
  color: #ad6800;
  font-weight: 700;
}

.item-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px;
  border-top: 1px solid #f3f5f4;
}

.item-card:first-child {
  border-top: none;
}

.item-card--invalid {
  border: 1px dashed rgba(250, 173, 20, 0.45);
  border-radius: 16px;
  background: rgba(250, 173, 20, 0.06);
}

.item-image {
  width: 92px;
  height: 92px;
  border-radius: 16px;
  overflow: hidden;
  background: #f4f6f5;
  flex-shrink: 0;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #90a194;
  font-size: 13px;
}

.item-info {
  min-width: 0;
  flex: 1;
}

.item-title {
  margin: 0;
  color: #173524;
  font-size: 16px;
  font-weight: 600;
  line-height: 1.5;
}

.item-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
}

.tag {
  padding: 4px 10px;
  border-radius: 999px;
  background: #eef6f0;
  color: #2c7a4b;
  font-size: 12px;
}

.item-meta {
  margin-top: 10px;
  color: #728779;
  font-size: 13px;
}

.item-meta--warning {
  color: #ad6800;
}

.item-summary {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
  color: #173524;
  flex-shrink: 0;
}

.price {
  color: #d6483d;
  font-size: 18px;
  font-weight: 700;
}

.subtotal {
  color: #728779;
  font-size: 13px;
}

.checkout-card {
  padding: 24px;
}

.price-detail {
  margin-bottom: 24px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 12px;
  color: #667b6d;
  font-size: 14px;
}

.detail-row .value {
  color: #173524;
  font-weight: 600;
}

.detail-row--warning .value {
  color: #ad6800;
}

.divider {
  height: 1px;
  margin: 16px 0;
  background: #edf1ee;
}

.total-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 16px;
}

.total-row span:first-child {
  color: #173524;
  font-size: 16px;
  font-weight: 600;
}

.total-price {
  color: #d6483d;
  font-size: 28px;
  font-weight: 700;
  line-height: 1;
}

.currency {
  margin-right: 2px;
  font-size: 15px;
}

.submit-btn {
  height: 46px;
  background: #1f8f56;
  border-color: #1f8f56;
  font-weight: 600;
}

.submit-btn:hover {
  background: #187447;
  border-color: #187447;
}

.agreement-text {
  margin-top: 12px;
  color: #90a194;
  font-size: 12px;
  line-height: 1.6;
  text-align: center;
}

@media (max-width: 900px) {
  .layout-grid {
    flex-direction: column;
  }

  .side-column {
    width: 100%;
    position: static;
  }
}

@media (max-width: 640px) {
  .order-create-container {
    padding-left: 12px;
    padding-right: 12px;
  }

  .item-card {
    align-items: flex-start;
    flex-wrap: wrap;
  }

  .item-summary {
    width: 100%;
    align-items: flex-start;
  }
}
</style>
