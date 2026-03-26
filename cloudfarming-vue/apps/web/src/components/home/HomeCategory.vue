<template>
  <div class="home-category" @mouseleave="clearActiveCategory">
    <div class="category-header">
      <div class="header-title">
        <AppstoreOutlined class="icon" />
        <span>全部分类</span>
      </div>
      <span class="header-tip">悬浮查看子分类，点击直达搜索结果</span>
    </div>

    <div class="category-body">
      <a-skeleton :loading="loading" active :paragraph="{ rows: 8 }">
        <ul class="category-list">
          <li
            v-for="category in categories"
            :key="category.id"
            class="category-item"
            :class="{ active: activeCategory?.id === category.id }"
            @mouseenter="setActiveCategory(category)"
          >
            <button
              type="button"
              class="category-link"
              @click="handleCategoryClick(category, category.id, category.id)"
            >
              <div class="category-main">
                <span class="category-icon">
                  <component :is="getCategoryIcon(category.name)" />
                </span>
                <div class="category-copy">
                  <span class="category-name">{{ category.name }}</span>
                  <span class="category-preview">{{ buildPreviewText(category.children) }}</span>
                </div>
              </div>
              <RightOutlined class="arrow-icon" />
            </button>
          </li>
        </ul>
      </a-skeleton>

      <transition name="panel-fade">
        <div v-if="activeCategory" class="category-panel">
          <div class="panel-header">
            <div>
              <p class="panel-eyebrow">精选类目导航</p>
              <h3>{{ activeCategory.name }}</h3>
              <span>点击分类后将带上条件进入商品列表页</span>
            </div>
            <button
              type="button"
              class="panel-all-button"
              @click="handleCategoryClick(activeCategory, activeCategory.id, activeCategory.id)"
            >
              查看全部
            </button>
          </div>

          <div class="panel-content">
            <div class="panel-groups">
              <template v-if="activeGroups.length">
                <div
                  v-for="group in activeGroups"
                  :key="group.id"
                  class="panel-group"
                >
                  <button
                    type="button"
                    class="group-title"
                    @click="handleCategoryClick(group, group.id, activeCategory.id)"
                  >
                    <span>{{ group.name }}</span>
                    <RightOutlined />
                  </button>
                  <div class="group-links">
                    <button
                      v-for="item in group.links"
                      :key="item.id"
                      type="button"
                      class="group-link"
                      @click="handleCategoryClick(item, item.id, activeCategory.id)"
                    >
                      {{ item.name }}
                    </button>
                  </div>
                </div>
              </template>

              <div v-else class="panel-empty">
                <p>该分类下暂无更细的子分类，点击右侧按钮可直接查看商品结果。</p>
              </div>
            </div>

            <aside class="panel-aside">
              <div class="aside-card">
                <span class="aside-label">当前分类</span>
                <strong>{{ activeCategory.name }}</strong>
                <p>
                  {{
                    activeGroups.length
                      ? `共 ${activeGroups.length} 组子分类入口`
                      : '已为你准备好该分类下的商品搜索入口'
                  }}
                </p>
              </div>

              <div class="aside-card">
                <span class="aside-label">热门子分类</span>
                <div class="aside-tags">
                  <button
                    v-for="item in activeHighlights"
                    :key="item.id"
                    type="button"
                    class="aside-tag"
                    @click="handleCategoryClick(item, item.id, activeCategory.id)"
                  >
                    {{ item.name }}
                  </button>
                </div>
              </div>
            </aside>
          </div>
        </div>
      </transition>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  AppstoreOutlined,
  GiftOutlined,
  RightOutlined,
  ShopOutlined,
  ShoppingOutlined,
  TagsOutlined
} from '@ant-design/icons-vue'
import { getCategoryTree } from '@/api/category'

const router = useRouter()
const loading = ref(false)
const categories = ref([])
const activeCategory = ref(null)

const fetchCategories = async () => {
  loading.value = true
  try {
    const res = await getCategoryTree()
    if (res.code === '0' && res.data?.length) {
      categories.value = res.data
    }
  } catch (error) {
    console.error('Failed to fetch categories:', error)
  } finally {
    loading.value = false
  }
}

const setActiveCategory = (category) => {
  activeCategory.value = category
}

const clearActiveCategory = () => {
  activeCategory.value = null
}

const getCategoryIcon = (name = '') => {
  if (name.includes('蔬') || name.includes('果')) return GiftOutlined
  if (name.includes('肉') || name.includes('禽')) return ShopOutlined
  if (name.includes('粮') || name.includes('油')) return TagsOutlined
  if (name.includes('水产') || name.includes('海')) return ShoppingOutlined
  return AppstoreOutlined
}

const buildPreviewText = (children = []) => {
  if (!children?.length) {
    return '点击查看该分类下的商品'
  }
  return children
    .slice(0, 3)
    .map((item) => item.name)
    .join(' / ')
}

const activeGroups = computed(() => {
  const children = activeCategory.value?.children || []
  return children.map((group) => ({
    id: group.id,
    name: group.name,
    links: group.children?.length ? group.children : [group]
  }))
})

const activeHighlights = computed(() => {
  const groups = activeGroups.value
  const result = []
  for (const group of groups) {
    for (const item of group.links) {
      result.push(item)
      if (result.length >= 8) {
        return result
      }
    }
  }
  return result.length ? result : [activeCategory.value].filter(Boolean)
})

const handleCategoryClick = (category, categoryId, topCategoryId) => {
  if (!categoryId) {
    return
  }
  router.push({
    name: 'productList',
    query: {
      categoryId: String(categoryId),
      categoryName: category.name,
      topCategoryId: String(topCategoryId || categoryId)
    }
  })
}

onMounted(() => {
  fetchCategories()
})
</script>

<style scoped>
.home-category {
  position: relative;
  width: 280px;
  height: 460px;
  background:
    linear-gradient(180deg, #fffdfa 0%, #ffffff 16%),
    linear-gradient(135deg, rgba(243, 165, 90, 0.08), transparent 34%);
  border-radius: 20px;
  box-shadow:
    0 18px 36px rgba(23, 33, 43, 0.08),
    0 0 0 1px rgba(23, 33, 43, 0.04);
  display: flex;
  flex-direction: column;
  overflow: visible;
}

.category-header {
  padding: 18px 20px 14px;
  border-bottom: 1px solid rgba(23, 33, 43, 0.06);
}

.header-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 17px;
  font-weight: 800;
  color: #17212b;
}

.header-title .icon {
  color: #c56a16;
}

.header-tip {
  display: block;
  margin-top: 8px;
  font-size: 12px;
  color: #8a6b4d;
}

.category-body {
  position: relative;
  flex: 1;
}

.category-list {
  height: 100%;
  margin: 0;
  padding: 12px 10px;
  list-style: none;
}

.category-item + .category-item {
  margin-top: 4px;
}

.category-link {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  border: none;
  border-radius: 16px;
  background: transparent;
  cursor: pointer;
  text-align: left;
  transition: background-color 0.2s ease, transform 0.2s ease;
}

.category-item.active .category-link,
.category-link:hover {
  background: linear-gradient(135deg, #fff1df 0%, #fff8ef 100%);
  transform: translateX(3px);
}

.category-main {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.category-icon {
  width: 34px;
  height: 34px;
  flex-shrink: 0;
  border-radius: 12px;
  background: #fff7ef;
  color: #c56a16;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  box-shadow: inset 0 0 0 1px rgba(197, 106, 22, 0.1);
}

.category-copy {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.category-name {
  font-size: 15px;
  font-weight: 700;
  color: #1f2937;
}

.category-preview {
  font-size: 12px;
  color: #7c8b98;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 162px;
}

.arrow-icon {
  flex-shrink: 0;
  color: #c7a27f;
  font-size: 12px;
}

.category-panel {
  position: absolute;
  top: 0;
  left: calc(100% - 14px);
  z-index: 10;
  width: 700px;
  min-height: 100%;
  padding: 22px 24px;
  border-radius: 20px;
  background:
    linear-gradient(180deg, rgba(255, 252, 247, 0.98) 0%, rgba(255, 255, 255, 0.98) 100%);
  box-shadow:
    0 28px 56px rgba(23, 33, 43, 0.18),
    0 0 0 1px rgba(23, 33, 43, 0.06);
  backdrop-filter: blur(8px);
}

.panel-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  padding-bottom: 18px;
  border-bottom: 1px solid rgba(23, 33, 43, 0.08);
}

.panel-eyebrow {
  margin: 0 0 6px;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 1px;
  color: #c56a16;
}

.panel-header h3 {
  margin: 0;
  font-size: 28px;
  font-weight: 800;
  color: #17212b;
}

.panel-header span {
  display: block;
  margin-top: 6px;
  font-size: 13px;
  color: #6b7280;
}

.panel-all-button {
  height: 40px;
  padding: 0 18px;
  border: none;
  border-radius: 999px;
  background: linear-gradient(135deg, #1b6a3a 0%, #2f8b49 100%);
  color: #fff;
  font-weight: 700;
  cursor: pointer;
  white-space: nowrap;
}

.panel-content {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 200px;
  gap: 22px;
  margin-top: 20px;
}

.panel-groups {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.panel-group {
  display: grid;
  grid-template-columns: 132px minmax(0, 1fr);
  gap: 14px;
  align-items: start;
}

.group-title {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  border: none;
  background: transparent;
  padding: 0;
  color: #17212b;
  font-size: 15px;
  font-weight: 800;
  cursor: pointer;
}

.group-links {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 12px;
}

.group-link {
  border: none;
  background: transparent;
  padding: 0;
  color: #4b5563;
  font-size: 14px;
  cursor: pointer;
  transition: color 0.2s ease;
}

.group-link:hover,
.group-title:hover {
  color: #c56a16;
}

.panel-empty {
  padding: 28px 0;
  color: #6b7280;
  font-size: 14px;
}

.panel-aside {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.aside-card {
  padding: 16px;
  border-radius: 18px;
  background: linear-gradient(180deg, #fff7ef 0%, #fffdf9 100%);
  border: 1px solid rgba(197, 106, 22, 0.12);
}

.aside-label {
  display: block;
  font-size: 12px;
  color: #c56a16;
  letter-spacing: 0.6px;
}

.aside-card strong {
  display: block;
  margin-top: 10px;
  font-size: 18px;
  color: #17212b;
}

.aside-card p {
  margin: 10px 0 0;
  font-size: 13px;
  line-height: 1.7;
  color: #6b7280;
}

.aside-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.aside-tag {
  min-height: 30px;
  padding: 0 12px;
  border: 1px solid rgba(47, 139, 73, 0.15);
  border-radius: 999px;
  background: #fff;
  color: #355241;
  font-size: 12px;
  cursor: pointer;
}

.panel-fade-enter-active,
.panel-fade-leave-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
}

.panel-fade-enter-from,
.panel-fade-leave-to {
  opacity: 0;
  transform: translateX(-8px);
}

@media (max-width: 1440px) {
  .category-panel {
    width: 620px;
  }

  .panel-content {
    grid-template-columns: minmax(0, 1fr);
  }

  .panel-aside {
    display: none;
  }
}

@media (max-width: 1200px) {
  .home-category {
    width: 100%;
    height: auto;
    min-height: 420px;
  }

  .category-panel {
    position: static;
    width: 100%;
    min-height: 0;
    margin-top: 10px;
  }
}
</style>
