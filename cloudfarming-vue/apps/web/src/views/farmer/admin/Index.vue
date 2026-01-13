<template>
  <div id="create-spu">
    <a-form
      :label-col="{ span: 2 }"
      :wrapper-col="{ span: 22 }"
      label-align="left"
    >
      <h2>填写商品基本信息</h2>

      <!-- 分类 -->
      <a-form-item label="分类">
        <CategorySelect
          v-model="selectedCategoryId"
          @change="handleCategoryChange"
          style="width: 240px"
        />
      </a-form-item>

      <!-- 标题 -->
      <a-form-item label="商品标题">
        <a-input placeholder="请输入商品标题" v-model="spuTitle" style="width: 360px" />
      </a-form-item>

      <a-divider />

      <!-- 基本属性 -->
      <a-form-item label="商品基本属性">
        <AttributeSelect
          :attributes="attributeList"
          :selectedAttrIds="selectedBaseAttributes.map(a => a.key)"
          :typeFilter="0"
          @add="addBaseAttribute"
        />
      </a-form-item>

      <DynamicAttribute
        :attributes="selectedBaseAttributes"
        :type="0"
        @remove-attribute="removeAttribute"
      />

      <a-divider />

      <!-- 销售属性 -->
      <a-form-item label="商品销售属性">
        <AttributeSelect
          :attributes="attributeList"
          :selectedAttrIds="selectedSaleAttributes.map(a => a.key)"
          :typeFilter="1"
          @add="addSaleAttribute"
        />
      </a-form-item>

      <DynamicAttribute
        :attributes="selectedSaleAttributes"
        :type="1"
        @remove-attribute="removeAttribute"
      />

      <a-divider />

      <!-- SKU 表格 -->
      <SkuTable
        :saleAttributes="selectedSaleAttributes"
        @change="handleSkuChange"
      />
      <a-divider />
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
import { ref } from 'vue'
import CategorySelect from './components/CategorySelect.vue'
import AttributeSelect from './components/AttributeSelect.vue'
import DynamicAttribute from './components/DynamicAttributeList.vue'
import SkuTable from './components/SkuTable.vue'
import {
  getAttributesByCategoryId,
  type AttributeRespDTO,
  createSpuAttrValue,
  saveSpu,
  createSku, type SkuItemDTO, type SaleAttrDTO
} from '@cloudfarming/core'
import { message } from 'ant-design-vue'

/* ========== 基础状态 ========== */
const saving = ref(false)

// 分类
const selectedCategoryId = ref<string>('')
const spuTitle = ref<string>('')
// 后端返回的“所有属性”
const attributeList = ref<AttributeRespDTO[]>([])

/* ========== 动态属性结构 ========== */

interface DynamicAttributeItem {
  key: string
  label: string
  value?: string        // 基本属性
  values?: string[]     // 销售属性
}

// 基本属性
const selectedBaseAttributes = ref<DynamicAttributeItem[]>([])

// 销售属性
const selectedSaleAttributes = ref<DynamicAttributeItem[]>([])

/* ========== SKU 最终结果（唯一来源） ========== */

interface SkuDTO {
  specs: {
    attrId: string
    attrName: string
    value: string
  }[]
  stock: number
  price?: number
}

const skuList = ref<SkuDTO[]>([])

/* ========== 事件处理 ========== */

// 分类切换
const handleCategoryChange = async (categoryId: string) => {
  selectedCategoryId.value = categoryId
  await loadAttributes(categoryId)

  // 切分类时清空状态
  selectedBaseAttributes.value = []
  selectedSaleAttributes.value = []
  skuList.value = []
}

// 拉取属性
const loadAttributes = async (categoryId: string) => {
  if (!categoryId) {
    attributeList.value = []
    return
  }

  const res = await getAttributesByCategoryId(categoryId)
  attributeList.value = res?.data ?? []
}

// 添加基本属性
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

// 添加销售属性
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

// 删除属性（通用）
const removeAttribute = (id: string, type: number) => {
  if (type === 0) {
    selectedBaseAttributes.value =
      selectedBaseAttributes.value.filter(a => a.key !== id)
  } else {
    selectedSaleAttributes.value =
      selectedSaleAttributes.value.filter(a => a.key !== id)
  }
}

// ⭐️ 关键函数：只接收 SKU 结果，不做任何计算
const skuItems = ref<SkuItemDTO[]>([])
const handleSkuChange = (skus: SkuDTO[]) => {
  skuList.value = skus
  console.log('最终 SKU 列表：', skus)
  // 清空数组，重新生成
  skuItems.value = []
  skus.forEach(sku => {
    // 为每个SKU创建独立的saleAttrs对象
    const saleAttrs: Record<string, string> = {}
    sku.specs.forEach(spec => {
      saleAttrs[spec.attrId] = spec.value
    })
    skuItems.value.push({
      stock: sku.stock,
      price: sku.price || 0, // 确保price有值
      image: '', // 添加必填的image字段
      saleAttrs: saleAttrs
    })
  })
}

const handleSave = async () => {
  try {
    saving.value = true
    // 保存spu
    const spuRes = await saveSpu({
      shopId: '1', // TODO
      categoryId: selectedCategoryId.value,
      title: spuTitle.value, // 这里替换成你真实的 v-model
      image: '',
      status: 2
    })
    if (spuRes.code != '0') {
      throw new Error('SPU 创建失败')
    }

    // 保存 SPU 基本属性
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

    // 保存sku
    if (skuList.value.length === 0) {
      message.warning('未生成 SKU，请检查销售属性')
      return
    }
    
    // 提取所有销售属性
    const allSaleAttrs: SaleAttrDTO[] = []
    const attrValueMap: Record<string, boolean> = {}
    skuList.value.forEach(sku => {
      sku.specs.forEach(spec => {
        const key = `${spec.attrId}_${spec.value}`
        if (!attrValueMap[key]) {
          allSaleAttrs.push({
            attrId: spec.attrId,
            attrName: spec.attrName,
            attrValueId: key, // 生成唯一值ID
            attrValueName: spec.value
          })
          attrValueMap[key] = true
        }
      })
    })
    
    // 构建完整的skuItems数组
    const skuItemsToCreate: SkuItemDTO[] = skuList.value.map(sku => {
      const saleAttrs: Record<string, string> = {}
      sku.specs.forEach(spec => {
        saleAttrs[spec.attrId] = spec.value
      })
      return {
        price: sku.price || 0,
        stock: sku.stock,
        image: '',
        saleAttrs: saleAttrs
      }
    })
    
    // 单次调用createSku接口创建所有SKU
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
