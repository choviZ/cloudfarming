<template>
  <div id="create-spu">
    <a-form
      :label-col="{ span: 2 }"
      :wrapper-col="{ span: 22 }"
      label-align="left"
    >
      <h2>填写商品基本信息</h2>

      <!-- 分类选择 -->
      <a-form-item label="分类">
        <CategorySelect
          v-model="selectedCategoryId"
          @change="handleCategoryChange"
          style="width: 240px"
        />
      </a-form-item>

      <!-- 商品标题 -->
      <a-form-item label="商品标题">
        <a-input
          placeholder="请输入商品标题"
          :value="spuTitle"
          @input="updateSpuTitle"
          style="width: 360px"
        />
      </a-form-item>

      <a-divider />

      <!-- 商品基本属性（类型0）- 这些是固定的商品属性，如产地、保质期等 -->
      <a-form-item label="商品基本属性">
        <AttributeSelect
          :attributes="attributeList"
          :selectedAttrIds="selectedBaseAttributes.map(a => a.key)"
          :typeFilter="0"
          @add="addBaseAttribute"
        />
      </a-form-item>

      <!-- 动态渲染已选择的基本属性输入框 -->
      <DynamicAttribute
        :attributes="selectedBaseAttributes"
        :type="0"
        @remove-attribute="removeAttribute"
      />

      <a-divider />

      <!-- 商品销售属性（类型1）- 这些是生成SKU的维度，如颜色、尺码 -->
      <a-form-item label="商品销售属性">
        <AttributeSelect
          :attributes="attributeList"
          :selectedAttrIds="selectedSaleAttributes.map(a => a.key)"
          :typeFilter="1"
          @add="addSaleAttribute"
        />
      </a-form-item>

      <!-- 动态渲染已选择的销售属性（可输入多个值） -->
      <DynamicAttribute
        :attributes="selectedSaleAttributes"
        :type="1"
        @remove-attribute="removeAttribute"
      />

      <a-divider />

      <!-- SKU表格 - 根据销售属性生成所有规格组合，供用户填写库存和价格 -->
      <SkuTable
        :saleAttributes="saleAttributesForTable"
        @change="handleSkuChange"
      />

      <a-divider />

      <!-- 保存按钮 -->
      <a-form-item :wrapper-col="{ span: 22, offset: 2 }">
        <a-space>
          <a-button
            type="primary"
            :loading="saving"
            @click="handleSave"
          >
            保存商品
          </a-button>
        </a-space>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import CategorySelect from './components/CategorySelect.vue'
import AttributeSelect from './components/AttributeSelect.vue'
import DynamicAttribute from './components/DynamicAttributeList.vue'
import SkuTable from './components/SkuTable.vue'
import {
  getAttributesByCategoryId,
  type AttributeRespDTO,
  createSpuAttrValue,
  saveSpu,
  createSku,
  type SkuItemDTO,
  type SaleAttrDTO
} from '@cloudfarming/core'
import type {
  BaseAttributeItem,
  SaleAttributeItem,
  SKU
} from '@/types'
import { message } from 'ant-design-vue'

/**
 * 保存状态 - 防止重复提交
 */
const saving = ref(false)

/**
 * 已选择的分类ID
 */
const selectedCategoryId = ref<string>('')

/**
 * 商品标题
 */
const spuTitle = ref('')

/**
 * 当前分类下的所有可用属性列表
 */
const attributeList = ref<AttributeRespDTO[]>([])

/**
 * 已选择的基本属性列表（类型0）
 */
const selectedBaseAttributes = ref<BaseAttributeItem[]>([])

/**
 * 已选择的销售属性列表（类型1）
 */
const selectedSaleAttributes = ref<SaleAttributeItem[]>([])

/**
 * SKU列表 - 存储从SkuTable组件接收的所有SKU数据
 * 当用户修改库存或价格时，SkuTable会emit新的数据
 */
const skuList = ref<SKU[]>([])

/**
 * 销售属性转换
 * 将 BaseAttributeItem[] 转换为 SkuTable 期望的 SaleAttribute[] 格式
 */
const saleAttributesForTable = computed(() =>
  selectedSaleAttributes.value.map(attr => ({
    key: attr.key,
    label: attr.label,
    values: attr.values
  }))
)

/**
 * 处理分类变化
 * 当用户选择不同分类时：
 * 1. 更新选中的分类ID
 * 2. 加载该分类下的属性列表
 * 3. 清空已选择的属性和SKU数据
 * @param categoryId - 选中的分类ID
 */
const handleCategoryChange = async (categoryId: string) => {
  selectedCategoryId.value = categoryId
  await loadAttributes(categoryId)
  selectedBaseAttributes.value = []
  selectedSaleAttributes.value = []
  skuList.value = []
}

/**
 * 加载指定分类下的属性列表
 * @param categoryId - 分类ID
 */
const loadAttributes = async (categoryId: string) => {
  if (!categoryId) {
    attributeList.value = []
    return
  }
  const res = await getAttributesByCategoryId(categoryId)
  attributeList.value = res?.data ?? []
}

/**
 * 添加基本属性
 * 检查是否已存在，避免重复添加
 * @param id - 要添加的属性ID
 */
const addBaseAttribute = (id: string) => {
  if (selectedBaseAttributes.value.some(a => a.key === id)) return
  const attr = attributeList.value.find(a => String(a.id) === id)
  if (!attr) return
  selectedBaseAttributes.value.push({
    key: id,
    label: attr.name,
    value: ''
  })
}

/**
 * 添加销售属性
 * 检查是否已存在，避免重复添加
 * @param id - 要添加的属性ID
 */
const addSaleAttribute = (id: string) => {
  if (selectedSaleAttributes.value.some(a => a.key === id)) return
  const attr = attributeList.value.find(a => String(a.id) === id)
  if (!attr) return
  selectedSaleAttributes.value.push({
    key: id,
    label: attr.name,
    values: []
  })
}

/**
 * 移除属性
 * @param id - 要移除的属性ID
 * @param type - 属性类型（0=基本属性，1=销售属性）
 */
const removeAttribute = (id: string, type: number) => {
  if (type === 0) {
    selectedBaseAttributes.value =
      selectedBaseAttributes.value.filter(a => a.key !== id)
  } else {
    selectedSaleAttributes.value =
      selectedSaleAttributes.value.filter(a => a.key !== id)
  }
}

/**
 * 处理SKU数据变化
 * 当用户在SKU表格中修改库存或价格时触发
 * @param skus - 最新的SKU列表
 */
const handleSkuChange = (skus: SKU[]) => {
  skuList.value = skus
}

/**
 * 更新商品标题
 * @param e - 输入事件
 */
const updateSpuTitle = (e: Event) => {
  const target = e.target as HTMLInputElement
  spuTitle.value = target.value
}

/**
 * 保存商品
 * 流程：
 * 1. 创建SPU（商品主信息）
 * 2. 创建SPU属性值（基本属性）
 * 3. 创建SKU（销售属性生成的规格组合）
 */
const handleSave = async () => {
  // 验证必填项
  if (!selectedCategoryId.value) {
    message.warning('请选择分类')
    return
  }
  if (!spuTitle.value.trim()) {
    message.warning('请输入商品标题')
    return
  }
  if (selectedSaleAttributes.value.length === 0) {
    message.warning('请至少添加一个销售属性')
    return
  }
  if (skuList.value.length === 0) {
    message.warning('未生成SKU，请检查销售属性是否设置了可选值')
    return
  }

  try {
    saving.value = true

    // 第一步：创建SPU（商品主表记录）
    const spuRes = await saveSpu({
      shopId: '1',
      categoryId: selectedCategoryId.value,
      title: spuTitle.value,
      image: '',
      status: 2
    })
    if (spuRes.code != '0') {
      throw new Error('SPU 创建失败')
    }

    // 第二步：创建SPU属性值（基本属性）
    if (selectedBaseAttributes.value.length > 0) {
      await Promise.all(
        selectedBaseAttributes.value.map(attr =>
          createSpuAttrValue({
            spuId: spuRes.data,
            attrId: attr.key,
            attrValue: attr.value || ''
          })
        )
      )
    }

    // 第三步：创建SKU
    // 3.1 收集所有销售属性值（去重）
    const allSaleAttrs: SaleAttrDTO[] = []
    const attrValueMap: Record<string, boolean> = {}
    skuList.value.forEach(sku => {
      sku.specs.forEach(spec => {
        const key = `${spec.attrId}_${spec.value}`
        if (!attrValueMap[key]) {
          allSaleAttrs.push({
            attrId: spec.attrId,
            attrName: spec.attrName,
            attrValueId: key,
            attrValueName: spec.value
          })
          attrValueMap[key] = true
        }
      })
    })

    // 3.2 构建SKU列表数据
    const skuItemsToCreate: SkuItemDTO[] = skuList.value.map(sku => ({
      price: sku.price,
      stock: sku.stock,
      image: '',
      attrValues: Object.fromEntries(sku.specs.map(s => [s.attrId, s.value]))
    }))

    // 3.3 调用创建SKU接口
    await createSku({
      spuId: spuRes.data,
      saleAttrs: allSaleAttrs,
      skuItems: skuItemsToCreate
    })

    message.success('商品保存成功')
  } catch (err) {
    console.error(err)
    message.error('保存商品失败')
  } finally {
    saving.value = false
  }
}
</script>
