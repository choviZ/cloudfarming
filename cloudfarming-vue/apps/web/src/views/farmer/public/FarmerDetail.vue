<template>
  <div class="farmer-detail-page">
    <a-spin :spinning="loading">
      <template v-if="detail">
        <section class="hero-card">
          <div class="hero-main">
            <div>
              <button class="back-button" @click="router.push('/farmers')">返回列表</button>
              <p class="hero-kicker">Premium Farm</p>
              <h1 class="hero-title">{{ detail.farmName }}</h1>
              <div class="hero-meta">
                <span class="meta-pill">{{ detail.breedingType || '生态农场' }}</span>
                <span class="meta-pill">{{ detail.farmArea ? `${detail.farmArea} 亩` : '面积待完善' }}</span>
                <span class="meta-pill">{{ detail.farmAddressMasked || '地址待完善' }}</span>
              </div>
            </div>
            <div class="license-panel">
              <span class="panel-label">展示资质</span>
              <a-image :src="detail.businessLicensePic" alt="营业执照" />
            </div>
          </div>
        </section>

        <section class="gallery-section">
          <div class="section-head">
            <h2>环境实拍</h2>
            <span>{{ detail.environmentImages?.length || 0 }} 张图片</span>
          </div>
          <a-image-preview-group>
            <div class="gallery-grid">
              <a-image
                v-for="(image, index) in detail.environmentImages"
                :key="`${detail.id}-${index}`"
                :src="image"
                :alt="`${detail.farmName}-${index + 1}`"
                class="gallery-image"
              />
            </div>
          </a-image-preview-group>
        </section>
      </template>
      <a-empty v-else-if="!loading" description="农户详情不存在" />
    </a-spin>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'
import { getFarmerShowcaseDetail } from '@/api/farmer'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref(null)

const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await getFarmerShowcaseDetail(route.params.id)
    if (res.code === '0' && res.data) {
      detail.value = res.data
      return
    }
    message.error(res.message || '加载农户详情失败')
  } catch (error) {
    console.error('加载农户详情失败', error)
    message.error('加载农户详情失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchDetail()
})
</script>

<style scoped>
.farmer-detail-page {
  min-height: calc(100vh - 120px);
  padding-top: 12px;
}

.hero-card {
  border-radius: 28px;
  overflow: hidden;
  background:
    radial-gradient(circle at top left, rgba(255, 255, 255, 0.18), transparent 24%),
    linear-gradient(135deg, #163a27 0%, #2f8b49 100%);
}

.hero-main {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 24px;
  padding: 42px;
  color: #fff;
}

.back-button {
  height: 38px;
  padding: 0 16px;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.32);
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
  cursor: pointer;
}

.hero-kicker {
  margin: 22px 0 12px;
  font-size: 12px;
  letter-spacing: 2px;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.78);
}

.hero-title {
  margin: 0;
  font-size: 38px;
  font-weight: 800;
}

.hero-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 18px;
}

.meta-pill {
  padding: 8px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.18);
}

.license-panel {
  padding: 18px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.14);
}

.panel-label {
  display: inline-block;
  margin-bottom: 12px;
  color: rgba(255, 255, 255, 0.8);
}

.gallery-section {
  margin-top: 24px;
  padding: 28px;
  border-radius: 24px;
  background: #fff;
  border: 1px solid #edf3ed;
  box-shadow: 0 14px 26px rgba(28, 54, 37, 0.06);
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
}

.section-head h2 {
  margin: 0;
  font-size: 24px;
}

.section-head span {
  color: #738176;
}

.gallery-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.gallery-image {
  width: 100%;
}

@media (max-width: 1100px) {
  .hero-main {
    grid-template-columns: 1fr;
  }

  .gallery-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .hero-main {
    padding: 28px 20px;
  }

  .hero-title {
    font-size: 30px;
  }

  .gallery-section {
    padding: 20px;
  }

  .gallery-grid {
    grid-template-columns: 1fr;
  }
}
</style>
