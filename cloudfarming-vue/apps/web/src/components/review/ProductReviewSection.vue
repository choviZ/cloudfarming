<template>
  <section class="review-panel">
    <div class="review-summary-card">
      <div class="summary-score">
        <p class="summary-label">商品口碑</p>
        <div class="summary-value">{{ avgScoreDisplay }}</div>
        <a-rate :value="Number(summary.avgScore || 0)" allow-half disabled />
        <p class="summary-caption">{{ totalReviewCount }} 条真实评价</p>
      </div>

      <div class="summary-distribution">
        <div
          v-for="item in scoreDistribution"
          :key="item.score"
          class="distribution-row"
        >
          <span class="distribution-label">{{ item.label }}</span>
          <div class="distribution-bar">
            <span class="distribution-fill" :style="{ width: `${item.percent}%` }"></span>
          </div>
          <span class="distribution-count">{{ item.count }}</span>
        </div>
      </div>
    </div>

    <div class="review-list">
      <a-spin :spinning="loading">
        <template v-if="reviews.length">
          <article
            v-for="review in reviews"
            :key="review.id"
            class="review-card"
          >
            <div class="review-header">
              <div class="review-user">
                <img
                  v-if="review.userAvatarSnapshot"
                  :src="review.userAvatarSnapshot"
                  :alt="review.userNameSnapshot || '用户头像'"
                  class="review-avatar"
                >
                <div v-else class="review-avatar review-avatar--placeholder">
                  {{ (review.userNameSnapshot || '用户').slice(0, 1) }}
                </div>
                <div>
                  <div class="review-user-name">{{ review.userNameSnapshot || '用户' }}</div>
                  <div class="review-meta">
                    <a-rate :value="review.score || 0" disabled />
                    <span>{{ formatDateTime(review.createTime) }}</span>
                  </div>
                </div>
              </div>
            </div>

            <p v-if="review.content" class="review-content">{{ review.content }}</p>

            <a-image-preview-group v-if="review.imageUrls?.length">
              <div class="review-images">
                <a-image
                  v-for="(image, index) in review.imageUrls"
                  :key="`${review.id}-${index}`"
                  :src="image"
                  class="review-image"
                />
              </div>
            </a-image-preview-group>
          </article>
        </template>

        <div v-else class="review-empty">
          <a-empty description="暂无评价，欢迎成为第一位分享体验的用户" />
        </div>
      </a-spin>
    </div>

    <div v-if="totalReviewCount > pageState.pageSize" class="review-pagination">
      <a-pagination
        v-model:current="pageState.current"
        :total="totalReviewCount"
        :page-size="pageState.pageSize"
        :show-size-changer="false"
        @change="handlePageChange"
      />
    </div>
  </section>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { getSpuReviewSummary, pageSpuReviews } from '@/api/order'

const props = defineProps({
  spuId: {
    type: Number,
    required: true
  }
})

const emit = defineEmits(['summary-change'])

const loading = ref(false)
const reviews = ref([])
const summary = reactive({
  totalReviewCount: 0,
  avgScore: 0,
  score1Count: 0,
  score2Count: 0,
  score3Count: 0,
  score4Count: 0,
  score5Count: 0
})
const pageState = reactive({
  current: 1,
  pageSize: 5,
  total: 0
})

const totalReviewCount = computed(() => Number(summary.totalReviewCount) || 0)
const avgScoreDisplay = computed(() => {
  const value = Number(summary.avgScore)
  return Number.isNaN(value) ? '0.0' : value.toFixed(1)
})

const scoreDistribution = computed(() => {
  const total = totalReviewCount.value
  return [
    { score: 5, label: '5分', count: Number(summary.score5Count) || 0 },
    { score: 4, label: '4分', count: Number(summary.score4Count) || 0 },
    { score: 3, label: '3分', count: Number(summary.score3Count) || 0 },
    { score: 2, label: '2分', count: Number(summary.score2Count) || 0 },
    { score: 1, label: '1分', count: Number(summary.score1Count) || 0 }
  ].map((item) => ({
    ...item,
    percent: total > 0 ? Math.max((item.count / total) * 100, item.count > 0 ? 6 : 0) : 0
  }))
})

const resetState = () => {
  reviews.value = []
  pageState.current = 1
  pageState.total = 0
  summary.totalReviewCount = 0
  summary.avgScore = 0
  summary.score1Count = 0
  summary.score2Count = 0
  summary.score3Count = 0
  summary.score4Count = 0
  summary.score5Count = 0
}

const applySummary = (data = {}) => {
  summary.totalReviewCount = Number(data.totalReviewCount) || 0
  summary.avgScore = Number(data.avgScore) || 0
  summary.score1Count = Number(data.score1Count) || 0
  summary.score2Count = Number(data.score2Count) || 0
  summary.score3Count = Number(data.score3Count) || 0
  summary.score4Count = Number(data.score4Count) || 0
  summary.score5Count = Number(data.score5Count) || 0
  emit('summary-change', { ...summary })
}

const fetchSummary = async () => {
  if (!props.spuId) {
    applySummary({})
    return
  }
  const response = await getSpuReviewSummary(props.spuId)
  if (response.code === '0' && response.data) {
    applySummary(response.data)
    return
  }
  applySummary({})
}

const fetchReviews = async () => {
  if (!props.spuId) {
    reviews.value = []
    return
  }
  loading.value = true
  try {
    const response = await pageSpuReviews({
      spuId: props.spuId,
      current: pageState.current,
      size: pageState.pageSize
    })
    if (response.code === '0' && response.data) {
      reviews.value = response.data.records || []
      pageState.total = Number(response.data.total) || 0
      pageState.current = Number(response.data.current) || 1
      return
    }
    reviews.value = []
    pageState.total = 0
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page) => {
  pageState.current = page
  fetchReviews()
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

watch(
  () => props.spuId,
  async (value) => {
    resetState()
    if (!value) {
      return
    }
    await fetchSummary()
    await fetchReviews()
  },
  { immediate: true }
)
</script>

<style scoped>
.review-panel {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.review-summary-card {
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr);
  gap: 28px;
  padding: 24px 28px;
  border-radius: 20px;
  background: linear-gradient(135deg, #f5fbf6 0%, #ffffff 48%, #f8fcf8 100%);
  border: 1px solid #e3eee5;
}

.summary-score {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 10px;
  padding-right: 24px;
  border-right: 1px solid #e7f0e8;
}

.summary-label {
  margin: 0;
  font-size: 13px;
  font-weight: 600;
  color: #5b6f63;
}

.summary-value {
  font-size: 48px;
  line-height: 1;
  font-weight: 800;
  color: #0f8a4b;
}

.summary-caption {
  margin: 0;
  font-size: 13px;
  color: #738177;
}

.summary-distribution {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 14px;
}

.distribution-row {
  display: grid;
  grid-template-columns: 48px minmax(0, 1fr) 40px;
  gap: 14px;
  align-items: center;
}

.distribution-label,
.distribution-count {
  font-size: 13px;
  color: #516458;
}

.distribution-bar {
  position: relative;
  height: 10px;
  border-radius: 999px;
  background: #edf3ee;
  overflow: hidden;
}

.distribution-fill {
  position: absolute;
  inset: 0 auto 0 0;
  border-radius: inherit;
  background: linear-gradient(90deg, #119b56 0%, #56be7b 100%);
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.review-card {
  padding: 22px 24px;
  border-radius: 18px;
  border: 1px solid #e7efe8;
  background: #ffffff;
}

.review-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.review-user {
  display: flex;
  align-items: center;
  gap: 14px;
}

.review-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
  background: #e9f7ec;
}

.review-avatar--placeholder {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 700;
  color: #178149;
}

.review-user-name {
  font-size: 15px;
  font-weight: 700;
  color: #203428;
}

.review-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 6px;
  font-size: 12px;
  color: #7b887f;
}

.review-content {
  margin: 16px 0 0;
  font-size: 14px;
  line-height: 1.8;
  color: #33453a;
  white-space: pre-wrap;
}

.review-images {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(90px, 90px));
  gap: 12px;
  margin-top: 16px;
}

.review-images :deep(.ant-image) {
  width: 90px;
  height: 90px;
  border-radius: 14px;
  overflow: hidden;
  border: 1px solid #e4ece6;
}

.review-images :deep(img) {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.review-empty {
  padding: 30px 0 10px;
}

.review-pagination {
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 900px) {
  .review-summary-card {
    grid-template-columns: 1fr;
    gap: 18px;
  }

  .summary-score {
    padding-right: 0;
    padding-bottom: 18px;
    border-right: none;
    border-bottom: 1px solid #e7f0e8;
  }
}
</style>
