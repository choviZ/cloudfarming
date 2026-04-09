<template>
  <div class="order-review-page">
    <div class="review-header">
      <div>
        <h2 class="page-title">商品评价</h2>
        <p class="page-subtitle">
          订单号 {{ orderNo || '--' }}
          <span class="subtitle-divider">|</span>
          剩余 {{ pendingCount }} 件待评价商品
        </p>
      </div>
      <div class="header-actions">
        <a-tag color="green" class="header-tag">{{ pendingCount > 0 ? '待评价' : '已全部评价' }}</a-tag>
        <a-button @click="goBack">返回订单</a-button>
      </div>
    </div>

    <div v-if="!items.length && !loading" class="empty-block">
      <a-empty description="当前订单暂无可评价商品" />
    </div>

    <a-spin :spinning="loading">
      <div class="review-list">
        <article
          v-for="item in items"
          :key="item.id"
          class="review-item-card"
        >
          <div class="item-summary">
            <img :src="item.skuImage || itemImageFallback" :alt="item.skuName || '商品图片'" class="item-image">
            <div class="item-main">
              <div class="item-top">
                <h3 class="item-title">{{ item.skuName || '订单商品' }}</h3>
                <a-tag :color="item.reviewed ? 'green' : 'orange'">
                  {{ item.reviewed ? '已评价' : '待评价' }}
                </a-tag>
              </div>
              <p v-if="formatSpecs(item.skuSpecs)" class="item-specs">{{ formatSpecs(item.skuSpecs) }}</p>
              <div class="item-meta">
                <span>数量 x{{ item.quantity || 1 }}</span>
                <span>实付 ¥{{ formatMoney(item.totalAmount || item.price) }}</span>
              </div>
            </div>
          </div>

          <div v-if="item.reviewed" class="review-result">
            <div class="review-result-header">
              <a-rate :value="item.reviewScore || 0" disabled />
              <span class="review-time">{{ formatDateTime(item.reviewCreateTime) }}</span>
            </div>
            <p v-if="item.reviewContent" class="review-result-content">{{ item.reviewContent }}</p>
            <a-image-preview-group v-if="item.reviewImageUrls?.length">
              <div class="review-result-images">
                <a-image
                  v-for="(image, index) in item.reviewImageUrls"
                  :key="`${item.id}-${index}`"
                  :src="image"
                />
              </div>
            </a-image-preview-group>
          </div>

          <div v-else class="review-editor">
            <div class="editor-row">
              <label class="editor-label">评分</label>
              <a-rate v-model:value="reviewForms[item.id].score" />
            </div>

            <div class="editor-row editor-row--textarea">
              <label class="editor-label">评价内容</label>
              <a-textarea
                v-model:value="reviewForms[item.id].content"
                :maxlength="300"
                show-count
                :rows="4"
                placeholder="从口感、包装、发货时效等方面分享你的真实体验"
              />
            </div>

            <div class="editor-row editor-row--upload">
              <label class="editor-label">评价晒图</label>
              <div class="upload-wrap">
                <multi-image-upload
                  v-model:value="reviewForms[item.id].imageUrls"
                  biz-code="PRODUCT_REVIEW"
                  :max-size="5"
                  :max-count="9"
                />
                <p class="upload-hint">支持上传最多 9 张图片，单张不超过 5MB</p>
              </div>
            </div>

            <div class="editor-actions">
              <a-button
                type="primary"
                class="submit-button"
                :loading="Boolean(submittingMap[item.id])"
                @click="submitReview(item)"
              >
                提交评价
              </a-button>
            </div>
          </div>
        </article>
      </div>
    </a-spin>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'
import MultiImageUpload from '@/components/Upload/MultiImageUpload.vue'
import { createOrderReview, getSkuOrderDetail } from '@/api/order'

const route = useRoute()
const router = useRouter()

const orderNo = computed(() => {
  const value = route.params.orderNo
  return Array.isArray(value) ? value[0] : value
})

const loading = ref(false)
const items = ref([])
const reviewForms = reactive({})
const submittingMap = reactive({})

const itemImageFallback = `data:image/svg+xml;charset=UTF-8,${encodeURIComponent(`
<svg xmlns="http://www.w3.org/2000/svg" width="120" height="120" viewBox="0 0 120 120">
  <rect width="120" height="120" rx="18" fill="#eff8f1" />
  <rect x="24" y="24" width="72" height="72" rx="14" fill="#ffffff" />
  <rect x="36" y="42" width="48" height="10" rx="5" fill="#11995a" opacity="0.8" />
  <rect x="36" y="60" width="36" height="8" rx="4" fill="#a9cfb4" />
</svg>
`)}` 

const pendingCount = computed(() => items.value.filter((item) => !item.reviewed).length)

const ensureForms = () => {
  const activeIds = new Set(items.value.map((item) => String(item.id)))
  Object.keys(reviewForms).forEach((key) => {
    if (!activeIds.has(key)) {
      delete reviewForms[key]
    }
  })
  items.value.forEach((item) => {
    if (item.reviewed) {
      return
    }
    if (!reviewForms[item.id]) {
      reviewForms[item.id] = {
        score: 5,
        content: '',
        imageUrls: []
      }
    }
  })
}

const fetchOrderDetails = async () => {
  if (!orderNo.value) {
    items.value = []
    return
  }
  loading.value = true
  try {
    const response = await getSkuOrderDetail(orderNo.value)
    if (response.code === '0' && Array.isArray(response.data)) {
      items.value = response.data.map((item) => ({
        ...item,
        reviewed: Boolean(item.reviewed),
        reviewImageUrls: Array.isArray(item.reviewImageUrls) ? item.reviewImageUrls : []
      }))
      ensureForms()
      return
    }
    items.value = []
    message.error(response.message || '获取订单商品失败')
  } catch (error) {
    console.error(error)
    message.error('获取订单商品失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const submitReview = async (item) => {
  const form = reviewForms[item.id]
  if (!form) {
    return
  }
  if (!form.score) {
    message.warning('请先选择评分')
    return
  }
  const content = (form.content || '').trim()
  const imageUrls = Array.isArray(form.imageUrls) ? form.imageUrls.filter(Boolean) : []
  if (!content && imageUrls.length === 0) {
    message.warning('请填写评价内容或上传图片')
    return
  }
  submittingMap[item.id] = true
  try {
    const response = await createOrderReview({
      orderDetailSkuId: item.id,
      score: form.score,
      content,
      imageUrls
    })
    if (response.code !== '0') {
      message.error(response.message || '提交评价失败')
      return
    }
    message.success('评价已提交')
    await fetchOrderDetails()
  } catch (error) {
    console.error(error)
    message.error('提交评价失败，请稍后重试')
  } finally {
    submittingMap[item.id] = false
  }
}

const goBack = () => {
  router.push({
    name: 'userOrders',
    query: {
      tab: pendingCount.value > 0 ? 'pendingReview' : 'all'
    }
  })
}

const formatMoney = (value) => {
  const amount = Number(value)
  if (Number.isNaN(amount)) {
    return '0.00'
  }
  return amount.toFixed(2)
}

const formatDateTime = (value) => {
  if (!value) {
    return ''
  }
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return String(value)
  }
  const month = `${date.getMonth() + 1}`.padStart(2, '0')
  const day = `${date.getDate()}`.padStart(2, '0')
  const hours = `${date.getHours()}`.padStart(2, '0')
  const minutes = `${date.getMinutes()}`.padStart(2, '0')
  return `${date.getFullYear()}-${month}-${day} ${hours}:${minutes}`
}

const formatSpecs = (rawValue) => {
  if (!rawValue) {
    return ''
  }
  try {
    const parsed = JSON.parse(rawValue)
    if (!parsed || typeof parsed !== 'object') {
      return String(rawValue)
    }
    return Object.entries(parsed)
      .map(([key, value]) => `${key}: ${value}`)
      .join(' / ')
  } catch (error) {
    return String(rawValue)
  }
}

fetchOrderDetails()
</script>

<style scoped>
.order-review-page {
  min-height: 100%;
  padding: 4px 0 8px;
}

.review-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 24px;
}

.page-title {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: #203328;
}

.page-subtitle {
  margin: 10px 0 0;
  font-size: 14px;
  color: #708176;
}

.subtitle-divider {
  margin: 0 10px;
  color: #c0cdc4;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-tag {
  padding: 4px 10px;
  border-radius: 999px;
}

.empty-block {
  padding: 80px 0 20px;
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.review-item-card {
  padding: 24px;
  border-radius: 20px;
  border: 1px solid #e4ece6;
  background: linear-gradient(180deg, #ffffff 0%, #fbfdfb 100%);
}

.item-summary {
  display: flex;
  gap: 18px;
}

.item-image {
  width: 108px;
  height: 108px;
  object-fit: cover;
  border-radius: 18px;
  border: 1px solid #e2ebe4;
  background: #f4faf5;
}

.item-main {
  flex: 1;
  min-width: 0;
}

.item-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.item-title {
  margin: 0;
  font-size: 18px;
  line-height: 1.5;
  font-weight: 700;
  color: #22362a;
}

.item-specs {
  margin: 10px 0 0;
  font-size: 13px;
  color: #6a7c70;
}

.item-meta {
  display: flex;
  gap: 20px;
  margin-top: 14px;
  font-size: 13px;
  color: #5c7063;
}

.review-editor,
.review-result {
  margin-top: 22px;
  padding-top: 22px;
  border-top: 1px solid #edf3ee;
}

.editor-row {
  display: grid;
  grid-template-columns: 84px minmax(0, 1fr);
  gap: 14px;
  align-items: flex-start;
  margin-bottom: 18px;
}

.editor-label {
  padding-top: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #31493a;
}

.upload-wrap {
  min-width: 0;
}

.upload-hint {
  margin: 10px 0 0;
  font-size: 12px;
  color: #819086;
}

.editor-actions {
  display: flex;
  justify-content: flex-end;
}

.submit-button {
  min-width: 120px;
  height: 40px;
  border-radius: 999px;
  background: linear-gradient(90deg, #0f8a4b 0%, #2ca661 100%);
  border: none;
  box-shadow: 0 10px 18px rgba(15, 138, 75, 0.18);
}

.review-result-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.review-time {
  font-size: 12px;
  color: #7b877f;
}

.review-result-content {
  margin: 14px 0 0;
  font-size: 14px;
  line-height: 1.8;
  color: #31473a;
  white-space: pre-wrap;
}

.review-result-images {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(88px, 88px));
  gap: 12px;
  margin-top: 16px;
}

.review-result-images :deep(.ant-image) {
  width: 88px;
  height: 88px;
  overflow: hidden;
  border-radius: 14px;
  border: 1px solid #e2ebe4;
}

.review-result-images :deep(img) {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

@media (max-width: 900px) {
  .review-header,
  .item-summary,
  .item-top {
    flex-direction: column;
  }

  .header-actions {
    width: 100%;
    justify-content: space-between;
  }

  .editor-row {
    grid-template-columns: 1fr;
    gap: 8px;
  }

  .editor-label {
    padding-top: 0;
  }
}
</style>
