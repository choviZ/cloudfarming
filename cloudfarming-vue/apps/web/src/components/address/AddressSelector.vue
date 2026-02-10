<template>
    <div class="address-selector">
        <!-- Loading State -->
        <div v-if="loading" class="loading-wrapper">
            <a-spin />
        </div>

        <!-- Selected Address View (Prototype Style) -->
        <div v-else-if="selectedAddress" class="address-summary-card">
            <div class="summary-content">
                <div class="summary-header">
                    <h2 class="section-title">
                        <environment-filled class="title-icon" /> 收货地址
                    </h2>
                    <span class="switch-btn" @click.stop="goToManage">管理地址</span>
                </div>

                <div class="info-block">
                    <div class="user-row">
                        <span class="user-name">{{ selectedAddress.receiverName }}</span>
                        <span class="user-phone">{{ formatPhone(selectedAddress.receiverPhone) }}</span>
                        <span v-if="selectedAddress.isDefault === 1" class="default-badge">默认</span>
                    </div>
                    <p class="address-detail">
                        {{ selectedAddress.provinceName }} {{ selectedAddress.cityName }} {{ selectedAddress.districtName }}
                        <br>
                        {{ selectedAddress.detailAddress }}
                    </p>
                </div>
            </div>
            <!-- Airmail Border -->
            <div class="airmail-border"></div>
        </div>

        <!-- Empty State -->
        <div v-else class="empty-state">
            <a-empty description="暂无收货地址" />
            <a-button type="primary" @click="goToManage">去添加地址</a-button>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { EnvironmentFilled } from '@ant-design/icons-vue';
import { getCurrentUserReceiveAddresses } from '@/api/address';

// 定义 Props 和 Emits
const props = defineProps({
    modelValue: String // 当前选中的地址 ID
});

const emit = defineEmits(['update:modelValue', 'change']);

const router = useRouter();
const loading = ref(false);
const addressList = ref([]);

const selectedAddress = computed(() => {
    return addressList.value.find(addr => addr.id === props.modelValue);
});

// 获取地址列表
const fetchAddresses = async () => {
    loading.value = true;
    try {
        const res = await getCurrentUserReceiveAddresses();
        if (res.code === '0' && res.data) {
            addressList.value = res.data;

            // 如果没有选中值且有默认地址，自动选中默认地址
            if (!props.modelValue && addressList.value.length > 0) {
                const defaultAddr = addressList.value.find(a => a.isDefault === 1);
                if (defaultAddr) {
                    emit('update:modelValue', defaultAddr.id);
                    emit('change', defaultAddr);
                } else {
                    emit('update:modelValue', addressList.value[0].id);
                    emit('change', addressList.value[0]);
                }
            }
        }
    } finally {
        loading.value = false;
    }
};

// 格式化手机号
const formatPhone = (phone) => {
    if (!phone) return '';
    return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2');
};

// 跳转管理页面
const goToManage = () => {
    router.push('/user/info/address');
};

onMounted(() => {
    fetchAddresses();
});
</script>

<style scoped>
.address-selector {
    width: 100%;
}

.loading-wrapper {
    text-align: center;
    padding: 20px 0;
}

/* --- Summary Card Style (Prototype Match) --- */
.address-summary-card {
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
    border: 1px solid #f3f4f6;
    overflow: hidden;
    position: relative;
    transition: all 0.2s;
}

.address-summary-card:hover {
    border-color: #6ee7b7; /* brand-300 */
}

.summary-content {
    padding: 24px;
}

.summary-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
}

.section-title {
    font-size: 18px;
    font-weight: 700;
    color: #111827; /* gray-900 */
    display: flex;
    align-items: center;
    gap: 8px;
    margin: 0;
}

.title-icon {
    color: #10b981; /* brand-500 */
    font-size: 20px;
}

.switch-btn {
    font-size: 14px;
    color: #059669; /* brand-600 */
    cursor: pointer;
}

.switch-btn:hover {
    text-decoration: underline;
}

.info-block {
    padding-left: 28px; /* Align with text start of title */
}

.user-row {
    display: flex;
    align-items: baseline;
    gap: 16px;
    margin-bottom: 8px;
}

.user-name {
    font-size: 20px;
    font-weight: 700;
    color: #111827;
}

.user-phone {
    font-size: 14px;
    font-weight: 500;
    color: #6b7280;
}

.default-badge {
    background-color: #ecfdf5; /* brand-50 */
    color: #059669; /* brand-600 */
    font-size: 12px;
    padding: 2px 8px;
    border-radius: 4px;
    border: 1px solid #a7f3d0; /* brand-200 */
}

.address-detail {
    font-size: 14px;
    color: #4b5563; /* gray-600 */
    line-height: 1.6;
    margin: 0;
}

.airmail-border {
    height: 4px;
    width: 100%;
    background: repeating-linear-gradient(
        -45deg,
        #ef4444 0,
        #ef4444 12px,
        transparent 12px,
        transparent 25px,
        #3b82f6 25px,
        #3b82f6 37px,
        transparent 37px,
        transparent 50px
    );
}

.empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 20px 0;
}
</style>
