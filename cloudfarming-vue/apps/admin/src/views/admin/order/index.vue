<template>
    <div class="order-management">
        <!-- 搜索区 -->
        <a-card class="search-card" :bordered="false">
            <a-form layout="horizontal" :model="searchForm" style="display: flex; flex-wrap: nowrap; overflow-x: auto;">
                <a-space :size="16">
                    <a-form-item label="订单ID">
                        <a-input v-model:value="searchForm.id" placeholder="请输入订单ID" allow-clear
                            @pressEnter="handleSearch" />
                    </a-form-item>
                    <a-form-item label="订单号">
                        <a-input v-model:value="searchForm.orderNo" placeholder="请输入订单号" allow-clear
                            @pressEnter="handleSearch" />
                    </a-form-item>
                    <a-form-item label="店铺ID">
                        <a-input v-model:value="searchForm.shopId" placeholder="请输入店铺ID" allow-clear
                            @pressEnter="handleSearch" />
                    </a-form-item>
                    <a-form-item label="用户ID">
                        <a-input v-model:value="searchForm.userId" placeholder="请输入用户ID" allow-clear
                            @pressEnter="handleSearch" />
                    </a-form-item>
                    <a-form-item label="订单状态">
                        <a-select v-model:value="searchForm.orderStatus" placeholder="请选择订单状态" allow-clear
                            style="width: 150px">
                            <a-select-option :value="0">待付款</a-select-option>
                            <a-select-option :value="1">待发货</a-select-option>
                            <a-select-option :value="2">已发货</a-select-option>
                            <a-select-option :value="3">已完成</a-select-option>
                            <a-select-option :value="4">已取消</a-select-option>
                        </a-select>
                    </a-form-item>
                    <a-form-item>
                        <a-space>
                            <a-button type="primary" @click="handleSearch">
                                <template #icon>
                                    <SearchOutlined />
                                </template>
                                查询
                            </a-button>
                            <a-button @click="handleReset">
                                <template #icon>
                                    <ReloadOutlined />
                                </template>
                                重置
                            </a-button>
                        </a-space>
                    </a-form-item>
                </a-space>
            </a-form>
        </a-card>

        <!-- 订单列表 -->
        <a-card :bordered="false" style="margin-top: 16px">
            <a-table :columns="columns" :data-source="orderList" :loading="loading" :pagination="false" row-key="id"
                :scroll="{ x: 'max-content' }">
                <template #bodyCell="{ column, record }">
                    <template v-if="column.key === 'orderType'">
                        <a-tag :color="record.orderType === 0 ? 'blue' : 'green'">
                            {{ record.orderType === 0 ? '委托养殖' : '普通商品' }}
                        </a-tag>
                    </template>
                    <template v-else-if="column.key === 'orderStatus'">
                        <a-tag :color="getOrderStatusColor(record.orderStatus)">
                            {{ getOrderStatusText(record.orderStatus) }}
                        </a-tag>
                    </template>
                    <template v-else-if="column.key === 'amountInfo'">
                        <div style="font-size: 12px; line-height: 1.8;">
                            <div>总额: ¥{{ record.totalAmount }}</div>
                            <div>实付: ¥{{ record.actualPayAmount }}</div>
                            <div>运费: ¥{{ record.freightAmount }}</div>
                            <div>优惠: ¥{{ record.discountAmount }}</div>
                        </div>
                    </template>
                    <template v-else-if="column.key === 'receiverInfo'">
                        <div>
                            <span>{{ record.receiveName }}</span>
                            <span style="margin-left: 8px; color: #666">{{ record.receivePhone }}</span>
                        </div>
                        <div style="font-size: 12px; color: #999; margin-top: 4px">
                            {{ record.receiveAddress }}
                        </div>
                    </template>
                    <template v-else-if="column.key === 'createTime'">
                        <span>{{ formatTime(record.createTime) }}</span>
                    </template>
                    <template v-else-if="column.key === 'deliveryTime'">
                        <span>{{ record.deliveryTime ? formatTime(record.deliveryTime) : '商家未发货' }}</span>
                    </template>
                    <template v-else-if="column.key === 'receiveTime'">
                        <span>{{ record.receiveTime ? formatTime(record.receiveTime) : '用户未确认收货' }}</span>
                    </template>
                    <template v-else-if="column.key === 'logisticsInfo'">
                        <span>物流单号：{{ record.logisticsNo }}</span>
                        <span>物流公司：{{ record.logisticsCompany }}</span>
                    </template>
                    <template v-else-if="column.key === 'action'">
                        <a-button type="link" size="small" @click="handleDetail(record)">
                            查看详情
                        </a-button>
                    </template>
                </template>
            </a-table>

            <!-- 分页 -->
            <div class="pagination" style="margin-top: 16px; text-align: right">
                <a-pagination v-model:current="pagination.current" v-model:page-size="pagination.size"
                    :total="pagination.total" :show-total="(total) => `共 ${total} 条`" show-size-changer
                    show-quick-jumper @change="handlePageChange" />
            </div>
        </a-card>

        <!-- 订单详情模态框 -->
        <OrderDetailModal
            v-model:open="detailModalVisible"
            :order-type="currentOrderType"
            :order-no="currentOrderNo"
            @close="handleDetailClose"
        />
    </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { SearchOutlined, ReloadOutlined } from '@ant-design/icons-vue'
import { listOrders } from '@/api/order'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import OrderDetailModal from './components/OrderDetailModal.vue'

// 搜索数据
const searchForm = reactive({
    id: '',
    orderNo: '',
    shopId: '',
    userId: '',
    orderStatus: undefined
})

// 表格数据
const loading = ref(false)
const orderList = ref([])
const pagination = reactive({
    current: 1,
    size: 10,
    total: 0
})

// 订单详情模态框状态
const detailModalVisible = ref(false)
const currentOrderType = ref(0)
const currentOrderNo = ref('')

// 表格列
const columns = [
    {
        title: 'ID',
        dataIndex: 'id',
        key: 'id',
        width: 80
    },
    {
        title: '订单号',
        dataIndex: 'orderNo',
        key: 'orderNo',
        width: 120
    },
    {
        title: '支付单号',
        dataIndex: 'payOrderNo',
        key: 'payOrderNo',
        width: 120
    },
    {
        title: '用户ID',
        dataIndex: 'userId',
        key: 'userId',
        width: 100
    },
    {
        title: '订单类型',
        dataIndex: 'orderType',
        key: 'orderType',
        width: 80
    },
    {
        title: '订单金额',
        key: 'amountInfo',
        width: 120
    },
    {
        title: '订单状态',
        dataIndex: 'orderStatus',
        key: 'orderStatus',
        width: 60
    },
    {
        title: '收货信息',
        key: 'receiverInfo',
        width: 180
    },
    {
        title: '物流信息',
        key: 'logisticsInfo',
        width: 160
    },
    {
        title: '下单时间',
        dataIndex: 'createTime',
        key: 'createTime',
        width: 140
    },
    {
        title: '发货时间',
        dataIndex: 'deliveryTime',
        key: 'deliveryTime',
        width: 140
    },
    {
        title: '收货时间',
        dataIndex: 'receiveTime',
        key: 'receiveTime',
        width: 140
    },
    {
        title: '操作',
        key: 'action',
        width: 100,
        sticky: 'right'
    }
]

// 获取数据
const fetchData = async () => {
    loading.value = true
    try {
        // 构建请求参数
        const params = {
            current: pagination.current,
            size: pagination.size
        }
        // 只添加有值的搜索条件（过滤空字符串）
        Object.keys(searchForm).forEach(key => {
            const value = searchForm[key]
            if (value !== '' && value !== undefined) {
                params[key] = value
            }
        })

        const res = await listOrders(params)
        if (res.code == 0) {
            orderList.value = res.data.records || []
            pagination.total = Number(res.data.total) || 0
        } else {
            message.error(res.message || '获取订单列表失败')
        }
    } finally {
        loading.value = false
    }
}

// 搜索
const handleSearch = () => {
    pagination.current = 1
    fetchData()
}

// 重置
const handleReset = () => {
    searchForm.id = ''
    searchForm.orderNo = ''
    searchForm.shopId = ''
    searchForm.userId = ''
    searchForm.orderStatus = undefined
    handleSearch()
}

// 分页变化
const handlePageChange = (page, pageSize) => {
    pagination.current = page
    pagination.size = pageSize
    fetchData()
}

/**
 * 打开订单详情模态框
 */
const handleDetail = (record) => {
    currentOrderType.value = record.orderType
    currentOrderNo.value = record.orderNo
    detailModalVisible.value = true
}

/**
 * 关闭订单详情模态框
 */
const handleDetailClose = () => {
    detailModalVisible.value = false
    currentOrderNo.value = ''
}

// Helpers
const getOrderStatusText = (status) => {
    const map = {
        0: '待付款',
        1: '待发货',
        2: '已发货',
        3: '已完成',
        4: '已取消',
        5: '售后中'
    }
    return map[status] || '未知状态'
}

const getOrderStatusColor = (status) => {
    const map = {
        0: 'orange',
        1: 'blue',
        2: 'cyan',
        3: 'green',
        4: 'red',
        5: 'purple'
    }
    return map[status] || 'default'
}

// 格式化时间
const formatTime = (time) => {
    return time ? dayjs(time).format('YYYY-MM-DD HH:mm:ss') : '-'
}

onMounted(() => {
    fetchData()
})
</script>

<style scoped>
.order-management {
    padding: 24px;
}

.search-card {
    margin-bottom: 16px;
}
</style>
