<template>
  <div class="product-list-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">我的商品</h2>
        <p class="page-desc">
          管理当前农户账号发布的商品，支持查询、上下架以及修改商品规格、价格和库存。
        </p>
      </div>
      <a-button type="primary" @click="router.push('/farmer/spu/create')">
        创建商品
      </a-button>
    </div>

    <a-card :bordered="false" class="search-card">
      <a-form layout="inline" :model="searchForm" @finish="handleSearch">
        <a-form-item label="商品名称">
          <a-input
            v-model:value="searchForm.spuName"
            placeholder="请输入商品名称"
            allow-clear
            style="width: 220px"
          />
        </a-form-item>
        <a-form-item label="上架状态">
          <a-select
            v-model:value="searchForm.status"
            allow-clear
            placeholder="全部"
            style="width: 140px"
          >
            <a-select-option :value="1">已上架</a-select-option>
            <a-select-option :value="0">已下架</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="审核状态">
          <a-select
            v-model:value="searchForm.auditStatus"
            allow-clear
            placeholder="全部"
            style="width: 140px"
          >
            <a-select-option :value="0">待审核</a-select-option>
            <a-select-option :value="1">已通过</a-select-option>
            <a-select-option :value="2">已驳回</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item class="search-actions">
          <a-space>
            <a-button type="primary" html-type="submit">查询</a-button>
            <a-button @click="handleReset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card :bordered="false" class="table-card">
      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'images'">
            <a-image
              :src="getCoverImage(record.images)"
              :width="64"
              :height="64"
              class="cover-image"
              fallback="https://placeholder.com/64x64"
            />
          </template>

          <template v-else-if="column.key === 'categoryId'">
            {{ categoryNameMap[record.categoryId] || `分类 #${record.categoryId}` }}
          </template>

          <template v-else-if="column.key === 'minPrice'">
            {{ formatPrice(record.minPrice) }}
          </template>

          <template v-else-if="column.key === 'auditStatus'">
            <a-tag :color="auditStatusColorMap[record.auditStatus] || 'default'">
              {{ auditStatusTextMap[record.auditStatus] || '未知' }}
            </a-tag>
          </template>

          <template v-else-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'blue' : 'default'">
              {{ record.status === 1 ? '已上架' : '已下架' }}
            </a-tag>
          </template>

          <template v-else-if="column.key === 'createTime'">
            {{ formatDateTime(record.createTime) }}
          </template>

          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleEdit(record)">
                修改
              </a-button>

              <a-popconfirm
                v-if="record.auditStatus === 1 && record.status === 0"
                title="确认上架该商品吗？"
                ok-text="确认"
                cancel-text="取消"
                @confirm="handleToggleStatus(record, 1)"
              >
                <a-button type="link" size="small">上架</a-button>
              </a-popconfirm>

              <a-popconfirm
                v-if="record.auditStatus === 1 && record.status === 1"
                title="确认下架该商品吗？"
                ok-text="确认"
                cancel-text="取消"
                @confirm="handleToggleStatus(record, 0)"
              >
                <a-button type="link" danger size="small">下架</a-button>
              </a-popconfirm>

              <span v-if="record.auditStatus !== 1" class="muted-text">
                {{ record.auditStatus === 0 ? '审核中' : '修改后需重新提交审核' }}
              </span>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:open="editVisible"
      title="修改商品"
      :width="1040"
      ok-text="保存修改"
      cancel-text="取消"
      :confirm-loading="editSubmitting"
      :mask-closable="false"
      :body-style="{ maxHeight: '70vh', overflowY: 'auto', padding: '20px 24px' }"
      destroy-on-close
      @ok="handleEditSubmit"
    >
      <a-spin :spinning="editDetailLoading">
        <a-form layout="vertical">
          <a-form-item label="商品分类" required>
            <CategorySelect v-model="editForm.categoryId" />
          </a-form-item>
          <a-form-item label="商品名称" required>
            <a-input
              v-model:value="editForm.title"
              placeholder="请输入商品名称"
              :maxlength="50"
              show-count
            />
          </a-form-item>
          <a-form-item label="商品主图">
            <ImageUpload v-model:value="editForm.images" biz-code="PRODUCT_SPU_COVER" />
          </a-form-item>

          <a-divider>基础属性</a-divider>

          <a-form-item label="商品基础属性">
            <AttributeSelect
              :attributes="attributeList"
              :selected-attr-ids="selectedBaseAttributes.map(item => item.key)"
              :type-filter="0"
              @add="addEditBaseAttribute"
            />
          </a-form-item>
          <DynamicAttribute
            :attributes="selectedBaseAttributes"
            :type="0"
            @remove-attribute="removeEditAttribute"
          />

          <a-divider>销售属性与SKU</a-divider>

          <a-form-item label="商品销售属性" required>
            <AttributeSelect
              :attributes="attributeList"
              :selected-attr-ids="selectedSaleAttributes.map(item => item.key)"
              :type-filter="1"
              @add="addEditSaleAttribute"
            />
          </a-form-item>
          <DynamicAttribute
            :attributes="selectedSaleAttributes"
            :type="1"
            @remove-attribute="removeEditAttribute"
          />

          <a-form-item label="SKU明细" required>
            <SkuTable
              :sale-attributes="saleAttributesForTable"
              :initial-skus="editSkuSeedList"
              @change="handleEditSkuChange"
            />
          </a-form-item>

          <a-alert
            class="edit-form-alert"
            type="info"
            show-icon
            message="只有在保存了实际修改后，商品才会自动下架并重新进入审核流程。"
          />
        </a-form>
      </a-spin>
    </a-modal>
  </div>
</template>

<script setup>
import { computed, createVNode, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { ExclamationCircleOutlined } from '@ant-design/icons-vue'
import { useRouter } from 'vue-router'
import { getAttributesByCategoryId } from '@/api/attribute'
import { getCategoryTree } from '@/api/category'
import {
  getSpuDetail,
  listMySpuByPage,
  listSpuAttrValuesBySpuId,
  updateSpu,
  updateSpuStatus
} from '@/api/spu'
import CategorySelect from './components/CategorySelect.vue'
import AttributeSelect from './components/AttributeSelect.vue'
import DynamicAttribute from './components/DynamicAttributeList.vue'
import SkuTable from './components/SkuTable.vue'
import ImageUpload from '@/components/Upload/ImageUpload.vue'

const router = useRouter()

const loading = ref(false)
const editVisible = ref(false)
const editSubmitting = ref(false)
const editDetailLoading = ref(false)
const dataSource = ref([])
const categoryNameMap = ref({})
const editSnapshot = ref('')
const attributeList = ref([])
const selectedBaseAttributes = ref([])
const selectedSaleAttributes = ref([])
const editSkuSeedList = ref([])
const editSkuList = ref([])
const hydratingEditData = ref(false)

const searchForm = reactive({
  spuName: '',
  status: undefined,
  auditStatus: undefined
})

const editForm = reactive({
  id: undefined,
  title: '',
  categoryId: '',
  images: ''
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total) => `共 ${total} 条`
})

const auditStatusTextMap = {
  0: '待审核',
  1: '已通过',
  2: '已驳回'
}

const auditStatusColorMap = {
  0: 'orange',
  1: 'green',
  2: 'red'
}

const columns = [
  {
    title: '主图',
    dataIndex: 'images',
    key: 'images',
    width: 96
  },
  {
    title: '商品名称',
    dataIndex: 'title',
    key: 'title',
    ellipsis: true
  },
  {
    title: '商品分类',
    dataIndex: 'categoryId',
    key: 'categoryId',
    width: 160
  },
  {
    title: '最低价',
    dataIndex: 'minPrice',
    key: 'minPrice',
    width: 120
  },
  {
    title: '审核状态',
    dataIndex: 'auditStatus',
    key: 'auditStatus',
    width: 120
  },
  {
    title: '上架状态',
    dataIndex: 'status',
    key: 'status',
    width: 120
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 180
  },
  {
    title: '操作',
    key: 'action',
    width: 220,
    fixed: 'right'
  }
]

const normalizeEditValue = (value) => String(value ?? '').trim()

const normalizeTagValues = (values = []) =>
  [...new Set(values.map(item => normalizeEditValue(item)).filter(Boolean))]

const normalizeSkuSpecs = (specs = []) =>
  [...specs]
    .filter(spec => spec?.attrId !== undefined && spec?.attrId !== null && normalizeEditValue(spec?.value))
    .map(spec => ({
      attrId: String(spec.attrId),
      attrName: spec.attrName || resolveAttributeLabel(spec.attrId),
      value: normalizeEditValue(spec.value)
    }))
    .sort((left, right) => String(left.attrId).localeCompare(String(right.attrId)))

const buildSkuKey = (specs = []) =>
  normalizeSkuSpecs(specs)
    .map(spec => `${spec.attrId}:${spec.value}`)
    .join('|')

const normalizeSkuDraftList = (skus = []) =>
  skus
    .map((sku) => ({
      specs: normalizeSkuSpecs(sku?.specs || []),
      stock: Number(sku?.stock ?? 0),
      price: Number(sku?.price ?? 0)
    }))
    .filter(item => item.specs.length > 0)
    .sort((left, right) => buildSkuKey(left.specs).localeCompare(buildSkuKey(right.specs)))

const saleAttributesForTable = computed(() =>
  selectedSaleAttributes.value.map(attr => ({
    key: String(attr.key),
    label: attr.label,
    values: normalizeTagValues(attr.values)
  }))
)

const attributeIdByNameMap = computed(() => {
  const map = new Map()
  attributeList.value.forEach((item) => {
    map.set(item.name, String(item.id))
  })
  return map
})

const createEditSnapshot = () => JSON.stringify({
  title: normalizeEditValue(editForm.title),
  categoryId: editForm.categoryId ? String(editForm.categoryId) : '',
  images: normalizeEditValue(editForm.images),
  baseAttributes: [...selectedBaseAttributes.value]
    .map(item => ({
      key: String(item.key),
      value: normalizeEditValue(item.value)
    }))
    .sort((left, right) => left.key.localeCompare(right.key)),
  saleAttributes: saleAttributesForTable.value
    .map(item => ({
      key: String(item.key),
      values: [...item.values].sort((left, right) => left.localeCompare(right))
    }))
    .sort((left, right) => left.key.localeCompare(right.key)),
  skus: normalizeSkuDraftList(editSkuList.value).map(item => ({
    specs: item.specs,
    stock: item.stock,
    price: Number(item.price.toFixed(2))
  }))
})

const hasEditChanged = () => createEditSnapshot() !== editSnapshot.value

const resolveAttributeLabel = (attrId) =>
  attributeList.value.find(item => String(item.id) === String(attrId))?.name || `属性${attrId}`

const parseJsonObject = (value) => {
  if (value && typeof value === 'object' && !Array.isArray(value)) {
    return value
  }
  if (typeof value !== 'string' || !value.trim()) {
    return {}
  }
  try {
    const parsed = JSON.parse(value)
    return parsed && typeof parsed === 'object' && !Array.isArray(parsed) ? parsed : {}
  } catch (error) {
    return {}
  }
}

const getCoverImage = (images) => {
  if (!images) return ''
  return String(images)
    .split(',')
    .map((item) => item.trim())
    .find(Boolean) || ''
}

const resetEditSpecState = () => {
  selectedBaseAttributes.value = []
  selectedSaleAttributes.value = []
  editSkuSeedList.value = []
  editSkuList.value = []
}

const resetEditState = () => {
  editForm.id = undefined
  editForm.title = ''
  editForm.categoryId = ''
  editForm.images = ''
  attributeList.value = []
  editSnapshot.value = ''
  resetEditSpecState()
}

const traverseCategories = (nodes, map) => {
  nodes.forEach((node) => {
    map[node.id] = node.name
    if (Array.isArray(node.children) && node.children.length > 0) {
      traverseCategories(node.children, map)
    }
  })
}

const fetchCategoryTree = async () => {
  try {
    const res = await getCategoryTree()
    const map = {}
    traverseCategories(res?.data || [], map)
    categoryNameMap.value = map
  } catch (error) {
    console.error('加载分类树失败', error)
  }
}

const fetchEditAttributes = async (categoryId) => {
  if (!categoryId) {
    attributeList.value = []
    return
  }
  const res = await getAttributesByCategoryId(Number(categoryId))
  attributeList.value = Array.isArray(res?.data) ? res.data : []
}

const createBaseAttributeItem = (attrId, value = '') => ({
  key: String(attrId),
  label: resolveAttributeLabel(attrId),
  value
})

const createSaleAttributeItem = (attrId, values = []) => ({
  key: String(attrId),
  label: resolveAttributeLabel(attrId),
  values: normalizeTagValues(values)
})

const fallbackBaseAttributes = (productSpu) => {
  const attrMap = parseJsonObject(productSpu?.attributes)
  return Object.entries(attrMap)
    .map(([attrName, attrValue]) => {
      const attrId = attributeIdByNameMap.value.get(attrName)
      if (!attrId) {
        return null
      }
      return createBaseAttributeItem(attrId, normalizeEditValue(attrValue))
    })
    .filter(Boolean)
}

const normalizeSaleAttrValuesFromSku = (sku) => {
  const directValues = parseJsonObject(sku?.saleAttrValues)
  if (Object.keys(directValues).length > 0) {
    return Object.fromEntries(
      Object.entries(directValues)
        .map(([attrId, value]) => [String(attrId), normalizeEditValue(value)])
        .filter(([, value]) => value)
    )
  }

  const attrByName = parseJsonObject(sku?.saleAttribute)
  const attrValues = {}
  Object.entries(attrByName).forEach(([attrName, attrValue]) => {
    const attrId = attributeIdByNameMap.value.get(attrName)
    const normalizedValue = normalizeEditValue(attrValue)
    if (attrId && normalizedValue) {
      attrValues[attrId] = normalizedValue
    }
  })
  return attrValues
}

const buildEditSkuSeedList = (productSkus = []) =>
  normalizeSkuDraftList(
    productSkus.map((sku) => {
      const attrValues = normalizeSaleAttrValuesFromSku(sku)
      const specs = Object.entries(attrValues).map(([attrId, value]) => ({
        attrId: String(attrId),
        attrName: resolveAttributeLabel(attrId),
        value
      }))
      return {
        specs,
        stock: Number(sku?.stock ?? 0),
        price: Number(sku?.price ?? 0)
      }
    })
  )

const hydrateEditForm = async (record, detailData, attrValueList) => {
  const productSpu = detailData?.productSpu || {}
  const productSkus = Array.isArray(detailData?.productSkus) ? detailData.productSkus : []

  hydratingEditData.value = true
  try {
    editForm.id = record.id
    editForm.title = productSpu.title || record.title || ''
    editForm.categoryId = productSpu.categoryId ? String(productSpu.categoryId) : String(record.categoryId || '')
    editForm.images = getCoverImage(productSpu.images || record.images)

    await fetchEditAttributes(editForm.categoryId)

    if (Array.isArray(attrValueList) && attrValueList.length > 0) {
      selectedBaseAttributes.value = attrValueList.map(item =>
        createBaseAttributeItem(item.attrId, normalizeEditValue(item.attrValue))
      )
    } else {
      selectedBaseAttributes.value = fallbackBaseAttributes(productSpu)
    }

    const saleAttrMap = new Map()
    productSkus.forEach((sku) => {
      const attrValues = normalizeSaleAttrValuesFromSku(sku)
      Object.entries(attrValues).forEach(([attrId, attrValue]) => {
        if (!saleAttrMap.has(attrId)) {
          saleAttrMap.set(attrId, createSaleAttributeItem(attrId))
        }
        saleAttrMap.get(attrId).values.push(attrValue)
      })
    })
    selectedSaleAttributes.value = [...saleAttrMap.values()].map(item =>
      createSaleAttributeItem(item.key, item.values)
    )

    const nextSkuSeedList = buildEditSkuSeedList(productSkus)
    await nextTick()
    editSkuSeedList.value = nextSkuSeedList
    editSkuList.value = nextSkuSeedList
    editSnapshot.value = createEditSnapshot()
  } finally {
    hydratingEditData.value = false
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await listMySpuByPage({
      current: pagination.current,
      size: pagination.pageSize,
      spuName: searchForm.spuName || undefined,
      status: searchForm.status,
      auditStatus: searchForm.auditStatus
    })

    if (res.code === '0' && res.data) {
      dataSource.value = res.data.records || []
      pagination.total = res.data.total || 0
      return
    }

    message.error(res.message || '加载商品列表失败')
  } catch (error) {
    console.error('加载商品列表失败', error)
    message.error('加载商品列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchData()
}

const handleReset = () => {
  searchForm.spuName = ''
  searchForm.status = undefined
  searchForm.auditStatus = undefined
  handleSearch()
}

const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchData()
}

const handleToggleStatus = async (record, status) => {
  try {
    const res = await updateSpuStatus(record.id, status)
    if (res.code === '0') {
      message.success(status === 1 ? '商品已上架' : '商品已下架')
      fetchData()
      return
    }

    message.error(res.message || '更新商品状态失败')
  } catch (error) {
    console.error('更新商品状态失败', error)
    message.error('更新商品状态失败')
  }
}

const addEditBaseAttribute = (id) => {
  if (selectedBaseAttributes.value.some(item => item.key === id)) {
    return
  }
  selectedBaseAttributes.value.push(createBaseAttributeItem(id))
}

const addEditSaleAttribute = (id) => {
  if (selectedSaleAttributes.value.some(item => item.key === id)) {
    return
  }
  selectedSaleAttributes.value.push(createSaleAttributeItem(id))
}

const removeEditAttribute = (id, type) => {
  if (type === 0) {
    selectedBaseAttributes.value = selectedBaseAttributes.value.filter(item => item.key !== id)
    return
  }
  selectedSaleAttributes.value = selectedSaleAttributes.value.filter(item => item.key !== id)
}

const handleEditSkuChange = (skus) => {
  editSkuList.value = normalizeSkuDraftList(skus)
}

const openEditModal = async (record) => {
  resetEditState()
  editVisible.value = true
  editDetailLoading.value = true
  try {
    const detailRes = await getSpuDetail(record.id)
    if (detailRes.code !== '0' || !detailRes.data) {
      throw new Error(detailRes.message || '获取商品详情失败')
    }

    let attrValueList = []
    try {
      const attrValueRes = await listSpuAttrValuesBySpuId(record.id)
      if (attrValueRes.code === '0' && Array.isArray(attrValueRes.data)) {
        attrValueList = attrValueRes.data
      }
    } catch (error) {
      console.warn('加载商品基础属性失败，使用详情回填兜底', error)
    }

    await hydrateEditForm(record, detailRes.data, attrValueList)
  } catch (error) {
    console.error('加载商品详情失败', error)
    message.error('加载商品详情失败')
    editVisible.value = false
    resetEditState()
  } finally {
    editDetailLoading.value = false
  }
}

const handleEdit = (record) => {
  if (record.status === 1) {
    Modal.confirm({
      title: '确认修改商品吗？',
      icon: createVNode(ExclamationCircleOutlined),
      content: '只有在保存了实际修改后，商品才会自动下架并重新进入审核流程。',
      okText: '继续修改',
      cancelText: '取消',
      onOk: () => openEditModal(record)
    })
    return
  }

  openEditModal(record)
}

const validateEditPayload = () => {
  if (!editForm.categoryId) {
    message.warning('请选择商品分类')
    return false
  }
  if (!normalizeEditValue(editForm.title)) {
    message.warning('请输入商品名称')
    return false
  }

  const emptyBaseAttr = selectedBaseAttributes.value.find(item => !normalizeEditValue(item.value))
  if (emptyBaseAttr) {
    message.warning(`请填写基础属性“${emptyBaseAttr.label}”的值`)
    return false
  }

  if (selectedSaleAttributes.value.length === 0) {
    message.warning('请至少配置一个销售属性')
    return false
  }

  const invalidSaleAttr = saleAttributesForTable.value.find(item => item.values.length === 0)
  if (invalidSaleAttr) {
    message.warning(`请至少为销售属性“${invalidSaleAttr.label}”填写一个可选值`)
    return false
  }

  if (editSkuList.value.length === 0) {
    message.warning('请至少配置一个 SKU')
    return false
  }

  const invalidSku = editSkuList.value.find((sku) => {
    const numericPrice = Number(sku.price)
    const numericStock = Number(sku.stock)
    return sku.specs.length === 0 || Number.isNaN(numericPrice) || numericPrice <= 0 || Number.isNaN(numericStock) || numericStock < 0
  })
  if (invalidSku) {
    message.warning(`SKU [${invalidSku.specs.map(spec => `${spec.attrName}:${spec.value}`).join(' / ')}] 的价格和库存填写有误`)
    return false
  }

  return true
}

const handleEditSubmit = async () => {
  if (editDetailLoading.value) {
    return
  }
  if (!validateEditPayload()) {
    return
  }
  if (!hasEditChanged()) {
    message.info('商品信息未发生变化，无需重新提交审核')
    return
  }

  editSubmitting.value = true
  try {
    const payload = {
      id: editForm.id,
      title: normalizeEditValue(editForm.title),
      categoryId: Number(editForm.categoryId),
      images: editForm.images,
      attrItems: selectedBaseAttributes.value.map(item => ({
        attrId: Number(item.key),
        attrValue: normalizeEditValue(item.value)
      })),
      saleAttrs: saleAttributesForTable.value.map(item => ({
        attrId: Number(item.key),
        values: item.values
      })),
      skuItems: normalizeSkuDraftList(editSkuList.value).map(item => ({
        price: Number(item.price),
        stock: Number(item.stock),
        image: '',
        attrValues: Object.fromEntries(item.specs.map(spec => [Number(spec.attrId), spec.value]))
      }))
    }

    const res = await updateSpu(payload)

    if (res.code === '0') {
      message.success('商品修改成功，已重新提交审核')
      editVisible.value = false
      fetchData()
      return
    }

    message.error(res.message || '修改商品失败')
  } catch (error) {
    console.error('修改商品失败', error)
    message.error('修改商品失败')
  } finally {
    editSubmitting.value = false
  }
}

const formatPrice = (price) => {
  if (price === null || price === undefined || price === '') {
    return '--'
  }
  const numericPrice = Number(price)
  return Number.isNaN(numericPrice) ? '--' : `￥${numericPrice.toFixed(2)}`
}

const formatDateTime = (value) => {
  if (!value) return '--'
  return String(value).replace('T', ' ')
}

watch(
  () => editForm.categoryId,
  async (newValue, oldValue) => {
    if (!editVisible.value || hydratingEditData.value || newValue === oldValue) {
      return
    }
    try {
      await fetchEditAttributes(newValue)
      resetEditSpecState()
      message.info('已切换商品分类，请重新选择基础属性、销售属性和 SKU')
    } catch (error) {
      console.error('加载分类属性失败', error)
      message.error('加载分类属性失败')
    }
  }
)

watch(
  () => editVisible.value,
  (open) => {
    if (!open) {
      resetEditState()
    }
  }
)

onMounted(() => {
  fetchCategoryTree()
  fetchData()
})
</script>

<style scoped>
.product-list-page {
  max-width: 1280px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 24px;
}

.page-title {
  margin: 0 0 8px;
  font-size: 24px;
  font-weight: 600;
  color: #1f1f1f;
}

.page-desc {
  margin: 0;
  color: #8c8c8c;
  font-size: 14px;
}

.search-card {
  margin-bottom: 16px;
}

.search-actions {
  margin-left: auto;
}

.table-card {
  min-height: 420px;
}

:deep(.cover-image img) {
  object-fit: cover;
}

.muted-text {
  color: #999;
  font-size: 12px;
}

.edit-tip {
  margin-top: 8px;
  color: #8c8c8c;
  font-size: 12px;
}

.edit-form-alert {
  margin-top: 8px;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: stretch;
  }

  .search-actions {
    margin-left: 0;
  }
}
</style>
