<template>
  <div class="home-category">
    <div class="category-header">
      <div class="header-title">
        <AppstoreOutlined class="icon" />
        <span>全部分类</span>
      </div>
    </div>
    <div class="category-list-container">
      <a-skeleton :loading="loading" active :paragraph="{ rows: 6 }">
        <ul class="category-list">
          <li v-for="category in categories" :key="category.id" class="category-item group">
            <a href="javascript:;" class="category-link">
              <div class="category-info">
                <span class="category-icon">
                  <component :is="getCategoryIcon(category.name)" />
                </span>
                <span class="category-name">{{ category.name }}</span>
              </div>
              <RightOutlined class="arrow-icon" />
            </a>
            <!-- Submenu could go here if needed -->
          </li>
        </ul>
      </a-skeleton>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { 
  AppstoreOutlined, 
  RightOutlined,
  ShoppingOutlined,
  TagsOutlined,
  RocketOutlined,
  GiftOutlined
} from '@ant-design/icons-vue';
import { getCategoryTree } from '@/api/category';
import { message } from 'ant-design-vue';

const loading = ref(false);
const categories = ref([]);

const fetchCategories = async () => {
  loading.value = true;
  try {
    const res = await getCategoryTree();
    if (res.code === '0' && res.data) {
      categories.value = res.data;
    }
  } catch (error) {
    console.error('Failed to fetch categories:', error);
    // Silent fail or minimal user impact
  } finally {
    loading.value = false;
  }
};

const getCategoryIcon = (name) => {
  if (name.includes('家畜')) return TagsOutlined;
  if (name.includes('家禽')) return RocketOutlined;
  if (name.includes('果')) return GiftOutlined;
  if (name.includes('水产')) return ShoppingOutlined;
  return AppstoreOutlined;
};

onMounted(() => {
  fetchCategories();
});
</script>

<style scoped>
.home-category {
  width: 240px;
  height: 460px;
  background-color: #ffffff;
  border-radius: 16px;
  box-shadow: 0 0 0 1px rgba(0,0,0,0.03), 0 2px 8px rgba(0,0,0,0.04);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.category-header {
  padding: 12px 20px;
  border-bottom: 1px solid #f8fafc;
}

.header-title {
  font-weight: 700;
  color: #1e293b;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
}

.header-title .icon {
  color: #22c55e; /* primary-500 */
}

.category-list-container {
  flex: 1;
  overflow-y: auto;
  padding: 4px 8px;
}

/* Hide scrollbar */
.category-list-container::-webkit-scrollbar {
  display: none;
}
.category-list-container {
  -ms-overflow-style: none;
  scrollbar-width: none;
}

.category-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.category-item {
  margin-bottom: 4px;
}

.category-link {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  border-radius: 12px;
  transition: all 0.2s;
  text-decoration: none;
}

.category-link:hover {
  background-color: #f0fdf4; /* primary-50 */
}

.category-info {
  display: flex;
  align-items: center;
  gap: 14px;
}

.category-icon {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background-color: #f8fafc;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  transition: all 0.2s;
}

.category-link:hover .category-icon {
  background-color: #dcfce7; /* primary-100 */
  color: #16a34a; /* primary-600 */
}

.category-name {
  font-size: 14px;
  color: #475569;
  transition: color 0.2s;
}

.category-link:hover .category-name {
  color: #0f172a;
  font-weight: 500;
}

.arrow-icon {
  font-size: 10px;
  color: #cbd5e1;
  transition: color 0.2s;
}

.category-link:hover .arrow-icon {
  color: #4ade80; /* primary-400 */
}
</style>
