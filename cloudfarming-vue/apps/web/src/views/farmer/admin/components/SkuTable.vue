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
        <!-- 库存列 -->
        <template v-if="column.key === 'stock'">
          <a-input-number
            :min="0"
            :value="getStockForRow(record.__skuKey__)"
            @change="val => updateStock(record.__skuKey__, val)"
            style="width: 120px"
          />
        </template>

        <!-- 价格列（如果需要价格列） -->
        <template v-else-if="column.key === 'price'">
          <a-input-number
            :min="0"
            :value="getPriceForRow(record.__skuKey__)"
            @change="val => updatePrice(record.__skuKey__, val)"
            style="width: 120px"
            :precision="2"
          />
        </template>
        <!-- 其余列使用默认渲染 -->
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import type { TableColumnsType } from 'ant-design-vue'

// 类型定义
interface SaleAttribute {
  key: string
  label: string
  values: string[]
}

interface SpecItem {
  attrId: string
  attrName: string
  value: string
}

interface SKU {
  specs: SpecItem[]
  stock: number
  price?: number
}

// 组件通信
const props = defineProps<{
  saleAttributes: SaleAttribute[]
}>()

const emit = defineEmits<{
  (e: 'change', skus: SKU[]): void
}>()

/* ---------- helper: 笛卡尔积 ---------- */
function cartesian<T>(list: T[][]): T[][] {
  if (!list || list.length === 0) return []
  return list.reduce(
    (acc, curr) => acc.flatMap(a => curr.map(c => [...a, c])),
    [[]] as T[][]
  )
}

/* ---------- 内部存储：stockMap / priceMap ---------- */
/*
  用一个稳定的 map 保存可变数据，key 用规格组合生成的唯一字符串
  形式： "attrId:value|attrId:value"
*/
const stockMap = ref<Record<string, number>>({})
const priceMap = ref<Record<string, number>>({}) // 可选：如果需要价格列

/* ---------- 生成 skuList（纯只读） ---------- */
const skuList = computed(() => {
  const attrs = props.saleAttributes || []
  if (attrs.length === 0) return []

  const groups = attrs.map(attr =>
    (attr.values || []).map(v => ({
      attrId: attr.key,
      attrName: attr.label,
      value: v
    }))
  )

  // 当任一属性没有 values 时，返回空数组（没有 SKU）
  if (groups.some(g => !g || g.length === 0)) return []

  const combos = cartesian(groups)
  return combos.map(specs => {
    const key = specs.map(s => `${s.attrId}:${s.value}`).join('|')
    return {
      specs,
      stock: stockMap.value[key] ?? 0,
      price: priceMap.value[key] ?? 0
    }
  })
})

/* ---------- 表头动态构造（属性列 + 库存/价格列） ---------- */
const columns = computed<TableColumnsType>(() => {
  const specCols = (props.saleAttributes || []).map(attr => ({
    title: attr.label,
    dataIndex: attr.key,
    key: attr.key
  }))

  // 最后一列：库存，倒数第二（可选）价格
  return [
    ...specCols,
    { title: '库存', key: 'stock' }
    ,{ title: '价格', key: 'price' }
  ]
})

/* ---------- 表格数据（扁平化） ---------- */
/*
  我们在每一行添加 __skuKey__ 字段（唯一键），并把每一列值放到 row[attrId]
*/
const tableData = computed(() => {
  return skuList.value.map((sku, idx) => {
    const row: Record<string, any> = { key: idx }
    const key = sku.specs.map(s => `${s.attrId}:${s.value}`).join('|')
    row.__skuKey__ = key
    sku.specs.forEach(spec => {
      row[spec.attrId] = spec.value
    })
    row.stock = sku.stock
    row.price = sku.price
    return row
  })
})

/* ---------- rowKey function ---------- */
const rowKey = (row: any) => row.__skuKey__ || row.key

/* ---------- 获取 / 更新 库存（稳定的 map） ---------- */
const getStockForRow = (skuKey: string) => {
  return stockMap.value[skuKey] ?? 0
}

const updateStock = (skuKey: string, val: number | null) => {
  const v = val ?? 0
  stockMap.value = { ...stockMap.value, [skuKey]: v } // 保持响应式替换整个对象
  // 同步 emit 给父组件最新的 skuList（包含 stock）
  emit('change', buildSkuResult())
}

/* ---------- 可选：价格处理（与库存类似） ---------- */
const getPriceForRow = (skuKey: string) => {
  return priceMap.value[skuKey] ?? 0
}
const updatePrice = (skuKey: string, val: number | null) => {
  const v = val ?? 0
  priceMap.value = { ...priceMap.value, [skuKey]: v }
  emit('change', buildSkuResult())
}

/* ---------- 构建对外的 SKU 结果 ---------- */
function buildSkuResult() {
  return skuList.value.map(sku => {
    const key = sku.specs.map(s => `${s.attrId}:${s.value}`).join('|')
    return {
      specs: sku.specs.map(s => ({ attrId: s.attrId, attrName: s.attrName, value: s.value })),
      stock: stockMap.value[key] ?? 0
      , price: priceMap.value[key] ?? 0
    }
  })
}

/* ---------- 当 saleAttributes 发生变化时：保持已有 stockMap 值（不覆盖） ---------- */
/* 如果你希望在属性变更时清空 stockMap，可以在这里处理 */
watch(
  () => props.saleAttributes,
  () => {
    // 不主动清空 stockMap：保留用户输入（若你想清空，取消注释下一行）
    // stockMap.value = {}

    // 注意：如果 saleAttributes 变化（比如值被删掉）导致某些 skuKey 不再存在，
    // stockMap 里仍会保留旧 key，不影响显示；如果你需要清理，可在此处做筛选。
  },
  { deep: true }
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
