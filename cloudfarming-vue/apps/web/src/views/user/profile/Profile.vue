<template>
  <div class="profile-page">
    <div class="page-header">
      <h2 class="page-title">个人资料</h2>
      <p class="page-subtitle">维护账号基础信息，便于订单通知与配送联系。</p>
    </div>

    <a-spin :spinning="loading">
      <div class="profile-grid">
        <section class="profile-main">
          <a-card :bordered="false" class="profile-card">
            <a-form
              ref="formRef"
              layout="vertical"
              :model="formState"
              :rules="rules"
            >
              <a-form-item label="头像" name="avatar">
                <ImageUpload
                  v-model:value="formState.avatar"
                  biz-code="USER_AVATAR"
                  :max-size="3"
                />
                <p class="avatar-hint">支持 JPG、PNG、GIF、WEBP，建议上传 1:1 比例头像。</p>
              </a-form-item>

              <a-form-item label="用户名" name="username">
                <a-input
                  v-model:value="formState.username"
                  maxlength="30"
                  show-count
                  placeholder="请输入用户名"
                />
              </a-form-item>

              <a-form-item label="手机号" name="phone">
                <a-input
                  v-model:value="formState.phone"
                  maxlength="11"
                  placeholder="请输入手机号"
                />
              </a-form-item>

              <a-form-item label="新密码（选填）" name="password">
                <a-input-password
                  v-model:value="formState.password"
                  maxlength="20"
                  placeholder="不修改可留空，6-20位且包含字母和数字"
                />
              </a-form-item>

              <a-form-item label="确认新密码" name="confirmPassword">
                <a-input-password
                  v-model:value="formState.confirmPassword"
                  maxlength="20"
                  placeholder="请再次输入新密码"
                />
              </a-form-item>

              <a-space>
                <a-button type="primary" :loading="saving" @click="handleSubmit">
                  保存修改
                </a-button>
                <a-button @click="handleReset">重置</a-button>
              </a-space>
            </a-form>
          </a-card>
        </section>

        <aside class="profile-side">
          <a-card :bordered="false" class="profile-summary-card">
            <div class="avatar-wrap">
              <a-avatar :src="avatarPreview || undefined" :size="72">
                {{ avatarInitial }}
              </a-avatar>
            </div>
            <h3 class="summary-name">{{ formState.username || '-' }}</h3>
            <p class="summary-phone">{{ maskedPhone }}</p>

            <div class="summary-lines">
              <div class="summary-line">
                <span>账号类型</span>
                <strong>{{ accountTypeText }}</strong>
              </div>
              <div class="summary-line">
                <span>账号状态</span>
                <a-tag :color="statusTagColor">{{ statusText }}</a-tag>
              </div>
            </div>
          </a-card>
        </aside>
      </div>
    </a-spin>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { updateUser } from '@/api/user'
import { useUserStore } from '@/stores/useUserStore'
import ImageUpload from '@/components/Upload/ImageUpload.vue'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)
const saving = ref(false)
const passwordPattern = /^(?=.*[a-zA-Z])(?=.*\d)[a-zA-Z\d!@#$%^&*()_+\-=\[\]{};':"\\|,.<>?]{6,20}$/

const formState = reactive({
  id: undefined,
  username: '',
  phone: '',
  avatar: '',
  password: '',
  confirmPassword: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 30, message: '用户名长度需在 2-30 个字符之间', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  avatar: [],
  password: [
    {
      validator: async (_rule, value) => {
        if (!value) {
          return Promise.resolve()
        }
        if (passwordPattern.test(value)) {
          return Promise.resolve()
        }
        return Promise.reject(new Error('密码需为 6-20 位，且包含字母和数字'))
      },
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    {
      validator: async (_rule, value) => {
        if (!formState.password && !value) {
          return Promise.resolve()
        }
        if (!formState.password && value) {
          return Promise.reject(new Error('请先填写新密码'))
        }
        if (formState.password && !value) {
          return Promise.reject(new Error('请再次输入新密码'))
        }
        if (value !== formState.password) {
          return Promise.reject(new Error('两次输入的密码不一致'))
        }
        return Promise.resolve()
      },
      trigger: 'blur'
    }
  ]
}

const accountTypeText = computed(() => {
  const map = {
    0: '普通用户',
    1: '农户',
    2: '管理员'
  }
  return map[userStore.loginUser?.userType] || '未知'
})

const statusText = computed(() => {
  const map = {
    0: '正常',
    1: '禁用'
  }
  return map[userStore.loginUser?.status] || '未知'
})

const statusTagColor = computed(() => (userStore.loginUser?.status === 0 ? 'success' : 'default'))

const avatarPreview = computed(() => formState.avatar || userStore.loginUser?.avatar || '')

const avatarInitial = computed(() => {
  const source = formState.username || userStore.loginUser?.username || 'U'
  return source.slice(0, 1).toUpperCase()
})

const maskedPhone = computed(() => {
  const phone = formState.phone || userStore.loginUser?.phone || ''
  if (!phone || phone.length < 11) {
    return '未设置手机号'
  }
  return `${phone.slice(0, 3)}****${phone.slice(7)}`
})

const fillFormFromUser = (user) => {
  if (!user) {
    return
  }
  formState.id = user.id
  formState.username = user.username || ''
  formState.phone = user.phone || ''
  formState.avatar = user.avatar || ''
  formState.password = ''
  formState.confirmPassword = ''
}

const loadProfile = async () => {
  loading.value = true
  try {
    if (!userStore.loginUser?.id) {
      await userStore.fetchUser()
    }
    if (!userStore.loginUser?.id) {
      message.warning('请先登录')
      router.push('/user/login')
      return
    }
    fillFormFromUser(userStore.loginUser)
  } catch (error) {
    message.error('获取个人资料失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const buildUpdatePayload = () => {
  const payload = {
    id: formState.id,
    username: formState.username?.trim(),
    phone: formState.phone?.trim()
  }

  const avatar = formState.avatar?.trim()
  if (avatar) {
    payload.avatar = avatar
  }

  const password = formState.password?.trim()
  if (password) {
    payload.password = password
  }

  return payload
}

const handleSubmit = async () => {
  if (!formRef.value) {
    return
  }
  try {
    await formRef.value.validate()
  } catch (error) {
    return
  }

  if (!formState.id) {
    message.error('未获取到当前用户信息，请刷新后重试')
    return
  }

  saving.value = true
  try {
    const res = await updateUser(buildUpdatePayload())
    if (res.code === '0') {
      message.success('个人资料已更新')
      await userStore.fetchUser()
      fillFormFromUser(userStore.loginUser)
      return
    }
    message.error(res.message || '保存失败，请稍后重试')
  } catch (error) {
    message.error('保存失败，请稍后重试')
  } finally {
    saving.value = false
  }
}

const handleReset = () => {
  fillFormFromUser(userStore.loginUser)
}

watch(
  () => formState.password,
  () => {
    if (formState.confirmPassword && formRef.value) {
      formRef.value.validateFields(['confirmPassword']).catch(() => {})
    }
  }
)

onMounted(() => {
  loadProfile()
})
</script>

<style scoped>
.profile-page {
  background: #fff;
  min-height: 100%;
}

.page-header {
  margin-bottom: 18px;
}

.page-title {
  margin: 0;
  font-size: 30px;
  font-weight: 800;
  color: #111827;
}

.page-subtitle {
  margin: 10px 0 0;
  color: #4b5563;
  font-size: 14px;
}

.profile-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 16px;
}

.profile-card,
.profile-summary-card {
  border-radius: 14px;
  box-shadow: 0 6px 20px rgba(17, 24, 39, 0.06);
}

.avatar-wrap {
  display: flex;
  justify-content: center;
}

.summary-name {
  margin: 12px 0 0;
  font-size: 20px;
  font-weight: 700;
  color: #111827;
  text-align: center;
}

.summary-phone {
  margin: 8px 0 0;
  font-size: 13px;
  color: #6b7280;
  text-align: center;
}

.summary-lines {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #edf0f2;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.summary-line {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  font-size: 14px;
  color: #4b5563;
}

.summary-line strong {
  color: #111827;
  font-weight: 600;
}

.avatar-hint {
  margin: 8px 0 0;
  font-size: 12px;
  color: #6b7280;
}

@media (max-width: 1080px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }
}
</style>
