<template>
<!--  <a-form-item label="分类" style="width: 240px">-->
    <a-tree-select
      v-model:value="value"
      placeholder="请选择分类"
      allow-clear
      tree-default-expand-all
      :tree-data="treeData"
    />
<!--  </a-form-item>-->
</template>

<script setup>
import { ref, watch } from 'vue'
import { getCategoryTree } from '@/api/category'

// ===== v-model 支持 =====
const props = defineProps({
  modelValue: String
})

const emit = defineEmits(['update:modelValue'])

const value = ref(props.modelValue)

watch(() => props.modelValue, v => {
  value.value = v
})

watch(value, v => {
  emit('update:modelValue', v)
})

// ===== Tree 数据 =====
const treeData = ref([])

const initTreeData = async () => {
  const res = await getCategoryTree()
  treeData.value = transformCategoryToTree(res.data)
}

initTreeData()

/**
 * 将分类数据转换为树形结构
 * @param data
 */
const transformCategoryToTree = (data) => {
  if (!data?.length) return []

  const convert = (node) => {
    const hasChildren = !!node.children && node.children.length > 0
    return {
      label: node.name,
      value: node.id,
      key: node.id,
      disabled: hasChildren,
      children: node.children?.map(convert)
    }
  }
  return data.map(convert)
}
</script>
