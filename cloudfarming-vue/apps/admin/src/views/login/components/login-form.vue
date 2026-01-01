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
        <a-button type="primary" html-type="submit" long :loading="loading" size="large" style="width: 100%">
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
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue';
import { useStorage } from '@vueuse/core';
import { useUserStore } from '@/store';
import useLoading from '@/hooks/loading';
import type { LoginData } from '@/api/user';

const router = useRouter();
const errorMessage = ref('');
const { loading, setLoading } = useLoading();
const userStore = useUserStore();

const loginConfig = useStorage('login-config', {
  rememberPassword: true,
  username: 'admin', // 演示默认值
  password: 'admin', // 演示默认值
});

const userInfo = reactive({
  username: loginConfig.value.username,
  password: loginConfig.value.password,
});

// Ant Design Vue 表单验证通过后会触发 @finish 事件
const handleSubmit = async (values: Record<string, any>) => {
  if (loading.value) return;

  setLoading(true);
  try {
    await userStore.login(values as LoginData);
    const { redirect, ...othersQuery } = router.currentRoute.value.query;
    router.push({
      name: (redirect as string) || 'Workplace',
      query: {
        ...othersQuery,
      },
    });
    message.success('登录成功');

    const { rememberPassword } = loginConfig.value;
    const { username, password } = values;
    // 实际生产环境需要进行加密存储。
    loginConfig.value.username = rememberPassword ? username : '';
    loginConfig.value.password = rememberPassword ? password : '';
  } catch (err) {
    errorMessage.value = (err as Error).message;
  } finally {
    setLoading(false);
  }
};
</script>

<style lang="less" scoped>
.login-form {
  &-wrapper {
    width: 320px;
  }

  &-title {
    color: var(--color-text-1);
    font-weight: 500;
    font-size: 24px;
    line-height: 32px;
  }

  &-sub-title {
    color: var(--color-text-3);
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
