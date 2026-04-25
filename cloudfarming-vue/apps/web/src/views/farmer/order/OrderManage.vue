<template>
  <div class="farmer-order-page">
    <section class="page-header">
      <div>
        <h1 class="page-title">订单管理</h1>
        <p class="page-desc">集中处理农产品发货与认养订单分配，按状态快速筛选当前店铺订单。</p>
      </div>
      <a-button type="primary" :loading="loading" @click="fetchOrders">
        刷新订单
      </a-button>
    </section>

    <section class="toolbar-card">
      <div class="summary-grid">
        <div class="summary-item">
          <span class="summary-label">当前筛选</span>
          <strong class="summary-value">{{ activeFilter.label }}</strong>
          <p class="summary-hint">{{ activeFilter.description }}</p>
        </div>
        <div class="summary-item">
          <span class="summary-label">订单总数</span>
          <strong class="summary-value">{{ pagination.total }}</strong>
          <p class="summary-hint">当前筛选条件下的订单总量</p>
        </div>
        <div class="summary-item">
          <span class="summary-label">当前页记录</span>
          <strong class="summary-value">{{ orderRecords.length }}</strong>
          <p class="summary-hint">第 {{ pagination.current }} 页，每页 {{ pagination.pageSize }} 条</p>
        </div>
      </div>

      <a-tabs v-model:activeKey="activeFilterKey" class="status-tabs" @change="handleFilterChange">
        <a-tab-pane v-for="option in FILTER_OPTIONS" :key="option.key" :tab="option.label" />
      </a-tabs>
    </section>

    <section class="table-card">
      <div class="table-head">
        <div>
          <h2 class="table-title">订单列表</h2>
          <p class="table-desc">商品订单与认养订单都支持物流发货，认养订单会在履约完成后进入待发货。</p>
        </div>
      </div>

      <a-table
        :columns="columns"
        :data-source="orderRecords"
        :loading="loading"
        :pagination="false"
        :scroll="{ x: 1440 }"
        :locale="{ emptyText: '当前筛选条件下暂无订单' }"
        row-key="id"
        class="order-table"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'orderInfo'">
            <div class="order-info-cell">
              <p class="primary-text">订单号：{{ record.orderNo || '--' }}</p>
              <p class="secondary-text">支付单号：{{ record.payOrderNo || '--' }}</p>
            </div>
          </template>

          <template v-else-if="column.key === 'orderType'">
            <a-tag :color="record.orderType === ORDER_TYPE.ADOPT ? 'gold' : 'green'">
              {{ getOrderTypeText(record.orderType) }}
            </a-tag>
          </template>

          <template v-else-if="column.key === 'orderStatus'">
            <a-tag :color="getStatusColor(record.orderStatus)">
              {{ getStatusText(record.orderStatus) }}
            </a-tag>
          </template>

          <template v-else-if="column.key === 'receiverInfo'">
            <div class="receiver-cell">
              <p class="primary-text">{{ record.receiveName || '--' }} {{ formatPhone(record.receivePhone) }}</p>
              <p class="secondary-text clamp-two-lines">{{ record.receiveAddress || '暂无收货地址信息' }}</p>
            </div>
          </template>

          <template v-else-if="column.key === 'amountInfo'">
            <div class="amount-cell">
              <p class="amount-primary">¥{{ formatMoney(record.actualPayAmount) }}</p>
              <p class="secondary-text">订单总额：¥{{ formatMoney(record.totalAmount) }}</p>
            </div>
          </template>

          <template v-else-if="column.key === 'progressInfo'">
            <div class="logistics-cell">
              <p class="primary-text">{{ getProgressTitle(record) }}</p>
              <p class="secondary-text">{{ getProgressDescription(record) }}</p>
            </div>
          </template>

          <template v-else-if="column.key === 'timeInfo'">
            <div class="time-cell">
              <p class="secondary-text">下单：{{ formatDate(record.createTime) }}</p>
              <p class="secondary-text">履约：{{ getFulfillmentTimeText(record) }}</p>
              <p class="secondary-text">收货：{{ formatOptionalTime(record.receiveTime, '待确认') }}</p>
            </div>
          </template>

          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button v-if="canAssign(record)" type="link" size="small" @click="openAssignModal(record)">
                分配牲畜
              </a-button>
              <a-button v-else-if="canShip(record)" type="link" size="small" @click="openShipModal(record)">
                发货
              </a-button>
              <a-button v-else-if="canViewLogistics(record)" type="link" size="small" @click="openLogisticsModal(record)">
                查看物流
              </a-button>
              <span v-else class="secondary-text action-text">{{ getActionText(record) }}</span>
            </a-space>
          </template>
        </template>
      </a-table>

      <div class="pagination-wrap">
        <a-pagination
          v-model:current="pagination.current"
          :total="pagination.total"
          :page-size="pagination.pageSize"
          :show-size-changer="false"
          :show-total="(total) => `共 ${total} 笔订单`"
          @change="handlePageChange"
        />
      </div>
    </section>

    <a-modal
      v-model:open="shipModalVisible"
      title="订单发货"
      ok-text="确认发货"
      cancel-text="取消"
      :confirm-loading="shipSubmitting"
      :mask-closable="false"
      destroy-on-close
      @ok="handleShipSubmit"
      @cancel="handleShipCancel"
    >
      <a-alert
        class="ship-alert"
        type="info"
        show-icon
        :message="`订单号：${shipForm.orderNo || '--'}`"
        :description="shipOrderSummary"
      />

      <a-form ref="shipFormRef" layout="vertical" :model="shipForm" :rules="shipRules">
        <a-form-item label="物流公司" name="logisticsCompany">
          <a-input
            v-model:value="shipForm.logisticsCompany"
            :maxlength="30"
            placeholder="请输入物流公司名称"
            allow-clear
          />
        </a-form-item>

        <a-form-item label="物流单号" name="logisticsNo">
          <a-input
            v-model:value="shipForm.logisticsNo"
            :maxlength="50"
            placeholder="请输入物流单号"
            allow-clear
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal
      v-model:open="assignModalVisible"
      title="分配认养牲畜"
      ok-text="确认分配"
      cancel-text="取消"
      :confirm-loading="assignSubmitting"
      :mask-closable="false"
      destroy-on-close
      :width="760"
      @ok="handleAssignSubmit"
      @cancel="handleAssignCancel"
    >
      <a-alert
        class="assign-alert"
        type="info"
        show-icon
        :message="`订单号：${assignForm.orderNo || '--'}`"
        :description="assignOrderSummary"
      />

      <a-spin :spinning="assignDetailLoading">
        <div v-if="assignForm.items.length" class="assign-content">
          <p class="assign-tip">请按认养数量填写耳标号，支持英文逗号、中文逗号或换行分隔，提交后会创建对应数量的养殖实例。</p>

          <div v-for="item in assignForm.items" :key="item.key" class="assign-item-card">
            <div class="assign-item-head">
              <div>
                <h3 class="assign-item-title">{{ item.itemName || '认养项目' }}</h3>
                <p class="assign-item-meta">项目ID：{{ item.adoptItemId }} · 需分配 {{ item.quantity }} 只</p>
              </div>
              <a-tag color="gold">待分配</a-tag>
            </div>

            <a-textarea
              v-model:value="item.earTagInput"
              :rows="Math.min(Math.max(item.quantity, 3), 6)"
              :placeholder="`请输入${item.quantity}个耳标号，支持逗号或换行分隔`"
              allow-clear
            />

            <p class="assign-item-hint">已填写 {{ countEarTags(item.earTagInput) }} / {{ item.quantity }} 个耳标号</p>
          </div>
        </div>

        <a-empty v-else description="当前订单暂无可分配的认养明细" />
      </a-spin>
    </a-modal>

    <OrderLogisticsModal
      v-model:open="logisticsModalVisible"
      :order-no="currentLogisticsOrderNo"
      @close="handleLogisticsModalClose"
    />
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import {
  assignFarmerAdoptOrder,
  getAdoptOrderDetail,
  getFarmerOrderList,
  shipFarmerOrder,
  ORDER_STATUS,
  ORDER_STATUS_TEXT,
  ORDER_TYPE
} from '@/api/order'
import OrderLogisticsModal from '@/components/order/OrderLogisticsModal.vue'

const PAGE_SIZE = 10

const FILTER_OPTIONS = [
  {
    key: 'all',
    label: '全部订单',
    description: '查看当前店铺全部订单'
  },
  {
    key: 'pendingPayment',
    label: '待付款',
    value: ORDER_STATUS.PENDING_PAYMENT,
    description: '等待买家完成支付'
  },
  {
    key: 'pendingAssignment',
    label: '待分配',
    value: ORDER_STATUS.PENDING_ASSIGNMENT,
    description: '优先为认养订单分配耳标号'
  },
  {
    key: 'pendingShipment',
    label: '待发货',
    value: ORDER_STATUS.PENDING_SHIPMENT,
    description: '优先处理待发货订单，包含履约完成后的认养订单'
  },
  {
    key: 'breeding',
    label: '养殖中',
    value: ORDER_STATUS.BREEDING,
    description: '查看已分配牲畜的认养订单'
  },
  {
    key: 'shipped',
    label: '已发货',
    value: ORDER_STATUS.SHIPPED,
    description: '关注运输中的商品订单'
  },
  {
    key: 'completed',
    label: '已完成',
    value: ORDER_STATUS.COMPLETED,
    description: '查看已完成履约订单'
  },
  {
    key: 'cancel',
    label: '已关闭',
    value: ORDER_STATUS.CANCEL,
    description: '展示已关闭或取消订单'
  },
  {
    key: 'afterSale',
    label: '售后中',
    value: ORDER_STATUS.AFTER_SALE,
    description: '跟进当前售后处理'
  }
]

const columns = [
  {
    title: '订单信息',
    key: 'orderInfo',
    width: 240,
    fixed: 'left'
  },
  {
    title: '订单类型',
    dataIndex: 'orderType',
    key: 'orderType',
    width: 110,
    align: 'center'
  },
  {
    title: '订单状态',
    dataIndex: 'orderStatus',
    key: 'orderStatus',
    width: 110,
    align: 'center'
  },
  {
    title: '收货信息',
    key: 'receiverInfo',
    width: 280
  },
  {
    title: '金额信息',
    key: 'amountInfo',
    width: 180
  },
  {
    title: '履约进度',
    key: 'progressInfo',
    width: 230
  },
  {
    title: '时间节点',
    key: 'timeInfo',
    width: 230
  },
  {
    title: '操作',
    key: 'action',
    width: 136,
    align: 'center',
    fixed: 'right'
  }
]

const loading = ref(false)
const shipSubmitting = ref(false)
const shipModalVisible = ref(false)
const shipFormRef = ref()
const assignModalVisible = ref(false)
const assignDetailLoading = ref(false)
const assignSubmitting = ref(false)
const activeFilterKey = ref('all')
const selectedShipOrder = ref(null)
const selectedAssignOrder = ref(null)
const logisticsModalVisible = ref(false)
const currentLogisticsOrderNo = ref('')
const orderPage = reactive({
  records: [],
  total: 0,
  current: 1,
  size: PAGE_SIZE
})
const pagination = reactive({
  current: 1,
  pageSize: PAGE_SIZE,
  total: 0
})
const shipForm = reactive({
  orderNo: '',
  logisticsCompany: '',
  logisticsNo: ''
})
const assignForm = reactive({
  orderNo: '',
  items: []
})

const shipRules = {
  logisticsCompany: [{ required: true, message: '请输入物流公司', trigger: 'blur' }],
  logisticsNo: [{ required: true, message: '请输入物流单号', trigger: 'blur' }]
}

const activeFilter = computed(() => {
  return FILTER_OPTIONS.find((option) => option.key === activeFilterKey.value) || FILTER_OPTIONS[0]
})

const orderRecords = computed(() => {
  return Array.isArray(orderPage.records) ? orderPage.records : []
})

const shipOrderSummary = computed(() => {
  if (!selectedShipOrder.value) {
    return '请填写物流信息后确认发货。'
  }
  return `${selectedShipOrder.value.receiveName || '--'} ${formatPhone(selectedShipOrder.value.receivePhone)}，${selectedShipOrder.value.receiveAddress || '暂无收货地址信息'}`
})

const assignOrderSummary = computed(() => {
  if (!selectedAssignOrder.value) {
    return '请按认养明细填写耳标号后确认分配。'
  }
  return `${selectedAssignOrder.value.receiveName || '--'} ${formatPhone(selectedAssignOrder.value.receivePhone)}，分配完成后订单将进入养殖中。`
})

const isSuccessCode = (code) => String(code) === '0'

const getStatusText = (status) => ORDER_STATUS_TEXT[status] || '未知状态'

const getStatusColor = (status) => {
  if (status === ORDER_STATUS.PENDING_PAYMENT) {
    return 'orange'
  }
  if (status === ORDER_STATUS.PENDING_ASSIGNMENT) {
    return 'gold'
  }
  if (status === ORDER_STATUS.PENDING_SHIPMENT) {
    return 'blue'
  }
  if (status === ORDER_STATUS.SHIPPED) {
    return 'cyan'
  }
  if (status === ORDER_STATUS.COMPLETED) {
    return 'green'
  }
  if (status === ORDER_STATUS.CANCEL) {
    return 'default'
  }
  if (status === ORDER_STATUS.AFTER_SALE) {
    return 'purple'
  }
  if (status === ORDER_STATUS.BREEDING) {
    return 'lime'
  }
  return 'default'
}

const getOrderTypeText = (orderType) => {
  if (orderType === ORDER_TYPE.ADOPT) {
    return '认养项目'
  }
  if (orderType === ORDER_TYPE.GOODS) {
    return '农产品'
  }
  return '其他订单'
}

const formatMoney = (value) => Number(value || 0).toFixed(2)

const formatDate = (value) => {
  return value ? dayjs(value).format('YYYY-MM-DD HH:mm') : '--'
}

const formatOptionalTime = (value, fallback) => {
  return value ? dayjs(value).format('YYYY-MM-DD HH:mm') : fallback
}

const formatPhone = (value) => {
  const normalizedValue = String(value || '').trim()
  if (!normalizedValue) {
    return ''
  }
  if (normalizedValue.length < 7) {
    return normalizedValue
  }
  return normalizedValue.replace(/(\d{3})\d{4}(\d+)/, '$1****$2')
}

const parseEarTags = (value) => {
  return String(value || '')
    .split(/[\n,，、\s]+/)
    .map((item) => item.trim())
    .filter(Boolean)
}

const countEarTags = (value) => parseEarTags(value).length

const canShip = (order) => {
  return (
    (order?.orderType === ORDER_TYPE.GOODS || order?.orderType === ORDER_TYPE.ADOPT) &&
    order?.orderStatus === ORDER_STATUS.PENDING_SHIPMENT
  )
}

const canAssign = (order) => {
  return order?.orderType === ORDER_TYPE.ADOPT && order?.orderStatus === ORDER_STATUS.PENDING_ASSIGNMENT
}

const canViewLogistics = (order) => {
  return Boolean(order?.orderNo && order?.logisticsNo)
}

const getProgressTitle = (order) => {
  if (order?.orderType === ORDER_TYPE.ADOPT) {
    if (order?.orderStatus === ORDER_STATUS.PENDING_ASSIGNMENT) {
      return '待分配牲畜'
    }
    if (order?.orderStatus === ORDER_STATUS.BREEDING) {
      return '已完成耳标分配'
    }
    if (order?.orderStatus === ORDER_STATUS.PENDING_SHIPMENT) {
      return '认养履约已完成'
    }
    if (order?.orderStatus === ORDER_STATUS.SHIPPED) {
      return order?.logisticsCompany || '认养订单已发货'
    }
    if (order?.orderStatus === ORDER_STATUS.COMPLETED) {
      return '认养订单已签收'
    }
    return '认养订单物流处理中'
  }
  return order?.logisticsCompany || '暂未发货'
}

const getProgressDescription = (order) => {
  if (order?.orderType === ORDER_TYPE.ADOPT) {
    if (order?.orderStatus === ORDER_STATUS.PENDING_ASSIGNMENT) {
      return '录入耳标号后将创建养殖实例'
    }
    if (order?.orderStatus === ORDER_STATUS.BREEDING) {
      return '可基于养殖实例继续记录养殖日志'
    }
    if (order?.orderStatus === ORDER_STATUS.PENDING_SHIPMENT) {
      return '全部认养实例已履约完成，待录入物流信息后发货'
    }
    if (order?.orderStatus === ORDER_STATUS.SHIPPED) {
      return order?.logisticsNo || '已录入物流信息，等待用户确认收货'
    }
    if (order?.orderStatus === ORDER_STATUS.COMPLETED) {
      return formatOptionalTime(order?.receiveTime, '用户已确认收货')
    }
    if (order?.orderStatus === ORDER_STATUS.PENDING_PAYMENT) {
      return '待买家支付后进入分配流程'
    }
    return '认养订单物流流程已结束'
  }
  return order?.logisticsNo || '暂无物流单号'
}

const getFulfillmentTimeText = (order) => {
  if (order?.orderType === ORDER_TYPE.ADOPT) {
    if (order?.orderStatus === ORDER_STATUS.PENDING_ASSIGNMENT) {
      return '待分配牲畜'
    }
    if (order?.orderStatus === ORDER_STATUS.BREEDING) {
      return '养殖中'
    }
    if (order?.orderStatus === ORDER_STATUS.PENDING_SHIPMENT) {
      return '已履约，待发货'
    }
    if (order?.orderStatus === ORDER_STATUS.SHIPPED) {
      return formatOptionalTime(order?.deliveryTime, '已发货')
    }
    if (order?.orderStatus === ORDER_STATUS.COMPLETED) {
      return formatOptionalTime(order?.deliveryTime, '已完成')
    }
    return '--'
  }
  return formatOptionalTime(order?.deliveryTime, '待发货')
}

const getActionText = (order) => {
  if (order?.orderType === ORDER_TYPE.ADOPT) {
    if (order?.orderStatus === ORDER_STATUS.BREEDING) {
      return '养殖中'
    }
    if (order?.orderStatus === ORDER_STATUS.PENDING_ASSIGNMENT) {
      return '待分配'
    }
    if (order?.orderStatus === ORDER_STATUS.PENDING_SHIPMENT) {
      return '待发货'
    }
    if (order?.orderStatus === ORDER_STATUS.SHIPPED) {
      return '已发货'
    }
    if (order?.orderStatus === ORDER_STATUS.PENDING_PAYMENT) {
      return '待付款'
    }
    if (order?.orderStatus === ORDER_STATUS.CANCEL) {
      return '已关闭'
    }
    if (order?.orderStatus === ORDER_STATUS.AFTER_SALE) {
      return '售后中'
    }
    return order?.orderStatus === ORDER_STATUS.COMPLETED ? '已完成' : '--'
  }
  if (order?.orderStatus === ORDER_STATUS.SHIPPED || order?.orderStatus === ORDER_STATUS.COMPLETED) {
    return '已发货'
  }
  if (order?.orderStatus === ORDER_STATUS.PENDING_PAYMENT) {
    return '待付款'
  }
  if (order?.orderStatus === ORDER_STATUS.CANCEL) {
    return '已关闭'
  }
  if (order?.orderStatus === ORDER_STATUS.AFTER_SALE) {
    return '售后中'
  }
  return '--'
}

const resetShipForm = () => {
  shipForm.orderNo = ''
  shipForm.logisticsCompany = ''
  shipForm.logisticsNo = ''
  selectedShipOrder.value = null
  shipFormRef.value?.resetFields?.()
}

const resetAssignForm = () => {
  assignForm.orderNo = ''
  assignForm.items = []
  selectedAssignOrder.value = null
}

const openLogisticsModal = (order) => {
  if (!canViewLogistics(order)) {
    message.warning('褰撳墠璁㈠崟鏆傛棤鐗╂祦淇℃伅')
    return
  }
  currentLogisticsOrderNo.value = order.orderNo
  logisticsModalVisible.value = true
}

const handleLogisticsModalClose = () => {
  logisticsModalVisible.value = false
  currentLogisticsOrderNo.value = ''
}

const openShipModal = async (order) => {
  selectedShipOrder.value = order
  shipForm.orderNo = order.orderNo || ''
  shipForm.logisticsCompany = order.logisticsCompany || ''
  shipForm.logisticsNo = order.logisticsNo || ''
  shipModalVisible.value = true
  await nextTick()
  shipFormRef.value?.clearValidate?.()
}

const openAssignModal = async (order) => {
  selectedAssignOrder.value = order
  assignForm.orderNo = order.orderNo || ''
  assignForm.items = []
  assignModalVisible.value = true
  assignDetailLoading.value = true
  try {
    const response = await getAdoptOrderDetail(order.orderNo)
    if (!isSuccessCode(response.code)) {
      message.error(response.message || '获取认养订单明细失败')
      assignModalVisible.value = false
      resetAssignForm()
      return
    }
    const details = Array.isArray(response.data) ? response.data : []
    assignForm.items = details.map((item, index) => ({
      key: `${item.id || item.adoptItemId || index}`,
      adoptItemId: item.adoptItemId,
      itemName: item.itemName,
      quantity: Number(item.quantity || 0),
      earTagInput: ''
    }))
    if (!assignForm.items.length) {
      message.warning('当前订单暂无可分配的认养明细')
    }
  } catch (error) {
    console.error('获取认养订单明细失败', error)
    message.error('获取认养订单明细失败，请稍后重试')
    assignModalVisible.value = false
    resetAssignForm()
  } finally {
    assignDetailLoading.value = false
  }
}

const handleShipCancel = () => {
  shipModalVisible.value = false
  resetShipForm()
}

const handleAssignCancel = () => {
  assignModalVisible.value = false
  resetAssignForm()
}

const applyPageData = (data = {}) => {
  orderPage.records = Array.isArray(data.records) ? data.records : []
  orderPage.total = Number(data.total || 0)
  orderPage.current = Number(data.current || pagination.current)
  orderPage.size = Number(data.size || pagination.pageSize)

  pagination.current = orderPage.current
  pagination.total = orderPage.total
}

const fetchOrders = async () => {
  loading.value = true
  try {
    const payload = {
      current: pagination.current,
      size: pagination.pageSize
    }

    if (activeFilter.value.value !== undefined) {
      payload.orderStatus = activeFilter.value.value
    }

    const response = await getFarmerOrderList(payload)
    if (!isSuccessCode(response.code)) {
      message.error(response.message || '获取订单列表失败')
      return
    }

    applyPageData(response.data || {})
  } catch (error) {
    console.error('获取农户订单列表失败', error)
    message.error('获取订单列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleShipSubmit = async () => {
  try {
    await shipFormRef.value?.validate()
  } catch (error) {
    return
  }

  shipSubmitting.value = true
  try {
    const response = await shipFarmerOrder({
      orderNo: shipForm.orderNo,
      logisticsCompany: shipForm.logisticsCompany,
      logisticsNo: shipForm.logisticsNo
    })

    if (!isSuccessCode(response.code)) {
      message.error(response.message || '订单发货失败')
      return
    }

    message.success('订单已发货')
    shipModalVisible.value = false
    resetShipForm()

    if (
      activeFilterKey.value === 'pendingShipment' &&
      orderRecords.value.length === 1 &&
      pagination.current > 1
    ) {
      pagination.current -= 1
    }

    fetchOrders()
  } catch (error) {
    console.error('订单发货失败', error)
    message.error('订单发货失败，请稍后重试')
  } finally {
    shipSubmitting.value = false
  }
}

const buildAssignPayload = () => {
  if (!assignForm.items.length) {
    throw new Error('当前订单暂无可分配的认养明细')
  }

  const uniqueEarTags = new Set()
  return {
    orderNo: assignForm.orderNo,
    items: assignForm.items.map((item) => {
      const normalizedEarTags = parseEarTags(item.earTagInput).map((earTag) => earTag.replace(/^0+(?=\d)/, ''))
      if (normalizedEarTags.length !== Number(item.quantity || 0)) {
        throw new Error(`${item.itemName || '认养项目'}需要填写${item.quantity}个耳标号`)
      }
      normalizedEarTags.forEach((earTag) => {
        if (!/^\d+$/.test(earTag)) {
          throw new Error('耳标号只能填写数字')
        }
        if (earTag === '0') {
          throw new Error('耳标号必须大于0')
        }
        if (uniqueEarTags.has(earTag)) {
          throw new Error('耳标号不能重复')
        }
        uniqueEarTags.add(earTag)
      })
      return {
        adoptItemId: item.adoptItemId,
        earTagNos: normalizedEarTags
      }
    })
  }
}

const handleAssignSubmit = async () => {
  let payload
  try {
    payload = buildAssignPayload()
  } catch (error) {
    message.error(error.message || '请检查耳标号填写内容')
    return
  }

  assignSubmitting.value = true
  try {
    const response = await assignFarmerAdoptOrder(payload)
    if (!isSuccessCode(response.code)) {
      message.error(response.message || '分配牲畜失败')
      return
    }

    message.success('牲畜分配成功，已创建养殖实例')
    assignModalVisible.value = false
    resetAssignForm()

    if (
      activeFilterKey.value === 'pendingAssignment' &&
      orderRecords.value.length === 1 &&
      pagination.current > 1
    ) {
      pagination.current -= 1
    }

    fetchOrders()
  } catch (error) {
    console.error('分配牲畜失败', error)
    message.error('分配牲畜失败，请稍后重试')
  } finally {
    assignSubmitting.value = false
  }
}

const handleFilterChange = (filterKey) => {
  activeFilterKey.value = filterKey
  pagination.current = 1
  fetchOrders()
}

const handlePageChange = (page) => {
  pagination.current = page
  fetchOrders()
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.farmer-order-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-width: 1440px;
  margin: 0 auto;
}

.page-header,
.toolbar-card,
.table-card {
  background: #fff;
  border: 1px solid #edf2ee;
  border-radius: 18px;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.04);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
}

.page-title {
  margin: 0;
  color: #17311f;
  font-size: 28px;
  font-weight: 700;
}

.page-desc {
  margin: 8px 0 0;
  color: #6b7c72;
  font-size: 14px;
}

.toolbar-card {
  padding: 20px 24px 8px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 12px;
}

.summary-item {
  padding: 16px 18px;
  border-radius: 14px;
  background: #f7faf8;
  border: 1px solid #e7efe9;
}

.summary-label {
  display: block;
  margin-bottom: 8px;
  color: #8a9b90;
  font-size: 12px;
}

.summary-value {
  display: block;
  color: #17311f;
  font-size: 22px;
  font-weight: 700;
  line-height: 1.2;
}

.summary-hint {
  margin: 8px 0 0;
  color: #7b8b7f;
  font-size: 12px;
  line-height: 1.6;
}

.status-tabs :deep(.ant-tabs-nav) {
  margin-bottom: 0;
}

.table-card {
  padding: 20px 24px;
}

.table-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.table-title {
  margin: 0;
  color: #17311f;
  font-size: 20px;
  font-weight: 700;
}

.table-desc {
  margin: 6px 0 0;
  color: #7b8b7f;
  font-size: 13px;
}

.ship-alert,
.assign-alert {
  margin-bottom: 16px;
}

.order-info-cell,
.receiver-cell,
.amount-cell,
.logistics-cell,
.time-cell {
  line-height: 1.7;
}

.primary-text {
  margin: 0;
  color: #1f2937;
  font-size: 13px;
  font-weight: 600;
  word-break: break-all;
}

.secondary-text {
  margin: 0;
  color: #7b8b7f;
  font-size: 12px;
  word-break: break-word;
}

.amount-primary {
  margin: 0;
  color: #17311f;
  font-size: 18px;
  font-weight: 700;
}

.action-text {
  display: inline-block;
  text-align: center;
}

.clamp-two-lines {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.assign-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.assign-tip {
  margin: 0;
  color: #6b7c72;
  font-size: 13px;
  line-height: 1.7;
}

.assign-item-card {
  padding: 16px;
  border: 1px solid #e7efe9;
  border-radius: 14px;
  background: #fafcfb;
}

.assign-item-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 12px;
}

.assign-item-title {
  margin: 0;
  color: #17311f;
  font-size: 15px;
  font-weight: 700;
}

.assign-item-meta {
  margin: 6px 0 0;
  color: #7b8b7f;
  font-size: 12px;
}

.assign-item-hint {
  margin: 8px 0 0;
  color: #7b8b7f;
  font-size: 12px;
}

.order-table :deep(.ant-table-thead > tr > th) {
  color: #445449;
  font-weight: 600;
  background: #f7faf8;
}

.order-table :deep(.ant-table-tbody > tr:hover > td) {
  background: #fbfdfb;
}

@media (max-width: 992px) {
  .summary-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .page-header,
  .toolbar-card,
  .table-card {
    padding-left: 16px;
    padding-right: 16px;
  }

  .page-header {
    flex-direction: column;
    align-items: stretch;
  }

  .page-title {
    font-size: 24px;
  }

  .assign-item-head {
    flex-direction: column;
  }
}
</style>
