<template>
  <div class="shop-manage-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">店铺管理</h2>
        <p class="page-desc">维护店铺首页基础信息和当前公告，买家侧将自动展示最新内容。</p>
      </div>
      <a-space>
        <a-button :disabled="!shopForm.id" @click="openShopHome">查看店铺首页</a-button>
        <a-button type="primary" :loading="saving" @click="handleSubmit">保存设置</a-button>
      </a-space>
    </div>

    <a-alert
      type="info"
      show-icon
      class="page-alert"
      message="店铺首页会自动聚合当前店铺已上架且已审核通过的商品和认养项目。"
    />

    <a-spin :spinning="loading">
      <div class="content-grid">
        <a-card title="店铺资料" :bordered="false" class="form-card">
          <a-form ref="formRef" layout="vertical" :model="shopForm" :rules="rules">
            <a-form-item label="店铺名称" name="shopName">
              <a-input
                v-model:value="shopForm.shopName"
                :maxlength="50"
                show-count
                placeholder="请输入店铺名称"
              />
            </a-form-item>

            <a-form-item label="店铺头像">
              <ImageUpload v-model:value="shopForm.shopAvatar" biz-code="SHOP_AVATAR" />
            </a-form-item>

            <a-form-item label="店铺横幅">
              <ImageUpload v-model:value="shopForm.shopBanner" biz-code="SHOP_BANNER" />
            </a-form-item>

            <a-form-item label="店铺简介" name="description">
              <a-textarea
                v-model:value="shopForm.description"
                :maxlength="500"
                :rows="5"
                show-count
                placeholder="介绍一下店铺特色、经营理念和主营方向"
              />
            </a-form-item>

            <a-form-item label="店铺公告" name="announcement">
              <a-textarea
                v-model:value="shopForm.announcement"
                :maxlength="200"
                :rows="4"
                show-count
                placeholder="公告会在买家侧店铺首页展示，可用于发布发货安排、活动提醒等信息"
              />
            </a-form-item>
          </a-form>
        </a-card>

        <a-card title="首页预览" :bordered="false" class="preview-card">
          <div class="shop-preview">
            <div class="preview-hero" :style="previewBannerStyle">
              <div class="preview-overlay" />
              <div class="preview-content">
                <div class="preview-avatar">
                  <img v-if="shopForm.shopAvatar" :src="shopForm.shopAvatar" alt="店铺头像" />
                  <span v-else>{{ previewInitial }}</span>
                </div>
                <div class="preview-meta">
                  <h3>{{ shopForm.shopName || '默认店铺' }}</h3>
                  <p>{{ shopDescription }}</p>
                </div>
              </div>
            </div>

            <div v-if="shopForm.announcement" class="preview-notice">
              <div class="preview-notice__label">店铺公告</div>
              <div class="preview-notice__text">{{ shopForm.announcement }}</div>
            </div>

            <div v-else class="preview-empty">
              暂未设置店铺公告，保存后买家侧将不展示公告模块。
            </div>

            <div class="preview-sections">
              <div class="preview-section">
                <h4>在售商品</h4>
                <p>系统将自动展示当前店铺已上架且已审核通过的商品。</p>
              </div>
              <div class="preview-section">
                <h4>认养项目</h4>
                <p>系统将自动展示当前店铺已上架且已审核通过的认养项目。</p>
              </div>
            </div>
          </div>
        </a-card>
      </div>
    </a-spin>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { getMyShopInfo, updateShopInfo } from '@/api/shop'
import ImageUpload from '@/components/Upload/ImageUpload.vue'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const saving = ref(false)

const shopForm = reactive({
  id: undefined,
  shopName: '',
  shopAvatar: '',
  shopBanner: '',
  description: '',
  announcement: ''
})

const rules = {
  shopName: [
    { required: true, message: '请输入店铺名称', trigger: 'blur' },
    { max: 50, message: '店铺名称长度不能超过 50 个字符', trigger: 'blur' }
  ],
  description: [
    { max: 500, message: '店铺简介长度不能超过 500 个字符', trigger: 'blur' }
  ],
  announcement: [
    { max: 200, message: '店铺公告长度不能超过 200 个字符', trigger: 'blur' }
  ]
}

const previewInitial = computed(() => {
  const name = (shopForm.shopName || '店').trim()
  return name ? name.charAt(0) : '店'
})

const shopDescription = computed(() => {
  const description = (shopForm.description || '').trim()
  return description || '店主还没有填写店铺简介，欢迎稍后再来看看。'
})

const previewBannerStyle = computed(() => {
  if (shopForm.shopBanner) {
    return {
      backgroundImage: `url(${shopForm.shopBanner})`
    }
  }
  return {}
})

const fetchShopInfo = async () => {
  loading.value = true
  try {
    const res = await getMyShopInfo()
    if (res.code === '0' && res.data) {
      Object.assign(shopForm, {
        id: res.data.id,
        shopName: res.data.shopName || '',
        shopAvatar: res.data.shopAvatar || '',
        shopBanner: res.data.shopBanner || '',
        description: res.data.description || '',
        announcement: res.data.announcement || ''
      })
      return
    }

    message.error(res.message || '加载店铺信息失败')
  } catch (error) {
    console.error('加载店铺信息失败', error)
    message.error('加载店铺信息失败')
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
  } catch (error) {
    return
  }

  saving.value = true
  try {
    const payload = {
      id: shopForm.id,
      shopName: shopForm.shopName.trim(),
      shopAvatar: shopForm.shopAvatar || '',
      shopBanner: shopForm.shopBanner || '',
      description: shopForm.description?.trim() || '',
      announcement: shopForm.announcement?.trim() || ''
    }
    const res = await updateShopInfo(payload)
    if (res.code === '0') {
      message.success('店铺信息已保存')
      return
    }

    message.error(res.message || '保存店铺信息失败')
  } catch (error) {
    console.error('保存店铺信息失败', error)
    message.error('保存店铺信息失败')
  } finally {
    saving.value = false
  }
}

const openShopHome = () => {
  if (!shopForm.id) {
    return
  }
  const route = router.resolve({
    name: 'shopHome',
    params: { shopId: shopForm.id }
  })
  window.open(route.href, '_blank')
}

onMounted(() => {
  fetchShopInfo()
})
</script>

<style scoped>
.shop-manage-page {
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.page-title {
  margin: 0 0 8px;
  font-size: 28px;
  font-weight: 700;
}

.page-desc {
  margin: 0;
  color: #8c8c8c;
}

.page-alert {
  margin-bottom: 16px;
}

.content-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(360px, 0.9fr);
  gap: 16px;
}

.form-card,
.preview-card {
  border-radius: 16px;
}

.shop-preview {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.preview-hero {
  position: relative;
  min-height: 240px;
  border-radius: 20px;
  overflow: hidden;
  background:
    radial-gradient(circle at top left, rgba(255, 255, 255, 0.18), transparent 30%),
    linear-gradient(135deg, #23643b 0%, #4ea05d 100%);
  background-size: cover;
  background-position: center;
}

.preview-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(17, 24, 39, 0.08) 0%, rgba(17, 24, 39, 0.45) 100%);
}

.preview-content {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: flex-end;
  gap: 16px;
  padding: 24px;
  min-height: 240px;
  color: #fff;
}

.preview-avatar {
  width: 88px;
  height: 88px;
  flex-shrink: 0;
  border-radius: 24px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.2);
  border: 2px solid rgba(255, 255, 255, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  font-weight: 700;
}

.preview-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-meta h3 {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
}

.preview-meta p {
  margin: 12px 0 0;
  max-width: 520px;
  color: rgba(255, 255, 255, 0.88);
  line-height: 1.7;
}

.preview-notice,
.preview-empty,
.preview-section {
  border-radius: 16px;
  background: #f8faf8;
  border: 1px solid #edf3ed;
}

.preview-notice {
  padding: 18px 20px;
}

.preview-notice__label {
  font-size: 12px;
  font-weight: 700;
  color: #2f8b49;
  letter-spacing: 1px;
}

.preview-notice__text {
  margin-top: 8px;
  color: #1f2937;
  line-height: 1.7;
}

.preview-empty {
  padding: 18px 20px;
  color: #8c8c8c;
}

.preview-sections {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.preview-section {
  padding: 18px 20px;
}

.preview-section h4 {
  margin: 0 0 8px;
  font-size: 16px;
}

.preview-section p {
  margin: 0;
  color: #6b7280;
  line-height: 1.7;
}

@media (max-width: 1100px) {
  .content-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: stretch;
  }

  .preview-content {
    flex-direction: column;
    align-items: flex-start;
  }

  .preview-sections {
    grid-template-columns: 1fr;
  }
}
</style>
