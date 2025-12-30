<template>
  <div class="login-container">
    <!-- 顶部Logo -->
    <div class="login-header">
      <a href="/" class="logo-text">云农场</a>
    </div>
    <!-- 登录主体区域 -->
    <div class="login-main">
      <!-- 左侧图片区域 -->
      <div class="login-banner">
        <div class="banner-content">
          <div class="banner-title">安全登录云农场</div>
          <div class="banner-subtitle">享受优质农产品和农资服务</div>
        </div>
      </div>
      <!-- 右侧表单区域 -->
      <div class="login-form-wrapper">
        <div class="login-form-container">
          <!-- 登录表单 -->
          <div v-if="!isRegister" class="form-container">
            <div class="form-title">密码登录</div>
            <a-form :model="formState" :rules="loginRules" ref="formRef" layout="vertical">
              <a-form-item name="username" label="用户名">
                <a-input
                  v-model:value="formState.username"
                  placeholder="请输入用户名"
                  size="large"
                />
              </a-form-item>
              <a-form-item name="password" label="密码">
                <a-input-password
                  v-model:value="formState.password"
                  placeholder="请输入密码"
                  size="large"
                />
              </a-form-item>
              <a-form-item>
                <a-button
                  type="primary"
                  html-type="submit"
                  size="large"
                  block
                  @click="handleLogin"
                >
                  登录
                </a-button>
              </a-form-item>
              <div class="form-footer">
                <a href="#" @click.prevent="toggleForm('register')">立即注册</a>
                <a href="#">忘记密码</a>
              </div>
            </a-form>
          </div>
          <!-- 注册表单 -->
          <div v-else class="form-container">
            <!-- 标题和返回按钮容器 -->
            <div class="form-header">
              <!-- 注册页面返回按钮 -->
              <a-button type="text" @click="toggleForm('login')" size="small" class="back-button">
                <template #icon>
                  <LeftOutlined />
                </template>
              </a-button>
              <div class="form-title">立即注册</div>
            </div>
            <a-form :model="registerState" :rules="registerRules" ref="registerFormRef" layout="vertical">
              <a-form-item name="username" label="用户名">
                <a-input
                  v-model:value="registerState.username"
                  placeholder="请输入用户名"
                  size="large"
                />
              </a-form-item>
              <a-form-item name="password" label="密码">
                <a-input-password
                  v-model:value="registerState.password"
                  placeholder="请输入密码"
                  size="large"
                />
              </a-form-item>
              <a-form-item name="checkPassword" label="确认密码">
                <a-input-password
                  v-model:value="registerState.checkPassword"
                  placeholder="请再次输入密码"
                  size="large"
                />
              </a-form-item>
              <a-form-item>
                <a-button
                  type="primary"
                  html-type="submit"
                  size="large"
                  block
                  @click="handleRegister"
                  :loading="loading"
                >
                  注册
                </a-button>
              </a-form-item>
            </a-form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import type { FormInstance } from 'ant-design-vue'
import { LeftOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { userLogin, userRegister } from '../api/user'
import { useUserStore } from '@/stores/useUserStore.ts'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 表单引用
const formRef = ref<FormInstance>()
const registerFormRef = ref<FormInstance>()

// 加载状态
const loading = ref(false)

// 登录表单状态
const formState = reactive({
  username: '',
  password: '',
  agreement: false
})

// 注册表单状态
const registerState = reactive({
  username: '',
  password: '',
  checkPassword: '',
  registerAgreement: false
})

// 表单切换状态
const isRegister = ref(false)

// 登录表单验证规则
const loginRules = reactive({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 6, max: 18, message: '用户名长度在6-18个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度不能少于6个字符', trigger: 'blur' }
  ]
})

// 注册表单验证规则
const registerRules = reactive({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    {
      pattern: /^[\u4e00-\u9fa5a-zA-Z0-9_]{6,18}$/,
      message: '用户名需为6-18位，仅允许中文、英文、数字、下划线',
      trigger: 'blur'
    }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    {
      pattern: /^(?=.*[a-zA-Z])(?=.*\d)[a-zA-Z\d!@#$%^&*()_+\-=\[\]{};':"|\\,.<>?]{6,20}$/,
      message: '密码必须为6-20位，且包含字母和数字',
      trigger: 'blur'
    }
  ],
  checkPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      pattern: /^(?=.*[a-zA-Z])(?=.*\d)[a-zA-Z\d!@#$%^&*()_+\-=\[\]{};':"|\\,.<>?]{6,20}$/,
      message: '密码必须为6-20位，且包含字母和数字',
      trigger: 'blur'
    },
    {
      validator: (_: any, value: string) =>
        value === registerState.password ? Promise.resolve() : Promise.reject(new Error('两次输入密码不一致')),
      trigger: 'blur'
    }
  ]
})

// 登录处理
const handleLogin = async () => {
  if (!formRef.value) return
  loading.value = true
  try {
    await formRef.value.validate()
    const res = await userLogin({
      username: formState.username,
      password: formState.password
    })
    if (res.code == '0' && res.data) {
      userStore.setUser(res.data)
      await router.push('/')
    } else {
      message.error(res.message || '登录失败')
    }
  } finally {
    loading.value = false
  }
}

// 注册处理
const handleRegister = async () => {
  if (!registerFormRef.value) return

  loading.value = true
  try {
    await registerFormRef.value.validate()
    // 调用注册API
    const response = await userRegister({
      username: registerState.username,
      password: registerState.password,
      checkPassword: registerState.checkPassword
    })
    // 处理注册成功
    if (response.code == '0' && response.data) {
      message.success('注册成功')
      // 注册成功后跳转到登录页
      await router.push('/user/login')
    } else {
      console.log(response.code)
      console.log(response.data)

      message.error(response.message || '注册失败')
    }
  } finally {
    loading.value = false
  }
}

// 表单切换函数
const toggleForm = (type: 'login' | 'register') => {
  isRegister.value = type === 'register'
  // 如果是从路由进入，不需要再改变路由
  if (route.path !== `/user/${type}`) {
    router.push(`/user/${type}`)
  }
}

// 监听路由变化，自动切换表单
watch(
  () => route.path,
  (newPath) => {
    isRegister.value = newPath === '/user/register'
  },
  { immediate: true }
)
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f5f5;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
}

.login-header {
  height: 80px;
  background-color: #fff;
  display: flex;
  align-items: center;
  padding: 0 120px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.09);
  position: sticky;
  top: 0;
  z-index: 100;
}

.logo-text {
  font-size: 32px;
  font-weight: bold;
  color: #52c41a;
  text-decoration: none;
  font-family: 'Arial', sans-serif;
  transition: all 0.3s ease;
}

.logo-text:hover {
  color: #73d13d;
  transform: scale(1.05);
}

.login-main {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px 0;
  background-color: #f0f2f5;
}

.login-banner {
  width: 500px;
  height: 450px;
  background-color: #52c41a;
  border-radius: 8px 0 0 8px;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #fff;
  background-image: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
  overflow: hidden;
  position: relative;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.login-banner::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><circle cx="20" cy="20" r="2" fill="rgba(255,255,255,0.2)"/><circle cx="80" cy="40" r="2" fill="rgba(255,255,255,0.2)"/><circle cx="60" cy="80" r="2" fill="rgba(255,255,255,0.2)"/><circle cx="40" cy="60" r="2" fill="rgba(255,255,255,0.2)"/><circle cx="80" cy="20" r="2" fill="rgba(255,255,255,0.2)"/></svg>');
  background-repeat: repeat;
  background-size: 100px;
  animation: float 15s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(1deg);
  }
}

.banner-content {
  text-align: center;
  z-index: 1;
  padding: 40px;
}

.banner-title {
  font-size: 36px;
  font-weight: bold;
  margin-bottom: 20px;
  line-height: 1.3;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.banner-subtitle {
  font-size: 18px;
  opacity: 0.9;
  line-height: 1.6;
}

.login-form-wrapper {
  width: 400px;
  height: 450px;
  background-color: #fff;
  border-radius: 0 8px 8px 0;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 4px 0 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.login-form-container {
  width: 85%;
  max-width: 320px;
}

.register-back .ant-btn-link {
  color: #52c41a;
  font-size: 14px;
}

.register-back .ant-btn-link:hover {
  color: #73d13d;
  background-color: transparent;
}

.form-header {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 32px;
}

.back-button {
  position: absolute;
  left: 0;
  font-size: 14px;
}

.back-button:hover {
  color: #73d13d;
}

.form-title {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 0;
  text-align: center;
  color: #333;
  letter-spacing: 1px;
}

/* 美化表单 */
.ant-form-item {
  margin-bottom: 24px;
}

.ant-form-item-label > label {
  font-weight: 500;
  color: #555;
  font-size: 14px;
}

.ant-input,
.ant-input-password {
  border-radius: 4px;
  border: 1px solid #d9d9d9;
  transition: all 0.3s ease;
  font-size: 14px;
  height: 40px;
}

.ant-input:focus,
.ant-input-password:focus-within {
  border-color: #52c41a;
  box-shadow: 0 0 0 2px rgba(82, 196, 26, 0.2);
}

.ant-btn-primary {
  background-color: #52c41a;
  border-color: #52c41a;
  height: 40px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 4px;
  transition: all 0.3s ease;
}

.ant-btn-primary:hover {
  background-color: #73d13d;
  border-color: #73d13d;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(82, 196, 26, 0.3);
}

.ant-btn-primary:active {
  transform: translateY(0);
}

.ant-checkbox-wrapper {
  font-size: 14px;
  color: #666;
}

.ant-checkbox-wrapper a {
  color: #52c41a;
  text-decoration: none;
}

.ant-checkbox-wrapper a:hover {
  color: #73d13d;
  text-decoration: underline;
}

.form-footer {
  display: flex;
  justify-content: space-between;
  margin-top: 20px;
}

.form-footer a {
  color: #52c41a;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.3s ease;
}

.form-footer a:hover {
  color: #73d13d;
  text-decoration: underline;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-header {
    padding: 0 20px;
  }

  .login-main {
    flex-direction: column;
    padding: 20px;
    background-color: #fff;
  }

  .login-banner {
    width: 100%;
    height: 200px;
    border-radius: 8px 8px 0 0;
    margin-bottom: -1px;
  }

  .login-form-wrapper {
    width: 100%;
    border-radius: 0 0 8px 8px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }

  .banner-title {
    font-size: 24px;
  }

  .banner-subtitle {
    font-size: 14px;
  }

  .form-title {
    font-size: 20px;
    margin-bottom: 24px;
  }
}
</style>