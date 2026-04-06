<template>
  <section class="timeline-card">
    <div class="timeline-head">
      <div>
        <p class="timeline-caption">成长记录</p>
        <h3 class="timeline-title">生长日记</h3>
      </div>
      <slot name="extra" />
    </div>

    <a-spin :spinning="loading">
      <div v-if="logs.length" class="timeline-list">
        <article v-for="log in logs" :key="log.id" class="timeline-item">
          <div class="timeline-indicator" />
          <div class="timeline-main">
            <div class="timeline-item-head">
              <div class="timeline-tags">
                <a-tag :color="resolveLogType(log).color">{{ log.logTypeDesc || resolveLogType(log).label }}</a-tag>
                <a-tag v-if="log.weight !== null && log.weight !== undefined" color="blue">体重 {{ Number(log.weight).toFixed(1) }}kg</a-tag>
              </div>
              <span class="timeline-time">{{ formatDate(log.createTime) }}</span>
            </div>

            <p v-if="log.content" class="timeline-content">{{ log.content }}</p>

            <a-image-preview-group v-if="log.imageUrls?.length">
              <div class="timeline-images">
                <a-image
                  v-for="(imageUrl, index) in log.imageUrls"
                  :key="`${log.id}-${index}`"
                  :src="imageUrl"
                  :alt="log.logTypeDesc || '生长日记图片'"
                  class="timeline-image"
                />
              </div>
            </a-image-preview-group>

            <video
              v-if="log.videoUrl"
              :src="log.videoUrl"
              class="timeline-video"
              controls
              preload="metadata"
            />
          </div>
        </article>
      </div>
      <a-empty v-else :description="emptyText" />
    </a-spin>
  </section>
</template>

<script setup>
import dayjs from 'dayjs'
import { getAdoptLogTypeOption } from '@/constants/adopt'

defineProps({
  logs: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  },
  emptyText: {
    type: String,
    default: '暂无生长日记'
  }
})

const resolveLogType = (log) => {
  return getAdoptLogTypeOption(log?.logType) || { label: '未知类型', color: 'default' }
}

const formatDate = (value) => {
  return value ? dayjs(value).format('YYYY-MM-DD HH:mm') : '--'
}
</script>

<style scoped>
.timeline-card {
  padding: 22px 24px;
  background: #fff;
  border: 1px solid #e6efe8;
  border-radius: 22px;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.05);
}

.timeline-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 18px;
}

.timeline-caption {
  margin: 0 0 8px;
  color: #7b8b7f;
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.timeline-title {
  margin: 0;
  color: #17311f;
  font-size: 22px;
  font-weight: 700;
}

.timeline-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.timeline-item {
  display: grid;
  grid-template-columns: 18px minmax(0, 1fr);
  gap: 14px;
}

.timeline-indicator {
  width: 12px;
  height: 12px;
  margin-top: 10px;
  border-radius: 50%;
  background: linear-gradient(135deg, #4e9967, #7bc47f);
  box-shadow: 0 0 0 6px rgba(78, 153, 103, 0.12);
}

.timeline-main {
  padding: 16px 18px;
  background: #fafcfb;
  border: 1px solid #e7efe9;
  border-radius: 18px;
}

.timeline-item-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 10px;
}

.timeline-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.timeline-time {
  color: #7b8b7f;
  font-size: 12px;
  white-space: nowrap;
}

.timeline-content {
  margin: 0 0 12px;
  color: #24372a;
  font-size: 14px;
  line-height: 1.8;
  white-space: pre-wrap;
}

.timeline-images {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 140px));
  gap: 10px;
  margin-top: 12px;
}

.timeline-image {
  width: 100%;
  height: 120px;
  object-fit: cover;
  border-radius: 14px;
  overflow: hidden;
}

.timeline-video {
  width: 100%;
  max-height: 340px;
  margin-top: 12px;
  border-radius: 16px;
  background: #000;
}

@media (max-width: 768px) {
  .timeline-card {
    padding: 18px;
  }

  .timeline-item-head {
    flex-direction: column;
  }
}
</style>
