<template>
  <a-config-provider :locale="zhCN">
    <router-view />
  </a-config-provider>
</template>
<script setup lang="ts">
import zhCN from 'ant-design-vue/es/locale/zh_CN'
import { useUserStore } from '@/stores/useUserStore.js'
import { onMounted } from 'vue'

const userStore = useUserStore()
onMounted(async () => {
  await userStore.fetchUser()
})
</script>
<style scoped></style>
