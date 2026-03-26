<template>
  <div class="article-list-page">
    <section class="hero-section">
      <div class="hero-inner">
        <div class="hero-copy">
          <span class="eyebrow">资讯与公告</span>
          <h1>平台公告、政策资讯与养殖知识</h1>
          <p>查看平台最新通知，跟进农业政策变化，持续获取养殖经营经验。</p>
        </div>

        <div class="hero-actions">
          <div class="search-box">
            <input
              v-model="filters.title"
              type="text"
              class="search-input"
              placeholder="搜索文章标题"
              @keyup.enter="handleSearch"
            >
            <button class="search-button" @click="handleSearch">搜索</button>
          </div>
        </div>
      </div>
    </section>

    <section class="content-section">
      <div class="toolbar">
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
      </div>

      <a-spin :spinning="loading">
        <div v-if="articleList.length" class="article-grid">
          <article
            v-for="article in articleList"
            :key="article.id"
            class="article-card"
            @click="goToDetail(article.id)"
          >
            <div class="card-cover-wrap">
              <img :src="article.coverImage || defaultImage" :alt="article.title" class="card-cover">
              <div class="card-badges">
                <span class="type-badge">{{ article.articleTypeDesc || '资讯文章' }}</span>
                <span v-if="article.isTop" class="top-badge">置顶</span>
              </div>
            </div>

            <div class="card-content">
              <h3 class="card-title">{{ article.title }}</h3>
              <p class="card-summary">{{ article.summary || '暂无摘要' }}</p>

              <div class="card-meta">
                <span>{{ article.articleTypeDesc || '资讯文章' }}</span>
                <span>{{ formatDate(article.publishTime || article.createTime) }}</span>
              </div>
            </div>
          </article>
        </div>

        <a-empty v-else description="暂无文章" />

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
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { pagePublishedArticles } from '@/api/article'

const articleTabs = [
  { value: '', label: '全部文章' },
  { value: '1', label: '平台公告' },
  { value: '2', label: '农业政策' },
  { value: '3', label: '养殖知识' }
]

const defaultImage = 'https://via.placeholder.com/640x420?text=Cloud+Farming+Article'

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
  }
)

onMounted(() => {
  syncFromRoute()
  fetchArticles()
})
</script>

<style scoped>
.article-list-page {
  min-height: 100vh;
  background:
    radial-gradient(circle at top left, rgba(56, 189, 248, 0.12), transparent 20%),
    linear-gradient(180deg, #f8faf7 0%, #eef4ec 100%);
}

.hero-section {
  padding: 48px 24px 24px;
}

.hero-inner {
  max-width: 1240px;
  margin: 0 auto;
  border-radius: 30px;
  padding: 36px;
  background:
    linear-gradient(135deg, rgba(18, 85, 54, 0.95), rgba(36, 121, 72, 0.88)),
    linear-gradient(180deg, #fefce8, #ffffff);
  color: #fff;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 28px;
}

.eyebrow {
  display: inline-block;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.14);
  font-size: 12px;
  letter-spacing: 0.08em;
}

.hero-copy h1 {
  margin: 14px 0 12px;
  font-size: 40px;
  line-height: 1.15;
}

.hero-copy p {
  margin: 0;
  max-width: 620px;
  line-height: 1.7;
  color: rgba(255, 255, 255, 0.86);
}

.hero-actions {
  width: 360px;
  max-width: 100%;
}

.search-box {
  display: flex;
  gap: 10px;
}

.search-input {
  flex: 1;
  height: 48px;
  padding: 0 16px;
  border: none;
  border-radius: 16px;
  background: #ffffff;
  color: #17212b;
  caret-color: #1f6f3e;
  font-size: 15px;
}

.search-input:focus {
  outline: none;
}

.search-input::placeholder {
  color: #8aa08f;
}

.search-button {
  height: 48px;
  padding: 0 20px;
  border: none;
  border-radius: 16px;
  background: #facc15;
  color: #1f2937;
  font-weight: 700;
  cursor: pointer;
}

.content-section {
  max-width: 1240px;
  margin: 0 auto;
  padding: 0 24px 48px;
}

.toolbar {
  margin: 20px 0 24px;
}

.tab-group {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tab-button {
  border: 1px solid rgba(18, 85, 54, 0.12);
  background: rgba(255, 255, 255, 0.88);
  border-radius: 999px;
  padding: 10px 18px;
  color: #355241;
  font-weight: 600;
  cursor: pointer;
}

.tab-button.active {
  background: #1f6f3e;
  color: #fff;
  border-color: #1f6f3e;
}

.article-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.article-card {
  background: #fff;
  border-radius: 22px;
  overflow: hidden;
  border: 1px solid rgba(15, 23, 42, 0.05);
  box-shadow: 0 20px 40px rgba(18, 38, 24, 0.08);
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.article-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 24px 46px rgba(18, 38, 24, 0.12);
}

.card-cover-wrap {
  position: relative;
  height: 190px;
  background: #e5efe2;
}

.card-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.card-badges {
  position: absolute;
  top: 14px;
  left: 14px;
  display: flex;
  gap: 8px;
}

.type-badge,
.top-badge {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.type-badge {
  background: rgba(17, 24, 39, 0.76);
  color: #fff;
}

.top-badge {
  background: #fef3c7;
  color: #92400e;
}

.card-content {
  padding: 18px;
}

.card-title {
  margin: 0;
  font-size: 20px;
  line-height: 1.4;
  color: #17212b;
}

.card-summary {
  margin: 12px 0 16px;
  min-height: 66px;
  color: #5f6d64;
  line-height: 1.7;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  color: #7c8b80;
  font-size: 13px;
}

.pagination-wrap {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

@media (max-width: 900px) {
  .hero-inner {
    flex-direction: column;
    align-items: stretch;
  }

  .hero-copy h1 {
    font-size: 32px;
  }

  .hero-actions {
    width: 100%;
  }
}

@media (max-width: 640px) {
  .hero-section,
  .content-section {
    padding-left: 16px;
    padding-right: 16px;
  }

  .hero-inner {
    padding: 24px;
  }

  .search-box {
    flex-direction: column;
  }
}
</style>