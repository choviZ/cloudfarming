<template>
    <!-- 订单详情模态框 -->
    <a-modal
        v-model:open="visible"
        title="订单详情"
        width="860px"
        :footer="null"
        :destroy-on-close="true"
        @cancel="handleClose"
    >
        <!-- 加载状态 -->
        <a-spin :spinning="loading" tip="加载中...">
            <!-- 领养订单详情 -->
            <template v-if="orderType == 0 && orderData && orderData.length">
                <div v-for="(item, index) in orderData" :key="index">
                    <a-divider v-if="index > 0" style="margin: 24px 0" />
                    <a-descriptions :column="2" bordered :title="`商品明细 #${index + 1}`">
                        <a-descriptions-item label="订单号" :span="2">
                            {{ item.orderNo }}
                        </a-descriptions-item>
                        <a-descriptions-item label="商品名称" :span="2">
                            {{ item.itemName }}
                        </a-descriptions-item>
                        <a-descriptions-item label="商品图片" :span="2">
                            <a-image
                                :src="item.itemImage"
                                :alt="item.itemName"
                                style="width: 120px; height: 120px; object-fit: cover;"
                            />
                        </a-descriptions-item>
                        <a-descriptions-item label="价格">
                            ¥{{ formatAmount(item.price) }}
                        </a-descriptions-item>
                        <a-descriptions-item label="数量">
                            {{ item.quantity }}
                        </a-descriptions-item>
                        <a-descriptions-item label="总金额" :span="2">
                            <span style="color: #f5222d; font-weight: bold;">
                                ¥{{ formatAmount(item.totalAmount) }}
                            </span>
                        </a-descriptions-item>
                        <a-descriptions-item label="开始日期">
                            {{ formatDate(item.startDate) }}
                        </a-descriptions-item>
                        <a-descriptions-item label="结束日期">
                            {{ formatDate(item.endDate) }}
                        </a-descriptions-item>
                        <a-descriptions-item label="创建时间" :span="2">
                            {{ formatTime(item.createTime) }}
                        </a-descriptions-item>
                    </a-descriptions>
                </div>
            </template>

            <!-- 商品订单详情 -->
            <template v-if="orderType == 1 && orderData && orderData.length">
                <div v-for="(item, index) in orderData" :key="index">
                    <a-divider v-if="index > 0" style="margin: 24px 0" />
                    <a-descriptions :column="2" bordered :title="`商品明细 #${index + 1}`">
                        <a-descriptions-item label="订单号" :span="2">
                            {{ item.orderNo }}
                        </a-descriptions-item>
                        <a-descriptions-item label="商品名称" :span="2">
                            {{ item.skuName }}
                        </a-descriptions-item>
                        <a-descriptions-item label="商品图片" :span="2">
                            <a-image
                                :src="item.skuImage"
                                :alt="item.skuName"
                                style="width: 120px; height: 120px; object-fit: cover;"
                            />
                        </a-descriptions-item>
                        <a-descriptions-item label="商品规格" :span="2">
                            {{ item.skuSpecs || '-' }}
                        </a-descriptions-item>
                        <a-descriptions-item label="价格">
                            ¥{{ formatAmount(item.price) }}
                        </a-descriptions-item>
                        <a-descriptions-item label="数量">
                            {{ item.quantity }}
                        </a-descriptions-item>
                        <a-descriptions-item label="总金额" :span="2">
                            <span style="color: #f5222d; font-weight: bold;">
                                ¥{{ formatAmount(item.totalAmount) }}
                            </span>
                        </a-descriptions-item>
                        <a-descriptions-item label="创建时间" :span="2">
                            {{ formatTime(item.createTime) }}
                        </a-descriptions-item>
                    </a-descriptions>
                </div>
            </template>

            <!-- 空状态 -->
            <a-empty v-if="!loading && (!orderData || orderData.length === 0)" description="暂无订单数据" />
        </a-spin>

        <!-- 底部按钮 -->
        <div style="margin-top: 24px; text-align: right;">
            <a-button @click="handleClose">关闭</a-button>
        </div>
    </a-modal>
</template>

<script setup>
import { ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { getAdoptOrderDetail, getSkuOrderDetail } from '@/api/order'

// 组件属性定义
const props = defineProps({
    // 控制模态框显示/隐藏
    open: {
        type: Boolean,
        default: false
    },
    // 订单类型：0-领养订单，1-商品订单
    orderType: {
        type: Number,
        default: 0
    },
    // 订单号
    orderNo: {
        type: String,
        default: ''
    }
})

// 组件事件定义
const emit = defineEmits(['update:open', 'close'])

// 组件内部状态
const visible = ref(false)
const loading = ref(false)
const orderData = ref(null)

// 监听外部传入的open属性，同步到内部visible状态
watch(() => props.open, (newVal) => {
    visible.value = newVal
    if (newVal && props.orderNo) {
        fetchOrderDetail()
    }
})

// 监听内部visible状态，同步到外部
watch(() => visible.value, (newVal) => {
    emit('update:open', newVal)
    if (!newVal) {
        // 模态框关闭时清空数据
        orderData.value = null
    }
})

/**
 * 获取订单详情
 * 根据订单类型调用不同的接口
 */
const fetchOrderDetail = async () => {
    if (!props.orderNo) {
        message.warning('订单号不能为空')
        return
    }

    loading.value = true
    orderData.value = null

    try {
        let res
        const params = { orderNo: props.orderNo }

        // 根据订单类型调用不同接口
        if (props.orderType === 0) {
            res = await getAdoptOrderDetail(params)
        } else {
            res = await getSkuOrderDetail(params)
        }

        if (res.code == 0) {
            orderData.value = Array.isArray(res.data) ? res.data : (res.data ? [res.data] : [])
        } else {
            message.error(res.message || '获取订单详情失败')
        }
    } catch (error) {
        message.error('获取订单详情失败，'+ error)
    } finally {
        loading.value = false
    }
}

/**
 * 关闭模态框
 */
const handleClose = () => {
    visible.value = false
    emit('close')
}

/**
 * 格式化金额，保留两位小数
 * @param {number} amount - 金额
 * @returns {string} 格式化后的金额字符串
 */
const formatAmount = (amount) => {
    if (amount === null || amount === undefined) return '0.00'
    return Number(amount).toFixed(2)
}

/**
 * 格式化日期
 * @param {string} date - 日期字符串
 * @returns {string} 格式化后的日期
 */
const formatDate = (date) => {
    if (!date) return '-'
    return dayjs(date).format('YYYY-MM-DD')
}

/**
 * 格式化日期时间
 * @param {string} time - 时间字符串
 * @returns {string} 格式化后的日期时间
 */
const formatTime = (time) => {
    if (!time) return '-'
    return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}
</script>

<style scoped>
/* 响应式布局适配 */
@media (max-width: 768px) {
    :deep(.ant-modal) {
        width: 95% !important;
        max-width: 95%;
    }
}
</style>
