<template>
  <div class="showcase-manage-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">展示资料</h2>
        <p class="page-desc">维护优质农户展示页中的环境图片，基础审核资料仍以入驻申请信息为准。</p>
      </div>
      <a-button type="primary" :loading="saving" @click="handleSubmit">
        保存展示资料
      </a-button>
    </div>

    <a-alert
      class="page-alert"
      type="info"
      show-icon
      message="管理端仅可对审核通过且资料完整的农户设为精选。请先补充环境图片，再联系平台审核精选展示。"
    />

    <a-spin :spinning="loading">
      <div class="content-grid">
        <a-card class="base-card" :bordered="false" title="基础资料">
          <div class="base-info">
            <div class="info-item">
              <span class="info-label">农场名称</span>
              <span class="info-value">{{ formState.farmName || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">养殖品类</span>
              <span class="info-value">{{ formState.breedingType || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">农场面积</span>
              <span class="info-value">{{ formState.farmArea ? `${formState.farmArea} 亩` : '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">农场地址</span>
              <span class="info-value">{{ formState.farmAddress || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">审核状态</span>
              <a-tag :color="reviewStatusColor">{{ reviewStatusText }}</a-tag>
            </div>
            <div class="info-item">
              <span class="info-label">精选状态</span>
              <a-tag :color="formState.featuredFlag === 1 ? 'gold' : 'default'">
                {{ formState.featuredFlag === 1 ? '已精选' : '未精选' }}
              </a-tag>
            </div>
          </div>
        </a-card>

        <a-card class="license-card" :bordered="false" title="营业执照">
          <div v-if="formState.businessLicensePic" class="license-preview">
            <a-image :src="formState.businessLicensePic" alt="营业执照" />
          </div>
          <a-empty v-else description="暂无营业执照图片" />
        </a-card>
      </div>

      <a-card class="gallery-card" :bordered="false" title="环境图片">
        <p class="gallery-desc">最多上传 6 张环境图片，建议展示养殖环境、清洁情况、活动区域等真实场景。</p>
        <MultiImageUpload
          v-model:value="formState.environmentImages"
          biz-code="FARMER_ENVIRONMENT"
          :max-count="6"
        />
      </a-card>
    </a-spin>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import MultiImageUpload from '@/components/Upload/MultiImageUpload.vue'
import { getMyFarmerShowcase, updateMyFarmerShowcase } from '@/api/farmer'

const loading = ref(false)
const saving = ref(false)

const formState = reactive({
  id: undefined,
  farmName: '',
  breedingType: '',
  farmArea: undefined,
  farmAddress: '',
  businessLicensePic: '',
  environmentImages: [],
  reviewStatus: undefined,
  featuredFlag: 0
})

const reviewStatusText = computed(() => {
  const map = {
    0: '待审核',
    1: '已通过',
    2: '未通过'
  }
  return map[formState.reviewStatus] || '未知状态'
})

const reviewStatusColor = computed(() => {
  const map = {
    0: 'processing',
    1: 'success',
    2: 'error'
  }
  return map[formState.reviewStatus] || 'default'
})

const fetchMyShowcase = async () => {
  loading.value = true
  try {
    const res = await getMyFarmerShowcase()
    if (res.code === '0' && res.data) {
      Object.assign(formState, {
        ...res.data,
        environmentImages: res.data.environmentImages || []
      })
      return
    }
    message.error(res.message || '加载展示资料失败')
  } catch (error) {
    console.error('加载展示资料失败', error)
    message.error('加载展示资料失败')
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  saving.value = true
  try {
    const res = await updateMyFarmerShowcase({
      environmentImages: formState.environmentImages || []
    })
    if (res.code === '0') {
      message.success('展示资料已保存')
      return
    }
    message.error(res.message || '保存展示资料失败')
  } catch (error) {
    console.error('保存展示资料失败', error)
    message.error('保存展示资料失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  fetchMyShowcase()
})
</script>

<style scoped>
.showcase-manage-page {
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
  grid-template-columns: minmax(0, 1.3fr) minmax(320px, 0.7fr);
  gap: 16px;
  margin-bottom: 16px;
}

.base-card,
.license-card,
.gallery-card {
  border-radius: 16px;
}

.base-info {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 16px;
  border-radius: 14px;
  background: #f8faf8;
  border: 1px solid #edf3ed;
}

.info-label {
  font-size: 12px;
  color: #7b8b7f;
}

.info-value {
  color: #1f2937;
  font-weight: 600;
  word-break: break-word;
}

.license-preview {
  display: flex;
  justify-content: center;
}

.gallery-desc {
  margin-top: 0;
  color: #7b8b7f;
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

  .base-info {
    grid-template-columns: 1fr;
  }
}
</style>
