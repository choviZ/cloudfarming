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
            :value="getStock(record.__skuKey__ as string)"
            @change="(val: number | null) => updateStock(record.__skuKey__ as string, val)"
            style="width: 120px"
          />
        </template>
        <template v-else-if="column.key === 'price'">
          <a-input-number
            :min="0"
            :value="getPrice(record.__skuKey__ as string)"
            @change="(val: number | null) => updatePrice(record.__skuKey__ as string, val)"
            style="width: 120px"
            :precision="2"
          />
        </template>
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import type { TableColumnsType } from 'ant-design-vue'
import type { SaleAttribute, SpecItem, SKU } from '@/types'

/**
 * 组件Props - 接收父组件传入的销售属性配置
 */
const props = defineProps<{
  saleAttributes: SaleAttribute[]
}>()

/**
 * 组件事件 - 当SKU数据发生变化时通知父组件
 * @param skus - 最新的SKU列表，包含所有规格组合的价格和库存
 */
const emit = defineEmits<{
  (e: 'change', skus: SKU[]): void
}>()

/**
 * 生成SKU的唯一标识键
 * 格式: "attrId:value|attrId:value|..."
 * 例如: "1:红色|2:S"
 * 用于在不同地方唯一标识一个SKU组合
 */
function generateSkuKey(specs: SpecItem[]): string {
  return specs.map(s => `${s.attrId}:${s.value}`).join('|')
}

/**
 * 计算笛卡尔积
 * 将多个数组的所有可能组合生成出来
 * 例如: [['红色','蓝色'], ['S','M']] -> [['红色','S'], ['红色','M'], ['蓝色','S'], ['蓝色','M']]
 */
function cartesian<T>(list: T[][]): T[][] {
  if (!list || list.length === 0) return []
  return list.reduce(
    (acc, curr) => acc.flatMap(a => curr.map(c => [...a, c])),
    [[]] as T[][]
  )
}

/**
 * 库存数据映射
 * key: SKU唯一标识键
 * value: 库存数量
 * 使用Map而非数组，便于通过key快速查找和更新
 */
const stockMap = ref(new Map<string, number>())

/**
 * 价格数据映射
 * key: SKU唯一标识键
 * value: 价格
 */
const priceMap = ref(new Map<string, number>())

/**
 * 从Map中安全获取值
 */
function getStock(key: string): number {
  return stockMap.value.get(key) ?? 0
}

function getPrice(key: string): number {
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
  if (attrs.length === 0) return []

  // 将每个属性的值转换为 SpecItem 数组
  const groups = attrs.map(attr =>
    (attr.values || []).map(v => ({
      attrId: attr.key,
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
    const key = generateSkuKey(specs)
    return {
      specs,
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
const columns = computed<TableColumnsType>(() => [
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
const rowKey = (row: { __skuKey__?: string; key: number }) => row.__skuKey__ ?? row.key

/**
 * 更新库存
 * @param skuKey - SKU唯一标识
 * @param val - 新的库存值（可能为null）
 */
const updateStock = (skuKey: string, val: number | null) => {
  stockMap.value.set(skuKey, val ?? 0)
  emit('change', skuList.value)
}

/**
 * 更新价格
 * @param skuKey - SKU唯一标识
 * @param val - 新的价格值（可能为null）
 */
const updatePrice = (skuKey: string, val: number | null) => {
  priceMap.value.set(skuKey, val ?? 0)
  emit('change', skuList.value)
}

/**
 * 监听销售属性变化
 * 当属性配置变化时，可能需要清空库存（可选）
 */
watch(() => props.saleAttributes, () => {
  // stockMap.value = {} // 可选：清空库存重新填写
}, { deep: true })
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
