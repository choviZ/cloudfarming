<template>
  <div class="product-list-page">
    <header class="page-header">
      <div class="page-header__inner">
        <div class="brand" @click="router.push('/')">
          <div class="brand__mark">
            <i class="fas fa-leaf"></i>
          </div>
          <div>
            <h1>云农场</h1>
            <span>Cloud Farming Market</span>
          </div>
        </div>

        <div class="search-box">
          <input
            v-model="searchParams.spuName"
            type="text"
            class="search-box__input"
            placeholder="搜索农产品名称，例如：有机草莓、生态鸡蛋"
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
      <div class="filter-panel__header">
        <div>
          <p class="filter-panel__eyebrow">分类搜索</p>
          <h2>按分类快速筛选商品</h2>
        </div>
      </div>

      <div class="category-pills">
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

      <div v-if="subCategoryList.length" class="sub-category-panel">
        <span class="sub-category-panel__label">细分类目</span>
        <div class="sub-category-panel__list">
          <button
            type="button"
            class="sub-category-chip"
            :class="{ active: selectedCategoryId === activeTopCategoryId }"
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
          <h3>商品结果</h3>
          <p>
            <template v-if="selectedCategoryName">
              正在查看 “{{ selectedCategoryName }}” 相关商品
            </template>
            <template v-else-if="topCategoryName">
              正在查看 “{{ topCategoryName }}” 下的全部商品
            </template>
            <template v-else>
              为你展示最新上架的优质农产品
            </template>
          </p>
        </div>
        <span class="result-count">共 {{ total }} 件商品</span>
      </div>

      <a-spin :spinning="loading">
        <div v-if="list.length" class="product-grid">
          <article
            v-for="item in list"
            :key="item.id"
            class="product-card"
            @click="goToDetail(item.id)"
          >
            <div class="product-card__image-wrap">
              <img :src="getMainImage(item.images || item.image)" :alt="item.title" class="product-card__image">
            </div>
            <div class="product-card__body">
              <div class="product-card__price">
                <span class="currency">¥</span>
                <span class="amount">{{ getPrice(item) }}</span>
              </div>
              <h4 class="product-card__title">{{ item.title }}</h4>
              <p class="product-card__desc">
                {{ selectedCategoryName || topCategoryName || '原产地直发，支持产地甄选与品质追溯' }}
              </p>
              <div class="product-card__footer">
                <span class="product-tag">农产品</span>
                <button type="button" class="detail-button" @click.stop="goToDetail(item.id)">
                  查看详情
                </button>
              </div>
            </div>
          </article>
        </div>

        <div v-else-if="!loading" class="empty-wrap">
          <a-empty description="暂无商品结果" />
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
import { getCategoryById, getChildrenByParentId, getTopLevelCategories } from '@/api/category'
import { listSpuByPage } from '@/api/spu'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const list = ref([])
const total = ref(0)
const categoryList = ref([])
const selectedCategoryName = ref('')
const selectedCategoryId = ref('')
const activeTopCategoryId = ref('')
const subCategoryList = ref([])
const CATEGORY_SELECTION_STORAGE_KEY = 'cloudfarming:selectedCategory'

const pagination = reactive({
  current: 1,
  size: 20
})

const searchParams = reactive({
  spuName: '',
  categoryId: '',
  status: 1
})

const topCategoryName = computed(() => {
  if (!activeTopCategoryId.value) {
    return ''
  }
  const matched = categoryList.value.find((item) => String(item.id) === String(activeTopCategoryId.value))
  return matched?.name || ''
})

const isTopCategoryAllMode = computed(() => {
  return Boolean(
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
  keyword = searchParams.spuName,
  categoryId = selectedCategoryId.value,
  categoryName = selectedCategoryName.value,
  topCategoryId = activeTopCategoryId.value
} = {}) => {
  const query = {}
  const trimmedKeyword = (keyword || '').trim()
  if (trimmedKeyword) {
    query.keyword = trimmedKeyword
  }
  if (categoryId) {
    query.selectedCategoryId = String(categoryId)
    query.categoryId = String(categoryId)
  }
  const resolvedCategoryName = categoryName || ''
  if (resolvedCategoryName) {
    query.selectedCategoryName = resolvedCategoryName
    query.categoryName = resolvedCategoryName
  }
  if (topCategoryId) {
    query.topCategoryId = String(topCategoryId)
  }
  return query
}

const isSameRouteQuery = (targetQuery) => {
  const currentQuery = {
    keyword: normalizeQueryValue(route.query.keyword || route.query.q),
    categoryId: normalizeQueryValue(route.query.selectedCategoryId || route.query.categoryId),
    categoryName: normalizeQueryValue(route.query.selectedCategoryName || route.query.categoryName),
    topCategoryId: normalizeQueryValue(route.query.topCategoryId)
  }
  return JSON.stringify(currentQuery) === JSON.stringify(targetQuery)
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

const syncStateFromRoute = () => {
  const keyword = normalizeQueryValue(route.query.keyword || route.query.q)
  const routeCategoryId = normalizeQueryValue(route.query.selectedCategoryId || route.query.categoryId)
  const routeCategoryName = normalizeQueryValue(route.query.selectedCategoryName || route.query.categoryName)
  const routeTopCategoryId = normalizeQueryValue(route.query.topCategoryId) || routeCategoryId
  const cachedSelection = getCachedCategorySelection()

  let categoryId = routeCategoryId
  let categoryName = routeCategoryName
  let topCategoryId = routeTopCategoryId

  if (
    cachedSelection &&
    routeTopCategoryId &&
    cachedSelection.topCategoryId === routeTopCategoryId &&
    (!routeCategoryId || routeCategoryId === routeTopCategoryId)
  ) {
    categoryId = cachedSelection.selectedCategoryId
    categoryName = cachedSelection.selectedCategoryName
    topCategoryId = cachedSelection.topCategoryId
  }

  searchParams.spuName = keyword
  selectedCategoryId.value = categoryId
  searchParams.categoryId = categoryId
  selectedCategoryName.value = categoryName
  activeTopCategoryId.value = topCategoryId
  pagination.current = 1
}

const getCachedCategorySelection = () => {
  try {
    const raw = sessionStorage.getItem(CATEGORY_SELECTION_STORAGE_KEY)
    if (!raw) {
      return null
    }
    const parsed = JSON.parse(raw)
    if (!parsed?.selectedCategoryId) {
      return null
    }
    return parsed
  } catch (error) {
    return null
  }
}

const clearCachedCategorySelection = () => {
  sessionStorage.removeItem(CATEGORY_SELECTION_STORAGE_KEY)
}

const fetchSubCategories = async () => {
  if (!activeTopCategoryId.value) {
    subCategoryList.value = []
    return
  }
  try {
    const res = await getChildrenByParentId(activeTopCategoryId.value)
    if (res.code === '0') {
      subCategoryList.value = res.data || []
      return
    }
    subCategoryList.value = []
  } catch (error) {
    console.error('Failed to fetch sub categories:', error)
    subCategoryList.value = []
  }
}

const syncSelectedCategoryName = async () => {
  if (!selectedCategoryId.value) {
    selectedCategoryName.value = ''
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

const buildListRequest = (extra = {}) => ({
  current: pagination.current,
  size: pagination.size,
  spuName: searchParams.spuName || undefined,
  categoryId: selectedCategoryId.value || undefined,
  status: searchParams.status,
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
        spuName: searchParams.spuName || undefined,
        categoryId: id,
        status: searchParams.status
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

const fetchData = async () => {
  loading.value = true
  try {
    if (isTopCategoryAllMode.value) {
      const categoryIds = [activeTopCategoryId.value, ...subCategoryList.value.map((item) => item.id)]
      const res = await listSpuByPage(
        buildListRequest({
          categoryIds,
          categoryId: selectedCategoryId.value || undefined
        })
      )
      if (res.code === '0' && res.data && Number(res.data.total || 0) > 0) {
        applyPageResult(res.data)
        return
      }
      await fetchTopCategoryAggregateData()
      return
    }

    const res = await listSpuByPage(buildListRequest())
    if (res.code === '0' && res.data) {
      applyPageResult(res.data)
      return
    }
    list.value = []
    total.value = 0
    message.error(res.message || '获取商品列表失败')
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
  await updateRouteQuery()
}

const handleCategoryClick = async (id = '', name = '', topCategoryId = '') => {
  pagination.current = 1
  if (id) {
    sessionStorage.setItem(
      CATEGORY_SELECTION_STORAGE_KEY,
      JSON.stringify({
        selectedCategoryId: String(id),
        selectedCategoryName: name,
        topCategoryId: String(topCategoryId || id)
      })
    )
  } else {
    clearCachedCategorySelection()
  }
  await updateRouteQuery(
    buildRouteQuery({
      keyword: searchParams.spuName,
      categoryId: id,
      categoryName: name,
      topCategoryId: topCategoryId || id
    })
  )
}

const isTopCategoryActive = (id) => {
  return String(activeTopCategoryId.value || '') === String(id)
}

const getMainImage = (imgStr) => {
  if (!imgStr) {
    return 'https://via.placeholder.com/480x360?text=Cloud+Farming'
  }
  return imgStr.trim().split(',')[0].trim() || imgStr
}

const getPrice = (item) => {
  if (item.priceSummary?.minPrice !== undefined && item.priceSummary?.minPrice !== null) {
    return Number(item.priceSummary.minPrice).toFixed(2)
  }
  if (item.minPrice !== undefined && item.minPrice !== null) {
    return Number(item.minPrice).toFixed(2)
  }
  return '待定'
}

const goToDetail = (id) => {
  router.push(`/product/${id}`)
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

onMounted(() => {
  fetchCategories()
})

watch(
  () => [
    route.query.keyword,
    route.query.q,
    route.query.selectedCategoryId,
    route.query.categoryId,
    route.query.selectedCategoryName,
    route.query.categoryName,
    route.query.topCategoryId
  ],
  async () => {
    syncStateFromRoute()
    await fetchSubCategories()
    await syncSelectedCategoryName()
    fetchData()
  },
  { immediate: true }
)
</script>

<style scoped>
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css');

.product-list-page {
  min-height: 100vh;
  background:
    radial-gradient(circle at top left, rgba(47, 139, 73, 0.12), transparent 22%),
    linear-gradient(180deg, #f8fbf5 0%, #ffffff 100%);
}

.page-header {
  position: sticky;
  top: 0;
  z-index: 30;
  background: rgba(255, 255, 255, 0.94);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(23, 33, 43, 0.06);
}

.page-header__inner,
.filter-panel,
.page-content {
  width: min(1620px, calc(100% - 40px));
  margin: 0 auto;
}

.page-header__inner {
  display: flex;
  align-items: center;
  gap: 28px;
  padding: 22px 0;
}

.brand {
  width: 240px;
  display: flex;
  align-items: center;
  gap: 14px;
  cursor: pointer;
  flex-shrink: 0;
}

.brand__mark {
  width: 52px;
  height: 52px;
  border-radius: 16px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1b6a3a 0%, #2f8b49 100%);
  color: #fff;
  font-size: 22px;
  box-shadow: 0 12px 24px rgba(47, 139, 73, 0.18);
}

.brand h1 {
  margin: 0;
  font-size: 28px;
  font-weight: 800;
  color: #17212b;
}

.brand span {
  display: block;
  margin-top: 6px;
  font-size: 12px;
  letter-spacing: 1px;
  color: #2f8b49;
}

.search-box {
  flex: 1;
  display: flex;
  align-items: center;
  background: #fff;
  border: 2px solid #2f8b49;
  border-radius: 999px;
  overflow: hidden;
  box-shadow: 0 14px 30px rgba(47, 139, 73, 0.08);
}

.search-box__input {
  flex: 1;
  height: 54px;
  padding: 0 22px;
  border: none;
  outline: none;
  background: transparent;
  font-size: 15px;
  color: #17212b;
}

.search-box__button {
  height: 54px;
  padding: 0 28px;
  border: none;
  background: linear-gradient(135deg, #1b6a3a 0%, #2f8b49 100%);
  color: #fff;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.cart-entry {
  height: 52px;
  padding: 0 20px;
  border: 1px solid #d9e6dc;
  border-radius: 999px;
  background: #fff;
  color: #355241;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.filter-panel {
  margin-top: 24px;
  padding: 24px 28px;
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow:
    0 24px 48px rgba(23, 33, 43, 0.08),
    0 0 0 1px rgba(23, 33, 43, 0.04);
}

.filter-panel__eyebrow {
  margin: 0 0 8px;
  color: #2f8b49;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 1px;
}

.filter-panel h2 {
  margin: 0;
  font-size: 28px;
  font-weight: 800;
  color: #17212b;
}

.category-pills {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 22px;
}

.category-pill {
  min-height: 42px;
  padding: 0 18px;
  border: 1px solid #d9e6dc;
  border-radius: 999px;
  background: #fff;
  color: #4b5563;
  font-size: 14px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s ease;
}

.category-pill:hover,
.category-pill.active {
  border-color: #2f8b49;
  color: #1b6a3a;
  background: #eef8f0;
  box-shadow: 0 10px 20px rgba(47, 139, 73, 0.08);
}

.sub-category-panel {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  margin-top: 18px;
  padding-top: 16px;
  border-top: 1px dashed rgba(23, 33, 43, 0.08);
}

.sub-category-panel__label {
  flex-shrink: 0;
  padding-top: 8px;
  font-size: 13px;
  color: #6b7280;
}

.sub-category-panel__list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.sub-category-chip {
  min-height: 36px;
  padding: 0 14px;
  border-radius: 999px;
  border: 1px solid #d9e6dc;
  background: #fff;
  color: #4b5563;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.sub-category-chip:hover,
.sub-category-chip.active {
  border-color: #2f8b49;
  background: #eef8f0;
  color: #1b6a3a;
  box-shadow: 0 10px 20px rgba(47, 139, 73, 0.08);
}

.page-content {
  margin-top: 28px;
  padding-bottom: 48px;
}

.content-summary {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 24px;
}

.content-summary h3 {
  margin: 0;
  font-size: 26px;
  font-weight: 800;
  color: #17212b;
}

.content-summary p {
  margin: 8px 0 0;
  color: #6b7280;
  font-size: 14px;
}

.result-count {
  font-size: 14px;
  color: #355241;
  font-weight: 700;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
}

.product-card {
  overflow: hidden;
  border-radius: 24px;
  background: #fff;
  cursor: pointer;
  box-shadow:
    0 16px 36px rgba(23, 33, 43, 0.08),
    0 0 0 1px rgba(23, 33, 43, 0.05);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow:
    0 22px 42px rgba(23, 33, 43, 0.12),
    0 0 0 1px rgba(23, 33, 43, 0.06);
}

.product-card__image-wrap {
  aspect-ratio: 1 / 1;
  overflow: hidden;
  background: #f5f7f5;
}

.product-card__image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-card__body {
  padding: 18px 18px 20px;
}

.product-card__price {
  display: flex;
  align-items: baseline;
  gap: 4px;
  color: #be3f28;
}

.currency {
  font-size: 16px;
  font-weight: 700;
}

.amount {
  font-size: 28px;
  font-weight: 800;
  line-height: 1;
}

.product-card__title {
  margin: 14px 0 0;
  font-size: 16px;
  line-height: 1.5;
  color: #17212b;
}

.product-card__desc {
  margin: 10px 0 0;
  font-size: 13px;
  line-height: 1.7;
  color: #6b7280;
  min-height: 44px;
}

.product-card__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 16px;
}

.product-tag {
  min-height: 30px;
  padding: 0 12px;
  border-radius: 999px;
  background: #fff5eb;
  color: #c56a16;
  font-size: 12px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
}

.detail-button {
  height: 34px;
  padding: 0 14px;
  border: none;
  border-radius: 999px;
  background: #17212b;
  color: #fff;
  font-size: 13px;
  cursor: pointer;
}

.empty-wrap,
.pagination-wrap {
  margin-top: 28px;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
}

@media (max-width: 1100px) {
  .page-header__inner {
    flex-wrap: wrap;
  }

  .brand,
  .cart-entry {
    width: auto;
  }
}

@media (max-width: 768px) {
  .page-header__inner,
  .filter-panel,
  .page-content {
    width: min(100%, calc(100% - 24px));
  }

  .search-box {
    width: 100%;
  }

  .search-box__button,
  .cart-entry {
    justify-content: center;
  }

  .content-summary {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
