<template>
  <div class="address-selector">
    <div v-if="loading" class="loading-wrapper">
      <a-spin />
    </div>

    <template v-else-if="addressList.length">
      <div class="address-panel">
        <div class="panel-header">
          <div class="panel-title">
            <EnvironmentFilled class="title-icon" />
            <div>
              <h2>收货地址</h2>
              <p>请选择本次订单使用的收货地址</p>
            </div>
          </div>

          <div class="panel-actions">
            <button type="button" class="action-link" @click="goToCreate">
              <PlusOutlined />
              使用新地址
            </button>
            <button type="button" class="action-link" @click="goToManage">管理地址</button>
          </div>
        </div>

        <div class="address-grid">
          <button
            v-for="address in addressList"
            :key="address.id"
            type="button"
            class="address-card"
            :class="{ active: isSelected(address.id) }"
            :aria-pressed="isSelected(address.id)"
            :aria-label="`选择收货地址：${address.receiverName}，${formatPhone(address.receiverPhone)}`"
            @click="handleSelect(address)"
          >
            <div class="card-top">
              <div class="card-badges">
                <span v-if="address.isDefault === 1" class="badge badge-default">默认</span>
                <span v-if="isSelected(address.id)" class="badge badge-selected">当前使用</span>
              </div>
              <CheckCircleFilled v-if="isSelected(address.id)" class="selected-mark" />
            </div>

            <p class="card-region" :title="buildRegion(address)">
              {{ buildRegion(address) }}
            </p>
            <p class="card-detail" :title="address.detailAddress">
              {{ address.detailAddress }}
            </p>

            <div class="card-contact">
              <strong>{{ address.receiverName }}</strong>
              <span>{{ formatPhone(address.receiverPhone) }}</span>
            </div>
          </button>
        </div>
      </div>
    </template>

    <div v-else class="empty-state">
      <a-empty description="暂无收货地址" />
      <a-button type="primary" @click="goToCreate">去新增地址</a-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { CheckCircleFilled, EnvironmentFilled, PlusOutlined } from '@ant-design/icons-vue'
import { getCurrentUserReceiveAddresses } from '@/api/address'

const props = defineProps({
  modelValue: [String, Number]
})

const emit = defineEmits(['update:modelValue', 'change'])

const router = useRouter()
const loading = ref(false)
const addressList = ref([])

const normalizeId = (value) => String(value ?? '')

const isSelected = (id) => normalizeId(id) === normalizeId(props.modelValue)

const formatPhone = (phone) => {
  if (!phone) {
    return ''
  }
  return String(phone).replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

const buildRegion = (address) => {
  return [
    address.provinceName,
    address.cityName,
    address.districtName,
    address.townName
  ]
    .filter(Boolean)
    .join(' ')
}

const applySelectedAddress = (address) => {
  if (!address) {
    return
  }
  emit('update:modelValue', address.id)
  emit('change', address)
}

const ensureSelectedAddress = () => {
  if (!addressList.value.length) {
    emit('update:modelValue', '')
    emit('change', null)
    return
  }

  const matched = addressList.value.find((item) => isSelected(item.id))
  if (matched) {
    emit('change', matched)
    return
  }

  const defaultAddress = addressList.value.find((item) => Number(item.isDefault) === 1)
  applySelectedAddress(defaultAddress || addressList.value[0])
}

const fetchAddresses = async () => {
  loading.value = true
  try {
    const res = await getCurrentUserReceiveAddresses()
    if (res.code === '0' && res.data) {
      addressList.value = res.data
    } else {
      addressList.value = []
    }
    ensureSelectedAddress()
  } finally {
    loading.value = false
  }
}

const handleSelect = (address) => {
  if (isSelected(address.id)) {
    return
  }
  applySelectedAddress(address)
}

const goToManage = () => {
  router.push('/user/info/address')
}

const goToCreate = () => {
  router.push('/user/info/address?mode=create')
}

onMounted(() => {
  fetchAddresses()
})
</script>

<style scoped>
.address-selector {
  width: 100%;
}

.loading-wrapper {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}

.address-panel {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.panel-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
}

.panel-title {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.title-icon {
  margin-top: 4px;
  color: #1f8f56;
  font-size: 20px;
}

.panel-title h2 {
  margin: 0;
  color: #173524;
  font-size: 18px;
  font-weight: 800;
}

.panel-title p {
  margin: 6px 0 0;
  color: #74867b;
  font-size: 13px;
  line-height: 1.6;
}

.panel-actions {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
}

.action-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 0;
  border: none;
  background: transparent;
  color: #1f8f56;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}

.action-link:hover {
  color: #157347;
}

.action-link:focus-visible {
  outline: 2px solid rgba(31, 143, 86, 0.22);
  outline-offset: 4px;
  border-radius: 8px;
}

.address-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 14px;
}

.address-card {
  position: relative;
  min-height: 152px;
  padding: 16px;
  border: 1px solid #e7ece8;
  border-radius: 16px;
  background: #ffffff;
  text-align: left;
  cursor: pointer;
  transition:
    border-color 0.2s ease,
    box-shadow 0.2s ease,
    transform 0.2s ease;
}

.address-card:hover {
  transform: translateY(-2px);
  border-color: rgba(31, 143, 86, 0.22);
  box-shadow: 0 14px 28px rgba(18, 42, 30, 0.06);
}

.address-card.active {
  border-color: #1f8f56;
  box-shadow:
    0 0 0 2px rgba(31, 143, 86, 0.08),
    0 14px 28px rgba(18, 42, 30, 0.08);
}

.address-card:focus-visible {
  outline: none;
  border-color: #1f8f56;
  box-shadow:
    0 0 0 3px rgba(31, 143, 86, 0.16),
    0 14px 28px rgba(18, 42, 30, 0.08);
}

.card-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.card-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 24px;
  padding: 0 8px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.badge-default {
  background: #fff4ea;
  color: #d4661f;
}

.badge-selected {
  background: #eef8f2;
  color: #1f8f56;
}

.selected-mark {
  color: #1f8f56;
  font-size: 18px;
  flex-shrink: 0;
}

.card-region,
.card-detail {
  margin: 0;
  overflow: hidden;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  word-break: break-word;
}

.card-region {
  min-height: 42px;
  color: #173524;
  font-size: 15px;
  font-weight: 700;
  line-height: 1.45;
  -webkit-line-clamp: 2;
}

.card-detail {
  margin-top: 8px;
  min-height: 40px;
  color: #6f8276;
  font-size: 13px;
  line-height: 1.55;
  -webkit-line-clamp: 2;
}

.card-contact {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 14px;
  color: #405147;
  font-size: 13px;
}

.card-contact strong {
  color: #173524;
  font-size: 14px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 20px 0;
}

@media (max-width: 900px) {
  .panel-header {
    flex-direction: column;
  }

  .panel-actions {
    width: 100%;
    justify-content: flex-end;
  }
}

@media (max-width: 640px) {
  .address-grid {
    grid-template-columns: 1fr;
  }

  .panel-actions {
    justify-content: flex-start;
    flex-wrap: wrap;
    gap: 12px;
  }

  .address-card {
    min-height: 0;
  }
}
</style>
