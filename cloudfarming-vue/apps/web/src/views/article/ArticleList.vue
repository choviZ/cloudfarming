<template>
  <div class="article-list-page">
    <div class="page-shell">
      <section class="page-header">
        <div class="page-header__main">
          <span class="page-eyebrow">资讯中心</span>
          <div class="page-title-row">
            <h1 class="page-title">平台公告、政策资讯与养殖知识</h1>
            <span class="page-count">{{ resultTotal }} 篇</span>
          </div>
          <p class="page-desc">
            集中查看平台公告、农业政策与养殖经验，页面风格与站内列表页保持统一，减少中间壳层和无效留白。
          </p>
        </div>

        <div class="page-header__side">
          <div class="summary-card">
            <span class="summary-label">当前分类</span>
            <strong>{{ currentTabLabel }}</strong>
          </div>
        </div>
      </section>

      <section class="toolbar-panel">
        <div class="tab-group">
          <button
            v-for="tab in articleTabs"
            :key="tab.value"
            class="tab-button"
            :class="{ active: activeType === tab.value }"
            @click="changeType(tab.value)"
          >
            {{ tab.label }}
          </button>
        </div>

        <div class="toolbar-search">
          <label class="search-label" for="article-search">搜索文章</label>
          <div class="search-box">
            <input
              id="article-search"
              v-model="filters.title"
              type="text"
              class="search-input"
              placeholder="搜索文章标题"
              @keyup.enter="handleSearch"
            >
            <button class="search-button" @click="handleSearch">搜索</button>
          </div>
        </div>
      </section>

      <section class="content-panel">
        <a-spin :spinning="loading">
          <div v-if="articleList.length" class="article-stack">
            <article
              v-for="article in articleList"
              :key="article.id"
              class="article-card"
              @click="goToDetail(article.id)"
            >
              <div class="card-cover-wrap">
                <img :src="article.coverImage || defaultImage" :alt="article.title" class="card-cover">
              </div>

              <div class="card-content">
                <div class="card-head">
                  <div class="card-badges">
                    <span class="type-badge">{{ article.articleTypeDesc || '资讯文章' }}</span>
                    <span v-if="article.isTop" class="top-badge">置顶</span>
                  </div>
                  <span class="card-date">{{ formatDate(article.publishTime || article.createTime) }}</span>
                </div>

                <h3 class="card-title">{{ article.title }}</h3>
                <p class="card-summary">{{ article.summary || '暂无摘要' }}</p>

                <div class="card-footer">
                  <span class="card-footer-link">查看详情</span>
                </div>
              </div>
            </article>
          </div>

          <div v-else class="empty-wrap">
            <a-empty description="暂无文章" />
          </div>

          <div v-if="pagination.total > 0" class="pagination-wrap">
            <a-pagination
              v-model:current="pagination.current"
              v-model:page-size="pagination.size"
              :total="pagination.total"
              :show-total="(total) => `共 ${total} 篇文章`"
              show-size-changer
              @change="handlePageChange"
              @showSizeChange="handleSizeChange"
            />
          </div>
        </a-spin>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { pagePublishedArticles } from '@/api/article'

const articleTabs = [
  { value: '', label: '全部文章' },
  { value: '1', label: '平台公告' },
  { value: '2', label: '农业政策' },
  { value: '3', label: '养殖知识' }
]

const defaultImage = `data:image/svg+xml;charset=UTF-8,${encodeURIComponent(`
<svg xmlns="http://www.w3.org/2000/svg" width="640" height="420" viewBox="0 0 640 420">
  <defs>
    <linearGradient id="bg" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="#eef8f0" />
      <stop offset="100%" stop-color="#dceedd" />
    </linearGradient>
  </defs>
  <rect width="640" height="420" rx="28" fill="url(#bg)" />
  <rect x="86" y="84" width="468" height="252" rx="22" fill="#ffffff" opacity="0.94" />
  <rect x="132" y="138" width="212" height="18" rx="9" fill="#2f8b49" opacity="0.82" />
  <rect x="132" y="176" width="346" height="12" rx="6" fill="#9cc8aa" />
  <rect x="132" y="204" width="304" height="12" rx="6" fill="#b7d8c0" />
  <rect x="132" y="250" width="144" height="32" rx="16" fill="#1f7a3f" opacity="0.84" />
  <text x="320" y="378" text-anchor="middle" font-size="30" fill="#2f5d3c" font-family="Microsoft YaHei, sans-serif">云养殖资讯</text>
</svg>
`)}` 

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const articleList = ref([])
const activeType = ref('')

const filters = reactive({
  title: ''
})

const pagination = reactive({
  current: 1,
  size: 9,
  total: 0
})

const currentTabLabel = computed(() => {
  return articleTabs.find((tab) => tab.value === activeType.value)?.label || '全部文章'
})

const resultTotal = computed(() => {
  return Number(pagination.total) || articleList.value.length
})

const normalizeQueryValue = (value) => {
  if (Array.isArray(value)) {
    return value[0] || ''
  }
  return value || ''
}

const syncFromRoute = () => {
  activeType.value = normalizeQueryValue(route.query.type)
  filters.title = normalizeQueryValue(route.query.keyword)
  pagination.current = 1
}

const fetchArticles = async () => {
  loading.value = true
  try {
    const req = {
      current: pagination.current,
      size: pagination.size,
      title: filters.title || undefined
    }
    if (activeType.value) {
      req.articleType = Number(activeType.value)
    }
    const res = await pagePublishedArticles(req)
    if (res.code === '0' && res.data) {
      articleList.value = res.data.records || []
      pagination.total = res.data.total || 0
      return
    }
    message.error(res.message || '获取文章列表失败')
  } catch (error) {
    console.error(error)
    message.error('系统繁忙，请稍后重试')
  } finally {
    loading.value = false
  }
}

const updateRouteQuery = async () => {
  const query = {}
  if (activeType.value) {
    query.type = activeType.value
  }
  if (filters.title.trim()) {
    query.keyword = filters.title.trim()
  }
  await router.push({
    name: 'articleList',
    query
  })
}

const handleSearch = async () => {
  pagination.current = 1
  await updateRouteQuery()
}

const changeType = async (type) => {
  if (activeType.value === type) {
    return
  }
  activeType.value = type
  pagination.current = 1
  await updateRouteQuery()
}

const handlePageChange = (page) => {
  pagination.current = page
  fetchArticles()
}

const handleSizeChange = (current, size) => {
  pagination.current = current
  pagination.size = size
  fetchArticles()
}

const goToDetail = (id) => {
  router.push({
    name: 'articleDetail',
    params: { id: String(id) }
  })
}

const formatDate = (value) => {
  if (!value) {
    return '未知时间'
  }
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }
  const year = date.getFullYear()
  const month = `${date.getMonth() + 1}`.padStart(2, '0')
  const day = `${date.getDate()}`.padStart(2, '0')
  return `${year}-${month}-${day}`
}

watch(
  () => [route.query.type, route.query.keyword],
  () => {
    syncFromRoute()
    fetchArticles()
  },
  { immediate: true }
)
</script>

<style scoped>
.article-list-page {
  background: transparent;
}

.page-shell {
  width: 100%;
  max-width: none;
  padding: 4px 0 24px;
}

.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24px;
  padding: 22px 24px;
  border-radius: 24px;
  background: linear-gradient(180deg, #fcfefd 0%, #f5faf6 100%);
  border: 1px solid #e4ece6;
  box-shadow: 0 12px 30px rgba(31, 122, 63, 0.05);
}

.page-header__main {
  min-width: 0;
  flex: 1;
}

.page-eyebrow {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  padding: 0 12px;
  border-radius: 999px;
  background: #edf8f0;
  color: #1f7a3f;
  font-size: 12px;
  font-weight: 700;
}

.page-title-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 14px;
}

.page-title {
  margin: 0;
  font-size: 32px;
  line-height: 1.2;
  font-weight: 800;
  color: #17212b;
  letter-spacing: -0.6px;
}

.page-count {
  display: inline-flex;
  align-items: center;
  min-height: 34px;
  padding: 0 14px;
  border-radius: 999px;
  background: #ffffff;
  border: 1px solid #dfece2;
  color: #1f7a3f;
  font-size: 13px;
  font-weight: 700;
}

.page-desc {
  margin: 10px 0 0;
  max-width: 900px;
  color: #617067;
  font-size: 14px;
  line-height: 1.75;
}

.page-header__side {
  display: flex;
  align-items: stretch;
}

.summary-card {
  min-width: 168px;
  padding: 14px 16px;
  border-radius: 18px;
  background: #ffffff;
  border: 1px solid #e4ece6;
  display: flex;
  flex-direction: column;
  gap: 8px;
  box-shadow: 0 10px 24px rgba(31, 122, 63, 0.04);
}

.summary-label {
  font-size: 12px;
  font-weight: 600;
  color: #8a948d;
}

.summary-card strong {
  color: #17212b;
  font-size: 18px;
  line-height: 1.35;
}

.toolbar-panel {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  margin-top: 14px;
  padding: 14px 18px;
  border-radius: 20px;
  background: #ffffff;
  border: 1px solid #e4ece6;
  box-shadow: 0 8px 24px rgba(31, 122, 63, 0.04);
}

.tab-group {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tab-button {
  min-height: 38px;
  padding: 0 18px;
  border: 1px solid #d9e7dc;
  background: #f8fbf8;
  border-radius: 999px;
  color: #355241;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.tab-button:hover {
  border-color: #bdd5c3;
  background: #f1f8f3;
}

.tab-button.active {
  background: #1f7a3f;
  color: #ffffff;
  border-color: #1f7a3f;
  box-shadow: 0 10px 20px rgba(31, 122, 63, 0.16);
}

.toolbar-search {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: min(420px, 100%);
  flex-shrink: 0;
}

.search-label {
  font-size: 13px;
  font-weight: 600;
  color: #6b7a71;
}

.search-box {
  display: flex;
  gap: 10px;
}

.search-input {
  flex: 1;
  height: 46px;
  padding: 0 16px;
  border: 1px solid #dce8df;
  border-radius: 14px;
  background: #ffffff;
  color: #17212b;
  font-size: 15px;
}

.search-input:focus {
  outline: none;
  border-color: #2f8b49;
  box-shadow: 0 0 0 4px rgba(47, 139, 73, 0.12);
}

.search-input::placeholder {
  color: #97a49a;
}

.search-button {
  height: 46px;
  padding: 0 20px;
  border: none;
  border-radius: 14px;
  background: linear-gradient(135deg, #22693c 0%, #369356 100%);
  color: #ffffff;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 10px 24px rgba(31, 122, 63, 0.16);
}

.content-panel {
  margin-top: 16px;
}

.article-stack {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.article-card {
  display: grid;
  grid-template-columns: 320px minmax(0, 1fr);
  min-height: 224px;
  border-radius: 24px;
  overflow: hidden;
  background: #ffffff;
  border: 1px solid #e4ece6;
  box-shadow: 0 14px 34px rgba(31, 122, 63, 0.05);
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.article-card:hover {
  transform: translateY(-2px);
  border-color: #cddfd1;
  box-shadow: 0 18px 40px rgba(31, 122, 63, 0.08);
}

.card-cover-wrap {
  height: 100%;
  background: #eef4ef;
}

.card-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.card-content {
  display: flex;
  flex-direction: column;
  min-width: 0;
  padding: 20px 22px;
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.card-badges {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.type-badge,
.top-badge {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  padding: 0 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.type-badge {
  background: #ecf7ef;
  color: #1f7a3f;
}

.top-badge {
  background: #fff4d6;
  color: #a16207;
}

.card-date {
  color: #8b949e;
  font-size: 13px;
  flex-shrink: 0;
}

.card-title {
  margin: 16px 0 10px;
  font-size: 24px;
  line-height: 1.35;
  font-weight: 700;
  color: #17212b;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-summary {
  margin: 0;
  color: #5f6d64;
  font-size: 14px;
  line-height: 1.8;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-footer {
  margin-top: auto;
  padding-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.card-footer-link {
  display: inline-flex;
  align-items: center;
  min-height: 36px;
  padding: 0 14px;
  border-radius: 999px;
  background: #f4fbf6;
  color: #1f7a3f;
  font-size: 13px;
  font-weight: 700;
}

.empty-wrap {
  padding: 48px 0 28px;
  border-radius: 24px;
  background: #ffffff;
  border: 1px solid #e4ece6;
}

.pagination-wrap {
  margin-top: 22px;
  display: flex;
  justify-content: center;
}

@media (max-width: 1080px) {
  .page-header,
  .toolbar-panel {
    flex-direction: column;
    align-items: stretch;
  }

  .page-header__side {
    justify-content: flex-start;
  }

  .toolbar-search {
    width: 100%;
  }

  .article-card {
    grid-template-columns: 260px minmax(0, 1fr);
  }
}

@media (max-width: 768px) {
  .page-title {
    font-size: 28px;
  }

  .search-box {
    flex-direction: column;
  }

  .article-card {
    grid-template-columns: 1fr;
  }

  .card-cover-wrap {
    height: 220px;
  }

  .card-head {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 640px) {
  .page-shell {
    padding-bottom: 20px;
  }

  .page-header {
    padding: 18px 16px;
    border-radius: 20px;
  }

  .page-title {
    font-size: 24px;
  }

  .toolbar-panel {
    padding: 14px;
    border-radius: 18px;
  }

  .tab-group {
    gap: 8px;
  }

  .tab-button {
    min-height: 36px;
    padding: 0 14px;
  }

  .card-content {
    padding: 18px 16px;
  }

  .card-title {
    font-size: 20px;
  }

  .card-footer {
    justify-content: flex-start;
  }
}
</style>
