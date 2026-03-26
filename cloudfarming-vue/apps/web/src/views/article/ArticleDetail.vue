<template>
  <div class="article-detail-page">
    <section class="detail-shell">
      <a-spin :spinning="loading">
        <template v-if="article">
          <nav class="breadcrumb">
            <span class="crumb link" @click="router.push('/')">首页</span>
            <span class="separator">/</span>
            <span class="crumb link" @click="router.push('/article/list')">资讯文章</span>
            <span class="separator">/</span>
            <span class="crumb current">{{ article.title }}</span>
          </nav>

          <article class="article-card">
            <header class="article-header">
              <div class="meta-row">
                <span class="meta-pill">{{ article.articleTypeDesc || '资讯文章' }}</span>
                <span v-if="article.isTop" class="top-pill">置顶</span>
              </div>
              <h1 class="article-title">{{ article.title }}</h1>
              <p v-if="article.summary" class="article-summary">{{ article.summary }}</p>

              <div class="article-meta">
                <span>发布时间：{{ formatDateTime(article.publishTime || article.createTime) }}</span>
              </div>
            </header>

            <div v-if="article.coverImage" class="cover-wrap">
              <img :src="article.coverImage" :alt="article.title" class="cover-image">
            </div>

            <div class="article-content" v-html="article.content"></div>
          </article>
        </template>

        <a-empty v-else-if="!loading" description="文章不存在或已下线" />
      </a-spin>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { getPublishedArticleDetail } from '@/api/article'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const article = ref(null)

const fetchDetail = async () => {
  const id = Array.isArray(route.params.id) ? route.params.id[0] : route.params.id
  if (!id) {
    message.error('文章参数错误')
    router.push('/article/list')
    return
  }
  loading.value = true
  try {
    const res = await getPublishedArticleDetail(id)
    if (res.code === '0' && res.data) {
      article.value = res.data
      return
    }
    message.error(res.message || '获取文章详情失败')
  } catch (error) {
    console.error(error)
    message.error('系统繁忙，请稍后重试')
  } finally {
    loading.value = false
  }
}

const formatDateTime = (value) => {
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
  const hour = `${date.getHours()}`.padStart(2, '0')
  const minute = `${date.getMinutes()}`.padStart(2, '0')
  return `${year}-${month}-${day} ${hour}:${minute}`
}

onMounted(() => {
  fetchDetail()
})
</script>

<style scoped>
.article-detail-page {
  min-height: 100vh;
  background:
    radial-gradient(circle at top right, rgba(251, 191, 36, 0.15), transparent 18%),
    linear-gradient(180deg, #fcfcf8 0%, #f3f5ef 100%);
}

.detail-shell {
  max-width: 980px;
  margin: 0 auto;
  padding: 28px 24px 48px;
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 22px;
  color: #6b7280;
  font-size: 14px;
  flex-wrap: wrap;
}

.crumb.link {
  cursor: pointer;
}

.crumb.link:hover {
  color: #1f6f3e;
}

.crumb.current {
  color: #17212b;
}

.article-card {
  background: #fff;
  border-radius: 28px;
  box-shadow: 0 24px 60px rgba(18, 38, 24, 0.08);
  border: 1px solid rgba(15, 23, 42, 0.05);
  overflow: hidden;
}

.article-header {
  padding: 36px 36px 24px;
}

.meta-row {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}

.meta-pill,
.top-pill {
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 700;
}

.meta-pill {
  background: #e7f7ea;
  color: #20693a;
}

.top-pill {
  background: #fef3c7;
  color: #92400e;
}

.article-title {
  margin: 0;
  font-size: 40px;
  line-height: 1.2;
  color: #111827;
}

.article-summary {
  margin: 16px 0 0;
  color: #5f6d64;
  line-height: 1.8;
  font-size: 16px;
}

.article-meta {
  margin-top: 18px;
  color: #7c8b80;
  font-size: 14px;
}

.cover-wrap {
  padding: 0 36px;
}

.cover-image {
  width: 100%;
  max-height: 420px;
  object-fit: cover;
  border-radius: 22px;
  display: block;
}

.article-content {
  padding: 36px;
  color: #1f2937;
  line-height: 1.9;
  font-size: 16px;
}

.article-content :deep(h1),
.article-content :deep(h2),
.article-content :deep(h3) {
  color: #111827;
  margin-top: 1.6em;
  margin-bottom: 0.8em;
}

.article-content :deep(p) {
  margin: 0 0 1.1em;
}

.article-content :deep(img) {
  max-width: 100%;
  border-radius: 16px;
}

@media (max-width: 640px) {
  .detail-shell {
    padding-left: 16px;
    padding-right: 16px;
  }

  .article-header,
  .article-content {
    padding: 22px 18px;
  }

  .cover-wrap {
    padding: 0 18px;
  }

  .article-title {
    font-size: 28px;
  }
}
</style>