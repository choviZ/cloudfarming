<template>
  <div class="search-page">
    <header class="search-header">
      <div class="header-inner">
        <div class="brand-block" @click="router.push('/')">
          <div class="brand-mark">云</div>
          <div class="brand-text">
            <h1>云农场</h1>
            <span>全站搜索</span>
          </div>
        </div>

        <div class="search-bar">
          <input
            v-model="keyword"
            class="search-input"
            type="text"
            placeholder="搜索农产品或认养项目"
            @keyup.enter="handleSearch"
          >
          <button class="search-button" @click="handleSearch">搜索</button>
        </div>
      </div>
    </header>

    <main class="search-main">
      <div class="search-summary">
        <div>
          <h2>搜索结果</h2>
          <p v-if="displayKeyword">关键词 “{{ displayKeyword }}”</p>
          <p v-else>请输入关键词开始搜索</p>
        </div>
        <div class="tab-group">
          <button
            v-for="tab in tabs"
            :key="tab.key"
            class="tab-button"
            :class="{ active: activeType === tab.key }"
            @click="changeType(tab.key)"
          >
            {{ tab.label }}
          </button>
        </div>
      </div>

      <a-spin :spinning="loading">
        <template v-if="displayKeyword">
          <template v-if="activeType === TYPE_ALL">
            <div class="group-layout">
              <section class="result-section">
                <div class="section-head">
                  <div>
                    <h3>农产品</h3>
                    <span>{{ searchResult.spuTotal || 0 }} 条结果</span>
                  </div>
                  <button class="section-link" @click="changeType(TYPE_SPU)">查看全部</button>
                </div>

                <div v-if="searchResult.spuRecords?.length" class="card-grid">
                  <article
                    v-for="item in searchResult.spuRecords"
                    :key="`spu-${item.id}`"
                    class="result-card"
                    @click="goToDetail(item)"
                  >
                    <div class="card-image-wrap">
                      <img :src="item.image || defaultImage" :alt="item.title" class="card-image">
                      <span class="type-badge type-spu">{{ item.productTypeDesc }}</span>
                    </div>
                    <div class="card-body">
                      <h4 class="card-title">{{ item.title }}</h4>
                      <p class="card-subtitle">{{ item.subtitle || '农产品' }}</p>
                      <div class="card-price">{{ formatPrice(item.price) }}</div>
                    </div>
                  </article>
                </div>
                <a-empty v-else description="暂无农产品结果" />
              </section>

              <section class="result-section">
                <div class="section-head">
                  <div>
                    <h3>认养项目</h3>
                    <span>{{ searchResult.adoptTotal || 0 }} 条结果</span>
                  </div>
                  <button class="section-link" @click="changeType(TYPE_ADOPT)">查看全部</button>
                </div>

                <div v-if="searchResult.adoptRecords?.length" class="card-grid">
                  <article
                    v-for="item in searchResult.adoptRecords"
                    :key="`adopt-${item.id}`"
                    class="result-card"
                    @click="goToDetail(item)"
                  >
                    <div class="card-image-wrap">
                      <img :src="item.image || defaultImage" :alt="item.title" class="card-image">
                      <span class="type-badge type-adopt">{{ item.productTypeDesc }}</span>
                    </div>
                    <div class="card-body">
                      <h4 class="card-title">{{ item.title }}</h4>
                      <p class="card-subtitle">{{ item.subtitle || '认养项目' }}</p>
                      <p class="card-desc">{{ item.description || '可查看认养周期与项目详情' }}</p>
                      <div class="card-price">{{ formatPrice(item.price) }}</div>
                    </div>
                  </article>
                </div>
                <a-empty v-else description="暂无认养项目结果" />
              </section>
            </div>
          </template>

          <template v-else>
            <section class="result-section single-section">
              <div class="section-head">
                <div>
                  <h3>{{ activeType === TYPE_SPU ? '农产品' : '认养项目' }}</h3>
                  <span>共 {{ searchResult.total || 0 }} 条结果</span>
                </div>
              </div>

              <div v-if="searchResult.records?.length" class="card-grid">
                <article
                  v-for="item in searchResult.records"
                  :key="`${item.productType}-${item.id}`"
                  class="result-card"
                  @click="goToDetail(item)"
                >
                  <div class="card-image-wrap">
                    <img :src="item.image || defaultImage" :alt="item.title" class="card-image">
                    <span
                      class="type-badge"
                      :class="item.productType === 1 ? 'type-spu' : 'type-adopt'"
                    >
                      {{ item.productTypeDesc }}
                    </span>
                  </div>
                  <div class="card-body">
                    <h4 class="card-title">{{ item.title }}</h4>
                    <p class="card-subtitle">{{ item.subtitle || getFallbackSubtitle(item) }}</p>
                    <p v-if="item.description" class="card-desc">{{ item.description }}</p>
                    <div class="card-price">{{ formatPrice(item.price) }}</div>
                  </div>
                </article>
              </div>
              <a-empty v-else description="暂无搜索结果" />

              <div v-if="(searchResult.total || 0) > pagination.size" class="pagination-wrap">
                <a-pagination
                  v-model:current="pagination.current"
                  v-model:page-size="pagination.size"
                  :total="searchResult.total || 0"
                  :show-total="(total) => `共 ${total} 条`"
                  show-size-changer
                  @change="handlePageChange"
                  @showSizeChange="handleSizeChange"
                />
              </div>
            </section>
          </template>
        </template>

        <a-empty v-else description="请输入关键词开始搜索" />
      </a-spin>
    </main>
  </div>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { searchByKeyword } from '@/api/search'

const TYPE_ALL = 'all'
const TYPE_ADOPT = '0'
const TYPE_SPU = '1'
const ALL_PREVIEW_SIZE = 6
const defaultImage = 'https://via.placeholder.com/480x320?text=Cloud+Farming'

const tabs = [
  { key: TYPE_ALL, label: '全部' },
  { key: TYPE_SPU, label: '农产品' },
  { key: TYPE_ADOPT, label: '认养项目' }
]

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const keyword = ref('')
const activeType = ref(TYPE_ALL)
const searchResult = ref({
  total: 0,
  records: [],
  spuTotal: 0,
  spuRecords: [],
  adoptTotal: 0,
  adoptRecords: []
})

const pagination = reactive({
  current: 1,
  size: 12
})

const displayKeyword = computed(() => keyword.value.trim())

const normalizeQueryValue = (value) => {
  if (Array.isArray(value)) {
    return value[0] || ''
  }
  return value || ''
}

const normalizeType = (value) => {
  const type = normalizeQueryValue(value)
  return [TYPE_SPU, TYPE_ADOPT].includes(type) ? type : TYPE_ALL
}

const buildRouteQuery = () => {
  const q = keyword.value.trim()
  if (!q) {
    return {}
  }
  if (activeType.value === TYPE_ALL) {
    return { q }
  }
  return {
    q,
    type: activeType.value
  }
}

const isSameRouteState = (query) => {
  const routeKeyword = normalizeQueryValue(route.query.q)
  const routeType = normalizeType(route.query.type)
  const targetKeyword = query.q || ''
  const targetType = query.type || TYPE_ALL
  return routeKeyword === targetKeyword && routeType === targetType
}

const resetSearchResult = () => {
  searchResult.value = {
    total: 0,
    records: [],
    spuTotal: 0,
    spuRecords: [],
    adoptTotal: 0,
    adoptRecords: []
  }
}

const fetchData = async () => {
  const trimmedKeyword = keyword.value.trim()
  if (!trimmedKeyword) {
    resetSearchResult()
    return
  }
  loading.value = true
  try {
    const req = {
      keyword: trimmedKeyword,
      current: pagination.current,
      size: activeType.value === TYPE_ALL ? ALL_PREVIEW_SIZE : pagination.size
    }
    if (activeType.value !== TYPE_ALL) {
      req.productType = Number(activeType.value)
    }
    const res = await searchByKeyword(req)
    if (res.code === '0' && res.data) {
      searchResult.value = {
        total: res.data.total || 0,
        records: res.data.records || [],
        spuTotal: res.data.spuTotal || 0,
        spuRecords: res.data.spuRecords || [],
        adoptTotal: res.data.adoptTotal || 0,
        adoptRecords: res.data.adoptRecords || []
      }
      return
    }
    message.error(res.message || '搜索失败')
  } catch (error) {
    console.error(error)
    message.error('系统繁忙，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleSearch = async () => {
  pagination.current = 1
  const targetQuery = buildRouteQuery()
  if (isSameRouteState(targetQuery)) {
    fetchData()
    return
  }
  await router.push({
    name: 'search',
    query: targetQuery
  })
}

const changeType = async (type) => {
  if (activeType.value === type) {
    return
  }
  activeType.value = type
  pagination.current = 1
  const targetQuery = buildRouteQuery()
  if (isSameRouteState(targetQuery)) {
    fetchData()
    return
  }
  await router.push({
    name: 'search',
    query: targetQuery
  })
}

const handlePageChange = (page) => {
  pagination.current = page
  fetchData()
}

const handleSizeChange = (current, size) => {
  pagination.current = current
  pagination.size = size
  fetchData()
}

const formatPrice = (price) => {
  if (price === null || price === undefined || price === '') {
    return '价格待定'
  }
  return `￥${Number(price).toFixed(2)}`
}

const getFallbackSubtitle = (item) => {
  return item.productType === 1 ? '农产品' : '认养项目'
}

const goToDetail = (item) => {
  if (item.targetPath) {
    router.push(item.targetPath)
    return
  }
  if (item.productType === 1) {
    router.push(`/product/${item.id}`)
    return
  }
  router.push(`/adopt/detail/${item.id}`)
}

watch(
  () => [route.query.q, route.query.type],
  () => {
    keyword.value = normalizeQueryValue(route.query.q)
    activeType.value = normalizeType(route.query.type)
    pagination.current = 1
    fetchData()
  },
  { immediate: true }
)
</script>

<style scoped>
.search-page {
  min-height: 100vh;
  background:
    radial-gradient(circle at top right, rgba(163, 230, 53, 0.18), transparent 24%),
    linear-gradient(180deg, #f7fbf2 0%, #f4f6ef 100%);
  color: #203127;
}

.search-header {
  position: sticky;
  top: 0;
  z-index: 20;
  backdrop-filter: blur(16px);
  background: rgba(247, 251, 242, 0.9);
  border-bottom: 1px solid rgba(32, 49, 39, 0.08);
}

.header-inner {
  max-width: 1240px;
  margin: 0 auto;
  padding: 20px 24px;
  display: flex;
  gap: 24px;
  align-items: center;
  justify-content: space-between;
}

.brand-block {
  display: flex;
  align-items: center;
  gap: 14px;
  cursor: pointer;
  min-width: 180px;
}

.brand-mark {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  background: linear-gradient(135deg, #1f6f3e 0%, #9bcf53 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  font-weight: 700;
}

.brand-text h1 {
  margin: 0;
  font-size: 22px;
  line-height: 1.1;
}

.brand-text span {
  font-size: 13px;
  color: #5e725f;
}

.search-bar {
  flex: 1;
  max-width: 760px;
  display: flex;
  gap: 12px;
}

.search-input {
  flex: 1;
  height: 48px;
  border-radius: 999px;
  border: 1px solid rgba(31, 111, 62, 0.18);
  background: #fff;
  padding: 0 20px;
  font-size: 15px;
  transition: box-shadow 0.2s ease, border-color 0.2s ease;
}

.search-input:focus {
  outline: none;
  border-color: #2d7d46;
  box-shadow: 0 0 0 4px rgba(45, 125, 70, 0.12);
}

.search-button {
  height: 48px;
  border: none;
  border-radius: 999px;
  padding: 0 24px;
  background: linear-gradient(135deg, #1f6f3e 0%, #2f8b49 100%);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
}

.search-main {
  max-width: 1240px;
  margin: 0 auto;
  padding: 32px 24px 48px;
}

.search-summary {
  display: flex;
  gap: 20px;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 24px;
}

.search-summary h2 {
  margin: 0 0 6px;
  font-size: 30px;
}

.search-summary p {
  margin: 0;
  color: #60705f;
}

.tab-group {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.tab-button {
  border: 1px solid rgba(31, 111, 62, 0.12);
  background: rgba(255, 255, 255, 0.84);
  color: #36503e;
  border-radius: 999px;
  padding: 10px 18px;
  cursor: pointer;
  font-weight: 600;
}

.tab-button.active {
  background: #214f31;
  border-color: #214f31;
  color: #fff;
}

.group-layout,
.single-section {
  display: grid;
  gap: 24px;
}

.result-section {
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(32, 49, 39, 0.08);
  border-radius: 28px;
  padding: 24px;
  box-shadow: 0 18px 40px rgba(31, 58, 39, 0.06);
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
}

.section-head h3 {
  margin: 0 0 4px;
  font-size: 22px;
}

.section-head span {
  color: #657565;
  font-size: 14px;
}

.section-link {
  border: none;
  background: transparent;
  color: #1f6f3e;
  cursor: pointer;
  font-weight: 600;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 18px;
}

.result-card {
  overflow: hidden;
  background: #fff;
  border-radius: 22px;
  border: 1px solid rgba(32, 49, 39, 0.08);
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.result-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 18px 32px rgba(31, 58, 39, 0.12);
}

.card-image-wrap {
  position: relative;
  aspect-ratio: 4 / 3;
  background: #eef3ea;
}

.card-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.type-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  border-radius: 999px;
  padding: 6px 10px;
  font-size: 12px;
  font-weight: 600;
  color: #fff;
}

.type-spu {
  background: rgba(32, 95, 43, 0.92);
}

.type-adopt {
  background: rgba(181, 107, 38, 0.92);
}

.card-body {
  padding: 16px;
}

.card-title {
  margin: 0 0 8px;
  font-size: 17px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-subtitle,
.card-desc {
  margin: 0 0 8px;
  color: #657565;
  font-size: 14px;
  line-height: 1.5;
}

.card-price {
  margin-top: 12px;
  font-size: 22px;
  font-weight: 700;
  color: #ba4a16;
}

.pagination-wrap {
  margin-top: 28px;
  display: flex;
  justify-content: center;
}

@media (max-width: 900px) {
  .header-inner,
  .search-summary {
    flex-direction: column;
    align-items: stretch;
  }

  .brand-block {
    min-width: 0;
  }

  .search-bar {
    max-width: none;
  }
}

@media (max-width: 640px) {
  .search-main,
  .header-inner {
    padding-left: 16px;
    padding-right: 16px;
  }

  .search-bar {
    flex-direction: column;
  }

  .search-button {
    width: 100%;
  }

  .result-section {
    padding: 18px;
    border-radius: 22px;
  }

  .search-summary h2 {
    font-size: 24px;
  }
}
</style>
