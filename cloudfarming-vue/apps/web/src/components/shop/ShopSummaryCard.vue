<template>
  <div class="shop-card shop-card--clickable" @click="emit('enter')">
    <div class="shop-header">
      <div class="shop-avatar">
        <img v-if="shopInfo?.shopAvatar" :src="shopInfo.shopAvatar" :alt="displayShopName" class="shop-logo">
        <ShopOutlined v-else />
      </div>
      <div class="shop-meta">
        <div class="shop-name">{{ displayShopName }}</div>
      </div>
    </div>

    <div class="shop-actions">
      <button type="button" class="btn-small btn-outline" @click.stop="emit('enter')">
        {{ buttonText }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { ShopOutlined } from '@ant-design/icons-vue'

const props = defineProps({
  shopInfo: {
    type: Object,
    default: () => ({})
  },
  loadingName: {
    type: String,
    default: '店铺名称加载中...'
  },
  buttonText: {
    type: String,
    default: '进入店铺'
  }
})

const emit = defineEmits(['enter'])

const displayShopName = computed(() => props.shopInfo?.shopName || props.loadingName)
</script>

<style scoped>
.shop-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #f3f4f6;
}

.shop-card--clickable {
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.shop-card--clickable:hover {
  transform: translateY(-2px);
  box-shadow: 0 16px 28px rgba(17, 24, 39, 0.08);
}

.shop-header {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.shop-avatar {
  width: 48px;
  height: 48px;
  background: #f3f4f6;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #9ca3af;
  overflow: hidden;
  flex-shrink: 0;
}

.shop-logo {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.shop-avatar .anticon {
  display: flex;
  align-items: center;
  justify-content: center;
}

.shop-meta {
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-width: 0;
}

.shop-name {
  font-weight: 700;
  color: #111827;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-all;
}

.shop-actions {
  display: flex;
}

.btn-small {
  flex: 1;
  padding: 8px 0;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  border: none;
}

.btn-outline {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  color: #374151;
}

.btn-outline:hover {
  background: #f9fafb;
}
</style>
