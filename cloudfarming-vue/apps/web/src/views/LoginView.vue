<template>
  <div class="auth-page">
    <header class="auth-header">
      <a-button type="text" class="home-link" @click="router.push('/')">
        返回首页
      </a-button>
    </header>

    <main class="auth-main">
      <section class="auth-intro" aria-label="平台介绍">
        <button type="button" class="brand-link" @click="router.push('/')">
          <span class="brand-mark">
            <HomeOutlined />
          </span>
          <span>
            <strong>云养殖助农平台</strong>
            <small>优质农产品与认养项目</small>
          </span>
        </button>

        <p class="intro-kicker">Cloud Farming</p>
        <h1>连接可信农户，安心认养与选购</h1>
        <div class="intro-grid">
          <div class="intro-metric">
            <ShoppingOutlined />
            <span>农产品选购</span>
          </div>
          <div class="intro-metric">
            <FieldTimeOutlined />
            <span>认养进度跟踪</span>
          </div>
          <div class="intro-metric">
            <ShopOutlined />
            <span>农户经营协同</span>
          </div>
        </div>
      </section>

      <section class="auth-panel">
        <div class="panel-head">
          <a-segmented
            v-model:value="authMode"
            class="mode-switch"
            :options="modeOptions"
            block
            @change="handleModeChange"
          />
          <h2>{{ isRegister ? '创建账号' : '欢迎回来' }}</h2>
          <p>{{ isRegister ? '填写基础信息后即可开始使用平台服务' : '登录后继续管理你的订单与认养记录' }}</p>
        </div>

        <a-form v-if="!isRegister" ref="formRef" :model="formState" :rules="loginRules" layout="vertical" class="auth-form" @finish="handleLogin">
          <a-form-item name="username" label="用户名">
            <a-input v-model:value="formState.username" placeholder="请输入用户名" size="large" allow-clear>
              <template #prefix><UserOutlined /></template>
            </a-input>
          </a-form-item>

          <a-form-item name="password" label="密码">
            <a-input-password v-model:value="formState.password" placeholder="请输入密码" size="large">
              <template #prefix><LockOutlined /></template>
            </a-input-password>
          </a-form-item>

          <a-button type="primary" html-type="submit" size="large" block :loading="loading" class="submit-button">
            登录
          </a-button>

          <div class="form-footer">
            <button type="button" @click="toggleForm('register')">还没有账号？立即注册</button>
            <button type="button" disabled>忘记密码</button>
          </div>
        </a-form>

        <a-form v-else ref="registerFormRef" :model="registerState" :rules="registerRules" layout="vertical" class="auth-form" @finish="handleRegister">
          <a-form-item name="username" label="用户名">
            <a-input v-model:value="registerState.username" placeholder="6-18 位中文、英文、数字或下划线" size="large" allow-clear>
              <template #prefix><UserOutlined /></template>
            </a-input>
          </a-form-item>

          <a-form-item name="password" label="密码">
            <a-input-password v-model:value="registerState.password" placeholder="6-20 位，需包含字母和数字" size="large">
              <template #prefix><LockOutlined /></template>
            </a-input-password>
          </a-form-item>

          <a-form-item name="checkPassword" label="确认密码">
            <a-input-password v-model:value="registerState.checkPassword" placeholder="请再次输入密码" size="large">
              <template #prefix><LockOutlined /></template>
            </a-input-password>
          </a-form-item>

          <a-button type="primary" html-type="submit" size="large" block :loading="loading" class="submit-button">
            注册
          </a-button>

          <div class="form-footer single">
            <button type="button" @click="toggleForm('login')">已有账号？返回登录</button>
          </div>
        </a-form>
      </section>
    </main>
  </div>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { FieldTimeOutlined, HomeOutlined, LockOutlined, SafetyCertificateOutlined, ShopOutlined, ShoppingOutlined, UserOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { userLogin, userRegister } from '../api/user.js'
import { useUserStore } from '@/stores/useUserStore.js'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref()
const registerFormRef = ref()
const loading = ref(false)
const authMode = ref('login')
const modeOptions = [{ label: '登录', value: 'login' }, { label: '注册', value: 'register' }]
const formState = reactive({ username: '', password: '' })
const registerState = reactive({ username: '', password: '', checkPassword: '' })
const isRegister = computed(() => authMode.value === 'register')

const loginRules = reactive({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }, { min: 6, max: 18, message: '用户名长度需在 6-18 个字符之间', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, max: 20, message: '密码长度不能少于 6 个字符', trigger: 'blur' }]
})
const registerRules = reactive({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }, { pattern: /^[一-龥a-zA-Z0-9_]{6,18}$/, message: '用户名需 6-18 位，仅允许中文、英文、数字、下划线', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { pattern: /^(?=.*[a-zA-Z])(?=.*\d)[a-zA-Z\d!@#$%^&*()_+\-=[\]{};':"|\,.<>?]{6,20}$/, message: '密码需 6-20 位，且包含字母和数字', trigger: 'blur' }],
  checkPassword: [{ required: true, message: '请再次输入密码', trigger: 'blur' }, { validator: (_, value) => value === registerState.password ? Promise.resolve() : Promise.reject(new Error('两次输入密码不一致')), trigger: 'blur' }]
})

const handleLogin = async () => {
  loading.value = true
  try {
    const res = await userLogin({ username: formState.username, password: formState.password })
    if (res.code === '0' && res.data) {
      userStore.setUser(res.data)
      await router.push('/')
      return
    }
    message.error(res.message || '登录失败')
  } finally {
    loading.value = false
  }
}
const handleRegister = async () => {
  loading.value = true
  try {
    const response = await userRegister({ username: registerState.username, password: registerState.password, checkPassword: registerState.checkPassword })
    if (response.code === '0' && response.data) {
      message.success('注册成功，请登录')
      toggleForm('login')
      return
    }
    message.error(response.message || '注册失败')
  } finally {
    loading.value = false
  }
}
const toggleForm = (type) => {
  authMode.value = type
  if (route.path !== `/user/${type}`) router.push(`/user/${type}`)
}
const handleModeChange = (value) => toggleForm(value)
watch(() => route.path, (newPath) => { authMode.value = newPath === '/user/register' ? 'register' : 'login' }, { immediate: true })
</script>

<style scoped>
.auth-page { min-height: 100vh; background: radial-gradient(circle at 10% 12%, rgba(47, 139, 73, 0.1), transparent 28%), radial-gradient(circle at 90% 82%, rgba(217, 119, 6, 0.08), transparent 30%), #f8fbf7; color: #17212b; }
.auth-header { height: 82px; max-width: 1220px; margin: 0 auto; padding: 0 24px; display: flex; align-items: center; justify-content: flex-end; }
.brand-link { border: none; background: transparent; padding: 0; display: inline-flex; align-items: center; gap: 12px; color: #17212b; cursor: pointer; text-align: left; }
.brand-mark { width: 44px; height: 44px; border-radius: 14px; background: #ffffff; color: #2f8b49; display: inline-flex; align-items: center; justify-content: center; box-shadow: 0 10px 24px rgba(31, 109, 61, 0.1); }
.brand-link strong, .brand-link small { display: block; }
.brand-link strong { font-size: 22px; font-weight: 800; line-height: 1.1; }
.brand-link small { margin-top: 4px; color: #2f7d46; font-size: 13px; }
.home-link { color: #355241; font-weight: 600; }
.auth-main { width: min(1120px, calc(100% - 48px)); min-height: calc(100vh - 124px); margin: 0 auto; padding: 36px 0 64px; display: grid; grid-template-columns: minmax(0, 1fr) 430px; align-items: center; gap: 56px; }
.auth-intro { max-width: 560px; }
.auth-intro .brand-link { margin-bottom: 42px; }
.intro-kicker { margin: 0 0 14px; color: #2f8b49; font-size: 13px; font-weight: 800; letter-spacing: 2px; text-transform: uppercase; }
.auth-intro h1 { margin: 0; color: #17212b; font-size: 48px; line-height: 1.14; font-weight: 800; }
.intro-desc { margin: 18px 0 0; color: #5f6d64; font-size: 16px; line-height: 1.8; }
.intro-card { margin-top: 34px; padding: 18px; border: 1px solid #e4eee6; border-radius: 18px; background: rgba(255, 255, 255, 0.88); box-shadow: 0 18px 48px rgba(33, 53, 39, 0.08); display: flex; align-items: flex-start; gap: 14px; }
.intro-card-icon { width: 44px; height: 44px; border-radius: 14px; background: #eef8ef; color: #2f8b49; display: flex; align-items: center; justify-content: center; flex-shrink: 0; font-size: 20px; }
.intro-card strong, .intro-card span { display: block; }
.intro-card strong { color: #17212b; font-size: 16px; line-height: 1.5; }
.intro-card span { margin-top: 4px; color: #64746a; font-size: 14px; line-height: 1.6; }
.intro-grid { margin-top: 18px; display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 12px; }
.intro-metric { min-height: 88px; padding: 16px; border: 1px solid #e8efe9; border-radius: 16px; background: rgba(255, 255, 255, 0.72); color: #355241; display: flex; flex-direction: column; justify-content: space-between; gap: 12px; }
.intro-metric :deep(.anticon) { color: #d97706; font-size: 20px; }
.intro-metric span { font-size: 13px; font-weight: 700; }
.auth-panel { padding: 30px; border-radius: 22px; border: 1px solid #e3ebe5; background: #ffffff; box-shadow: 0 24px 70px rgba(33, 53, 39, 0.13); }
.panel-head { margin-bottom: 26px; }
.mode-switch { width: 100%; margin-bottom: 24px; padding: 4px; border-radius: 14px; background: #f3f7f4; }
.panel-head h2 { margin: 0; color: #17212b; font-size: 28px; font-weight: 800; }
.panel-head p { margin: 8px 0 0; color: #6b7a70; font-size: 14px; }
.auth-form :deep(.ant-form-item) { margin-bottom: 20px; }
.auth-form :deep(.ant-form-item-label > label) { color: #355241; font-weight: 700; }
.auth-form :deep(.ant-input-affix-wrapper) { height: 46px; border-radius: 14px; border-color: #dfe8e2; background: #fbfdfb; }
.auth-form :deep(.ant-input-affix-wrapper-focused), .auth-form :deep(.ant-input-affix-wrapper:focus-within) { border-color: #2f8b49; box-shadow: 0 0 0 3px rgba(47, 139, 73, 0.1); }
.auth-form :deep(.ant-input-prefix) { margin-right: 10px; color: #8aa08f; }
.submit-button.ant-btn-primary {
  height: 46px;
  margin-top: 4px;
  border-radius: 14px;
  background: linear-gradient(135deg, #2f8b49 0%, #246b38 100%);
  border-color: #2f8b49;
  color: #ffffff;
  font-size: 16px;
  font-weight: 800;
  box-shadow: 0 12px 28px rgba(47, 139, 73, 0.2);
}

.submit-button.ant-btn-primary:hover,
.submit-button.ant-btn-primary:focus {
  background: linear-gradient(135deg, #359a53 0%, #2b7a41 100%);
  border-color: #359a53;
  color: #ffffff;
}

.submit-button.ant-btn-primary:active {
  background: #246b38;
  border-color: #246b38;
}
.form-footer { margin-top: 18px; display: flex; align-items: center; justify-content: space-between; gap: 14px; }
.form-footer.single { justify-content: center; }
.form-footer button { border: none; background: transparent; padding: 0; color: #2f8b49; font-size: 14px; font-weight: 700; cursor: pointer; }
.form-footer button:disabled { color: #9aa8a0; cursor: not-allowed; }
@media (max-width: 900px) { .auth-main { grid-template-columns: 1fr; gap: 28px; } .auth-intro { max-width: none; } .auth-intro h1 { font-size: 36px; } }
@media (max-width: 640px) { .auth-header { height: auto; padding: 18px; } .brand-link small, .home-link { display: none; } .auth-main { width: calc(100% - 32px); padding: 18px 0 40px; } .intro-grid { grid-template-columns: 1fr; } .auth-panel { padding: 22px; border-radius: 18px; } .auth-intro h1 { font-size: 30px; } .form-footer { flex-direction: column; align-items: flex-start; } }
</style>
