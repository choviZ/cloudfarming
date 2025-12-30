<template>
  <router-view />
</template>
<script setup lang="ts">
import { useUserStore } from '@/stores/useUserStore.ts'
import { onMounted } from 'vue'

const userStore = useUserStore()
onMounted(async () => {
  await userStore.fetchUser()
})
</script>
<style scoped></style>
