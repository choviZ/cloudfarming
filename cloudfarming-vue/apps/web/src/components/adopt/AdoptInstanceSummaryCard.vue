<template>
  <section class="summary-card">
    <div class="summary-cover">
      <img :src="coverImage" :alt="instance?.itemTitle || '认养项目'" class="summary-image" />
    </div>

    <div class="summary-content">
      <div class="summary-head">
        <div>
          <p class="summary-caption">认养实例概览</p>
          <h2 class="summary-title">{{ instance?.itemTitle || '认养项目' }}</h2>
        </div>
        <div class="summary-actions">
          <a-tag :color="statusColor">{{ statusText }}</a-tag>
          <slot name="extra" />
        </div>
      </div>

      <div class="summary-grid">
        <div v-for="item in summaryItems" :key="item.label" class="summary-item">
          <span class="summary-label">{{ item.label }}</span>
          <strong class="summary-value">{{ item.value }}</strong>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import dayjs from 'dayjs'
import { getAdoptInstanceStatusOption } from '@/constants/adopt'

const props = defineProps({
  instance: {
    type: Object,
    default: () => ({})
  }
})

const fallbackCover = `data:image/svg+xml;charset=UTF-8,${encodeURIComponent(`
<svg xmlns="http://www.w3.org/2000/svg" width="320" height="240" viewBox="0 0 320 240">
  <rect width="320" height="240" rx="28" fill="#f2f6f3"/>
  <path d="M72 160c18-33 42-51 69-51s51 18 68 51" fill="none" stroke="#88a092" stroke-width="12" stroke-linecap="round"/>
  <circle cx="122" cy="94" r="24" fill="#bfd0c6"/>
  <circle cx="198" cy="94" r="24" fill="#d6e1db"/>
</svg>
`)}`

const statusOption = computed(() => getAdoptInstanceStatusOption(props.instance?.status))
const statusText = computed(() => props.instance?.statusDesc || statusOption.value?.label || '未知状态')
const statusColor = computed(() => statusOption.value?.color || 'default')
const coverImage = computed(() => props.instance?.image || props.instance?.itemCoverImage || fallbackCover)
const summaryItems = computed(() => {
  const items = [
    {
      label: '耳标号',
      value: props.instance?.earTagNo || '--'
    },
    {
      label: '最新更新',
      value: formatDate(props.instance?.latestLogTime, '暂无日记')
    }
  ]
  if (props.instance?.deathTime) {
    items.push({
      label: '死亡时间',
      value: formatDate(props.instance?.deathTime)
    })
  }
  if (props.instance?.deathReason) {
    items.push({
      label: '异常说明',
      value: props.instance.deathReason
    })
  }
  return items
})

const formatDate = (value, fallback = '--') => {
  return value ? dayjs(value).format('YYYY-MM-DD HH:mm') : fallback
}
</script>

<style scoped>
.summary-card {
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr);
  gap: 20px;
  padding: 24px;
  background: linear-gradient(135deg, #ffffff 0%, #f9fcfa 100%);
  border: 1px solid #e6efe8;
  border-radius: 22px;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.05);
}

.summary-cover {
  min-height: 240px;
}

.summary-image {
  width: 100%;
  height: 100%;
  min-height: 240px;
  object-fit: cover;
  border-radius: 20px;
  background: #f3f6f4;
}

.summary-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-width: 0;
}

.summary-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.summary-caption {
  margin: 0 0 8px;
  color: #7b8b7f;
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.summary-title {
  margin: 0;
  color: #17311f;
  font-size: 28px;
  font-weight: 700;
  line-height: 1.2;
}

.summary-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 280px));
  gap: 14px;
  align-self: flex-start;
}

.summary-item {
  padding: 14px 16px;
  background: #f7faf8;
  border: 1px solid #e7efe9;
  border-radius: 16px;
}

.summary-label {
  display: block;
  margin-bottom: 8px;
  color: #7b8b7f;
  font-size: 12px;
}

.summary-value {
  display: block;
  color: #17311f;
  font-size: 16px;
  font-weight: 700;
  word-break: break-all;
}

@media (max-width: 992px) {
  .summary-card {
    grid-template-columns: 1fr;
  }

  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .summary-card {
    padding: 18px;
  }

  .summary-grid {
    grid-template-columns: 1fr;
  }

  .summary-head {
    flex-direction: column;
  }

  .summary-title {
    font-size: 24px;
  }
}
</style>
