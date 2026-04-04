<template>
  <div class="product-list-page">
    <header class="page-header">
      <div class="page-header__inner">
        <div class="brand" @click="router.push('/')">
          <div class="brand__mark">
            <i class="fas fa-leaf"></i>
          </div>
          <div class="brand__content">
            <h1>云农场</h1>
            <span>Cloud Farming Market</span>
          </div>
        </div>

        <div class="search-box">
          <input
            v-model="searchKeyword"
            type="text"
            class="search-box__input"
            :placeholder="searchPlaceholder"
            @keyup.enter="handleSearch"
          >
          <button type="button" class="search-box__button" @click="handleSearch">
            <i class="fas fa-search"></i>
            搜索
          </button>
        </div>

        <button type="button" class="cart-entry" @click="router.push('/cart')">
          <i class="fas fa-shopping-cart"></i>
          <span>我的购物车</span>
        </button>
      </div>
    </header>

    <section class="filter-panel">
      <div class="mode-tabs" role="tablist" aria-label="列表类型切换">
        <button
          type="button"
          class="mode-tab"
          :class="{ active: isProductMode }"
          :aria-selected="isProductMode"
          @click="handleModeChange(LIST_MODE.PRODUCT)"
        >
          农产品
        </button>
        <button
          type="button"
          class="mode-tab"
          :class="{ active: isAdoptMode }"
          :aria-selected="isAdoptMode"
          @click="handleModeChange(LIST_MODE.ADOPT)"
        >
          认养项目
        </button>
      </div>

      <div class="filter-panel__header">
        <div>
          <p class="filter-panel__eyebrow">{{ filterEyebrow }}</p>
          <h2>{{ filterTitle }}</h2>
          <p class="filter-panel__desc">{{ filterDescription }}</p>
        </div>
      </div>

      <div v-if="isProductMode" class="category-pills">
        <button
          type="button"
          class="category-pill"
          :class="{ active: !activeTopCategoryId }"
          @click="handleCategoryClick()"
        >
          全部
        </button>
        <button
          v-for="cat in categoryList"
          :key="cat.id"
          type="button"
          class="category-pill"
          :class="{ active: isTopCategoryActive(cat.id) }"
          @click="handleCategoryClick(cat.id, cat.name, cat.id)"
        >
          <i class="fas fa-seedling"></i>
          <span>{{ cat.name }}</span>
        </button>
      </div>

      <div v-else class="category-pills">
        <button
          type="button"
          class="category-pill"
          :class="{ active: !adoptStatus }"
          @click="handleAdoptStatusClick('')"
        >
          全部
        </button>
        <button
          type="button"
          class="category-pill"
          :class="{ active: adoptStatus === '1' }"
          @click="handleAdoptStatusClick('1')"
        >
          可认养
        </button>
        <button
          type="button"
          class="category-pill"
          :class="{ active: adoptStatus === '0' }"
          @click="handleAdoptStatusClick('0')"
        >
          已结束
        </button>
      </div>

      <div v-if="isProductMode && subCategoryList.length" class="sub-category-panel">
        <span class="sub-category-panel__label">细分类目</span>
        <div class="sub-category-panel__list">
          <button
            type="button"
            class="sub-category-chip"
            :class="{ active: String(selectedCategoryId || '') === String(activeTopCategoryId || '') }"
            @click="handleCategoryClick(activeTopCategoryId, topCategoryName, activeTopCategoryId)"
          >
            全部{{ topCategoryName }}
          </button>
          <button
            v-for="item in subCategoryList"
            :key="item.id"
            type="button"
            class="sub-category-chip"
            :class="{ active: String(selectedCategoryId) === String(item.id) }"
            @click="handleCategoryClick(item.id, item.name, activeTopCategoryId)"
          >
            {{ item.name }}
          </button>
        </div>
      </div>
    </section>

    <main class="page-content">
      <div class="content-summary">
        <div>
          <h3>{{ summaryTitle }}</h3>
          <p>{{ summaryCopy }}</p>
        </div>
        <span class="result-count">共 {{ total }} {{ resultUnit }}</span>
      </div>

      <a-spin :spinning="loading">
        <div v-if="list.length" class="result-grid">
          <template v-if="isProductMode">
            <article
              v-for="item in list"
              :key="item.id"
              class="result-card"
              @click="goToProductDetail(item.id)"
            >
              <div class="result-card__image-wrap">
                <img :src="getProductImage(item.images || item.image)" :alt="item.title" class="result-card__image">
              </div>

              <div class="result-card__body">
                <div class="result-card__meta">
                  <span class="result-tag">农产品</span>
                  <span class="result-card__place">产地直发</span>
                </div>

                <h4 class="result-card__title" :title="item.title">{{ item.title }}</h4>
                <p class="result-card__desc">{{ getProductCardDescription(item) }}</p>

                <div class="result-card__footer">
                  <div class="result-card__price">
                    <span v-if="getProductPrice(item)" class="currency">¥</span>
                    <span class="amount">{{ getProductPrice(item) || '待定' }}</span>
                  </div>

                  <button type="button" class="detail-button" @click.stop="goToProductDetail(item.id)">
                    查看详情
                  </button>
                </div>
              </div>
            </article>
          </template>

          <template v-else>
            <article
              v-for="item in list"
              :key="item.id"
              class="result-card"
              @click="goToAdoptDetail(item.id)"
            >
              <div class="result-card__image-wrap result-card__image-wrap--adopt">
                <img :src="getAdoptImage(item.coverImage)" :alt="item.title" class="result-card__image">
                <div class="adopt-status-badge" :class="item.status === 1 ? 'is-active' : 'is-inactive'">
                  {{ item.status === 1 ? '可认养' : '已结束' }}
                </div>
              </div>

              <div class="result-card__body">
                <div class="result-card__meta">
                  <span class="result-tag result-tag--adopt">认养项目</span>
                  <span class="result-card__place">云端牧场</span>
                </div>

                <h4 class="result-card__title" :title="item.title">{{ item.title }}</h4>
                <p class="result-card__desc">{{ getAdoptDescription(item) }}</p>

                <div class="result-card__footer">
                  <div class="result-card__price">
                    <span v-if="formatAdoptPrice(item.price)" class="currency">¥</span>
                    <span class="amount">{{ formatAdoptPrice(item.price) || '待定' }}</span>
                  </div>

                  <div class="result-card__footer-actions">
                    <span class="result-tag result-tag--adopt-accent">{{ getAdoptCycleTag(item) }}</span>
                    <button type="button" class="detail-button" @click.stop="goToAdoptDetail(item.id)">
                      查看详情
                    </button>
                  </div>
                </div>
              </div>
            </article>
          </template>
        </div>

        <div v-else-if="!loading" class="empty-wrap">
          <a-empty :description="emptyDescription" />
        </div>

        <div v-if="total > 0" class="pagination-wrap">
          <a-pagination
            v-model:current="pagination.current"
            v-model:page-size="pagination.size"
            :total="total"
            :show-total="(value) => `共 ${value} 条`"
            show-size-changer
            @change="handlePageChange"
            @showSizeChange="handleSizeChange"
          />
        </div>
      </a-spin>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { pageAdoptItems } from '@/api/adopt'
import { getCategoryById, getChildrenByParentId, getTopLevelCategories } from '@/api/category'
import { listSpuByPage } from '@/api/spu'

const LIST_MODE = Object.freeze({
  PRODUCT: 'product',
  ADOPT: 'adopt'
})

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const list = ref([])
const total = ref(0)
const categoryList = ref([])
const subCategoryList = ref([])
const selectedCategoryId = ref('')
const selectedCategoryName = ref('')
const activeTopCategoryId = ref('')
const adoptStatus = ref('')
const viewMode = ref(LIST_MODE.PRODUCT)
const searchKeyword = ref('')

const pagination = reactive({
  current: 1,
  size: 20
})

const productImagePlaceholder = `data:image/svg+xml;charset=UTF-8,${encodeURIComponent(`
<svg xmlns="http://www.w3.org/2000/svg" width="480" height="480" viewBox="0 0 480 480">
  <defs>
    <linearGradient id="bg" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="#f6faf4" />
      <stop offset="100%" stop-color="#e5efe2" />
    </linearGradient>
  </defs>
  <rect width="480" height="480" rx="36" fill="url(#bg)" />
  <circle cx="136" cy="146" r="44" fill="#bdd5b8" />
  <rect x="204" y="128" width="144" height="18" rx="9" fill="#6d9b69" />
  <rect x="204" y="162" width="118" height="14" rx="7" fill="#9bbf97" />
  <rect x="114" y="262" width="252" height="18" rx="9" fill="#81a97d" opacity="0.85" />
  <text x="240" y="346" text-anchor="middle" font-size="28" fill="#2d5a36" font-family="Microsoft YaHei, sans-serif">云农场</text>
</svg>
`)}` 

const adoptImagePlaceholder = `data:image/svg+xml;charset=UTF-8,${encodeURIComponent(`
<svg xmlns="http://www.w3.org/2000/svg" width="480" height="480" viewBox="0 0 480 480">
  <defs>
    <linearGradient id="bg" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="#fff8ee" />
      <stop offset="100%" stop-color="#f5e7cf" />
    </linearGradient>
  </defs>
  <rect width="480" height="480" rx="36" fill="url(#bg)" />
  <circle cx="138" cy="144" r="44" fill="#efcf9a" />
  <rect x="206" y="126" width="146" height="18" rx="9" fill="#cc8c3e" />
  <rect x="206" y="160" width="124" height="14" rx="7" fill="#e1b269" />
  <rect x="116" y="262" width="250" height="18" rx="9" fill="#d99e55" opacity="0.85" />
  <text x="240" y="346" text-anchor="middle" font-size="28" fill="#8b5a1f" font-family="Microsoft YaHei, sans-serif">认养项目</text>
</svg>
`)}` 

const isProductMode = computed(() => viewMode.value === LIST_MODE.PRODUCT)
const isAdoptMode = computed(() => viewMode.value === LIST_MODE.ADOPT)

const searchPlaceholder = computed(() =>
  isAdoptMode.value
    ? '搜索认养项目名称，例如：生态黑猪、散养土鸡'
    : '搜索农产品名称，例如：有机草莓、生态鲜蛋'
)

const filterEyebrow = computed(() => (isAdoptMode.value ? '项目筛选' : '分类筛选'))
const filterTitle = computed(() =>
  isAdoptMode.value ? '一页浏览全部认养项目' : '一页搜索全部生鲜农产品'
)
const filterDescription = computed(() =>
  isAdoptMode.value
    ? '顶部 tab 仅支持单选，切换后会展示认养项目专属筛选条件。'
    : '顶部 tab 仅支持单选，切换后会展示农产品专属分类筛选条件。'
)

const summaryTitle = computed(() => (isAdoptMode.value ? '认养项目结果' : '农产品结果'))
const resultUnit = computed(() => (isAdoptMode.value ? '个项目' : '件商品'))
const emptyDescription = computed(() => (isAdoptMode.value ? '暂无认养项目结果' : '暂无农产品结果'))

const topCategoryName = computed(() => {
  if (!activeTopCategoryId.value) {
    return ''
  }
  const matched = categoryList.value.find((item) => String(item.id) === String(activeTopCategoryId.value))
  return matched?.name || ''
})

const summaryCopy = computed(() => {
  if (isAdoptMode.value) {
    if (adoptStatus.value === '1') {
      return '当前为你展示可立即认养的项目。'
    }
    if (adoptStatus.value === '0') {
      return '当前为你展示已结束的认养项目。'
    }
    if (searchKeyword.value) {
      return `正在搜索“${searchKeyword.value}”相关认养项目。`
    }
    return '统一浏览云农场内可查看的认养项目。'
  }

  if (selectedCategoryName.value) {
    return `正在查看“${selectedCategoryName.value}”相关农产品。`
  }
  if (topCategoryName.value) {
    return `正在查看“${topCategoryName.value}”下的全部农产品。`
  }
  if (searchKeyword.value) {
    return `正在搜索“${searchKeyword.value}”相关农产品。`
  }
  return '统一浏览云农场内上新的优质农产品。'
})

const isTopCategoryAllMode = computed(() => {
  return Boolean(
    isProductMode.value &&
      selectedCategoryId.value &&
      activeTopCategoryId.value &&
      String(selectedCategoryId.value) === String(activeTopCategoryId.value) &&
      subCategoryList.value.length
  )
})

const normalizeQueryValue = (value) => {
  if (Array.isArray(value)) {
    return value[0] || ''
  }
  return value || ''
}

const buildRouteQuery = ({
  mode = viewMode.value,
  keyword = searchKeyword.value,
  categoryId = selectedCategoryId.value,
  categoryName = selectedCategoryName.value,
  topCategoryId = activeTopCategoryId.value,
  adoptStatusValue = adoptStatus.value
} = {}) => {
  const query = {
    mode: mode === LIST_MODE.ADOPT ? LIST_MODE.ADOPT : LIST_MODE.PRODUCT
  }

  const trimmedKeyword = String(keyword || '').trim()
  if (trimmedKeyword) {
    query.keyword = trimmedKeyword
  }

  if (query.mode === LIST_MODE.ADOPT) {
    if (adoptStatusValue !== '' && adoptStatusValue !== undefined && adoptStatusValue !== null) {
      query.adoptStatus = String(adoptStatusValue)
    }
    return query
  }

  if (categoryId) {
    query.selectedCategoryId = String(categoryId)
    query.categoryId = String(categoryId)
  }
  if (categoryName) {
    query.selectedCategoryName = categoryName
    query.categoryName = categoryName
  }
  if (topCategoryId) {
    query.topCategoryId = String(topCategoryId)
  }
  return query
}

const isSameRouteQuery = (targetQuery) => {
  const currentQuery = {
    mode: normalizeQueryValue(route.query.mode) || LIST_MODE.PRODUCT,
    keyword: normalizeQueryValue(route.query.keyword || route.query.q),
    categoryId: normalizeQueryValue(route.query.selectedCategoryId || route.query.categoryId),
    categoryName: normalizeQueryValue(route.query.selectedCategoryName || route.query.categoryName),
    topCategoryId: normalizeQueryValue(route.query.topCategoryId),
    adoptStatus: normalizeQueryValue(route.query.adoptStatus)
  }

  const normalizedTarget = {
    mode: targetQuery.mode || LIST_MODE.PRODUCT,
    keyword: targetQuery.keyword || '',
    categoryId: targetQuery.selectedCategoryId || targetQuery.categoryId || '',
    categoryName: targetQuery.selectedCategoryName || targetQuery.categoryName || '',
    topCategoryId: targetQuery.topCategoryId || '',
    adoptStatus: targetQuery.adoptStatus || ''
  }

  return JSON.stringify(currentQuery) === JSON.stringify(normalizedTarget)
}

const updateRouteQuery = async (query) => {
  if (isSameRouteQuery(query)) {
    await fetchData()
    return
  }

  await router.push({
    name: 'productList',
    query
  })
}

const resetProductFilters = () => {
  selectedCategoryId.value = ''
  selectedCategoryName.value = ''
  activeTopCategoryId.value = ''
  subCategoryList.value = []
}

const resetAdoptFilters = () => {
  adoptStatus.value = ''
}

const syncStateFromRoute = () => {
  const routeMode = normalizeQueryValue(route.query.mode)
  viewMode.value = routeMode === LIST_MODE.ADOPT ? LIST_MODE.ADOPT : LIST_MODE.PRODUCT
  searchKeyword.value = normalizeQueryValue(route.query.keyword || route.query.q)
  pagination.current = 1

  if (isAdoptMode.value) {
    adoptStatus.value = normalizeQueryValue(route.query.adoptStatus)
    resetProductFilters()
    return
  }

  resetAdoptFilters()
  selectedCategoryId.value = normalizeQueryValue(route.query.selectedCategoryId || route.query.categoryId)
  selectedCategoryName.value = normalizeQueryValue(
    route.query.selectedCategoryName || route.query.categoryName
  )
  activeTopCategoryId.value =
    normalizeQueryValue(route.query.topCategoryId) || normalizeQueryValue(route.query.selectedCategoryId)
}

const fetchCategories = async () => {
  try {
    const res = await getTopLevelCategories()
    if (res.code === '0') {
      categoryList.value = res.data || []
    }
  } catch (error) {
    console.error('Failed to fetch categories:', error)
  }
}

const fetchSubCategories = async () => {
  if (!isProductMode.value || !activeTopCategoryId.value) {
    subCategoryList.value = []
    return
  }

  try {
    const res = await getChildrenByParentId(activeTopCategoryId.value)
    subCategoryList.value = res.code === '0' ? res.data || [] : []
  } catch (error) {
    console.error('Failed to fetch sub categories:', error)
    subCategoryList.value = []
  }
}

const syncSelectedCategoryName = async () => {
  if (!isProductMode.value || !selectedCategoryId.value) {
    selectedCategoryName.value = ''
    return
  }

  if (selectedCategoryName.value) {
    return
  }

  try {
    const res = await getCategoryById(selectedCategoryId.value)
    if (res.code === '0' && res.data?.name) {
      selectedCategoryName.value = res.data.name
    }
  } catch (error) {
    console.error('Failed to fetch category detail:', error)
  }
}

const buildProductListRequest = (extra = {}) => ({
  current: pagination.current,
  size: pagination.size,
  spuName: searchKeyword.value || undefined,
  categoryId: selectedCategoryId.value || undefined,
  status: 1,
  ...extra
})

const applyPageResult = (pageData) => {
  list.value = pageData.records || []
  total.value = Number(pageData.total || 0)
}

const fetchTopCategoryAggregateData = async () => {
  const requestSize = pagination.current * pagination.size
  const categoryIds = [activeTopCategoryId.value, ...subCategoryList.value.map((item) => item.id)]
  const responses = await Promise.all(
    categoryIds.map((id) =>
      listSpuByPage({
        current: 1,
        size: requestSize,
        spuName: searchKeyword.value || undefined,
        categoryId: id,
        status: 1
      })
    )
  )

  const mergedMap = new Map()
  let mergedTotal = 0

  responses.forEach((res) => {
    if (res.code !== '0' || !res.data) {
      return
    }

    mergedTotal += Number(res.data.total || 0)
    ;(res.data.records || []).forEach((item) => {
      if (!mergedMap.has(item.id)) {
        mergedMap.set(item.id, item)
      }
    })
  })

  const mergedRecords = Array.from(mergedMap.values())
  const start = (pagination.current - 1) * pagination.size
  const end = start + pagination.size

  list.value = mergedRecords.slice(start, end)
  total.value = mergedTotal
}

const fetchProductData = async () => {
  if (isTopCategoryAllMode.value) {
    await fetchTopCategoryAggregateData()
    return
  }

  const res = await listSpuByPage(buildProductListRequest())
  if (res.code === '0' && res.data) {
    applyPageResult(res.data)
    return
  }

  list.value = []
  total.value = 0
  message.error(res.message || '获取农产品列表失败')
}

const fetchAdoptData = async () => {
  const req = {
    current: pagination.current,
    size: pagination.size,
    title: searchKeyword.value || undefined,
    reviewStatus: 1
  }

  if (adoptStatus.value !== '') {
    req.status = Number(adoptStatus.value)
  }

  const res = await pageAdoptItems(req)
  if (res.code === '0' && res.data) {
    applyPageResult(res.data)
    return
  }

  list.value = []
  total.value = 0
  message.error(res.message || '获取认养项目列表失败')
}

const fetchData = async () => {
  loading.value = true
  try {
    if (isAdoptMode.value) {
      await fetchAdoptData()
      return
    }
    await fetchProductData()
  } catch (error) {
    console.error(error)
    list.value = []
    total.value = 0
    message.error('系统繁忙，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleSearch = async () => {
  pagination.current = 1
  await updateRouteQuery(buildRouteQuery())
}

const handleModeChange = async (mode) => {
  if (mode === viewMode.value) {
    return
  }

  pagination.current = 1

  if (mode === LIST_MODE.ADOPT) {
    resetProductFilters()
  } else {
    resetAdoptFilters()
  }

  await updateRouteQuery(
    buildRouteQuery({
      mode,
      keyword: searchKeyword.value,
      categoryId: '',
      categoryName: '',
      topCategoryId: '',
      adoptStatusValue: ''
    })
  )
}

const handleCategoryClick = async (id = '', name = '', topCategoryId = '') => {
  if (!isProductMode.value) {
    return
  }

  pagination.current = 1
  await updateRouteQuery(
    buildRouteQuery({
      mode: LIST_MODE.PRODUCT,
      keyword: searchKeyword.value,
      categoryId: id,
      categoryName: name,
      topCategoryId: topCategoryId || id,
      adoptStatusValue: ''
    })
  )
}

const handleAdoptStatusClick = async (status) => {
  if (!isAdoptMode.value) {
    return
  }

  pagination.current = 1
  await updateRouteQuery(
    buildRouteQuery({
      mode: LIST_MODE.ADOPT,
      keyword: searchKeyword.value,
      categoryId: '',
      categoryName: '',
      topCategoryId: '',
      adoptStatusValue: status
    })
  )
}

const handlePageChange = (page) => {
  pagination.current = page
  fetchData()
}

const handleSizeChange = (current, size) => {
  pagination.current = 1
  pagination.size = size
  fetchData()
}

const isTopCategoryActive = (id) => String(activeTopCategoryId.value || '') === String(id)

const getProductImage = (imgStr) => {
  if (!imgStr) {
    return productImagePlaceholder
  }
  return imgStr.trim().split(',')[0].trim() || imgStr
}

const getAdoptImage = (imgStr) => imgStr || adoptImagePlaceholder

const getProductPrice = (item) => {
  if (item.priceSummary?.minPrice !== undefined && item.priceSummary?.minPrice !== null) {
    return Number(item.priceSummary.minPrice).toFixed(2)
  }
  if (item.minPrice !== undefined && item.minPrice !== null) {
    return Number(item.minPrice).toFixed(2)
  }
  return ''
}

const formatAdoptPrice = (value) => {
  if (value === null || value === undefined || value === '') {
    return ''
  }
  const price = Number(value)
  return Number.isNaN(price) ? String(value) : price.toFixed(2)
}

const getProductCardDescription = (item) => {
  if (item.description) {
    return item.description
  }
  if (item.subTitle) {
    return item.subTitle
  }
  if (selectedCategoryName.value) {
    return `${selectedCategoryName.value}精选好物，支持原产地直发。`
  }
  if (topCategoryName.value) {
    return `${topCategoryName.value}专场，优先展示当季新鲜商品。`
  }
  return '原产地直发，支持从农场到餐桌的新鲜履约。'
}

const getAdoptDescription = (item) => {
  const periodText = item.adoptDays ? `${item.adoptDays}天认养周期` : '认养周期待定'
  const yieldText = item.expectedYield ? ` · 预计收益 ${item.expectedYield}` : ''
  return `${periodText}${yieldText}`
}

const getAdoptCycleTag = (item) => {
  if (item.adoptDays) {
    return `${item.adoptDays}天周期`
  }
  return '认养项目'
}

const goToProductDetail = (id) => {
  router.push(`/product/${id}`)
}

const goToAdoptDetail = (id) => {
  router.push(`/adopt/detail/${id}`)
}

onMounted(() => {
  fetchCategories()
})

watch(
  () => [
    route.query.mode,
    route.query.keyword,
    route.query.q,
    route.query.selectedCategoryId,
    route.query.categoryId,
    route.query.selectedCategoryName,
    route.query.categoryName,
    route.query.topCategoryId,
    route.query.adoptStatus
  ],
  async () => {
    syncStateFromRoute()
    if (isProductMode.value) {
      await fetchSubCategories()
      await syncSelectedCategoryName()
    } else {
      subCategoryList.value = []
    }
    fetchData()
  },
  { immediate: true }
)
</script>

<style scoped>
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

.product-list-page {
  min-height: 100vh;
  background:
    radial-gradient(circle at top left, rgba(230, 239, 222, 0.72), transparent 22%),
    linear-gradient(180deg, #f8faf7 0%, #f3f5ef 100%);
  color: #17212b;
}

.page-header {
  background: rgba(255, 255, 255, 0.94);
  border-bottom: 1px solid rgba(15, 23, 42, 0.06);
  backdrop-filter: blur(14px);
  position: sticky;
  top: 0;
  z-index: 20;
}

.page-header__inner,
.filter-panel,
.page-content {
  width: 100%;
  max-width: 1620px;
  margin: 0 auto;
}

.page-header__inner {
  padding: 22px 20px;
  display: flex;
  align-items: center;
  gap: 28px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 230px;
  cursor: pointer;
}

.brand__mark {
  width: 54px;
  height: 54px;
  border-radius: 18px;
  background: linear-gradient(135deg, #1f6d3d 0%, #3e9960 100%);
  box-shadow: 0 14px 28px rgba(47, 139, 73, 0.18);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 20px;
}

.brand__content h1 {
  margin: 0;
  font-size: 30px;
  line-height: 1;
  letter-spacing: -0.8px;
  font-weight: 800;
  color: #17212b;
}

.brand__content span {
  display: block;
  margin-top: 7px;
  color: #5d7a68;
  font-size: 12px;
  letter-spacing: 1.2px;
  text-transform: uppercase;
}

.search-box {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.search-box__input {
  flex: 1;
  height: 54px;
  padding: 0 20px;
  border: 1px solid rgba(83, 108, 93, 0.16);
  border-radius: 999px;
  background: #fff;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.6);
  font-size: 15px;
  color: #17212b;
  outline: none;
  transition:
    border-color 0.2s ease,
    box-shadow 0.2s ease,
    transform 0.2s ease;
}

.search-box__input::placeholder {
  color: #8b9a8f;
}

.search-box__input:focus {
  border-color: #2f8b49;
  box-shadow: 0 0 0 4px rgba(47, 139, 73, 0.12);
}

.search-box__button,
.cart-entry,
.mode-tab,
.category-pill,
.sub-category-chip,
.detail-button {
  cursor: pointer;
}

.search-box__button {
  height: 54px;
  padding: 0 24px;
  border: none;
  border-radius: 999px;
  background: linear-gradient(135deg, #22693c 0%, #369356 100%);
  color: #fff;
  font-size: 15px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  box-shadow: 0 14px 28px rgba(47, 139, 73, 0.18);
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease;
}

.search-box__button:hover,
.detail-button:hover {
  transform: translateY(-1px);
}

.cart-entry {
  height: 50px;
  padding: 0 20px;
  border-radius: 999px;
  border: 1px solid rgba(83, 108, 93, 0.16);
  background: rgba(255, 255, 255, 0.92);
  color: #355241;
  font-size: 14px;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  transition:
    border-color 0.2s ease,
    color 0.2s ease,
    box-shadow 0.2s ease;
}

.cart-entry:hover {
  border-color: rgba(47, 139, 73, 0.26);
  color: #1f6d3d;
  box-shadow: 0 10px 20px rgba(23, 33, 43, 0.06);
}

.filter-panel {
  margin-top: 28px;
  padding: 28px 32px 30px;
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.94);
  border: 1px solid rgba(233, 237, 228, 0.92);
  box-shadow:
    0 20px 50px rgba(23, 33, 43, 0.06),
    inset 0 1px 0 rgba(255, 255, 255, 0.7);
}

.mode-tabs {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px;
  border-radius: 999px;
  background: #eef3ec;
  border: 1px solid rgba(53, 82, 65, 0.08);
}

.mode-tab {
  min-width: 128px;
  height: 46px;
  padding: 0 20px;
  border: none;
  border-radius: 999px;
  background: transparent;
  color: #5f7265;
  font-size: 15px;
  font-weight: 700;
  transition:
    background 0.2s ease,
    color 0.2s ease,
    box-shadow 0.2s ease;
}

.mode-tab.active {
  background: linear-gradient(135deg, #ffffff 0%, #f8fbf8 100%);
  color: #1f6d3d;
  box-shadow: 0 10px 22px rgba(53, 82, 65, 0.12);
}

.filter-panel__header {
  margin-top: 22px;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
}

.filter-panel__eyebrow {
  margin: 0 0 10px;
  color: #2f8b49;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 1.8px;
  text-transform: uppercase;
}

.filter-panel__header h2 {
  margin: 0;
  font-size: 30px;
  line-height: 1.15;
  font-weight: 800;
  color: #17212b;
  letter-spacing: -0.8px;
}

.filter-panel__desc {
  margin: 10px 0 0;
  color: #6b7a71;
  font-size: 14px;
  line-height: 1.7;
}

.category-pills,
.sub-category-panel__list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.category-pills {
  margin-top: 24px;
}

.category-pill,
.sub-category-chip {
  min-height: 42px;
  padding: 0 18px;
  border-radius: 999px;
  border: 1px solid rgba(83, 108, 93, 0.14);
  background: #fff;
  color: #4f6255;
  font-size: 14px;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition:
    border-color 0.2s ease,
    color 0.2s ease,
    box-shadow 0.2s ease,
    transform 0.2s ease;
}

.category-pill:hover,
.sub-category-chip:hover {
  transform: translateY(-1px);
  border-color: rgba(47, 139, 73, 0.22);
  box-shadow: 0 10px 20px rgba(23, 33, 43, 0.06);
}

.category-pill.active,
.sub-category-chip.active {
  border-color: transparent;
  background: linear-gradient(135deg, #1f6d3d 0%, #369356 100%);
  color: #fff;
  box-shadow: 0 14px 28px rgba(47, 139, 73, 0.18);
}

.sub-category-panel {
  margin-top: 20px;
  padding: 18px 20px;
  border-radius: 22px;
  background: linear-gradient(180deg, #f7faf6 0%, #fdfefd 100%);
  border: 1px solid rgba(83, 108, 93, 0.08);
}

.sub-category-panel__label {
  display: inline-block;
  margin-bottom: 14px;
  font-size: 13px;
  font-weight: 700;
  color: #738378;
  letter-spacing: 0.6px;
}

.page-content {
  padding: 26px 20px 48px;
}

.content-summary {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
  padding: 0 4px;
}

.content-summary h3 {
  margin: 0;
  font-size: 24px;
  line-height: 1.2;
  font-weight: 800;
  color: #17212b;
}

.content-summary p {
  margin: 8px 0 0;
  color: #6b7a71;
  font-size: 14px;
  line-height: 1.7;
}

.result-count {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  height: 40px;
  padding: 0 16px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.88);
  border: 1px solid rgba(83, 108, 93, 0.12);
  color: #355241;
  font-size: 14px;
  font-weight: 700;
}

.result-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.result-card {
  display: flex;
  flex-direction: column;
  min-width: 0;
  border-radius: 26px;
  overflow: hidden;
  background: #fff;
  border: 1px solid rgba(233, 237, 228, 0.96);
  box-shadow: 0 16px 36px rgba(23, 33, 43, 0.06);
  transition:
    transform 0.22s ease,
    box-shadow 0.22s ease,
    border-color 0.22s ease;
  cursor: pointer;
}

.result-card:hover {
  transform: translateY(-5px);
  border-color: rgba(47, 139, 73, 0.16);
  box-shadow: 0 20px 44px rgba(23, 33, 43, 0.09);
}

.result-card__image-wrap {
  position: relative;
  aspect-ratio: 1 / 1;
  overflow: hidden;
  background: linear-gradient(180deg, #edf3ea 0%, #f7faf5 100%);
}

.result-card__image-wrap--adopt {
  background: linear-gradient(180deg, #fff4e4 0%, #fffaf3 100%);
}

.result-card__image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.28s ease;
}

.result-card:hover .result-card__image {
  transform: scale(1.04);
}

.result-card__body {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 12px;
  padding: 18px 18px 20px;
  min-height: 198px;
}

.result-card__meta,
.result-card__footer,
.result-card__footer-actions {
  display: flex;
  align-items: center;
}

.result-card__meta {
  justify-content: space-between;
  gap: 12px;
}

.result-card__place {
  color: #86958b;
  font-size: 12px;
  font-weight: 600;
}

.result-card__title {
  margin: 0;
  font-size: 18px;
  line-height: 1.5;
  font-weight: 700;
  color: #17212b;
  min-height: 54px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  word-break: break-word;
}

.result-card__desc {
  margin: 0;
  font-size: 14px;
  line-height: 1.7;
  color: #6b7a71;
  min-height: 48px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  word-break: break-word;
}

.result-card__footer {
  justify-content: space-between;
  gap: 14px;
  margin-top: auto;
}

.result-card__footer-actions {
  gap: 10px;
  min-width: 0;
}

.result-card__price {
  display: inline-flex;
  align-items: baseline;
  gap: 4px;
  color: #d5612d;
  flex-shrink: 0;
}

.currency {
  font-size: 14px;
  font-weight: 700;
}

.amount {
  font-size: 28px;
  line-height: 1;
  font-weight: 800;
  letter-spacing: -0.6px;
}

.result-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 28px;
  padding: 0 12px;
  border-radius: 999px;
  background: rgba(47, 139, 73, 0.1);
  color: #1f6d3d;
  font-size: 12px;
  font-weight: 700;
}

.result-tag--adopt {
  background: rgba(203, 143, 58, 0.12);
  color: #9a5d17;
}

.result-tag--adopt-accent {
  background: rgba(245, 160, 76, 0.14);
  color: #a76016;
  flex-shrink: 0;
}

.detail-button {
  flex-shrink: 0;
  height: 38px;
  padding: 0 18px;
  border-radius: 999px;
  border: 1px solid rgba(47, 139, 73, 0.18);
  background: #fff;
  color: #1f6d3d;
  font-size: 13px;
  font-weight: 700;
  transition:
    border-color 0.2s ease,
    box-shadow 0.2s ease,
    color 0.2s ease;
}

.detail-button:hover {
  border-color: rgba(47, 139, 73, 0.35);
  box-shadow: 0 10px 20px rgba(47, 139, 73, 0.12);
}

.adopt-status-badge {
  position: absolute;
  top: 14px;
  left: 14px;
  min-height: 32px;
  padding: 0 12px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  backdrop-filter: blur(12px);
}

.adopt-status-badge.is-active {
  background: rgba(25, 135, 84, 0.16);
  color: #0f6f42;
}

.adopt-status-badge.is-inactive {
  background: rgba(116, 129, 139, 0.16);
  color: #51616d;
}

.empty-wrap {
  padding: 72px 0 84px;
  background: rgba(255, 255, 255, 0.66);
  border-radius: 26px;
  border: 1px solid rgba(233, 237, 228, 0.88);
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 28px;
}

:deep(.ant-pagination-item-active) {
  border-color: #2f8b49;
}

:deep(.ant-pagination-item-active a) {
  color: #2f8b49;
}

:deep(.ant-spin-dot-item) {
  background-color: #2f8b49;
}

.search-box__button:focus-visible,
.cart-entry:focus-visible,
.mode-tab:focus-visible,
.category-pill:focus-visible,
.sub-category-chip:focus-visible,
.detail-button:focus-visible {
  outline: none;
  box-shadow: 0 0 0 4px rgba(47, 139, 73, 0.12);
}

@media (max-width: 1200px) {
  .page-header__inner {
    flex-wrap: wrap;
  }

  .brand {
    min-width: 0;
  }

  .search-box {
    order: 3;
    width: 100%;
  }
}

@media (max-width: 900px) {
  .filter-panel {
    padding: 22px 18px 24px;
    border-radius: 24px;
  }

  .filter-panel__header h2 {
    font-size: 24px;
  }

  .content-summary {
    align-items: flex-start;
    flex-direction: column;
  }

  .result-grid {
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  }
}

@media (max-width: 640px) {
  .page-header__inner,
  .page-content {
    padding-left: 12px;
    padding-right: 12px;
  }

  .filter-panel {
    margin-top: 18px;
  }

  .brand__content h1 {
    font-size: 24px;
  }

  .search-box {
    flex-direction: column;
  }

  .search-box__input,
  .search-box__button,
  .cart-entry {
    width: 100%;
  }

  .mode-tabs {
    display: flex;
    width: 100%;
  }

  .mode-tab {
    flex: 1;
    min-width: 0;
  }

  .result-grid {
    grid-template-columns: 1fr;
  }

  .result-card__body {
    min-height: 0;
  }

  .result-card__footer {
    align-items: flex-start;
    flex-direction: column;
  }

  .result-card__footer-actions {
    width: 100%;
    justify-content: space-between;
  }
}

@media (prefers-reduced-motion: reduce) {
  .search-box__input,
  .search-box__button,
  .cart-entry,
  .mode-tab,
  .category-pill,
  .sub-category-chip,
  .result-card,
  .result-card__image,
  .detail-button {
    transition: none;
  }
}
</style>
