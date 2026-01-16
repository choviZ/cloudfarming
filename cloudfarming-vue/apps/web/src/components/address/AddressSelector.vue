<template>
    <div class="address-selector">
        <div class="header">
            <span class="title">收货地址</span>
            <a-button type="link" class="manage-btn" @click="goToManage">管理地址</a-button>
        </div>

        <div v-if="loading" class="loading-wrapper">
            <a-spin />
        </div>

        <div v-else-if="addressList.length > 0" class="address-list">
            <div v-for="addr in addressList" :key="addr.id" class="address-card"
                :class="{ active: modelValue === addr.id }" @click="handleSelect(addr)">
                <div class="card-content">
                    <div class="location-row">
                        <environment-outlined class="location-icon" />
                        <span class="region">
                            {{ addr.provinceName }} {{ addr.cityName }} {{ addr.districtName }}
                        </span>
                    </div>

                    <div class="detail-address">
                        {{ addr.detailAddress }}
                    </div>

                    <div class="user-info">
                        <span class="name">{{ addr.receiverName }}</span>
                        <span class="phone">{{ formatPhone(addr.receiverPhone) }}</span>
                    </div>

                    <div v-if="addr.isDefault === 1" class="default-tag">默认</div>
                </div>

                <!-- 选中状态下的右下角勾选图标 -->
                <div v-if="modelValue === addr.id" class="check-mark">
                    <check-outlined />
                </div>
            </div>
        </div>

        <div v-else class="empty-state">
            <a-empty description="暂无收货地址" />
            <a-button type="primary" @click="goToManage">去添加地址</a-button>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { EnvironmentOutlined, CheckOutlined } from '@ant-design/icons-vue';
import { getCurrentUserReceiveAddresses } from '@cloudfarming/core/api/address';
import type { ReceiveAddressResp } from '@cloudfarming/core/api/address';

// 定义 Props 和 Emits
const props = defineProps<{
    modelValue?: string; // 当前选中的地址 ID
}>();

const emit = defineEmits<{
    (e: 'update:modelValue', id: string): void;
    (e: 'change', address: ReceiveAddressResp): void;
}>();

const router = useRouter();
const loading = ref(false);
const addressList = ref<ReceiveAddressResp[]>([]);

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
                    handleSelect(defaultAddr);
                } else {
                    // 没有默认地址则选中第一个
                    handleSelect(addressList.value[0]!);
                }
            }
        }
    } finally {
        loading.value = false;
    }
};

// 选择地址
const handleSelect = (addr: ReceiveAddressResp) => {
    emit('update:modelValue', addr.id);
    emit('change', addr);
};

// 格式化手机号
const formatPhone = (phone: string) => {
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
    background: #fff;
    padding: 20px;
    border-radius: 8px;
}

.header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
}

.title {
    font-size: 16px;
    font-weight: 600;
    color: #333;
}

.manage-btn {
    padding: 0;
    height: auto;
}

.loading-wrapper {
    text-align: center;
    padding: 20px 0;
}

.address-list {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
}

.address-card {
    position: relative;
    width: 280px;
    border: 1px solid #e8e8e8;
    border-radius: 8px;
    padding: 16px;
    cursor: pointer;
    transition: all 0.3s;
    background: #fff;
}

.address-card:hover {
    border-color: #ffda44;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.address-card.active {
    border-color: #ffda44;
    background-color: #fffbf0;
    /* 极淡的黄色背景 */
}

.card-content {
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.location-row {
    display: flex;
    align-items: flex-start;
    color: #666;
    font-size: 13px;
    line-height: 1.4;
}

.location-icon {
    margin-top: 3px;
    margin-right: 6px;
    font-size: 14px;
}

.region {
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
}

.detail-address {
    font-size: 16px;
    font-weight: 600;
    color: #111;
    margin: 4px 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.user-info {
    font-size: 14px;
    color: #666;
}

.name {
    margin-right: 12px;
}

.default-tag {
    position: absolute;
    top: 0;
    right: 0;
    background: #ccc;
    color: #fff;
    font-size: 12px;
    padding: 2px 6px;
    border-bottom-left-radius: 8px;
    border-top-right-radius: 8px;
    /* 跟随卡片圆角 */
}

.address-card.active .default-tag {
    background: #ffda44;
    color: #111;
}

.check-mark {
    position: absolute;
    bottom: 0;
    right: 0;
    width: 0;
    height: 0;
    border-style: solid;
    border-width: 0 0 24px 24px;
    border-color: transparent transparent #ffda44 transparent;
    color: #111;
}

.check-mark .anticon {
    position: absolute;
    right: 0;
    /* 这里的定位需要根据三角形微调 */
    bottom: -24px;
    font-size: 12px;
    transform: translate(-2px, -2px);
    /* 微调位置 */
}

/* 修正 check-mark 的图标定位，因为 border 创建的三角形内部定位比较麻烦，
   不如直接用一个绝对定位的 div 包含图标 */
.check-mark {
    position: absolute;
    bottom: 0;
    right: 0;
    width: 28px;
    height: 22px;
    background: #ffda44;
    border-top-left-radius: 12px;
    border-bottom-right-radius: 8px;
    /* 跟随卡片圆角 */
    display: flex;
    align-items: center;
    justify-content: center;
    border-width: 0;
    /* 重置上面的三角形样式 */
}

.empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 20px 0;
}
</style>
