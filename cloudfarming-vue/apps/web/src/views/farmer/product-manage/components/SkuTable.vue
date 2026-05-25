<template>
  <div class="sku-table-wrapper">
    <a-table
      :columns="columns"
      :data-source="tableData"
      :pagination="false"
      bordered
      :row-key="rowKey"
      size="small"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'stock'">
          <a-input-number
            :min="0"
            :value="getStock(record.__skuKey__)"
            @change="(val) => updateStock(record.__skuKey__, val)"
            style="width: 120px"
          />
        </template>
        <template v-else-if="column.key === 'price'">
          <a-input-number
            :min="0"
            :value="getPrice(record.__skuKey__)"
            @change="(val) => updatePrice(record.__skuKey__, val)"
            style="width: 120px"
            :precision="2"
          />
        </template>
      </template>
    </a-table>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'

/**
 * 组件Props - 接收父组件传入的销售属性配置
 */
const props = defineProps({
  saleAttributes: {
    type: Array,
    default: () => []
  },
  initialSkus: {
    type: Array,
    default: () => []
  }
})

/**
 * 组件事件 - 当SKU数据发生变化时通知父组件
 * @param skus - 最新的SKU列表，包含所有规格组合的价格和库存
 */
const emit = defineEmits(['change'])

/**
 * 生成SKU的唯一标识键
 * 格式: "attrId:value|attrId:value|..."
 * 例如: "1:红色|2:S"
 * 用于在不同地方唯一标识一个SKU组合
 */
function generateSkuKey(specs) {
  return normalizeSpecs(specs)
    .map(s => `${s.attrId}:${s.value}`)
    .join('|')
}

function normalizeSpecs(specs = []) {
  return [...specs]
    .filter(spec => spec?.attrId !== undefined && spec?.attrId !== null && String(spec?.value ?? '').trim())
    .map(spec => ({
      attrId: String(spec.attrId),
      attrName: spec.attrName,
      value: String(spec.value).trim()
    }))
    .sort((left, right) => String(left.attrId).localeCompare(String(right.attrId)))
}

/**
 * 计算笛卡尔积
 * 将多个数组的所有可能组合生成出来
 * 例如: [['红色','蓝色'], ['S','M']] -> [['红色','S'], ['红色','M'], ['蓝色','S'], ['蓝色','M']]
 */
function cartesian(list) {
  if (!list || list.length === 0) return []
  return list.reduce(
    (acc, curr) => acc.flatMap(a => curr.map(c => [...a, c])),
    [[]]
  )
}

/**
 * 库存数据映射
 * key: SKU唯一标识键
 * value: 库存数量
 * 使用Map而非数组，便于通过key快速查找和更新
 */
const stockMap = ref(new Map())

/**
 * 价格数据映射
 * key: SKU唯一标识键
 * value: 价格
 */
const priceMap = ref(new Map())

/**
 * 从Map中安全获取值
 */
function getStock(key) {
  return stockMap.value.get(key) ?? 0
}

function getPrice(key) {
  return priceMap.value.get(key) ?? 0
}

/**
 * SKU列表计算属性
 * 根据销售属性配置，生成所有可能的SKU组合
 * 1. 将每个属性的可选值转换为 SpecItem 数组
 * 2. 使用笛卡尔积生成所有组合
 * 3. 为每个组合生成唯一key，并关联库存和价格
 */
const skuList = computed(() => {
  const attrs = props.saleAttributes
  if (!attrs || attrs.length === 0) return []

  // 将每个属性的值转换为 SpecItem 数组
  const groups = attrs.map(attr =>
    [...new Set((attr.values || []).map(v => String(v ?? '').trim()).filter(Boolean))].map(v => ({
      attrId: String(attr.key),
      attrName: attr.label,
      value: v
    }))
  )

  // 如果有属性没有可选值，返回空
  if (groups.some(g => !g || g.length === 0)) return []

  // 生成笛卡尔积
  const combos = cartesian(groups)

  // 为每个组合生成SKU对象
  return combos.map(specs => {
    const normalizedSpecs = normalizeSpecs(specs)
    const key = generateSkuKey(normalizedSpecs)
    return {
      specs: normalizedSpecs,
      stock: stockMap.value.get(key) ?? 0,
      price: priceMap.value.get(key) ?? 0,
      _key: key
    }
  })
})

/**
 * 表格列配置计算属性
 * 根据销售属性动态生成列：
 * - 每个销售属性一列，显示其值
 * - 库存列
 * - 价格列
 */
const columns = computed(() => [
  ...props.saleAttributes.map(attr => ({
    title: attr.label,
    dataIndex: attr.key,
    key: attr.key
  })),
  { title: '库存', key: 'stock' },
  { title: '价格', key: 'price' }
])

/**
 * 表格数据计算属性
 * 将SKU列表转换为表格需要的数据格式
 * - __skuKey__: 内部使用的SKU标识（不显示）
 * - attrId: 各属性值（动态列的数据）
 * - stock: 库存
 * - price: 价格
 */
const tableData = computed(() => {
  return skuList.value.map((sku, idx) => ({
    key: idx,
    __skuKey__: sku._key,
    ...Object.fromEntries(sku.specs.map(s => [s.attrId, s.value])),
    stock: sku.stock,
    price: sku.price
  }))
})

/**
 * 行key函数 - 使用SKU的唯一标识作为行key
 */
const rowKey = (row) => row.__skuKey__ ?? row.key

/**
 * 更新库存
 * @param skuKey - SKU唯一标识
 * @param val - 新的库存值（可能为null）
 */
const updateStock = (skuKey, val) => {
  stockMap.value.set(skuKey, val ?? 0)
  emit('change', skuList.value)
}

/**
 * 更新价格
 * @param skuKey - SKU唯一标识
 * @param val - 新的价格值（可能为null）
 */
const updatePrice = (skuKey, val) => {
  priceMap.value.set(skuKey, val ?? 0)
  emit('change', skuList.value)
}

/**
 * 回填已有SKU数据
 */
const applyInitialSkus = () => {
  const nextStockMap = new Map()
  const nextPriceMap = new Map()

  ;(props.initialSkus || []).forEach((sku) => {
    const key = generateSkuKey(sku?.specs || [])
    if (!key) {
      return
    }
    nextStockMap.set(key, Number(sku?.stock ?? 0))
    nextPriceMap.set(key, Number(sku?.price ?? 0))
  })

  stockMap.value = nextStockMap
  priceMap.value = nextPriceMap
}

const pruneValueMap = (sourceMap, validKeys) =>
  new Map([...sourceMap.entries()].filter(([key]) => validKeys.has(key)))

const collectValidSkuKeys = (saleAttributes = []) => {
  if (!saleAttributes.length) {
    return new Set()
  }

  const groups = saleAttributes.map(attr =>
    [...new Set((attr.values || []).map(v => String(v ?? '').trim()).filter(Boolean))].map(v => ({
      attrId: String(attr.key),
      attrName: attr.label,
      value: v
    }))
  )

  if (groups.some(group => group.length === 0)) {
    return new Set()
  }

  return new Set(
    cartesian(groups).map(specs => generateSkuKey(specs)).filter(Boolean)
  )
}

watch(
  () => props.initialSkus,
  () => {
    applyInitialSkus()
  },
  { deep: true, immediate: true }
)

watch(
  () => props.saleAttributes,
  (saleAttributes) => {
    const validKeys = collectValidSkuKeys(saleAttributes || [])
    stockMap.value = pruneValueMap(stockMap.value, validKeys)
    priceMap.value = pruneValueMap(priceMap.value, validKeys)
    emit('change', skuList.value)
  },
  { deep: true, immediate: true }
)
</script>

<style scoped>
.sku-table-wrapper {
  margin-top: 16px;
}

.sku-table-wrapper :deep(.ant-table) {
  width: fit-content;
}

.sku-table-wrapper :deep(.ant-table-container) {
  width: fit-content;
}
</style>
