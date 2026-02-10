<template>
  <a-card class="sku-table-card" title="SKU列表" :bordered="false">
    <a-table
      :columns="tableColumns"
      :data-source="skuList"
      :pagination="false"
      :scroll="{ x: 800 }"
      row-class-name="sku-table-row"
      bordered
    >
      <template #bodyCell="{ column, record }">
        <!-- 属性值列（动态） -->
        <template v-if="column.isAttribute">
          <a-tag v-if="column.color" :color="column.color">
            {{ getAttributeValue(record, column.attrId) }}
          </a-tag>
          <span v-else>{{ getAttributeValue(record, column.attrId) }}</span>
        </template>

        <!-- 价格列 -->
        <template v-else-if="column.key === 'price'">
          <div v-if="record.editable" class="editable-cell">
            <a-input-number
              :value="record.price"
              :min="0.01"
              :precision="2"
              :step="0.01"
              style="width: 100%"
              @change="handlePriceChange(record.key, $event)"
              @blur="handleCellBlur(record, 'price')"
              @pressEnter="handleCellBlur(record, 'price')"
            />
          </div>
          <div v-else class="editable-cell-value" @click="handleEdit(record)">
            {{ formatPrice(record.price) }}
          </div>
        </template>

        <!-- 库存列 -->
        <template v-else-if="column.key === 'stock'">
          <div v-if="record.editable" class="editable-cell">
            <a-input-number
              :value="record.stock"
              :min="0"
              :precision="0"
              style="width: 100%"
              @change="handleStockChange(record.key, $event)"
              @blur="handleCellBlur(record, 'stock')"
              @pressEnter="handleCellBlur(record, 'stock')"
            />
          </div>
          <div v-else class="editable-cell-value" @click="handleEdit(record)">
            {{ record.stock }}
          </div>
        </template>
      </template>
    </a-table>

    <!-- 空状态 -->
    <div v-if="skuList.length === 0" class="empty-state">
      <a-empty description="请先选择销售属性以生成SKU" />
    </div>
  </a-card>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { message } from 'ant-design-vue'

// Props
const props = defineProps({
  attributeNameMap: Object
})

// SKU列表
const skuList = ref([])

// 颜色数组用于属性列
const attributeColors = ['blue', 'green', 'orange', 'purple', 'cyan', 'magenta']

/**
 * 计算表格列
 */
const tableColumns = computed(() => {
  const cols = []

  // 添加属性列（动态）
  if (skuList.value.length > 0) {
    const firstSku = skuList.value[0]
    Object.keys(firstSku.attributes).forEach((attrIdStr, index) => {
      const attrId = Number(attrIdStr)
      const attrName = props.attributeNameMap?.[attrId] || `属性${attrId}`
      cols.push({
        title: attrName,
        dataIndex: 'attributes',
        key: `attr-${attrId}`,
        width: 120,
        fixed: index === 0 ? 'left' : undefined,
        isAttribute: true,
        attrId,
        color: attributeColors[index % attributeColors.length]
      })
    })
  }

  // 添加价格和库存列
  cols.push(
    {
      title: '价格（元）',
      dataIndex: 'price',
      key: 'price',
      width: 150
    },
    {
      title: '库存',
      dataIndex: 'stock',
      key: 'stock',
      width: 150
    }
  )

  return cols
})

/**
 * 获取属性值显示文本
 */
const getAttributeValue = (record, attrId) => {
  const valueId = record.attributes[attrId]
  return props.attributeNameMap?.[valueId] || String(valueId)
}

/**
 * 格式化价格显示
 */
const formatPrice = (price) => {
  return price > 0 ? `¥${price.toFixed(2)}` : '点击设置价格'
}

/**
 * 处理价格变化
 */
const handlePriceChange = (key, value) => {
  if (value !== null && value >= 0) {
    const sku = skuList.value.find(item => item.key === key)
    if (sku) {
      sku.price = value
    }
  }
}

/**
 * 处理库存变化
 */
const handleStockChange = (key, value) => {
  if (value !== null && value >= 0) {
    const sku = skuList.value.find(item => item.key === key)
    if (sku) {
      sku.stock = value
    }
  }
}

/**
 * 处理单元格失焦
 */
const handleCellBlur = (record, field) => {
  // 验证数据
  if (field === 'price' && record.price <= 0) {
    message.warning(`${record.attributeNames} 的价格必须大于0`)
    return
  }
  if (field === 'stock' && record.stock < 0) {
    message.warning(`${record.attributeNames} 的库存不能为负数`)
    return
  }

  // 退出编辑模式
  record.editable = false
}

/**
 * 进入编辑模式
 */
const handleEdit = (record) => {
  // 先关闭其他正在编辑的行
  skuList.value.forEach(item => {
    if (item.key !== record.key) {
      item.editable = false
    }
  })
  // 开启当前行编辑
  record.editable = true
}

/**
 * 设置SKU列表
 */
const setSkuList = (list) => {
  skuList.value = list
}

/**
 * 验证所有SKU数据
 */
const validateSkuData = () => {
  for (const sku of skuList.value) {
    if (sku.price <= 0) {
      return { valid: false, error: `SKU [${sku.attributeNames}] 的价格必须大于0` }
    }
    if (sku.stock < 0) {
      return { valid: false, error: `SKU [${sku.attributeNames}] 的库存不能为负数` }
    }
  }
  return { valid: true }
}

/**
 * 重置表格
 */
const resetTable = () => {
  skuList.value.forEach(item => {
    item.editable = false
  })
}

/**
 * 清空SKU列表
 */
const clearSkuList = () => {
  skuList.value = []
}

/**
 * 获取SKU列表数据
 */
const getSkuList = () => {
  return skuList.value
}

/**
 * 根据销售属性生成SKU列表（保留兼容性）
 */
const generateSkuList = (weight, diameter) => {
  if (!weight || !diameter) {
    skuList.value = []
    return
  }

  const weightOptions = ['3斤', '5斤']
  const diameterOptions = ['30mm', '50mm', '70mm']

  const newSkuList = []
  let keyIndex = 0

  for (const w of weightOptions) {
    for (const d of diameterOptions) {
      newSkuList.push({
        key: `sku-${keyIndex++}`,
        attributes: { 1: Number(weight.includes('3') ? 1 : 2) },
        attributeNames: `${w} / ${d}`,
        price: 0,
        stock: 0,
        editable: false
      })
    }
  }

  skuList.value = newSkuList
}

// 暴露方法供父组件调用
defineExpose({
  generateSkuList,
  setSkuList,
  validateSkuData,
  resetTable,
  clearSkuList,
  getSkuList,
  skuList
})
</script>

<style scoped>
.sku-table-card {
  margin-bottom: 0;
}

/* 可编辑单元格样式 */
.editable-cell {
  position: relative;
}

.editable-cell-value {
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background-color 0.2s;
  color: #1890ff;
}

.editable-cell-value:hover {
  background-color: #e6f7ff;
}

.editable-cell-value:empty::before {
  content: '点击编辑';
  color: #bfbfbf;
}

/* 表格行样式 */
:deep(.sku-table-row) {
  transition: background-color 0.2s;
}

:deep(.sku-table-row:hover) {
  background-color: #fafafa;
}

:deep(.sku-table-row.ant-table-row:nth-child(even)) {
  background-color: #f9f9f9;
}

:deep(.sku-table-row.ant-table-row:nth-child(even):hover) {
  background-color: #f0f0f0;
}

/* 空状态 */
.empty-state {
  padding: 40px 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .editable-cell-value {
    padding: 8px;
    font-size: 14px;
  }
}
</style>
