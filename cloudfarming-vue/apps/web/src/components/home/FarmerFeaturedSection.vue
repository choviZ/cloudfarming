<template>
  <section class="farmer-featured-section">
    <div class="section-header">
      <div>
        <h2 class="section-title">优质农户</h2>
        <p class="section-subtitle">展示经过审核与精选的养殖农场，查看农场环境与资质信息</p>
      </div>
      <button class="view-all-button" @click="router.push('/farmers')">
        查看全部
      </button>
    </div>

    <a-skeleton :loading="loading" active :paragraph="{ rows: 6 }">
      <div v-if="farmers.length" class="card-grid">
        <article
          v-for="farmer in farmers"
          :key="farmer.id"
          class="farmer-card"
          @click="goToDetail(farmer.id)"
        >
          <div class="card-cover">
            <img :src="farmer.environmentImages?.[0] || placeholderImage" :alt="farmer.farmName" class="cover-image">
            <span class="cover-badge">{{ farmer.breedingType || '生态农场' }}</span>
          </div>
          <div class="card-body">
            <div class="card-top">
              <h3 class="card-title">{{ farmer.farmName }}</h3>
              <span class="card-area">{{ formatArea(farmer.farmArea) }}</span>
            </div>
            <p class="card-address">{{ farmer.farmAddressMasked || '地址信息待完善' }}</p>
            <div class="card-footer">
              <span class="footer-tag">环境实拍</span>
              <span class="footer-count">{{ farmer.environmentImages?.length || 0 }} 张</span>
            </div>
          </div>
        </article>
      </div>
      <a-empty v-else description="暂无精选农户" />
    </a-skeleton>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { pageFarmerShowcase } from '@/api/farmer'

const router = useRouter()
const loading = ref(false)
const farmers = ref([])

const placeholderImage = `data:image/svg+xml;charset=UTF-8,${encodeURIComponent(`
<svg xmlns="http://www.w3.org/2000/svg" width="640" height="420" viewBox="0 0 640 420">
  <defs>
    <linearGradient id="bg" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="#eef7ea"/>
      <stop offset="100%" stop-color="#d8ead2"/>
    </linearGradient>
  </defs>
  <rect width="640" height="420" rx="28" fill="url(#bg)"/>
  <circle cx="140" cy="120" r="44" fill="#bad7b1"/>
  <path d="M120 300c38-90 120-140 250-140 76 0 132 18 170 48v92H120z" fill="#7bb26d"/>
  <path d="M80 320c42-64 108-96 196-96 88 0 156 24 204 72v44H80z" fill="#4f8b46"/>
  <text x="320" y="92" text-anchor="middle" font-size="28" fill="#29513a" font-family="Microsoft YaHei, sans-serif">优质农户</text>
</svg>
`)}` 

const fetchFarmers = async () => {
  loading.value = true
  try {
    const res = await pageFarmerShowcase({
      current: 1,
      size: 4
    })
    if (res.code === '0' && res.data) {
      farmers.value = res.data.records || []
    }
  } catch (error) {
    console.error('加载优质农户失败', error)
  } finally {
    loading.value = false
  }
}

const formatArea = (value) => {
  if (!value && value !== 0) {
    return '面积待更新'
  }
  return `${value} 亩`
}

const goToDetail = (id) => {
  router.push(`/farmers/${id}`)
}

onMounted(() => {
  fetchFarmers()
})
</script>

<style scoped>
.farmer-featured-section {
  width: 100%;
}

.section-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 28px;
}

.section-title {
  margin: 0;
  font-size: 26px;
  font-weight: 800;
  color: #17212b;
}

.section-subtitle {
  margin: 8px 0 0;
  color: #5d6e63;
  font-size: 14px;
}

.view-all-button {
  height: 40px;
  padding: 0 18px;
  border: 1px solid #dbe5dd;
  border-radius: 999px;
  background: #fff;
  color: #355241;
  font-weight: 600;
  cursor: pointer;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 20px;
}

.farmer-card {
  border-radius: 20px;
  overflow: hidden;
  background: #fff;
  box-shadow: 0 16px 30px rgba(33, 53, 39, 0.08);
  cursor: pointer;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.farmer-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 18px 38px rgba(33, 53, 39, 0.14);
}

.card-cover {
  position: relative;
  height: 220px;
  background: #edf4ea;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-badge {
  position: absolute;
  left: 16px;
  top: 16px;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(18, 73, 37, 0.82);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
}

.card-body {
  padding: 18px;
}

.card-top,
.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.card-title {
  margin: 0;
  font-size: 18px;
  color: #17212b;
}

.card-area {
  color: #1f7a42;
  font-size: 13px;
  font-weight: 700;
  white-space: nowrap;
}

.card-address {
  margin: 12px 0 18px;
  min-height: 22px;
  color: #68776c;
  font-size: 14px;
}

.footer-tag {
  color: #1f7a42;
  font-weight: 700;
  font-size: 13px;
}

.footer-count {
  color: #7e8d82;
  font-size: 13px;
}

@media (max-width: 1280px) {
  .card-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .section-header {
    flex-direction: column;
    align-items: stretch;
  }

  .view-all-button {
    width: 100%;
  }

  .card-grid {
    grid-template-columns: 1fr;
  }
}
</style>
