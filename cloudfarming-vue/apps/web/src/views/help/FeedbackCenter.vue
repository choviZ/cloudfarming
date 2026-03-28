<template>
  <div class="feedback-page">
    <section class="hero-card">
      <div>
        <span class="hero-badge">帮助中心</span>
        <h1 class="hero-title">意见反馈</h1>
        <p class="hero-desc">
          欢迎提交商品、认养项目、订单、投诉或功能建议。平台会尽快查看并处理你的反馈。
        </p>
      </div>
      <div class="hero-side">
        <div class="hero-side__item">
          <span class="hero-side__label">当前身份</span>
          <strong>{{ submitterRoleLabel }}</strong>
        </div>
        <div class="hero-side__item">
          <span class="hero-side__label">处理时效</span>
          <strong>1-3 个工作日</strong>
        </div>
      </div>
    </section>

    <section class="content-grid">
      <a-card class="feedback-card" :bordered="false">
        <template #title>提交反馈</template>
        <a-alert
          class="feedback-alert"
          type="info"
          show-icon
          message="提交后平台管理员会进行处理"
          description="请尽量写清问题场景、时间、商品或订单信息，便于我们更快定位问题。"
        />
        <a-form ref="formRef" layout="vertical" :model="formState" :rules="rules">
          <a-form-item label="问题类型" name="feedbackType">
            <a-select
              v-model:value="formState.feedbackType"
              placeholder="请选择问题类型"
              :options="typeOptions"
            />
          </a-form-item>
          <a-form-item label="联系电话" name="contactPhone">
            <a-input
              v-model:value="formState.contactPhone"
              placeholder="请输入 11 位手机号"
              :maxlength="11"
            />
          </a-form-item>
          <a-form-item label="具体反馈内容" name="content">
            <a-textarea
              v-model:value="formState.content"
              :rows="8"
              :maxlength="1000"
              show-count
              placeholder="请描述你遇到的问题、出现时间，以及希望平台如何协助处理。"
            />
          </a-form-item>
          <div class="form-actions">
            <a-button @click="handleReset">重置</a-button>
            <a-button type="primary" :loading="submitting" @click="handleSubmit">提交反馈</a-button>
          </div>
        </a-form>
      </a-card>

      <div class="side-stack">
        <a-card class="tips-card" :bordered="false" title="填写建议">
          <div class="tip-item">
            <strong>描述尽量具体</strong>
            <p>例如写明商品名称、认养项目、订单号、出现时间和异常表现。</p>
          </div>
          <div class="tip-item">
            <strong>投诉问题优先选择“投诉举报”</strong>
            <p>平台会重点关注涉及商家服务、履约、虚假信息等问题。</p>
          </div>
          <div class="tip-item">
            <strong>联系电话保持可联系</strong>
            <p>如有必要，平台会通过该手机号与你进一步确认细节。</p>
          </div>
        </a-card>

        <a-card class="tips-card" :bordered="false" title="常见类型">
          <div class="type-list">
            <span v-for="item in typeOptions" :key="item.value" class="type-chip">
              {{ item.label }}
            </span>
          </div>
        </a-card>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { getFeedbackTypes, submitFeedback } from '@/api/feedback'
import { useUserStore } from '@/stores/useUserStore'

const fallbackTypeOptions = [
  { value: 'PRODUCT', label: '商品问题' },
  { value: 'ADOPT', label: '认养项目问题' },
  { value: 'ORDER', label: '订单/支付' },
  { value: 'LOGISTICS', label: '物流/售后' },
  { value: 'SHOP_SERVICE', label: '店铺/服务' },
  { value: 'ACCOUNT', label: '账号/登录' },
  { value: 'COMPLAINT', label: '投诉举报' },
  { value: 'SUGGESTION', label: '功能建议' },
  { value: 'OTHER', label: '其他' }
]

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const submitting = ref(false)
const typeOptions = ref([...fallbackTypeOptions])

const formState = reactive({
  feedbackType: undefined,
  contactPhone: '',
  content: ''
})

const rules = {
  feedbackType: [{ required: true, message: '请选择问题类型', trigger: 'change' }],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的 11 位手机号', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入具体反馈内容', trigger: 'blur' },
    { min: 10, message: '反馈内容至少 10 个字，方便平台判断问题', trigger: 'blur' }
  ]
}

const submitterRoleLabel = computed(() => {
  if (userStore.loginUser?.userType === 1) {
    return '农户'
  }
  return '普通用户'
})

const ensureLogin = async () => {
  if (!userStore.loginUser) {
    await userStore.fetchUser()
  }
  if (!userStore.loginUser) {
    message.warning('请先登录后再提交反馈')
    await router.push('/user/login')
    return false
  }
  formState.contactPhone = userStore.loginUser.phone || ''
  return true
}

const loadFeedbackTypes = async () => {
  try {
    const response = await getFeedbackTypes()
    if (response.code === '0' && Array.isArray(response.data) && response.data.length) {
      typeOptions.value = response.data.map((item) => ({
        value: item.code,
        label: item.name
      }))
    }
  } catch (error) {
    console.log(error)
  }
}

const handleReset = () => {
  formState.feedbackType = undefined
  formState.contactPhone = userStore.loginUser?.phone || ''
  formState.content = ''
  formRef.value?.clearValidate?.()
}

const handleSubmit = async () => {
  const ready = await ensureLogin()
  if (!ready) {
    return
  }

  try {
    await formRef.value?.validate()
  } catch (error) {
    return
  }

  submitting.value = true
  try {
    const response = await submitFeedback({
      feedbackType: formState.feedbackType,
      contactPhone: formState.contactPhone,
      content: formState.content
    })
    if (response.code === '0') {
      message.success('反馈已提交，我们会尽快处理')
      handleReset()
      return
    }
    message.error(response.message || '提交反馈失败')
  } catch (error) {
    message.error('提交反馈失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  const ready = await ensureLogin()
  if (!ready) {
    return
  }
  await loadFeedbackTypes()
})
</script>

<style scoped>
.feedback-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding: 12px 0 36px;
}

.hero-card {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  padding: 32px;
  border-radius: 28px;
  background:
    radial-gradient(circle at top right, rgba(125, 211, 155, 0.22), transparent 32%),
    linear-gradient(135deg, #163b2b 0%, #214d35 46%, #2d6a45 100%);
  color: #f6fff8;
  box-shadow: 0 22px 48px rgba(19, 52, 34, 0.18);
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.14);
  font-size: 12px;
  letter-spacing: 0.08em;
}

.hero-title {
  margin: 16px 0 10px;
  font-size: 34px;
  line-height: 1.2;
  color: #ffffff;
}

.hero-desc {
  max-width: 720px;
  margin: 0;
  color: rgba(246, 255, 248, 0.82);
  font-size: 15px;
  line-height: 1.8;
}

.hero-side {
  display: grid;
  gap: 14px;
  min-width: 240px;
}

.hero-side__item {
  padding: 18px 20px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
}

.hero-side__label {
  display: block;
  margin-bottom: 6px;
  color: rgba(246, 255, 248, 0.72);
  font-size: 13px;
}

.content-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) minmax(300px, 0.8fr);
  gap: 24px;
}

.feedback-card,
.tips-card {
  border-radius: 24px;
  box-shadow: 0 18px 40px rgba(18, 42, 30, 0.08);
}

.feedback-alert {
  margin-bottom: 20px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.side-stack {
  display: grid;
  gap: 24px;
}

.tip-item + .tip-item {
  margin-top: 18px;
  padding-top: 18px;
  border-top: 1px solid #eef3ef;
}

.tip-item strong {
  color: #163b2b;
  font-size: 15px;
}

.tip-item p {
  margin: 8px 0 0;
  color: #667a6f;
  line-height: 1.7;
}

.type-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.type-chip {
  display: inline-flex;
  align-items: center;
  padding: 8px 14px;
  border-radius: 999px;
  background: #f2f8f3;
  color: #28533b;
  font-size: 13px;
}

@media (max-width: 960px) {
  .hero-card,
  .content-grid {
    grid-template-columns: 1fr;
  }

  .hero-card {
    flex-direction: column;
  }

  .hero-side {
    min-width: 0;
    grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  }
}

@media (max-width: 640px) {
  .feedback-page {
    gap: 18px;
  }

  .hero-card {
    padding: 24px 20px;
    border-radius: 24px;
  }

  .hero-title {
    font-size: 28px;
  }
}
</style>
