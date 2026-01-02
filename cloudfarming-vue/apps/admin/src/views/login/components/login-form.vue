<template>
  <div class="login-form-wrapper">
    <div class="login-form-title">用户登录</div>
    <div class="login-form-sub-title">欢迎回来</div>
    <div class="login-form-error-msg">{{ errorMessage }}</div>

    <a-form
      ref="loginForm"
      :model="userInfo"
      class="login-form"
      layout="vertical"
      @finish="handleSubmit"
    >
      <a-form-item
        name="username"
        :rules="[{ required: true, message: '用户名不能为空' }]"
      >
        <a-input
          v-model:value="userInfo.username"
          placeholder="请输入用户名"
          size="large"
        >
          <template #prefix>
            <UserOutlined />
          </template>
        </a-input>
      </a-form-item>

      <a-form-item
        name="password"
        :rules="[{ required: true, message: '密码不能为空' }]"
      >
        <a-input-password
          v-model:value="userInfo.password"
          placeholder="请输入密码"
          size="large"
        >
          <template #prefix>
            <LockOutlined />
          </template>
        </a-input-password>
      </a-form-item>

      <a-space :size="16" direction="vertical" style="width: 100%">
        <div class="login-form-password-actions">
          <a-checkbox v-model:checked="loginConfig.rememberPassword">
            记住密码
          </a-checkbox>
          <a href="javascript:;">忘记密码?</a>
        </div>
        <a-button type="primary" html-type="submit" long :loading="loading" size="large"
                  style="width: 100%">
          登录
        </a-button>
        <a-button type="link" long class="login-form-register-btn" style="width: 100%">
          注册账号
        </a-button>
      </a-space>
    </a-form>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue'
import { useStorage } from '@vueuse/core'
import { useUserStore } from '@/store'
import useLoading from '@/hooks/loading'
import { userLogin, type UserLoginReqDTO } from '@cloudfarming/core'

const router = useRouter()
const errorMessage = ref('')
const { loading, setLoading } = useLoading()
const userStore = useUserStore()

// localStorage配置
const loginConfig = useStorage('login-config', {
  rememberPassword: true,
  username: '',
  password: ''
})

// 用户信息
const userInfo = reactive<UserLoginReqDTO>({
  username: '',
  password: ''
})

// 提交登录请求
const handleSubmit = async (values: Record<string, any>) => {
  // 设置加载
  if (loading.value) return
  setLoading(true)
  // 发送请求
  try {
    const res = await userLogin(userInfo)
    if (res.code != '0') {
      message.error('登录失败' + res.message)
      return;
    }else if(res.data.userType != 2){
      message.error('当前账号不是管理员账号！')
      return;
    }
    userStore.setUser(res.data)
    // query 是一个对象，对应 URL 中 ? 后面的键值对
    const { redirect, ...othersQuery } = router.currentRoute.value.query
    // 跳转至redirect指定的路由，若没有指定则跳转至首页
    await router.push({
      path: (redirect as string) || '/',
      query: {
        ...othersQuery
      }
    })
    message.success('登录成功')
    // 持久化
    const { rememberPassword } = loginConfig.value
    const { username, password } = values
    // 实际生产环境需要进行加密存储。
    loginConfig.value.username = rememberPassword ? username : ''
    loginConfig.value.password = rememberPassword ? password : ''
  } finally {
    setLoading(false)
  }
}
</script>

<style lang="less" scoped>
.login-form {
  &-wrapper {
    width: 320px;
  }

  &-title {
    color: #1F2329;
    font-weight: 500;
    font-size: 24px;
    line-height: 32px;
  }

  &-sub-title {
    color: #7A869A;
    font-size: 16px;
    line-height: 24px;
  }

  &-error-msg {
    height: 32px;
    color: rgb(var(--red-6));
    line-height: 32px;
  }

  &-password-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  &-register-btn {
    color: var(--color-text-3) !important;
  }
}
</style>
