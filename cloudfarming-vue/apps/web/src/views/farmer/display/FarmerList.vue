<template>
  <div class="farmer-list-page">
    <section class="hero-section">
      <div class="hero-content">
        <p class="hero-kicker">Featured Farmers</p>
        <h1 class="hero-title">优质农户展示</h1>
        <p class="hero-desc">
          查看平台精选农户的养殖环境、农场基础信息与营业执照，帮助用户更直观地了解入驻农户。
        </p>
      </div>
    </section>

    <section class="list-section">
      <a-spin :spinning="loading">
        <div v-if="list.length" class="farmer-grid">
          <article
            v-for="item in list"
            :key="item.id"
            class="farmer-card"
            @click="goToDetail(item.id)"
          >
            <div class="card-media">
              <img :src="item.environmentImages?.[0] || placeholderImage" :alt="item.farmName" class="media-image">
              <span class="media-tag">{{ item.breedingType || '生态农场' }}</span>
            </div>
            <div class="card-body">
              <div class="card-head">
                <h3 class="card-title">{{ item.farmName }}</h3>
                <span class="card-area">{{ item.farmArea ? `${item.farmArea} 亩` : '面积待更新' }}</span>
              </div>
              <p class="card-address">{{ item.farmAddressMasked || '地址待完善' }}</p>
              <div class="card-foot">
                <span>环境图 {{ item.environmentImages?.length || 0 }} 张</span>
                <span>营业执照已展示</span>
              </div>
            </div>
          </article>
        </div>
        <a-empty v-else description="暂无优质农户" />

        <div v-if="pagination.total > 0" class="pagination-wrap">
          <a-pagination
            v-model:current="pagination.current"
            :page-size="pagination.size"
            :total="pagination.total"
            :show-total="(total) => `共 ${total} 位农户`"
            @change="handlePageChange"
          />
        </div>
      </a-spin>
    </section>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { pageFarmerShowcase } from '@/api/farmer'

const router = useRouter()
const loading = ref(false)
const list = ref([])

const pagination = reactive({
  current: 1,
  size: 8,
  total: 0
})

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

const fetchData = async () => {
  loading.value = true
  try {
    const res = await pageFarmerShowcase({
      current: pagination.current,
      size: pagination.size
    })
    if (res.code === '0' && res.data) {
      list.value = res.data.records || []
      pagination.total = Number(res.data.total || 0)
      return
    }
    message.error(res.message || '加载优质农户失败')
  } catch (error) {
    console.error('加载优质农户失败', error)
    message.error('加载优质农户失败')
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page) => {
  pagination.current = page
  fetchData()
}

const goToDetail = (id) => {
  router.push(`/farmers/${id}`)
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.farmer-list-page {
  min-height: calc(100vh - 120px);
}

.hero-section {
  margin-top: 12px;
  border-radius: 28px;
  overflow: hidden;
  background:
    radial-gradient(circle at top left, rgba(255, 255, 255, 0.18), transparent 24%),
    linear-gradient(135deg, #1e5f38 0%, #5aa062 100%);
}

.hero-content {
  padding: 52px 48px;
  color: #fff;
}

.hero-kicker {
  margin: 0 0 12px;
  font-size: 12px;
  letter-spacing: 2px;
  text-transform: uppercase;
  opacity: 0.85;
}

.hero-title {
  margin: 0;
  font-size: 40px;
  font-weight: 800;
}

.hero-desc {
  max-width: 760px;
  margin: 18px 0 0;
  font-size: 15px;
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.9);
}

.list-section {
  margin-top: 28px;
}

.farmer-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 22px;
}

.farmer-card {
  border-radius: 22px;
  overflow: hidden;
  background: #fff;
  border: 1px solid #edf3ed;
  box-shadow: 0 14px 26px rgba(28, 54, 37, 0.08);
  cursor: pointer;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.farmer-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 18px 34px rgba(28, 54, 37, 0.12);
}

.card-media {
  position: relative;
  height: 230px;
  background: #eef3ea;
}

.media-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.media-tag {
  position: absolute;
  left: 18px;
  top: 18px;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(19, 72, 37, 0.84);
  color: #fff;
  font-size: 12px;
  font-weight: 700;
}

.card-body {
  padding: 18px;
}

.card-head,
.card-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.card-title {
  margin: 0;
  color: #17212b;
  font-size: 18px;
}

.card-area {
  color: #2f8b49;
  font-size: 13px;
  font-weight: 700;
  white-space: nowrap;
}

.card-address {
  margin: 14px 0;
  color: #68776c;
  min-height: 22px;
}

.card-foot {
  color: #7b8b7f;
  font-size: 13px;
}

.pagination-wrap {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

@media (max-width: 1280px) {
  .farmer-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .hero-content {
    padding: 36px 24px;
  }

  .hero-title {
    font-size: 30px;
  }

  .farmer-grid {
    grid-template-columns: 1fr;
  }
}
</style>
